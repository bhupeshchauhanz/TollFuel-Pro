package com.tollfuelpro.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tollfuelpro.app.R;
import com.tollfuelpro.app.models.TripRecord;
import com.tollfuelpro.app.services.StorageService;
import com.tollfuelpro.app.utils.DateUtils;
import android.widget.ImageView;
import java.util.List;
import com.tollfuelpro.app.fragments.TripResultFragment;

public class HomeFragment extends Fragment {

    private TextView tvTotalSpent, tvDistance, tvTrips, tvTollPlazas, tvTollCosts, tvFuelCosts;
    private RelativeLayout layoutRecentHeader;
    private LinearLayout containerRecentTrips, layoutEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvTotalSpent = view.findViewById(R.id.tv_total_spent);
        tvDistance = view.findViewById(R.id.tv_distance);
        tvTrips = view.findViewById(R.id.tv_trips);
        tvTollPlazas = view.findViewById(R.id.tv_toll_plazas);
        tvTollCosts = view.findViewById(R.id.tv_toll_costs);
        tvFuelCosts = view.findViewById(R.id.tv_fuel_costs);
        layoutRecentHeader = view.findViewById(R.id.layout_recent_trips_header);
        containerRecentTrips = view.findViewById(R.id.container_recent_trips);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        CardView cardHeroCta = view.findViewById(R.id.card_hero_cta);
        cardHeroCta.setOnClickListener(v -> navigateToCalculate());

        TextView tvSeeAll = view.findViewById(R.id.tv_see_all);
        tvSeeAll.setOnClickListener(v -> navigateToHistory());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (getContext() == null)
            return;
        List<TripRecord> trips = StorageService.getAllTrips(getContext());
        double totalSpent = 0, totalKm = 0, totalToll = 0, totalFuel = 0;

        for (TripRecord t : trips) {
            totalSpent += t.getTotalCost();
            totalKm += t.getDistanceKm();
            totalToll += t.getTollCost();
            totalFuel += t.getFuelCost();
        }

        tvTotalSpent.setText(getString(R.string.rupee_amount, (int) totalSpent));
        tvDistance.setText(getString(R.string.km_amount, (int) totalKm));
        tvTrips.setText(String.valueOf(trips.size()));
        tvTollPlazas.setText(String.valueOf(getResources().getInteger(R.integer.total_toll_plazas)));
        tvTollCosts.setText(getString(R.string.rupee_amount, (int) totalToll));
        tvFuelCosts.setText(getString(R.string.rupee_amount, (int) totalFuel));

        if (trips.isEmpty()) {
            layoutRecentHeader.setVisibility(View.GONE);
            containerRecentTrips.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            layoutRecentHeader.setVisibility(View.VISIBLE);
            containerRecentTrips.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            populateRecentTrips(trips);
        }
    }

    private void populateRecentTrips(List<TripRecord> trips) {
        containerRecentTrips.removeAllViews();
        if (trips.isEmpty())
            return;

        // StorageService appending usually means the last item is the most recent (or
        // first, depending on list order).
        // Let's assume the last item is the latest or sort it by timestamp descending.
        trips.sort((t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));

        int maxTrips = Math.min(trips.size(), 5);
        for (int i = 0; i < maxTrips; i++) {
            TripRecord trip = trips.get(i);

            View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_trip_history,
                    containerRecentTrips, false);

            TextView tvRoute = itemView.findViewById(R.id.tv_route);
            TextView tvDetails = itemView.findViewById(R.id.tv_trip_details);
            TextView tvTotalCost = itemView.findViewById(R.id.tv_total_cost);
            TextView tvDate = itemView.findViewById(R.id.tv_date);
            ImageView ivVehicle = itemView.findViewById(R.id.iv_vehicle);
            ImageView btnDelete = itemView.findViewById(R.id.btn_delete_item);

            btnDelete.setVisibility(View.GONE);

            String src = trip.getSource().contains(",") ? trip.getSource().split(",")[0].trim() : trip.getSource();
            String dst = trip.getDestination().contains(",") ? trip.getDestination().split(",")[0].trim()
                    : trip.getDestination();
            tvRoute.setText(getString(R.string.route_format, src, dst));
            String vehicleType = trip.getVehicleType().substring(0, 1).toUpperCase(java.util.Locale.getDefault()) +
                    trip.getVehicleType().substring(1);
            String tripType = trip.isRoundTrip() ? "Round Trip" : "One Way";
            tvDetails.setText(getString(R.string.trip_details_format, vehicleType, tripType));

            tvTotalCost.setText(getString(R.string.rupee_amount, (int) trip.getTotalCost()));
            tvDate.setText(DateUtils.formatTimestamp(trip.getTimestamp()));

            switch (trip.getVehicleType()) {
                case "suv":
                    ivVehicle.setImageResource(R.drawable.ic_suv);
                    break;
                case "bus":
                case "truck":
                    ivVehicle.setImageResource(R.drawable.ic_bus);
                    break;
                default:
                    ivVehicle.setImageResource(R.drawable.ic_car);
                    break;
            }

            itemView.setOnClickListener(v -> {
                if (!isAdded() || getActivity() == null) return;
                TripResultFragment fragment = TripResultFragment.newInstance(trip);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            });

            containerRecentTrips.addView(itemView);
        }
    }

    private void navigateToCalculate() {
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_calculate);
            }
        }
    }

    private void navigateToHistory() {
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_history);
            }
        }
    }
}
