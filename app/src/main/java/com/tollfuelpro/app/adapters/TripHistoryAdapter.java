package com.tollfuelpro.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tollfuelpro.app.R;
import com.tollfuelpro.app.models.TripRecord;
import com.tollfuelpro.app.utils.DateUtils;
import java.util.List;

public class TripHistoryAdapter extends RecyclerView.Adapter<TripHistoryAdapter.ViewHolder> {

    private final List<TripRecord> trips;
    private final OnDeleteClickListener deleteListener;
    private final OnTripClickListener tripListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(String tripId);
    }

    public interface OnTripClickListener {
        void onTripClick(TripRecord trip);
    }

    public TripHistoryAdapter(List<TripRecord> trips, OnDeleteClickListener deleteListener,
            OnTripClickListener tripListener) {
        this.trips = trips;
        this.deleteListener = deleteListener;
        this.tripListener = tripListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TripRecord trip = trips.get(position);

        String src = trip.getSource().contains(",") ? trip.getSource().split(",")[0].trim() : trip.getSource();
        String dst = trip.getDestination().contains(",") ? trip.getDestination().split(",")[0].trim()
                : trip.getDestination();
        holder.tvRoute.setText(holder.itemView.getContext().getString(R.string.route_format, src, dst));

        String vehicleType = trip.getVehicleType().substring(0, 1).toUpperCase(java.util.Locale.getDefault()) +
                trip.getVehicleType().substring(1);
        String tripType = trip.isRoundTrip() ? "Round Trip" : "One Way";
        holder.tvDetails.setText(holder.itemView.getContext().getString(R.string.trip_details_format, vehicleType, tripType));
        
        holder.tvDist.setText(holder.itemView.getContext().getString(R.string.km_amount, (int) trip.getDistanceKm()));
        holder.tvToll.setText(holder.itemView.getContext().getString(R.string.rupee_amount, (int) trip.getTollCost()));
        holder.tvFuel.setText(holder.itemView.getContext().getString(R.string.rupee_amount, (int) trip.getFuelCost()));
        holder.tvTotalCost.setText(holder.itemView.getContext().getString(R.string.rupee_amount, (int) trip.getTotalCost()));

        holder.tvDate.setText(DateUtils.formatTimestamp(trip.getTimestamp()));

        // Match icon based on vehicle
        switch (trip.getVehicleType()) {
            case "suv":
                holder.ivVehicle.setImageResource(R.drawable.ic_suv);
                break;
            case "bus":
            case "truck":
                holder.ivVehicle.setImageResource(R.drawable.ic_bus);
                break;
            default:
                holder.ivVehicle.setImageResource(R.drawable.ic_car);
                break;
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(trip.getId());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (tripListener != null) {
                tripListener.onTripClick(trip);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoute, tvDetails, tvDist, tvToll, tvFuel, tvTotalCost, tvDate;
        ImageView ivVehicle, btnDelete;

        ViewHolder(View view) {
            super(view);
            tvRoute = view.findViewById(R.id.tv_route);
            tvDetails = view.findViewById(R.id.tv_trip_details);
            tvDist = view.findViewById(R.id.tv_dist);
            tvToll = view.findViewById(R.id.tv_toll);
            tvFuel = view.findViewById(R.id.tv_fuel);
            tvTotalCost = view.findViewById(R.id.tv_total_cost);
            tvDate = view.findViewById(R.id.tv_date);
            ivVehicle = view.findViewById(R.id.iv_vehicle);
            btnDelete = view.findViewById(R.id.btn_delete_item);
        }
    }
}
