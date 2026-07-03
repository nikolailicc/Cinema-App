package com.example.cinebook.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinebook.R;
import com.example.cinebook.model.Reservation;

import java.util.ArrayList;
import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(Reservation reservation);
    }

    private List<Reservation> reservations = new ArrayList<>();
    private final OnDeleteClickListener listener;

    public ReservationAdapter(OnDeleteClickListener listener) {
        this.listener = listener;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations != null ? reservations : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        String movieTitle = reservation.getMovie() != null ? reservation.getMovie().getTitle() : "Film";
        holder.title.setText(movieTitle);
        holder.details.setText("Karata: " + reservation.getNumberOfTickets()
                + " • " + reservation.getReservationDate());
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(reservation));
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView title, details;
        ImageButton btnDelete;

        ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textMovieTitle);
            details = itemView.findViewById(R.id.textDetails);
            btnDelete = itemView.findViewById(R.id.btnDeleteReservation);
        }
    }
}
