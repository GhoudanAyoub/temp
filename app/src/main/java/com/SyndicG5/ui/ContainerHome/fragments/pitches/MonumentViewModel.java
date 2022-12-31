package com.SyndicG5.ui.ContainerHome.fragments.pitches;

import android.annotation.SuppressLint;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.syndicg5.networking.models.Monument;
import com.syndicg5.networking.repository.apiRepository;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MonumentViewModel extends ViewModel {

    private apiRepository repository;
    private MutableLiveData<List<Monument>> listMonumentMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> reservationMutableLiveData = new MutableLiveData<>();


    @ViewModelInject
    public MonumentViewModel(apiRepository repository) {
        this.repository = repository;
    }

    @SuppressLint("CheckResult")
    public void getMonuments() {
        repository.getMonuments()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> listMonumentMutableLiveData.setValue(response), Throwable::printStackTrace);
    }

    public MutableLiveData<List<Monument>> getListMonumentMutableLiveData() {
        return listMonumentMutableLiveData;
    }

}
