package com.tollfuelpro.app.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tollfuelpro.app.R;
import com.tollfuelpro.app.models.TripRecord;
import com.tollfuelpro.app.services.StorageService;
import java.util.List;
import com.tollfuelpro.app.adapters.TripHistoryAdapter;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private LinearLayout layoutEmptyState;
    private TextView tvTripsCount;
    private ImageView btnDeleteAll;
    private TripHistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        
        rvHistory = view.findViewById(R.id.rv_history);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        tvTripsCount = view.findViewById(R.id.tv_trips_count);
        btnDeleteAll = view.findViewById(R.id.btn_delete_all);

        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        btnDeleteAll.setOnClickListener(v -> showClearAllDialog());
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        if (getContext() == null) return;
        List<TripRecord> trips = StorageService.getAllTrips(getContext());
        
        tvTripsCount.setText(getResources().getQuantityString(R.plurals.trips_recorded_plural, trips.size(), trips.size()));
        
        if (trips.isEmpty()) {
            rvHistory.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
            btnDeleteAll.setVisibility(View.GONE);
        } else {
            rvHistory.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            btnDeleteAll.setVisibility(View.VISIBLE);
            
            // Wait until TripHistoryAdapter is fully created
            adapter = new TripHistoryAdapter(trips, this::showDeleteDialog, this::showTripDetails);
            rvHistory.setAdapter(adapter);
        }
    }

    private void showClearAllDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.clear_all_confirm)
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                StorageService.deleteAllTrips(requireContext());
                loadHistory();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
    
    // Will be called from adapter
    private void showDeleteDialog(String tripId) {
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_trip_confirm)
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                StorageService.deleteTrip(requireContext(), tripId);
                loadHistory();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showTripDetails(TripRecord trip) {
        TripResultFragment fragment = TripResultFragment.newInstance(trip);
        requireActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit();
    }
}
