package com.syndicg5.networking.api;

import com.syndicg5.networking.models.Monument;

import java.util.List;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APISettings {


    @POST("api/user/mlogin")
    Single<Response<ResponseBody>> Login(@Query("email") String username, @Query("password") String password);


    @GET("Projet1WebWS/monuments")
    Single<List<Monument>> getMonuments();

}
