package com.SyndicG5;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import com.SyndicG5.ui.ContainerHome.HomeContainer;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;


@RequiresApi(api = Build.VERSION_CODES.N)
@SuppressLint("CheckResult")
public class SplashScreen extends SyndicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        goHome();
    }


    private void goHome() {
        Completable.timer(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    startActivity(new Intent(getApplication(), HomeContainer.class));
                });
    }
}
