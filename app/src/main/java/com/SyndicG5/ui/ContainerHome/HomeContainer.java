package com.SyndicG5.ui.ContainerHome;

import static com.SyndicG5.ui.ContainerHome.fragments.map.MapFragment.bookedPitchPage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.SyndicG5.R;
import com.SyndicG5.SyndicActivity;
import com.SyndicG5.databinding.ActivityHomeContainerBinding;
import com.SyndicG5.ui.ContainerHome.fragments.map.MapFragment;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.N)
public class HomeContainer extends SyndicActivity  {
    private static int PERMISSION_REQUEST_CODE = 1000;

    private ActivityHomeContainerBinding binding;
    private static Toolbar toolbar;
    private static boolean isGPSEnabled = false;
    private static boolean isPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeContainerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        isGpsEnabled();
        isPermissionGranted = isPermissionGiven();
        grantLocationPermission();
        replace(MapFragment.newInstance(null));
        toolbar = findViewById(R.id.toolbar);
    }

    private void grantLocationPermission() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if (!isGPSEnabled) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                        isGPSEnabled = true;
                        isPermissionGranted = true;
                        Timber.d("permission granted");
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(
                                HomeContainer.this,
                                "Please enable location permission",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

    private boolean isPermissionGiven() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isGpsEnabled() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Timber.d("GPS already enabled");
        } catch (Exception ex) {
            Timber.e(ex);
        }
        return isGPSEnabled;
    }

    public static void setActivityName(String name) {
        toolbar.setTitle(name);
    }

    private void replace(Fragment fragment, String s) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.addToBackStack(s);
        transaction.commit();
    }

    private void replace(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        List<String> requiredPermissions = new ArrayList<>(Arrays.asList(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requiredPermissions.add(Manifest.permission.ACTIVITY_RECOGNITION);
            requiredPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        } else {
            requiredPermissions.add(Manifest.permission.READ_PHONE_STATE);
        }

        List<String> missingPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            String[] permissionsArray = missingPermissions.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, permissionsArray, PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onBackPressed() {
        if(bookedPitchPage==true){
            bookedPitchPage=false;
            replace(new MapFragment());
        }else{
            if(getSupportFragmentManager().getBackStackEntryCount()>0){
                getSupportFragmentManager().popBackStack();
            }else{
                finishAffinity();
            }
        }
    }
}
