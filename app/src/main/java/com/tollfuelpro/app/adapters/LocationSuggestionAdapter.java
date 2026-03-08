package com.tollfuelpro.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tollfuelpro.app.services.MapboxService;
import java.util.List;

public class LocationSuggestionAdapter extends RecyclerView.Adapter<LocationSuggestionAdapter.ViewHolder> {

    private final List<MapboxService.GeocodingResult> results;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MapboxService.GeocodingResult result);
    }

    public LocationSuggestionAdapter(List<MapboxService.GeocodingResult> results, OnItemClickListener listener) {
        this.results = results;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Simple plain text view item for suggestions
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MapboxService.GeocodingResult result = results.get(position);
        holder.tvName.setText(result.placeName);
        holder.tvName.setTextColor(0xFFFFFFFF); // White text assuming dark theme
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(result);
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(android.R.id.text1);
        }
    }
}
