package com.tollfuelpro.app.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.tollfuelpro.app.R;
import com.tollfuelpro.app.models.TripRecord;
import com.tollfuelpro.app.services.MapboxService;
import com.tollfuelpro.app.services.StorageService;
import com.tollfuelpro.app.utils.TripCalculator;

public class CalculateFragment extends Fragment {

    private EditText etSource, etDestination, etMileage, etFuelPrice;
    private EditText etBatteryCapacity, etChargeLevel, etCostPerKwh, etEvRange;
    private android.widget.Spinner spinnerVehicleType;
    private TextView tvOneWay, tvRoundTrip;
    private TextView tvFuelPetrol, tvFuelDiesel, tvFuelEV;
    private LinearLayout layoutFuelFields, layoutEVFields;
    private android.widget.CheckBox cbIncludeFuel;
    private Button btnCalculate;
    private ProgressBar pbCalculate;

    private CardView cardSuggestions;
    private TextView tvSuggestionHeader;
    private LinearLayout containerPresetCities;
    private RecyclerView rvMapboxSuggestions;

    private String selectedVehicle = "Car/Jeep/Van";
    private String selectedFuelType = "petrol";
    private boolean isRoundTrip = false;

    private double srcLat = 0, srcLng = 0;
    private double dstLat = 0, dstLng = 0;

    private boolean isSettingSource = true;

