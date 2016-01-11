package ntust.speedcameradetection;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    final static String PREFS_KEY = "PREFS_KEY";
    private Activity activity;
    public float distanceInKMeter;
    public float routeStartLatitude;
    public float routeStartLongitude;
    public float routeEndLatitude;
    public float routeEndLongitude;

    public Settings(Activity activity) {
        this.activity = activity;
        load();
    }

    public void load() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
        distanceInKMeter = prefs.getFloat("distanceInKMeter", 0);
        routeStartLatitude = prefs.getFloat("routeStartLatitude", 0);
        routeStartLongitude = prefs.getFloat("routeStartLongitude", 0);
        routeEndLatitude = prefs.getFloat("routeEndLatitude", 0);
        routeEndLongitude = prefs.getFloat("routeEndLongitude", 0);
    }

    public void save() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("distanceInKMeter", distanceInKMeter);
        editor.putFloat("routeStartLatitude", routeStartLatitude);
        editor.putFloat("routeStartLongitude", routeStartLongitude);
        editor.putFloat("routeEndLatitude", routeEndLatitude);
        editor.putFloat("routeEndLongitude", routeEndLongitude);
        editor.apply();
    }
}
