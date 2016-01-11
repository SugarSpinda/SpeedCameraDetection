package ntust.speedcameradetection;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.widget.Toast;

public class PermissionManager {
    Activity activity;

    public PermissionManager(Activity activity) {
        this.activity = activity;
    }

    private final static int REQUEST_INTERNET = 100;
    private final static int REQUEST_ACCESS_NETWORK_STATE = 101;
    private final static int REQUEST_ACCESS_COARSE_LOCATION = 102;
    private final static int REQUEST_ACCESS_FINE_LOCATION = 103;
    private final static int REQUEST_READ_EXTERNAL_STORAGE = 104;
    private final static int REQUEST_WRITE_EXTERNAL_STORAGE = 105;

    private void checkMapPermission(String permission, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{permission}, requestCode);
            }
        }
    }

    public void checkMapPermissions() {
        checkMapPermission(Manifest.permission.INTERNET, REQUEST_INTERNET);
        checkMapPermission(Manifest.permission.ACCESS_NETWORK_STATE, REQUEST_ACCESS_NETWORK_STATE);
        checkMapPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_ACCESS_COARSE_LOCATION);
        checkMapPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_ACCESS_FINE_LOCATION);
        checkMapPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_EXTERNAL_STORAGE);
        checkMapPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    public LocationManager requestLocationUpdates(long minTime, float minDistance, LocationListener listener) {
        LocationManager locationManager = (LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER, minTime, minDistance, listener);
//                    You can also use LocationManager.NETWORK_PROVIDER and
//                    LocationManager.GPS_PROVIDER
//                    LocationManager.PASSIVE_PROVIDER
        } else {
            Toast.makeText(activity, R.string.msg_GpsNotTurnOn, Toast.LENGTH_SHORT).show();
        }

        return locationManager;
    }

    public boolean checkPermissionsResult(int requestCode) {
        return requestCode == REQUEST_INTERNET ||
                requestCode == REQUEST_ACCESS_NETWORK_STATE ||
                requestCode == REQUEST_ACCESS_COARSE_LOCATION ||
                requestCode == REQUEST_ACCESS_FINE_LOCATION ||
                requestCode == REQUEST_READ_EXTERNAL_STORAGE ||
                requestCode == REQUEST_WRITE_EXTERNAL_STORAGE;
    }
}
