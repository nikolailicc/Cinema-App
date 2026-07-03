package com.example.movies.controller;

import com.example.movies.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/movies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> moviesReport() throws Exception {
        byte[] data = reportService.generateMoviesReport();
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_PDF);
        h.setContentDispositionFormData("attachment", "movies_report.pdf");
        return ResponseEntity.ok().headers(h).body(data);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> usersReport() throws Exception {
        byte[] data = reportService.generateUsersReport();
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_PDF);
        h.setContentDispositionFormData("attachment", "users_report.pdf");
        return ResponseEntity.ok().headers(h).body(data);
    }
}