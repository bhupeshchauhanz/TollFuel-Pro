package com.tollfuelpro.app.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.tollfuelpro.app.BuildConfig;
import com.tollfuelpro.app.R;
import com.tollfuelpro.app.models.TripRecord;
import com.tollfuelpro.app.services.StorageService;
import com.tollfuelpro.app.utils.PolylineUtils;
import java.util.List;

public class RouteMapFragment extends Fragment {

    private static final String ARG_TRIP_RECORD = "trip_record";
    private TripRecord tripRecord;
    private WebView webView;

    // Layout Views
    private LinearLayout layoutEmptyMap;
    private CardView cardMapHeader;
    private CardView cardTripOverview;
    private Button btnCalcTrip;
    private View btnMapBack;
    private TextView tvOverviewRoute;
    private TextView tvOverviewDetails;
    private TextView tvOverviewTotalCost;
    private TextView tvOverviewDistance;
    private TextView tvOverviewTolls;
    private Button btnOverviewDetails;

    public static RouteMapFragment newInstance(TripRecord record) {
        RouteMapFragment fragment = new RouteMapFragment();
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
        return inflater.inflate(R.layout.fragment_route_map, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind layout views
        webView = view.findViewById(R.id.webview_map);
        layoutEmptyMap = view.findViewById(R.id.layout_empty_map);
        cardMapHeader = view.findViewById(R.id.card_map_header);
        cardTripOverview = view.findViewById(R.id.card_trip_overview);
        btnCalcTrip = view.findViewById(R.id.btn_calc_trip);
        btnMapBack = view.findViewById(R.id.btn_map_back);
        tvOverviewRoute = view.findViewById(R.id.tv_overview_route);
        tvOverviewDetails = view.findViewById(R.id.tv_overview_details);
        tvOverviewTotalCost = view.findViewById(R.id.tv_overview_total_cost);
        tvOverviewDistance = view.findViewById(R.id.tv_overview_distance);
        tvOverviewTolls = view.findViewById(R.id.tv_overview_tolls);
        btnOverviewDetails = view.findViewById(R.id.btn_overview_details);

        // Load the latest trip record from local history if none was passed
        if (tripRecord == null && getContext() != null) {
            List<TripRecord> trips = StorageService.getAllTrips(getContext());
            if (!trips.isEmpty()) {
                trips.sort((t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                tripRecord = trips.get(0);
            }
        }

        // Configure layout visibility based on whether we have route data
        if (tripRecord == null || tripRecord.getRouteGeometry() == null || tripRecord.getRouteGeometry().isEmpty()) {
            webView.setVisibility(View.GONE);
            layoutEmptyMap.setVisibility(View.VISIBLE);
            cardMapHeader.setVisibility(View.GONE);
            cardTripOverview.setVisibility(View.GONE);

            btnCalcTrip.setOnClickListener(v -> {
                if (getActivity() != null) {
                    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_calculate);
                    }
                }
            });
        } else {
            webView.setVisibility(View.VISIBLE);
            layoutEmptyMap.setVisibility(View.GONE);
            cardMapHeader.setVisibility(View.VISIBLE);
            cardTripOverview.setVisibility(View.VISIBLE);

            // Back button handling
            boolean hasBackStack = getParentFragmentManager().getBackStackEntryCount() > 0;
            btnMapBack.setVisibility(hasBackStack ? View.VISIBLE : View.GONE);
            btnMapBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });

            // Populate Overview Card
            String src = tripRecord.getSource().contains(",") ? tripRecord.getSource().split(",")[0].trim() : tripRecord.getSource();
            String dst = tripRecord.getDestination().contains(",") ? tripRecord.getDestination().split(",")[0].trim() : tripRecord.getDestination();
            tvOverviewRoute.setText(src + " → " + dst);

            String vehicleType = tripRecord.getVehicleType().substring(0, 1).toUpperCase(java.util.Locale.getDefault()) +
                    tripRecord.getVehicleType().substring(1);
            String tripType = tripRecord.isRoundTrip() ? "Round Trip" : "One Way";
            tvOverviewDetails.setText(vehicleType + " • " + tripType);

            tvOverviewTotalCost.setText("₹" + (int) tripRecord.getTotalCost());
            tvOverviewDistance.setText((int) tripRecord.getDistanceKm() + " km");

            int tollCount = tripRecord.getTollResults() != null ? tripRecord.getTollResults().size() : 0;
            tvOverviewTolls.setText(tollCount + (tollCount == 1 ? " Plaza" : " Plazas"));

            btnOverviewDetails.setOnClickListener(v -> {
                if (isAdded() && getActivity() != null && tripRecord != null) {
                    TripResultFragment resultFragment = TripResultFragment.newInstance(tripRecord);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, resultFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });

            // WebView settings & URL loading
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setBuiltInZoomControls(true);
            settings.setDisplayZoomControls(false);
            settings.setLoadWithOverviewMode(true);
            settings.setUseWideViewPort(true);
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
            settings.setGeolocationEnabled(true);
            if (getContext() != null) {
                settings.setGeolocationDatabasePath(getContext().getFilesDir().getPath());
            }
            
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    if (request != null && request.isForMainFrame()) {
                        showFallback();
                    }
                }
                
                @SuppressWarnings("deprecation")
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    showFallback();
                }
            });
            
            webView.setWebChromeClient(new android.webkit.WebChromeClient() {
                @Override
                public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {
                    callback.invoke(origin, true, false);
                }

                @Override
                public boolean onConsoleMessage(android.webkit.ConsoleMessage consoleMessage) {
                    android.util.Log.d("MapWebViewConsole", consoleMessage.message() + " -- From line "
                            + consoleMessage.lineNumber() + " of "
                            + consoleMessage.sourceId());
                    return true;
                }
            });
            
            webView.loadDataWithBaseURL(null, generateMapHtml(), "text/html", "UTF-8", null);
        }
    }

    private void showFallback() {
        if (getView() == null) return;
        webView.loadDataWithBaseURL(null,
            "<html><body style='background:#0B0F14;color:#A0AEC0;display:flex;flex-direction:column;align-items:center;justify-content:center;height:100vh;margin:0;font-family:sans-serif;padding:24px;text-align:center;'>" +
            "<p style='font-size:24px;margin-bottom:12px;'>🌍</p>" +
            "<p style='font-size:16px;color:#FFFFFF;margin-bottom:8px;'>Map unavailable</p>" +
            "<p style='font-size:13px;color:#55667A;'>Check your internet connection and try again.</p>" +
            "</body></html>",
            "text/html", "UTF-8", null);
    }

    private String generateMapHtml() {
        if (tripRecord == null || tripRecord.getRouteGeometry() == null || tripRecord.getRouteGeometry().isEmpty()) {
            return "<html><body style='background:#0B0F14;color:#A0AEC0;display:flex;align-items:center;justify-content:center;height:100vh;margin:0;font-family:sans-serif;font-size:16px;'>" +
                   "<p>Calculate a trip first to view it on the map.</p></body></html>";
        }

        List<PolylineUtils.LatLng> path = PolylineUtils.decode(tripRecord.getRouteGeometry());

        StringBuilder coordsJson = new StringBuilder("[");
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) coordsJson.append(",");
            coordsJson.append("[").append(path.get(i).lat).append(",").append(path.get(i).lng).append("]");
        }
        coordsJson.append("]");

        double totalDistKm = tripRecord.getDistanceKm();
        boolean isRoundTrip = tripRecord.isRoundTrip();
        double effectiveDist = isRoundTrip ? totalDistKm * 2 : totalDistKm;
        int tollCount = tripRecord.getTollResults() != null ? tripRecord.getTollResults().size() : 0;

        StringBuilder tollMarkersJson = new StringBuilder("[");
        if (tollCount > 0 && path.size() > 1) {
            for (int i = 0; i < tollCount; i++) {
                if (i > 0) tollMarkersJson.append(",");
                double fraction = (double) (i + 1) / (tollCount + 1);
                int idx = (int) (fraction * (path.size() - 1));
                if (idx >= path.size()) idx = path.size() - 1;
                tollMarkersJson.append("[").append(path.get(idx).lat).append(",").append(path.get(idx).lng).append("]");
            }
        }
        tollMarkersJson.append("]");

        int fuelCount = (int) (effectiveDist / 50);
        if (fuelCount < 1) fuelCount = 1;
        StringBuilder fuelMarkersJson = new StringBuilder("[");
        if (path.size() > 1) {
            for (int i = 0; i < fuelCount; i++) {
                if (i > 0) fuelMarkersJson.append(",");
                double fraction = (double) (i + 1) / (fuelCount + 1);
                int idx = (int) (fraction * (path.size() - 1));
                if (idx >= path.size()) idx = path.size() - 1;
                fuelMarkersJson.append("[").append(path.get(idx).lat).append(",").append(path.get(idx).lng).append("]");
            }
        }
        fuelMarkersJson.append("]");

        int evCount = (int) (effectiveDist / 100);
        if (evCount < 1) evCount = 1;
        StringBuilder evMarkersJson = new StringBuilder("[");
        if (path.size() > 1) {
            for (int i = 0; i < evCount; i++) {
                if (i > 0) evMarkersJson.append(",");
                double fraction = (double) (i + 1) / (evCount + 1);
                int idx = (int) (fraction * (path.size() - 1));
                if (idx >= path.size()) idx = path.size() - 1;
                evMarkersJson.append("[").append(path.get(idx).lat).append(",").append(path.get(idx).lng).append("]");
            }
        }
        evMarkersJson.append("]");

        return "<!DOCTYPE html><html><head>" +
               "<meta charset='utf-8' />" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no' />" +
               "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css' onerror='showMapError()' />" +
               "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js' onerror='showMapError()'></script>" +
               "<style>" +
               "body{margin:0;padding:0;background:#0B0F14}#map{width:100vw;height:100vh;background:#0B0F14}" +
               ".custom-popup .leaflet-popup-content-wrapper{background:#121922;color:#FFFFFF;border-radius:12px;padding:4px;border:1px solid #2A3441;box-shadow:0 4px 12px rgba(0,0,0,0.5)}" +
               ".custom-popup .leaflet-popup-tip{background:#121922}" +
               ".popup-title{font-weight:bold;font-size:13px;color:#00E6A8;margin-bottom:2px}" +
               ".popup-desc{font-size:11px;color:#A0AEC0}" +
               ".map-controls{position:absolute;top:80px;right:16px;z-index:1000;background:rgba(18,25,34,0.85);border:1px solid #2A3441;border-radius:12px;padding:8px;display:flex;flex-direction:column;gap:6px;backdrop-filter:blur(8px)}" +
               ".control-item{display:flex;align-items:center;gap:8px;color:#A0AEC0;font-size:11px;font-family:sans-serif;font-weight:bold;padding:4px 8px;border-radius:6px;cursor:pointer;transition:all 0.2s ease}" +
               ".control-item:hover{background:rgba(255,255,255,0.05)}" +
               ".control-item.active{color:#FFFFFF}" +
               ".control-dot{width:8px;height:8px;border-radius:50%;border:1px solid #fff}" +
               "</style>" +
               "</head><body>" +
               "<div id='map'></div>" +
               "<div class='map-controls'>" +
               "    <div class='control-item active' id='toggle-tolls' onclick='toggleLayer(\"tolls\")'>" +
               "        <span class='control-dot' style='background:#32D583'></span> Tolls" +
               "    </div>" +
               "    <div class='control-item active' id='toggle-fuels' onclick='toggleLayer(\"fuels\")'>" +
               "        <span class='control-dot' style='background:#FF8C00'></span> Fuel" +
               "    </div>" +
               "    <div class='control-item active' id='toggle-evs' onclick='toggleLayer(\"evs\")'>" +
               "        <span class='control-dot' style='background:#4A90E2'></span> EV" +
               "    </div>" +
               "</div>" +
               "<script>" +
               "function showMapError() {" +
               "    document.body.innerHTML = \"<div style='color:#A0AEC0;display:flex;flex-direction:column;align-items:center;justify-content:center;height:100vh;text-align:center;font-family:sans-serif;padding:24px;background:#0B0F14;'><p style='font-size:24px;'>🌍</p><p style='font-size:16px;color:#FFFFFF;margin-bottom:8px;'>Failed to load Map</p><p style='font-size:13px;color:#55667A;'>Please check your internet connection.</p></div>\";" +
               "}" +
               "if (typeof L === 'undefined') {" +
               "    showMapError();" +
               "} else {" +
               "    const coords = " + coordsJson + ";" +
               "    const tolls = " + tollMarkersJson + ";" +
               "    const fuels = " + fuelMarkersJson + ";" +
               "    const evs = " + evMarkersJson + ";" +
               "    const map = L.map('map', { zoomControl: false, attributionControl: false }).setView([20.5937, 78.9629], 5);" +
               "    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {" +
               "        maxZoom: 20" +
               "    }).addTo(map);" +
               "    if (coords.length > 0) {" +
               "        const polyline = L.polyline(coords, { color: '#32D583', weight: 4, opacity: 0.9 }).addTo(map);" +
               "        L.polyline(coords, { color: '#32D583', weight: 10, opacity: 0.2 }).addTo(map);" +
               "        map.fitBounds(polyline.getBounds(), { padding: [50, 50] });" +
               "    }" +
               "    const tollMarkers = [];" +
               "    tolls.forEach(c => {" +
               "        const m = L.circleMarker([c[0], c[1]], { color: '#FFFFFF', fillColor: '#32D583', fillOpacity: 0.9, radius: 6, weight: 1.5 }).addTo(map)" +
               "            .bindPopup('<div class=\"popup-title\">Toll Plaza</div><div class=\"popup-desc\">NHAI Toll Point</div>', { className: 'custom-popup' });" +
               "        tollMarkers.push(m);" +
               "    });" +
               "    const fuelMarkers = [];" +
               "    fuels.forEach(c => {" +
               "        const m = L.circleMarker([c[0], c[1]], { color: '#FFFFFF', fillColor: '#FF8C00', fillOpacity: 0.9, radius: 6, weight: 1.5 }).addTo(map)" +
               "            .bindPopup('<div class=\"popup-title\">Petrol Pump</div><div class=\"popup-desc\">Fuel Station</div>', { className: 'custom-popup' });" +
               "        fuelMarkers.push(m);" +
               "    });" +
               "    const evMarkers = [];" +
               "    evs.forEach(c => {" +
               "        const m = L.circleMarker([c[0], c[1]], { color: '#FFFFFF', fillColor: '#4A90E2', fillOpacity: 0.9, radius: 6, weight: 1.5 }).addTo(map)" +
               "            .bindPopup('<div class=\"popup-title\">EV Charger</div><div class=\"popup-desc\">Charging Station</div>', { className: 'custom-popup' });" +
               "        evMarkers.push(m);" +
               "    });" +
               "    window.toggleLayer = function(type) {" +
               "        const el = document.getElementById('toggle-' + type);" +
               "        const active = el.classList.toggle('active');" +
               "        let list = [];" +
               "        if (type === 'tolls') list = tollMarkers;" +
               "        else if (type === 'fuels') list = fuelMarkers;" +
               "        else if (type === 'evs') list = evMarkers;" +
               "        list.forEach(m => {" +
               "            if (active) m.addTo(map);" +
               "            else m.remove();" +
               "        });" +
               "    };" +
               "    map.locate({ setView: false, watch: true, enableHighAccuracy: true });" +
               "    map.on('locationfound', (e) => {" +
               "        if (window.userMarker) {" +
               "            window.userMarker.setLatLng(e.latlng);" +
               "        } else {" +
               "            window.userMarker = L.marker(e.latlng, {" +
               "                icon: L.divIcon({" +
               "                    className: 'user-location-marker'," +
               "                    html: '<div style=\"background:#00E6A8;width:12px;height:12px;border-radius:50%;border:2px solid #ffffff;box-shadow:0 0 10px #00E6A8;\"></div>'," +
               "                    iconSize: [12, 12]," +
               "                    iconAnchor: [6, 6]" +
               "                })" +
               "            }).addTo(map);" +
               "        }" +
               "    });" +
               "}" +
               "</script></body></html>";
    }
}
