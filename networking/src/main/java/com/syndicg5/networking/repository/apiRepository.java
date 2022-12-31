package com.syndicg5.networking.repository;


import com.syndicg5.networking.api.APISettings;
import com.syndicg5.networking.models.Monument;

import java.util.List;

import javax.inject.Inject;

import dagger.Reusable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;

@Reusable
public class apiRepository {
    private final APISettings apiSettings;

    @Inject
    public apiRepository(APISettings apiSettings) {
        this.apiSettings = apiSettings;
    }

    public Single<Response<ResponseBody>> Login(String email, String password) {
        return apiSettings.Login(email, password);
    }

    public Single<List<Monument>> getMonuments() {
        return apiSettings.getMonuments();
    }
}
