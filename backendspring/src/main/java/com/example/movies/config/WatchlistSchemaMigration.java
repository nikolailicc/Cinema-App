package com.example.movies.config;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class WatchlistSchemaMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public WatchlistSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.update("UPDATE watchlist SET list_type = 'WATCHLIST' WHERE list_type IS NULL");

        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                "SELECT INDEX_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'watchlist' " +
                        "AND INDEX_NAME <> 'PRIMARY' ORDER BY INDEX_NAME, SEQ_IN_INDEX");

        Set<String> indexNames = new HashSet<>();
        for (Map<String, Object> index : indexes) {
            String name = String.valueOf(index.get("INDEX_NAME"));
            indexNames.add(name);
        }

        for (String indexName : indexNames) {
            List<String> columns = indexes.stream()
                    .filter(index -> indexName.equals(String.valueOf(index.get("INDEX_NAME"))))
                    .map(index -> String.valueOf(index.get("COLUMN_NAME")))
                    .toList();
            if (columns.size() == 2 && columns.contains("user_id") && columns.contains("movie_id")) {
                jdbcTemplate.execute("ALTER TABLE watchlist DROP INDEX `" + indexName.replace("`", "") + "`");
            }
        }

        jdbcTemplate.execute("ALTER TABLE watchlist MODIFY list_type VARCHAR(255) NOT NULL");
    }
}
