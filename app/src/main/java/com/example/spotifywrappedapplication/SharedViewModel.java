package com.example.spotifywrappedapplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    /*
    Associated with the GameNavActivity.java class,
    which is responsible for displaying one (only one) game
     */
    private final MutableLiveData<FragmentData> complexData = new MutableLiveData<>();

    public void setComplexData(FragmentData data) {
        complexData.setValue(data);
    }

    public LiveData<FragmentData> getComplexData() {
        return complexData;
    }
}