    private okhttp3.Call activeDirectionsCall;
    private final java.util.List<okhttp3.Call> activeGeocodingCalls = new java.util.ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calculate, container, false);
        initViews(view);
        setupVehicleSelection();
        setupTripTypeSelection();
        setupFuelTypeSelection();
        setupCalculateAction();
        setupLocationInput();
        return view;
    }

    private void initViews(View view) {
        etSource = view.findViewById(R.id.et_source);
        etDestination = view.findViewById(R.id.et_destination);
        etMileage = view.findViewById(R.id.et_mileage);
        etFuelPrice = view.findViewById(R.id.et_fuel_price);
        etBatteryCapacity = view.findViewById(R.id.et_battery_capacity);
        etChargeLevel = view.findViewById(R.id.et_charge_level);
        etCostPerKwh = view.findViewById(R.id.et_cost_per_kwh);
        etEvRange = view.findViewById(R.id.et_ev_range);

        tvOneWay = view.findViewById(R.id.tv_trip_one_way);
        tvRoundTrip = view.findViewById(R.id.tv_trip_round);

        tvFuelPetrol = view.findViewById(R.id.tv_fuel_petrol);
        tvFuelDiesel = view.findViewById(R.id.tv_fuel_diesel);
        tvFuelEV = view.findViewById(R.id.tv_fuel_ev);
        layoutFuelFields = view.findViewById(R.id.layout_fuel_fields);
        layoutEVFields = view.findViewById(R.id.layout_ev_fields);
        cbIncludeFuel = view.findViewById(R.id.cb_include_fuel);

        btnCalculate = view.findViewById(R.id.btn_calculate);
        pbCalculate = view.findViewById(R.id.pb_calculate);

        cardSuggestions = view.findViewById(R.id.card_suggestions);
        tvSuggestionHeader = view.findViewById(R.id.tv_suggestion_header);
        containerPresetCities = view.findViewById(R.id.container_preset_cities);
        rvMapboxSuggestions = view.findViewById(R.id.rv_mapbox_suggestions);
        spinnerVehicleType = view.findViewById(R.id.spinner_vehicle_type);

        view.findViewById(R.id.btn_swap).setOnClickListener(v -> swapLocations());
    }

    private void setupVehicleSelection() {
        String[] vehicleTypes = {
                getString(R.string.vehicle_car),
                getString(R.string.vehicle_lcv),
                getString(R.string.vehicle_bus_truck),
                getString(R.string.vehicle_3_axle),
                getString(R.string.vehicle_4_6_axle),
                getString(R.string.vehicle_hcm_eme),
                getString(R.string.vehicle_7_axle)
        };
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, vehicleTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleType.setAdapter(adapter);
        spinnerVehicleType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedVehicle = vehicleTypes[position];
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedVehicle = vehicleTypes[0];
            }
        });
        selectedVehicle = vehicleTypes[0];
    }

    private void setupTripTypeSelection() {
        tvOneWay.setOnClickListener(v -> {
            isRoundTrip = false;
            tvOneWay.setBackgroundResource(R.drawable.bg_trip_type_selected);
            tvOneWay.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            tvRoundTrip.setBackgroundResource(R.drawable.bg_trip_type_unselected);
            tvRoundTrip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        });

        tvRoundTrip.setOnClickListener(v -> {
            isRoundTrip = true;
            tvRoundTrip.setBackgroundResource(R.drawable.bg_trip_type_selected);
            tvRoundTrip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            tvOneWay.setBackgroundResource(R.drawable.bg_trip_type_unselected);
            tvOneWay.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        });
        
        // Final trip type labels
        tvOneWay.setText(R.string.one_way);
        tvRoundTrip.setText(R.string.round_trip);
    }

    private void setupFuelTypeSelection() {
        tvFuelPetrol.setOnClickListener(v -> selectFuelType("petrol", tvFuelPetrol));
        tvFuelDiesel.setOnClickListener(v -> selectFuelType("diesel", tvFuelDiesel));
        tvFuelEV.setOnClickListener(v -> selectFuelType("ev", tvFuelEV));
        selectFuelType("petrol", tvFuelPetrol);
    }

    private void selectFuelType(String fuelType, TextView selected) {
        selectedFuelType = fuelType;
        TextView[] all = { tvFuelPetrol, tvFuelDiesel, tvFuelEV };
        for (TextView tv : all) {
            boolean isSelected = tv == selected;
            tv.setBackgroundResource(isSelected ? R.drawable.bg_trip_type_selected : R.drawable.bg_trip_type_unselected);
            tv.setTextColor(ContextCompat.getColor(requireContext(),
                    isSelected ? R.color.text_primary : R.color.text_secondary));
        }
        boolean isEv = "ev".equals(fuelType);
        layoutFuelFields.setVisibility(isEv ? View.GONE : View.VISIBLE);
        layoutEVFields.setVisibility(isEv ? View.VISIBLE : View.GONE);
        cbIncludeFuel.setVisibility(isEv ? View.GONE : View.VISIBLE);
    }

    private void swapLocations() {
        String tempStr = etSource.getText().toString();
        etSource.setText(etDestination.getText().toString());
        etDestination.setText(tempStr);

        double tempLat = srcLat, tempLng = srcLng;
        srcLat = dstLat;
        srcLng = dstLng;
        dstLat = tempLat;
        dstLng = tempLng;
    }

    private void setupLocationInput() {
        // Build preset chips
        String[] presetCities = getResources().getStringArray(R.array.preset_cities);
        for (String city : presetCities) {
            TextView chip = new TextView(requireContext());
            chip.setText(city);
            chip.setBackgroundResource(R.drawable.bg_chip);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            chip.setTextSize(13);
            int p = (int) (8 * getResources().getDisplayMetrics().density);
            int h = (int) (12 * getResources().getDisplayMetrics().density);
            chip.setPadding(h, p, h, p);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(p);
            chip.setLayoutParams(lp);

            chip.setOnClickListener(v -> {
                if (city.equals(presetCities[0])) { // 📍 Current Location
                    fetchCurrentLocation();
                    cardSuggestions.setVisibility(View.GONE);
                } else {
                    if (isSettingSource) {
                        etDestination.requestFocus();
                        etSource.setText(city);
                        resolvePresetCity(city, true);
                    } else {
                        etDestination.clearFocus();
                        etDestination.setText(city);
                        cardSuggestions.setVisibility(View.GONE);
                        resolvePresetCity(city, false);
                    }
                }
            });
            containerPresetCities.addView(chip);
        }

        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                isSettingSource = (v.getId() == R.id.et_source);
                tvSuggestionHeader.setText(isSettingSource ? getString(R.string.select_source_header) : getString(R.string.select_destination_header));
                cardSuggestions.setVisibility(View.VISIBLE);

                EditText activeEt = isSettingSource ? etSource : etDestination;
                if (activeEt.getText().toString().trim().length() < 3) {
                    containerPresetCities.setVisibility(View.VISIBLE);
                    rvMapboxSuggestions.setVisibility(View.GONE);
                } else {
                    containerPresetCities.setVisibility(View.GONE);
                    rvMapboxSuggestions.setVisibility(View.VISIBLE);
                }
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!etSource.hasFocus() && !etDestination.hasFocus()) {
                        cardSuggestions.setVisibility(View.GONE);
                    }
                }, 200);
            }
        };

        etSource.setOnFocusChangeListener(focusListener);
        etDestination.setOnFocusChangeListener(focusListener);

        rvMapboxSuggestions.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));

        etSource.addTextChangedListener(createTextWatcher(etSource, true));
        etDestination.addTextChangedListener(createTextWatcher(etDestination, false));
    }

    private TextWatcher createTextWatcher(EditText editText, boolean isSource) {
        return new TextWatcher() {
            private Runnable searchRunnable;
            private okhttp3.Call currentCall;
            private String lastQuery = "";
            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null)
                    handler.removeCallbacks(searchRunnable);

                if (!editText.hasFocus())
                    return;

                cardSuggestions.setVisibility(View.VISIBLE);

                if (s.toString().trim().length() < 3) {
                    containerPresetCities.setVisibility(View.VISIBLE);
                    rvMapboxSuggestions.setVisibility(View.GONE);
                    lastQuery = "";
                    return;
                }

                String currentQuery = s.toString().trim();
                if (currentQuery.equals(lastQuery))
                    return;
                lastQuery = currentQuery;

                containerPresetCities.setVisibility(View.GONE);

                if (currentCall != null) {
                    currentCall.cancel();
                }

                searchRunnable = () -> {
                    if (!editText.hasFocus())
                        return;

                    currentCall = MapboxService.searchCity(s.toString().trim(), new MapboxService.GeocodingCallback() {
                        @Override
                        public void onResults(java.util.List<MapboxService.GeocodingResult> results) {
                            handler.post(() -> {
                                if (!editText.hasFocus())
                                    return; // Stop cross-field race condition

                                rvMapboxSuggestions.setVisibility(View.VISIBLE);
                                com.tollfuelpro.app.adapters.LocationSuggestionAdapter adapter = new com.tollfuelpro.app.adapters.LocationSuggestionAdapter(
                                        results, result -> {
                                            if (isSource) {
                                                srcLat = result.latitude;
                                                srcLng = result.longitude;
                                                etDestination.requestFocus();
                                                etSource.setText(result.placeName);
                                                etSource.setSelection(etSource.getText().length());
                                            } else {
                                                dstLat = result.latitude;
                                                dstLng = result.longitude;
                                                etDestination.clearFocus();
                                                etDestination.setText(result.placeName);
                                                etDestination.setSelection(etDestination.getText().length());
                                                cardSuggestions.setVisibility(View.GONE);
                                            }
                                        });
                                rvMapboxSuggestions.setAdapter(adapter);
                            });
                        }

                        @Override
                        public void onError(String message) {
                        }
                    });
                };
                handler.postDelayed(searchRunnable, 150); // Lowered debounce for snappier feeling
            }
        };
    }

    private void fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(requireContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            showError(getString(R.string.enable_location_error));
            requestPermissions(new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION }, 100);
            return;
        }

        android.location.LocationManager lm = (android.location.LocationManager) requireContext()
                .getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            boolean isSourceSnap = isSettingSource;
            android.location.Location loc = lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
            if (loc == null)
                loc = lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
            if (loc != null) {
                double lat = loc.getLatitude();
                double lng = loc.getLongitude();

                if (isSourceSnap)
                    etSource.setText(getString(R.string.fetching_location));
                else
                    etDestination.setText(getString(R.string.fetching_location));

                okhttp3.Call geoCall = MapboxService.reverseGeocode(lat, lng, new MapboxService.GeocodingCallback() {
                    @Override
                    public void onResults(java.util.List<MapboxService.GeocodingResult> results) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (!results.isEmpty()) {
                                String placeName = results.get(0).placeName;
                                if (isSourceSnap) {
                                    srcLat = lat;
                                    srcLng = lng;
                                    etSource.setText(placeName);
                                    etSource.clearFocus();
                                    etDestination.requestFocus();
                                } else {
                                    dstLat = lat;
                                    dstLng = lng;
                                    etDestination.setText(placeName);
                                    etDestination.clearFocus();
                                }
                            } else {
                                showError(getString(R.string.error_identify_place));
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            showError(getString(R.string.error_geocoding_failed, message));
                        });
                    }
                });
                if (geoCall != null) {
                    activeGeocodingCalls.add(geoCall);
                }
            } else {
                showError(getString(R.string.error_retrieve_location));
            }
        }
    }

    private void resolvePresetCity(String city, boolean isSource) {
        MapboxService.searchCity(city, new MapboxService.GeocodingCallback() {
            @Override
            public void onResults(java.util.List<MapboxService.GeocodingResult> results) {
                if (!results.isEmpty()) {
                    if (isSource) {
                        srcLat = results.get(0).latitude;
                        srcLng = results.get(0).longitude;
                    } else {
                        dstLat = results.get(0).latitude;
                        dstLng = results.get(0).longitude;
                    }
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void setupCalculateAction() {
        btnCalculate.setOnClickListener(v -> {
            String src = etSource.getText().toString().trim();
            String dst = etDestination.getText().toString().trim();
            String milStr = etMileage.getText().toString().trim();
            String fuelStr = etFuelPrice.getText().toString().trim();

            if (src.isEmpty()) {
                showError(getString(R.string.error_select_source));
                return;
            }
            if (dst.isEmpty()) {
                showError(getString(R.string.error_select_destination));
                return;
            }
            if (src.equalsIgnoreCase(dst)) {
                showError(getString(R.string.error_same_city));
                return;
            }

            boolean isEv = "ev".equals(selectedFuelType);

            if (isEv) {
                String batStr = etBatteryCapacity.getText().toString().trim();
                String chgStr = etChargeLevel.getText().toString().trim();
                String kwhStr = etCostPerKwh.getText().toString().trim();
                String rangeStr = etEvRange.getText().toString().trim();
                if (batStr.isEmpty() || chgStr.isEmpty() || kwhStr.isEmpty() || rangeStr.isEmpty()) {
                    showError(getString(R.string.error_enter_details));
                    return;
                }
            } else {
                if (milStr.isEmpty() || fuelStr.isEmpty()) {
                    showError(getString(R.string.error_enter_details));
                    return;
                }
            }

            double mileage = milStr.isEmpty() ? 0 : Double.parseDouble(milStr);
            double fuelPrice = fuelStr.isEmpty() ? 0 : Double.parseDouble(fuelStr);
            double batteryCapacityKwh = etBatteryCapacity.getText().toString().trim().isEmpty() ? 0
                    : Double.parseDouble(etBatteryCapacity.getText().toString().trim());
            double chargeLevelPercent = etChargeLevel.getText().toString().trim().isEmpty() ? 0
                    : Double.parseDouble(etChargeLevel.getText().toString().trim());
            double costPerKwh = etCostPerKwh.getText().toString().trim().isEmpty() ? 0
                    : Double.parseDouble(etCostPerKwh.getText().toString().trim());
            double evRangeKm = etEvRange.getText().toString().trim().isEmpty() ? 0
                    : Double.parseDouble(etEvRange.getText().toString().trim());

            if (!isEv && (mileage <= 0 || fuelPrice <= 0)) {
                showError(getString(R.string.error_invalid_details));
                return;
            }

            if (srcLat == 0 || dstLat == 0) {
                showError(getString(R.string.error_pick_from_suggestions));
                return;
            }

            if (!isNetworkAvailable()) {
                showError(getString(R.string.error_network));
                return;
            }

            boolean includeFuelCost = cbIncludeFuel.isChecked();
            String fuelType = selectedFuelType;

            btnCalculate.setText("");
            btnCalculate.setEnabled(false);
            pbCalculate.setVisibility(View.VISIBLE);

            activeDirectionsCall = MapboxService.getDrivingDistance(srcLat, srcLng, dstLat, dstLng, new MapboxService.DirectionsCallback() {
                @Override
                public void onResult(double distanceKm, java.util.List<String> routeKeywords, String routeGeometry) {
                    java.util.List<com.tollfuelpro.app.utils.PolylineUtils.LatLng> path = com.tollfuelpro.app.utils.PolylineUtils
                            .decode(routeGeometry);
                    java.util.List<com.tollfuelpro.app.utils.PolylineUtils.LatLng> samples = new java.util.ArrayList<>();
                    if (!path.isEmpty()) {
                        if (path.size() <= 10) {
                            samples.addAll(path);
                        } else {
                            int stepSize = path.size() / 10;
                            for (int i = 0; i < path.size(); i += stepSize) {
                                samples.add(path.get(i));
                                if (samples.size() == 10)
                                    break;
                            }
                        }
                    }

                    java.util.List<String> geoKeywords = new java.util.concurrent.CopyOnWriteArrayList<>(routeKeywords);
                    java.util.List<String> routeStates = new java.util.concurrent.CopyOnWriteArrayList<>();
                    java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(
                            samples.size());

                    if (samples.isEmpty()) {
                        finishCalculation(distanceKm, geoKeywords, routeStates, routeGeometry);
                        return;
                    }

                    for (com.tollfuelpro.app.utils.PolylineUtils.LatLng pt : samples) {
                        okhttp3.Call geoCall = MapboxService.reverseGeocode(pt.lat, pt.lng, new MapboxService.GeocodingCallback() {
                            @Override
                            public void onResults(java.util.List<MapboxService.GeocodingResult> results) {
                                for (MapboxService.GeocodingResult r : results) {
                                    if (r.placeName != null) {
                                        for (String part : r.placeName.split(",")) {
                                            geoKeywords.add(part.trim());
                                        }
                                    }
                                    if (r.state != null && !r.state.isEmpty()) {
                                        String stateLow = r.state.toLowerCase(java.util.Locale.ROOT).trim();
                                        if (!routeStates.contains(stateLow)) {
                                            routeStates.add(stateLow);
                                        }
                                    }
                                }
                                checkFinish();
                            }

                            @Override
                            public void onError(String msg) {
                                checkFinish();
                            }

                            private void checkFinish() {
                                if (counter.decrementAndGet() == 0) {
                                    finishCalculation(distanceKm, geoKeywords, routeStates, routeGeometry);
                                }
                            }
                        });
                        if (geoCall != null) {
                            activeGeocodingCalls.add(geoCall);
                        }
                    }
                }

                private void finishCalculation(double distanceKm, java.util.List<String> finalKeywords, java.util.List<String> finalRouteStates, String routeGeometry) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        TripRecord record = TripCalculator.calculate(
                                requireContext(), src, dst, srcLat, srcLng, dstLat, dstLng,
                                distanceKm, finalKeywords, finalRouteStates, selectedVehicle, isRoundTrip,
                                mileage, fuelPrice, routeGeometry, fuelType, includeFuelCost,
                                batteryCapacityKwh, chargeLevelPercent, costPerKwh, evRangeKm);
                        StorageService.saveTrip(requireContext(), record);

                        btnCalculate.setText(getString(R.string.calculate_trip_cost));
                        btnCalculate.setEnabled(true);
                        pbCalculate.setVisibility(View.GONE);

                        if (!isAdded() || getActivity() == null) return;
                        TripResultFragment resultFragment = TripResultFragment.newInstance(record);
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, resultFragment)
                                .addToBackStack(null)
                                .commit();
                    });
                }

                @Override
                public void onError(String message) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        btnCalculate.setText(getString(R.string.calculate_trip_cost));
                        btnCalculate.setEnabled(true);
                        pbCalculate.setVisibility(View.GONE);
                        showError(getString(R.string.error_no_route) + " " + message);
                    });
                }
            });
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0
                && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        } else if (requestCode == 100) {
            showError(getString(R.string.enable_location_error));
        }
    }

    private void showError(String msg) {
        if (!isAdded() || getView() == null) return;
        Snackbar snackbar = Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.danger));
        snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        snackbar.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainHandler.removeCallbacksAndMessages(null);
        if (activeDirectionsCall != null) {
            activeDirectionsCall.cancel();
        }
        for (okhttp3.Call call : activeGeocodingCalls) {
            if (call != null && !call.isCanceled()) {
                call.cancel();
            }
        }
        activeGeocodingCalls.clear();
    }
}
