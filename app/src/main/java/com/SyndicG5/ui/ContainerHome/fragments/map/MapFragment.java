package com.SyndicG5.ui.ContainerHome.fragments.map;

import static com.SyndicG5.ui.ContainerHome.HomeContainer.setActivityName;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SyndicG5.Adapters.MonumentAdapter;
import com.SyndicG5.R;
import com.SyndicG5.databinding.FragmentEmptyBinding;
import com.SyndicG5.ui.ContainerHome.fragments.pitches.GridAutoFitItemDecoration;
import com.SyndicG5.ui.ContainerHome.fragments.pitches.MonumentViewModel;
import com.SyndicG5.ui.ContainerHome.fragments.pitches.PitchDetailsFragment;
import com.SyndicG5.ui.util.BasicUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.syndicg5.networking.models.Monument;
import com.syndicg5.networking.repository.apiRepository;
import com.syndicg5.networking.utils.AppUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.N)
@AndroidEntryPoint
public class MapFragment extends Fragment implements OnMapReadyCallback, MonumentAdapter.PitcherListener {

    public static final int LOCATION_REQUEST_CODE = 100;
    private FragmentEmptyBinding binding;
    MapViewModel viewModel;
    MonumentViewModel pitchesViewModel;
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient client;
    GoogleMap gMap;
    LatLng globalLatLng;
    BasicUtils utils = new BasicUtils();
    private RecyclerView recyclerView;
    private MonumentAdapter reservationAdapter;

    HashMap<String, Monument> pitchesHashMap = new HashMap<String, Monument>();
    HashMap<String, Monument> currentHashMap = new HashMap<String, Monument>();
    private boolean isUp = false;
    public static boolean bookedPitchPage = false;
    @Inject
    apiRepository repository;
    private List<Monument> pitchesList = new ArrayList<>();
    private static Monument monument;

