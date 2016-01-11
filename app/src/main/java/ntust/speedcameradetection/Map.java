package ntust.speedcameradetection;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

class Map implements LocationListener {
    private final static String TAG = "MapActivity";
    public static final long MIN_TIME_MS = 1000;
    public static final float MIN_DISTANCE_METER = 50;
    public static final float MIN_SPEED_DISTANCE_METER = 50;
    private MapActivity activity;
    private GoogleMap map;
    private Geocoder geocoder;
    private Location location;
    private float currentZoom = 16;
    private boolean isLocateFinished = false;
    private Position previousPosition, currentPosition;

    public Map(MapActivity activity) {
        this.activity = activity;
        this.previousPosition = new Position(0, 0, 0);
        this.currentPosition = new Position(0, 0, 0);
        initMap();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        moveToLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    public void initMap() {
        if (map == null) {
            map = ((SupportMapFragment) activity.getSupportFragmentManager()
                    .findFragmentById(R.id.fmMap)).getMap();
            if (map != null) {
                setUpMap();
            }
        }

        if (geocoder == null) {
            geocoder = new Geocoder(activity);
        }
    }

    private void setUpMap() {
        map.setMyLocationEnabled(true);
        map.setTrafficEnabled(false);
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                if (!isLocateFinished) {
                    isLocateFinished = true;
                } else if (position.zoom != currentZoom) {
                    currentZoom = position.zoom;
                    Log.d(TAG, "Change zoom level: " + currentZoom);
                }
            }
        });
    }

    public GoogleMap getMap() {
        return map;
    }

    public Location getLocation() {
        return location;
    }

    public LatLng getLatLng() {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public boolean getIsLocateFinished() {
        return isLocateFinished;
    }

    public boolean isMapReady() {
        if (map == null) {
            Toast.makeText(activity,
                    R.string.msg_MapNotReady, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public double getOneMeterDistance() {
        if (location == null) {
            location = map.getMyLocation();
        }

        float bearing = location.getBearing();
        double latitude = UiHandler.MAX_NEIBORHOOD_METER * Math.sin(bearing) /
                Math.cos(location.getLatitude()) / 111111;
        double longitude = UiHandler.MAX_NEIBORHOOD_METER * Math.cos(bearing) / 111111;
        return latitude * latitude + longitude * longitude;
    }

    public Address getAddress() {
        Location location = map.getMyLocation();
        return getAddress(location.getLatitude(), location.getLongitude());
    }

    public Address getAddress(double latitude, double longitude) {
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0);
        } else {
            return null;
        }
    }

    public String getAddressString() {
        Location location = map.getMyLocation();
        return getAddressString(location.getLatitude(), location.getLongitude());
    }

    public String getAddressString(double latitude, double longitude) {
        Address address = getAddress(latitude, longitude);

        if (address != null) {
            Log.d(TAG, "addr: " + address.toString());
            return address.toString();
        } else {
            return "{無法取得地址}";
        }
    }

    public void moveToLocation(double latitude, double longitude) {
        LatLng latlng = new LatLng(latitude, longitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, currentZoom));
    }

    public void moveToLocation(Location location) {
        CameraPosition place = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .bearing(location.getBearing()).tilt(65.5f).zoom(currentZoom).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(place));
    }

    public void moveToCurrentLocation() {
        moveToLocation(map.getMyLocation());
    }

    public void markLocation(double latitude, double longitude) {
        markLocation(latitude, longitude, getAddressString(latitude, longitude));
    }

    public void markLocation(double latitude, double longitude, String address) {
        cameraAlert(address);
        map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(address).snippet(address));
    }

    public void cameraAlert(String address) {
        activity.makeText(address);
    }

    public Speed getSpeed() {
        if (!isMapReady()) {
            return new Speed(0, 0);
        }

        Location currentLocation = map.getMyLocation();
        previousPosition = currentPosition;
        currentPosition = new Position(currentLocation, System.currentTimeMillis());
        float distance;

        if (previousPosition.getTime() == 0) {
            distance = 0.0f;
        } else {
            distance = previousPosition.distanceBetween(currentPosition);
        }

        if (currentLocation.hasSpeed()) {
            return new Speed(currentLocation.getSpeed() * 3600 / 1000, distance / 1000);
        } else {
            if (distance >= MIN_SPEED_DISTANCE_METER) {
                float time = (currentPosition.getTime() - previousPosition.getTime()) / 1000;
                float speed;

                if (time > 0) {
                    speed = distance * 3660 / 1000 / time;
                } else {
                    speed = 0;
                }

                if (speed > 999) {
                    Log.e(TAG, "speed value is too large");
                    speed = 0;
                }

                return new Speed(speed, distance / 1000);
            }
        }

        return new Speed(0, 0);
    }

    class Position {
        public double latitude;
        public double longitude;
        long time;

        public Position(double latitude, double longitude, long time) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.time = time;
        }

        public Position(Location location, long time) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
            this.time = time;
        }

        public long getTime() {
            return time;
        }

        public float distanceBetween(Position pos) {
            float[] result = new float[1];
            Location.distanceBetween(latitude, longitude, pos.latitude, pos.longitude, result);
            return result[0];
        }
    }

    public static class Speed {
        float kmPerHour;
        float distanceInKMeter;

        public Speed(float kmPerHour, float distanceInKMeter) {
            this.kmPerHour = kmPerHour;
            this.distanceInKMeter = distanceInKMeter;
        }

        public float getKmPerHour() {
            return kmPerHour;
        }

        public float getDistanceInKMeter() {
            return distanceInKMeter;
        }
    }
}
