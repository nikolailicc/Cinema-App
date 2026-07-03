package com.example.cinebook.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinebook.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Buduci da backend ne definise tacnu semu za komentar (Object u swaggeru),
 * adapter radi sa generickom Map<String,Object> strukturom i sam "pogadja"
 * koje polje predstavlja tekst, a koje autora.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Map<String, Object>> comments = new ArrayList<>();

    public void setComments(List<Map<String, Object>> comments) {
        this.comments = comments != null ? comments : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Map<String, Object> comment = comments.get(position);
        String author = firstNonNull(comment, "username", "author", "user");
        String text = firstNonNull(comment, "text", "comment", "content", "message");

        holder.author.setText(author != null ? author : "Korisnik");
        holder.text.setText(text != null ? text : "");
    }

    private String firstNonNull(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) return String.valueOf(value);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView author, text;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.textAuthor);
            text = itemView.findViewById(R.id.textComment);
        }
    }
}
