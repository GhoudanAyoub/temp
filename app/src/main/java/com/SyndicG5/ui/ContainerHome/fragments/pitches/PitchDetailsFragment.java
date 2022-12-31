package com.SyndicG5.ui.ContainerHome.fragments.pitches;

import static com.SyndicG5.ui.ContainerHome.HomeContainer.setActivityName;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SyndicG5.Adapters.MonumentAdapter;
import com.SyndicG5.R;
import com.SyndicG5.databinding.FragmentPitchDetailsBinding;
import com.SyndicG5.ui.ContainerHome.fragments.map.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.syndicg5.networking.models.Monument;
import com.syndicg5.networking.repository.apiRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;

@RequiresApi(api = Build.VERSION_CODES.N)
@AndroidEntryPoint
public class PitchDetailsFragment extends Fragment implements MonumentAdapter.PitcherListener {

    private FragmentPitchDetailsBinding binding;
    private MonumentViewModel mViewModel;
    private RecyclerView recyclerView;
    private MonumentAdapter pitchesAdapter;
    private List<Monument> pitchesList;
    private Monument pitche;
    private ProgressDialog progressDialog;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Inject
    apiRepository repository;
    private Date selectedDeliveryDate;

    public PitchDetailsFragment(Monument monument) {
        pitche = monument;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(MonumentViewModel.class);
        binding = FragmentPitchDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setActivityName("Monuments Details ");
        recyclerView = view.findViewById(R.id.similar_pitch_recycler);
        pitchesAdapter = new MonumentAdapter(getContext(), repository, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.addItemDecoration(new GridAutoFitItemDecoration(1, getResources().getDimensionPixelSize(R.dimen.alternative_horizontal_margin_page)));
        recyclerView.setAdapter(pitchesAdapter);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Login ..........");

        binding.pitchName.setText(pitche.getNom());
        binding.pitchCap.setText(pitche.getAdresse());
        binding.pitchDesc.setText(pitche.getDescription());
        List<String> urls = new ArrayList<>();
        pitche.getImages().stream().forEach(image -> {
            urls.add(image.getUrl());
        });
        ProductImagesAdapter adapter =new ProductImagesAdapter(urls);
        binding.articleImagesPager.setAdapter(adapter);
        binding.productImagesIndicator.attachTo(binding.articleImagesPager);
        binding.findLocation.setOnClickListener(v -> {
            replace( MapFragment.newInstance(pitche),"PitchDetailsFragment");
        });
        subscribe(pitche);
    }


    private void replace(Fragment fragment, String s) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.addToBackStack(s);
        transaction.commit();
    }
    private void subscribe(Monument p) {
        mViewModel.getMonuments();
        mViewModel.getListMonumentMutableLiveData().observe(getViewLifecycleOwner(), monuments -> {
            pitchesList = monuments;
            if (p != null) {
                List<Monument> monumentList = monuments.stream()
                        .filter(monument -> monument.getId() != p.getId()
                                        && CheckDistanceBetweenTwoLocations(
                                        new LatLng(monument.getLatitude()
                                                , monument.getLongitude()),
                                        new LatLng(p.getLatitude(),
                                                p.getLongitude()))
                        )
                        .collect(Collectors.toList());
                if (!monumentList.isEmpty()) {
                    binding.blocSimilars.setVisibility(View.VISIBLE);
                    pitchesAdapter.setList(
                            (ArrayList<Monument>) monumentList);
                }
                else
                    binding.blocSimilars.setVisibility(View.GONE);
            }
        });
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

    @Override
    public void onPitcherClicked(Monument monument) {
        replace(new PitchDetailsFragment(monument));
    }

    private void replace(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.addToBackStack(MonumentFragment.class.getName());
        transaction.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
        progressDialog.dismiss();
    }
}
