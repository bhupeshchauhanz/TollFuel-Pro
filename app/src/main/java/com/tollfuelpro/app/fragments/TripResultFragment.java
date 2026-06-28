package com.tollfuelpro.app.fragments;

import android.content.Intent;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.tollfuelpro.app.R;
import com.tollfuelpro.app.fragments.RouteMapFragment;
import com.tollfuelpro.app.models.TripRecord;
import com.tollfuelpro.app.adapters.TollBreakdownAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class TripResultFragment extends Fragment {

    private static final String ARG_TRIP_RECORD = "trip_record";
    private TripRecord tripRecord;

    private TextView tvTotalCost, tvTollCost, tvFuelCost;
    private TextView tvDistance, tvPlazasCount, tvTripType;
    private RecyclerView rvTollPlazas;
    private TollBreakdownAdapter adapter;
    private ValueAnimator totalCostAnimator;

    private View cardRoute, cardTotalCost, cardCostsRow, cardFuelDetails, layoutTollTitle, rvTollPlazasView;
    private TextView tvSource, tvDestination, tvChipVehicle, tvChipTripType, tvChipDistance, tvMileageVal,
            tvFuelPriceVal, tvFuelNeededVal;
    private TextView tvChipFuelType, tvFuelBadge;
    private LinearLayout layoutEvDetails;
    private TextView tvBatteryVal, tvChargeVal, tvCostPerKwhVal;

    public static TripResultFragment newInstance(TripRecord record) {
        TripResultFragment fragment = new TripResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TRIP_RECORD, new Gson().toJson(record));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String json = getArguments().getString(ARG_TRIP_RECORD);
            tripRecord = new Gson().fromJson(json, TripRecord.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_result, container, false);
        initViews(view);
        populateData();
        animateEntrance();
        return view;
    }

    private void initViews(View view) {
        view.findViewById(R.id.btn_close).setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().onBackPressed();
        });

        tvTotalCost = view.findViewById(R.id.tv_total_cost);
        tvTollCost = view.findViewById(R.id.tv_toll_cost);
        tvFuelCost = view.findViewById(R.id.tv_fuel_cost);
        tvPlazasCount = view.findViewById(R.id.tv_plazas_count);
        rvTollPlazas = view.findViewById(R.id.rv_toll_plazas);

        tvSource = view.findViewById(R.id.tv_source);
        tvDestination = view.findViewById(R.id.tv_destination);
        tvChipVehicle = view.findViewById(R.id.chip_vehicle);
        tvChipTripType = view.findViewById(R.id.chip_trip_type);
        tvChipDistance = view.findViewById(R.id.chip_distance);
        tvMileageVal = view.findViewById(R.id.tv_mileage_val);
        tvFuelPriceVal = view.findViewById(R.id.tv_fuel_price_val);
        tvFuelNeededVal = view.findViewById(R.id.tv_fuel_needed_val);

        tvChipFuelType = view.findViewById(R.id.chip_fuel_type);
        tvFuelBadge = view.findViewById(R.id.tv_fuel_badge);
        layoutEvDetails = view.findViewById(R.id.layout_ev_details);
        tvBatteryVal = view.findViewById(R.id.tv_battery_val);
        tvChargeVal = view.findViewById(R.id.tv_charge_val);
        tvCostPerKwhVal = view.findViewById(R.id.tv_cost_per_kwh_val);

        cardRoute = view.findViewById(R.id.card_route);
        cardTotalCost = view.findViewById(R.id.card_total_cost);
        cardCostsRow = view.findViewById(R.id.card_costs_row);
        cardFuelDetails = view.findViewById(R.id.card_fuel_details);
        layoutTollTitle = view.findViewById(R.id.layout_toll_title);
        rvTollPlazas.setLayoutManager(new LinearLayoutManager(requireContext()));

        view.findViewById(R.id.btn_view_map).setOnClickListener(v -> openMap());
        view.findViewById(R.id.btn_share_header).setOnClickListener(v -> shareBreakdown());
        view.findViewById(R.id.btn_share_main).setOnClickListener(v -> shareBreakdown());
    }

    private void animateEntrance() {
        View[] animatedViews = { cardRoute, cardTotalCost, cardCostsRow, cardFuelDetails, layoutTollTitle,
                rvTollPlazas };

        for (int i = 0; i < animatedViews.length; i++) {
            View v = animatedViews[i];
            v.setTranslationY(60f);
            v.setAlpha(0f);
            v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            v.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setStartDelay(80 + (i * 80))
                    .setDuration(400)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator(1.2f))
                    .withLayer()
                    .withEndAction(() -> v.setLayerType(View.LAYER_TYPE_NONE, null))
                    .start();
        }
    }

    private void populateData() {
        if (tripRecord == null)
            return;

        animateTotalCost(tripRecord.getTotalCost());

        tvSource.setText(tripRecord.getSource());
        tvDestination.setText(tripRecord.getDestination());

        tvSource.setOnClickListener(v -> showTooltip(tripRecord.getSource(), "Source"));
        tvDestination.setOnClickListener(v -> showTooltip(tripRecord.getDestination(), "Destination"));

        String vehicleType = tripRecord.getVehicleType().substring(0, 1).toUpperCase(java.util.Locale.getDefault()) +
                tripRecord.getVehicleType().substring(1);
        String tripType = tripRecord.isRoundTrip() ? getString(R.string.round_trip) : getString(R.string.one_way);
        tvChipVehicle.setText(getString(R.string.trip_details_format, vehicleType, tripType));
        tvChipTripType.setText(tripType);
        tvChipDistance.setText(getString(R.string.km_amount, (int) tripRecord.getDistanceKm()));

        tvTollCost.setText(getString(R.string.rupee_amount, (int) tripRecord.getTollCost()));
        tvFuelCost.setText(getString(R.string.rupee_amount, (int) tripRecord.getFuelCost()));

        String fuelType = tripRecord.getFuelType();
        boolean isEv = "ev".equals(fuelType);

        if (fuelType != null) {
            tvChipFuelType.setVisibility(View.VISIBLE);
            if (isEv) {
                tvChipFuelType.setText(R.string.vehicle_ev);
            } else {
                tvChipFuelType.setText("petrol".equals(fuelType) ? R.string.petrol : R.string.diesel);
            }
        }

        if (tripRecord.isFuelCostIncluded()) {
            tvFuelBadge.setVisibility(View.VISIBLE);
            tvFuelBadge.setText(R.string.fuel_included);
        } else {
            tvFuelBadge.setVisibility(View.VISIBLE);
            tvFuelBadge.setText(R.string.fuel_excluded_short);
        }

        if (isEv) {
            tvMileageVal.setVisibility(View.GONE);
            tvFuelPriceVal.setVisibility(View.GONE);
            tvFuelNeededVal.setText(getString(R.string.litres_format,
                    String.format(java.util.Locale.getDefault(), "%.1f kWh", tripRecord.getFuelNeeded())));
            layoutEvDetails.setVisibility(View.VISIBLE);
            tvBatteryVal.setText(String.format(java.util.Locale.getDefault(), "%.0f kWh", tripRecord.getBatteryCapacityKwh()));
            tvChargeVal.setText(String.format(java.util.Locale.getDefault(), "%.0f%%", tripRecord.getChargeLevelPercent()));
            tvCostPerKwhVal.setText(String.format(java.util.Locale.getDefault(), "₹%.2f", tripRecord.getCostPerKwh()));
        } else {
            tvMileageVal.setVisibility(View.VISIBLE);
            tvFuelPriceVal.setVisibility(View.VISIBLE);
            tvMileageVal.setText(getString(R.string.mileage_format, String.valueOf(tripRecord.getMileage())));
            tvFuelPriceVal.setText(getString(R.string.rupee_per_litre_format, String.valueOf(tripRecord.getFuelPricePerLitre())));
            double fuelNeeded = tripRecord.getDistanceKm() / tripRecord.getMileage();
            if (tripRecord.isRoundTrip())
                fuelNeeded *= 2;
            tvFuelNeededVal.setText(getString(R.string.litres_format, String.format(java.util.Locale.getDefault(), "%.1f", fuelNeeded)));
            layoutEvDetails.setVisibility(View.GONE);
        }

        tvPlazasCount.setText(getResources().getQuantityString(R.plurals.plazas_count_plural, tripRecord.getTollResults().size(), tripRecord.getTollResults().size()));

        adapter = new TollBreakdownAdapter(tripRecord.getTollResults(), tripRecord.isRoundTrip(),
                tripRecord.getVehicleType());
        rvTollPlazas.setAdapter(adapter);
    }

    private void showTooltip(String fullText, String title) {
        if (getContext() == null || getActivity() == null)
            return;

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(),
                com.google.android.material.R.style.Theme_Design_BottomSheetDialog);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(64, 64, 64, 64);
        layout.setBackgroundColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.tooltip_bg));

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(title);
        tvTitle.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.tooltip_accent));
        tvTitle.setTextSize(14f);
        tvTitle.setPadding(0, 0, 0, 16);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvContent = new TextView(requireContext());
        tvContent.setText(fullText);
        tvContent.setTextColor(android.graphics.Color.WHITE);
        tvContent.setTextSize(18f);
        tvContent.setTypeface(null, android.graphics.Typeface.BOLD);

        layout.addView(tvTitle);
        layout.addView(tvContent);

        dialog.setContentView(layout);
        dialog.show();
    }

    private void openMap() {
        if (tripRecord == null || getActivity() == null) return;
        RouteMapFragment mapFragment = RouteMapFragment.newInstance(tripRecord);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
                .addToBackStack(null)
                .commit();
    }

    private void shareBreakdown() {
        if (tripRecord == null || getContext() == null)
            return;

        // Inflate the off-screen layout
        @SuppressLint("InflateParams")
        View receiptView = LayoutInflater.from(getContext()).inflate(R.layout.layout_share_receipt, null);

        // Populate the basic data
        ((TextView) receiptView.findViewById(R.id.tv_source)).setText(tripRecord.getSource());
        ((TextView) receiptView.findViewById(R.id.tv_destination)).setText(tripRecord.getDestination());
        String shareVehicleType = tripRecord.getVehicleType().substring(0, 1).toUpperCase(java.util.Locale.getDefault())
                + tripRecord.getVehicleType().substring(1);
        String shareTripType = tripRecord.isRoundTrip() ? "Round Trip" : "One Way";
        ((TextView) receiptView.findViewById(R.id.chip_vehicle)).setText(
                getString(R.string.trip_details_format, shareVehicleType, shareTripType));
        ((TextView) receiptView.findViewById(R.id.chip_trip_type)).setText(shareTripType);
        ((TextView) receiptView.findViewById(R.id.chip_distance)).setText(getString(R.string.km_amount, (int) tripRecord.getDistanceKm()));

        String fuelType = tripRecord.getFuelType();
        if (fuelType != null) {
            TextView chipFuelType = receiptView.findViewById(R.id.chip_fuel_type);
            chipFuelType.setVisibility(View.VISIBLE);
            chipFuelType.setText("ev".equals(fuelType) ? R.string.vehicle_ev
                    : "petrol".equals(fuelType) ? R.string.petrol : R.string.diesel);
        }

        ((TextView) receiptView.findViewById(R.id.tv_total_cost)).setText(getString(R.string.rupee_amount, (int) tripRecord.getTotalCost()));
        ((TextView) receiptView.findViewById(R.id.tv_toll_cost)).setText(getString(R.string.rupee_amount, (int) tripRecord.getTollCost()));
        ((TextView) receiptView.findViewById(R.id.tv_fuel_cost)).setText(getString(R.string.rupee_amount, (int) tripRecord.getFuelCost()));

        boolean isEv = "ev".equals(fuelType);
        if (isEv) {
            receiptView.findViewById(R.id.tv_mileage_val).setVisibility(View.GONE);
            receiptView.findViewById(R.id.layout_mileage_row).setVisibility(View.GONE);
            receiptView.findViewById(R.id.layout_fuel_price_row).setVisibility(View.GONE);
            receiptView.findViewById(R.id.tv_fuel_price_val).setVisibility(View.GONE);
            ((TextView) receiptView.findViewById(R.id.tv_fuel_needed_val))
                    .setText(String.format(java.util.Locale.getDefault(), "%.1f kWh", tripRecord.getFuelNeeded()));
        } else {
            receiptView.findViewById(R.id.tv_mileage_val).setVisibility(View.VISIBLE);
            receiptView.findViewById(R.id.layout_mileage_row).setVisibility(View.VISIBLE);
            receiptView.findViewById(R.id.layout_fuel_price_row).setVisibility(View.VISIBLE);
            receiptView.findViewById(R.id.tv_fuel_price_val).setVisibility(View.VISIBLE);
            ((TextView) receiptView.findViewById(R.id.tv_mileage_val)).setText(getString(R.string.mileage_format, String.valueOf(tripRecord.getMileage())));
            ((TextView) receiptView.findViewById(R.id.tv_fuel_price_val))
                    .setText(getString(R.string.rupee_per_litre_format, String.valueOf(tripRecord.getFuelPricePerLitre())));
            double fuelNeeded = tripRecord.getDistanceKm() / tripRecord.getMileage();
            if (tripRecord.isRoundTrip())
                fuelNeeded *= 2;
            ((TextView) receiptView.findViewById(R.id.tv_fuel_needed_val))
                    .setText(getString(R.string.litres_format, String.format(java.util.Locale.getDefault(), "%.1f", fuelNeeded)));
        }

        // Populate highlight box
        int plazasCount = tripRecord.getTollResults() != null ? tripRecord.getTollResults().size() : 0;
        ((TextView) receiptView.findViewById(R.id.tv_plazas_highlight))
                .setText(getString(R.string.plazas_on_route_format, plazasCount));

        // Ensure toll container remains empty on receipt to keep it concise.
        // Measure and layout the view to its exact size
        int widthSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        receiptView.measure(widthSpec, heightSpec);
        int measuredWidth = receiptView.getMeasuredWidth();
        int measuredHeight = receiptView.getMeasuredHeight();
        receiptView.layout(0, 0, measuredWidth, measuredHeight);

        // Draw onto a Bitmap
        Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        receiptView.draw(canvas);

        // Save and trigger share intent
        try {
            File cachePath = new File(requireContext().getCacheDir(), "images");
            cachePath.mkdirs();
            File imageFile = new File(cachePath, "shared_trip.png");
            FileOutputStream stream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri imageUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", imageFile);

            String fuelLabel = tripRecord.isFuelCostIncluded() ? "Fuel Included" : "Toll Only";
            String shareText = "My Trip Estimation via TollFuel Pro\n" +
                    "Route: " + tripRecord.getSource() + " to " + tripRecord.getDestination() + "\n" +
                    "Total Cost: ₹" + (int) tripRecord.getTotalCost() + " (" + fuelLabel + ")\n" +
                    "(Toll: ₹" + (int) tripRecord.getTollCost() + ", Fuel: ₹" + (int) tripRecord.getFuelCost() + ")";

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            sendIntent.setType("image/png");
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent shareIntent = Intent.createChooser(sendIntent, "Share Trip Details");
            startActivity(shareIntent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void animateTotalCost(double targetCost) {
        // Smooth increment implementation
        if (totalCostAnimator != null) totalCostAnimator.cancel();
        
        totalCostAnimator = ValueAnimator.ofInt(0, (int) targetCost);
        totalCostAnimator.setDuration(1500);
        totalCostAnimator.setInterpolator(new android.view.animation.DecelerateInterpolator(2f));
        totalCostAnimator.addUpdateListener(animation -> {
            if (tvTotalCost != null) {
                int animatedValue = (int) animation.getAnimatedValue();
                tvTotalCost.setText(getString(R.string.rupee_amount, animatedValue));
            }
        });
        totalCostAnimator.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (totalCostAnimator != null) {
            totalCostAnimator.cancel();
            totalCostAnimator = null;
        }
    }
}
