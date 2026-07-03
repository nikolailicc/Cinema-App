package com.example.cinebook.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinebook.R;
import com.example.cinebook.api.ApiService;
import com.example.cinebook.api.RetrofitClient;
import com.example.cinebook.model.Reservation;
import com.example.cinebook.notification.ReminderScheduler;
import com.example.cinebook.ui.adapter.ReservationAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyReservationsActivity extends AppCompatActivity implements ReservationAdapter.OnDeleteClickListener {

    private RecyclerView recyclerView;
    private TextView textEmpty;
    private ReservationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recyclerReservations);
        textEmpty = findViewById(R.id.textEmpty);

        adapter = new ReservationAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadReservations();
    }

    private void loadReservations() {
        ApiService api = RetrofitClient.getApiService(this);
        api.getMyReservations().enqueue(new Callback<List<Reservation>>() {
            @Override
            public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Reservation> reservations = response.body();
                    adapter.setReservations(reservations);
                    textEmpty.setVisibility(reservations.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(MyReservationsActivity.this, "Greška pri učitavanju", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Reservation>> call, Throwable t) {
                Toast.makeText(MyReservationsActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDelete(Reservation reservation) {
        ApiService api = RetrofitClient.getApiService(this);
        api.deleteReservation(reservation.getId()).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (reservation.getId() != null) {
                    ReminderScheduler.cancelReminder(MyReservationsActivity.this, reservation.getId());
                }
                Toast.makeText(MyReservationsActivity.this, "Rezervacija otkazana", Toast.LENGTH_SHORT).show();
                loadReservations();
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(MyReservationsActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
