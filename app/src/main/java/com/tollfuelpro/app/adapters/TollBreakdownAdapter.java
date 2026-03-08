package com.tollfuelpro.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tollfuelpro.app.R;
import com.tollfuelpro.app.models.TollResult;
import java.util.List;

public class TollBreakdownAdapter extends RecyclerView.Adapter<TollBreakdownAdapter.ViewHolder> {

    private final List<TollResult> plazas;
    private final boolean isRoundTrip;
    private final String vehicleType;

    public TollBreakdownAdapter(List<TollResult> plazas, boolean isRoundTrip, String vehicleType) {
        this.plazas = plazas;
        this.isRoundTrip = isRoundTrip;
        this.vehicleType = vehicleType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_toll_plaza, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TollResult result = plazas.get(position);
        
        holder.tvName.setText(result.getPlazaName());
        
        holder.tvNumber.setText(String.valueOf(position + 1));

        String highway = result.getHighway() != null ? result.getHighway() : "National Highway";
        holder.tvDetails.setText(highway);
        
        double cost = result.getCharge();
        holder.tvCost.setText(holder.itemView.getContext().getString(R.string.rupee_amount, (int) cost));
    }

    @Override
    public int getItemCount() {
        return plazas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCost, tvDetails, tvNumber;

        ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tv_plaza_name);
            tvCost = view.findViewById(R.id.tv_plaza_cost);
            tvDetails = view.findViewById(R.id.tv_plaza_details);
            tvNumber = view.findViewById(R.id.tv_plaza_number);
        }
    }
}
