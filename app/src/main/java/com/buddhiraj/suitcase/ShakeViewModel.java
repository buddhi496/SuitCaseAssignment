package com.buddhiraj.suitcase;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

public class ShakeViewModel extends AndroidViewModel {
    private static final float SHAKE_THRESHOLD = 10f; // Adjust the threshold as needed
    private MutableLiveData<Boolean> shakeDetected = new MutableLiveData<>(false);
    private float lastX, lastY, lastZ;
    private long lastTime;

    public ShakeViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
    }

    public LiveData<Boolean> getShakeStatus() {
        return shakeDetected;
    }

    public void handleSensorData(float x, float y, float z, long time) {
        if (isShake(x, y, z)) {
            shakeDetected.setValue(true);
        }
    }

    public void clearShakeStatus() {
        shakeDetected.setValue(false);
    }

    private boolean isShake(float x, float y, float z) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastTime;
        if (timeDifference >= 100) { // Adjust the time interval as needed
            float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / timeDifference * 10000;
            if (speed > SHAKE_THRESHOLD) {
                lastTime = currentTime;
                return true;
            }
            lastX = x;
            lastY = y;
            lastZ = z;
        }
        return false;
    }
}