    public static MapFragment newInstance(Monument m) {
        monument = m;
        bookedPitchPage = false;
        return new MapFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        pitchesViewModel = new ViewModelProvider(this).get(MonumentViewModel.class);
        binding = FragmentEmptyBinding.inflate(inflater, container, false);

        Dexter.withContext(requireContext())
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE
                ).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(
                            List<PermissionRequest> permissions,
                            PermissionToken token
                    ) {
                    }
                }).check();

        initComponents(binding.getRoot());
        attachListeners();

        getPreCurrentLocation();
        if (!utils.isNetworkAvailable(getActivity().getApplication())) {
            Toast.makeText(getActivity(), "No Network Available!", Toast.LENGTH_SHORT).show();
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setActivityName("Historical monuments");

        recyclerView = view.findViewById(R.id.pitche_recycler_view);
        reservationAdapter = new MonumentAdapter(getContext(), repository, this);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.addItemDecoration(new GridAutoFitItemDecoration(2, getResources().getDimensionPixelSize(R.dimen.alternative_horizontal_margin_page)));
        recyclerView.setAdapter(reservationAdapter);
        updateMapToBookedPitches();
    }

    private void initComponents(View root) {
        supportMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.google_map);
        supportMapFragment.getMapAsync(this);
        client = LocationServices.getFusedLocationProviderClient(getActivity());


        binding.searchBtn.setOnClickListener(view1 -> {
            binding.clientsDetails.setVisibility(View.VISIBLE);
            binding.searchBtn.setVisibility(View.GONE);
            binding.clientsSearchView.setVisibility(View.VISIBLE);
            binding.clientsHeaderBar.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
            );
            binding.clientsSearchView.setIconified(true);
            binding.clientsSearchView.requestFocus();
            AppUtils.showKeyboard(requireActivity());
        });
        binding.clientsSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                clearAndHideSearchView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void attachListeners() {

        binding.getLocationBtn.setOnClickListener(view -> supportMapFragment.getMapAsync(googleMap -> {
            gMap = googleMap;
            gMap.clear();
            getPreCurrentLocation();
            attachMarkerOnMap(pitchesHashMap);
        }));

        binding.showFullMap.setOnClickListener(view -> changeGoogleMapScale());
        binding.nearByBtn.setOnClickListener(view -> {
            HashMap<String, Monument> nearestPitches = new HashMap<String, Monument>();
            for (Map.Entry<String, Monument> pitchAreaEntry : pitchesHashMap.entrySet()) {
                Map.Entry<String, Monument> mapElement = pitchAreaEntry;
                Monument pitch = (Monument) mapElement.getValue();
                LatLng current = new LatLng(pitch.getLatitude(),
                        pitch.getLongitude());
                if (CheckDistanceBetweenTwoLocations(globalLatLng, current)) {
                    nearestPitches.put(pitchAreaEntry.getKey(), pitchAreaEntry.getValue());
                }
            }
            gMap.clear();
            attachMarkerOnMap(nearestPitches);
            if (globalLatLng != null && monument == null)
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(globalLatLng, 12));
        });

        binding.myBookingsBtn.setOnClickListener(view -> {
            bookedPitchPage = true;
            updateMapToBookedPitches();
        });
    }

    private void updateMapToBookedPitches() {
        binding.homeMapContainer.setVisibility(bookedPitchPage ? View.GONE : View.VISIBLE);
        binding.homeMapButtonsContainer.setVisibility(bookedPitchPage ? View.GONE : View.VISIBLE);
        binding.bookedPitchesContainer.setVisibility(bookedPitchPage ? View.VISIBLE : View.GONE);
        binding.clientsHeaderBar.setVisibility(bookedPitchPage ? View.VISIBLE : View.GONE);
    }

    private void filter(String query) {
        String lowerCaseQuery = query.toUpperCase(Locale.ROOT);
        List<Monument> filteredModelList = query.isEmpty() ?
                pitchesList :
                pitchesList
                        .stream()
                        .filter(pitch -> pitch.getAdresse().toUpperCase(Locale.ROOT).contains(lowerCaseQuery) || pitch.getNom().toUpperCase(Locale.ROOT).contains(lowerCaseQuery))
                        .collect(Collectors.toList());
        reservationAdapter.setList((ArrayList<Monument>) filteredModelList);
    }

    private void clearAndHideSearchView() {
        binding.clientsDetails.setVisibility(View.VISIBLE);
        binding.searchBtn.setVisibility(View.VISIBLE);
        binding.clientsSearchView.setVisibility(View.GONE);
        binding.clientsSearchView.setIconified(false);
        binding.clientsHeaderBar.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.white)
        );
        binding.clientsSearchView.clearFocus();
        AppUtils.hideKeyboard(requireActivity());
    }

    private void changeGoogleMapScale() {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        isUp = !isUp;
        if (isUp) {
            int pixels = (int) (600 * scale + 0.5f);
            binding.showFullMap.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_up));
            binding.mapContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    pixels));
        } else {
            int pixels = (int) (250 * scale + 0.5f);
            binding.showFullMap.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_down));
            binding.mapContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    pixels));
        }
    }

    public Boolean CheckDistanceBetweenTwoLocations(LatLng location, LatLng location2) {
        double lat1 = Math.toRadians(location.latitude);
        double lon1 = Math.toRadians(location.longitude);
        double lat2 = Math.toRadians(location2.latitude);
        double lon2 = Math.toRadians(location2.longitude);

        // Calculate the distance between the two locations using the Haversine formula
        final int EARTH_RADIUS = 6371; // Earth's radius in kilometers
        double distance = 2 * EARTH_RADIUS * Math.asin(Math.sqrt(Math.pow(Math.sin((lat2 - lat1) / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin((lon2 - lon1) / 2), 2)));

        return distance <= 10;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void attachMarkerOnMap(HashMap<String, Monument> hashMap) {
        supportMapFragment.getMapAsync(googleMap -> {
            gMap = googleMap;
            if (globalLatLng != null) {
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(globalLatLng, 12));
                googleMap.addMarker(new MarkerOptions().position(globalLatLng).title("You are here"));
            }
            for (Map.Entry<String, Monument> stringParkingAreaEntry : hashMap.entrySet()) {
                Map.Entry<String, Monument> mapElement = stringParkingAreaEntry;
                Monument parking = (Monument) mapElement.getValue();
                Timber.e("Add marker on map: " + parking.getNom());
                LatLng latLngParking = new LatLng(parking.getLatitude(),
                        parking.getLongitude());
                MarkerOptions option = new MarkerOptions().position(latLngParking)
                        .title(mapElement.getKey().toString())
                        .snippet(parking.getNom());
                gMap.addMarker(option);
            }
        });
    }

    private void getPreCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation(true);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation(final Boolean zoom) {
        pitchesViewModel.getMonuments();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }

        Task<Location> task = client.getLastLocation();
        task.addOnCompleteListener(t -> {
            if (t.isSuccessful() && t.getResult() != null) {
                Location location = task.getResult();
                supportMapFragment.getMapAsync(googleMap -> {
                            gMap = googleMap;
                            globalLatLng = new LatLng(location.getLatitude(),
                                    location.getLongitude());
                            googleMap.addMarker(new MarkerOptions().position(globalLatLng).title("You are here"));
                            if (zoom) {
                                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(globalLatLng, 18));
                            }
                            if (monument == null) {
                                pitchesViewModel.getListMonumentMutableLiveData()
                                        .observe(getViewLifecycleOwner(), moniment -> {
                                                    pitchesHashMap.clear();
                                                    for (Monument m : moniment) {
                                                        pitchesHashMap.put(m.getNom(), m);
                                                    }
                                                    attachMarkerOnMap(pitchesHashMap);
                                                    Timber.d("GPS Map list %s", String.valueOf(pitchesHashMap));
                                                }
                                        );

                            } else {
                                currentHashMap.clear();
                                currentHashMap.put(monument.getNom(), monument);
                                attachMarkerOnMap(currentHashMap);
                                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(monument.getLatitude(), monument.getLongitude()), 12));
                            }
                            pitchesViewModel.getMonuments();
                            pitchesViewModel.getListMonumentMutableLiveData().observe(getViewLifecycleOwner(),
                                    result -> {
                                        pitchesList = result;
                                        reservationAdapter.setList((ArrayList<Monument>) result);
                                    }
                            );
                        }
                );
            }
        });
        task.addOnFailureListener(e -> {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation(true);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        gMap.setOnInfoWindowClickListener(marker -> {
            LatLng position = marker.getPosition();
            if (position.equals(globalLatLng)) {
                return;
            }
            String[] items = {"More Infos"};
            androidx.appcompat.app.AlertDialog.Builder itemDilog = new AlertDialog.Builder(getActivity());
            itemDilog.setTitle("");
            itemDilog.setCancelable(true);
            itemDilog.setItems(items, (dialog, which) -> {
                switch (which) {
                    case 0: {
                        String UUID = marker.getTitle();
                        Monument val = (Monument) monument == null ? pitchesHashMap.get(UUID) : currentHashMap.get(UUID);
                        replace(new PitchDetailsFragment(val), "MapFragment");
                    }
                    break;
                }

            });
            itemDilog.show();

        });

    }


    private void replace(Fragment fragment, String s) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.addToBackStack(s);
        transaction.commit();
    }

    @Override
    public void onPitcherClicked(Monument monument) {
        replace(new PitchDetailsFragment(monument), "homefragment");
    }
}
