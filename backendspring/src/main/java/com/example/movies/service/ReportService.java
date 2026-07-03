package com.example.movies.service;

import com.example.movies.model.Movie;
import com.example.movies.model.User;
import com.example.movies.repository.MovieRepository;
import com.example.movies.repository.UserRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private UserRepository userRepository;

    public byte[] generateMoviesReport() throws Exception {
        List<Movie> movies = movieRepository.findAll();
        File file = ResourceUtils.getFile("classpath:reports/movies_report.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(movies);
        Map<String, Object> params = new HashMap<>();
        params.put("createdBy", "Cinema Admin");
        params.put("totalMovies", movies.size());
        JasperPrint print = JasperFillManager.fillReport(jasperReport, params, ds);
        return JasperExportManager.exportReportToPdf(print);
    }

    public byte[] generateUsersReport() throws Exception {
        List<User> users = userRepository.findAll();
        File file = ResourceUtils.getFile("classpath:reports/users_report.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(users);
        Map<String, Object> params = new HashMap<>();
        params.put("createdBy", "Cinema Admin");
        params.put("totalUsers", users.size());
        JasperPrint print = JasperFillManager.fillReport(jasperReport, params, ds);
        return JasperExportManager.exportReportToPdf(print);
    }
}