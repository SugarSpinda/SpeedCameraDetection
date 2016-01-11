package ntust.speedcameradetection;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class UiHandler extends Handler {
    private final static String TAG = "MapActivity";
    public static final double MAX_NEIBORHOOD_METER = 1000;
    public static final double MAX_NEIBORHOOD_COUNTER = 20;
    public static final int ADD_TRAFFICSTICK = 1;
    public static final int MARK_EVENT = 2;
    private static final int UPDATE_UI_PERIOD = 1000;
    private final WeakReference<MapActivity> activity;
    private final UpdateRunnable updateUi;
    private Map map;
    private RouteTask routeTask;
    private Settings settings;
    private Database db;
    private double maxNeiborhoodDistance;
    private boolean isMapReady = false;
    private ArrayList<Database.TrafficStick> allTrafficStick;
    private ArrayList<Database.TrafficStick> markedTrafficStick;
    private TextView tvDistanceInKMeter;
    private TextView tvNearCamera;

    public UiHandler(MapActivity activity, Map map) {
        this.activity = new WeakReference<>(activity);
        this.updateUi = new UpdateRunnable(activity);
        this.map = map;
        this.routeTask = new RouteTask(map.getMap());
        this.settings = new Settings(activity);
        this.db = new Database(activity);

        TrafficStickThread trafficStickThread = new TrafficStickThread();
        trafficStickThread.start();

        allTrafficStick = new ArrayList<>();
        markedTrafficStick = new ArrayList<>();

        tvDistanceInKMeter = (TextView)activity.findViewById(R.id.tvDistanceInKMeter);
        tvNearCamera = (TextView)activity.findViewById(R.id.tvNearCamera);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case ADD_TRAFFICSTICK: {
                Bundle b = msg.getData();
                Database.TrafficStick t = new Database.TrafficStick(
                        b.getInt("id"), b.getDouble("latitude"),
                        b.getDouble("longitude"), b.getString("address"));
                allTrafficStick.add(t);
                break;
            }
            case MARK_EVENT: {
                Bundle b = msg.getData();
                map.markLocation(b.getDouble("latitude"),
                        b.getDouble("longitude"), b.getString("address"));
                break;
            }
            default:
                Log.d(TAG, "unknown event");
                break;
        }
    }

    public void onPause() {
        removeCallbacks(updateUi);
    }

    public void onResume() {
        postDelayed(updateUi, UPDATE_UI_PERIOD);
    }

    class TrafficStickThread extends Thread {
        private void waitMapReady() {
            while (!isMapReady) {
                Log.d(TAG, "Wait map ready...");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Sleep interrupt.");
                }
            }
        }

        @Override
        public void run() {
            if (db.getCount() == 0) {
                Log.d(TAG, "Database is empty, download from network.");
                Net.getTrafficStick(db);
            } else {
                Log.d(TAG, "Database is ready.");
            }

            waitMapReady();
        }
    }

    class UpdateRunnable implements Runnable {
        MapActivity activity;
        Comparator<Database.TrafficStick> neighborhoodTrafficStickComparator;
        int counter;

        public UpdateRunnable(MapActivity activity) {
            this.activity = activity;
            counter = 0;

            neighborhoodTrafficStickComparator = new Comparator<Database.TrafficStick>() {
                @Override
                public int compare(Database.TrafficStick lhs, Database.TrafficStick rhs) {
                    Location location = map.getLocation();
                    double d1 = lhs.distanceBetween(location.getLatitude(), location.getLongitude());
                    double d2 = rhs.distanceBetween(location.getLatitude(), location.getLongitude());
                    return (int)((d1 - d2) * 1e8);
                }
            };
        }

        private void sendMarkEvent(double latitude, double longitude, String address) {
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", latitude);
            bundle.putDouble("longitude", longitude);
            bundle.putString("address", address);
            Message msg = new Message();
            msg.what = MARK_EVENT;
            msg.setData(bundle);
            sendMessage(msg);
        }

        boolean isMarkedTrafficStick(int id) {
            for (Database.TrafficStick t : markedTrafficStick) {
                if (t.getId() == id) {
                    return true;
                }
            }
            return false;
        }

        private boolean isNeiborhoodTrafficStick(Database.TrafficStick t) {
            Location location = map.getLocation();
            double d = t.distanceBetween(location.getLatitude(), location.getLongitude());
            return d < maxNeiborhoodDistance;
        }

        private void updateNearestCamera() {
            float[] result = new float[1];
            Database.TrafficStick t = allTrafficStick.get(0);
            Location current = map.getLocation();
            Location.distanceBetween(t.latitude, t.longitude,
                    current.getLatitude(), current.getLongitude(), result);
            if (result[0] > MAX_NEIBORHOOD_METER) {
                tvNearCamera.setText(R.string.text_no_camera);
            } else {
                tvNearCamera.setText("距離相機" + (int)result[0] + "m");
            }
        }

        private void updateNeiborhoodTrafficStick() {
            Collections.sort(allTrafficStick, neighborhoodTrafficStickComparator);

            int count = 0;
            for (Database.TrafficStick t : allTrafficStick) {
                if (!isMarkedTrafficStick(t.getId()) && isNeiborhoodTrafficStick(t)) {
                    markedTrafficStick.add(t);
                    sendMarkEvent(t.latitude, t.longitude, t.address);
                    if (count++ > MAX_NEIBORHOOD_COUNTER) {
                        Log.e(TAG, "distance too near");
                        break;
                    }
                }
            }
        }

        private void drawRoute() {
            if (settings.routeStartLatitude != 0 &&
                settings.routeStartLongitude != 0 &&
                settings.routeEndLatitude != 0 &&
                settings.routeEndLongitude != 0) {
                LatLng start = new LatLng(settings.routeStartLatitude, settings.routeStartLongitude);
                LatLng end = new LatLng(settings.routeEndLatitude, settings.routeEndLongitude);
                routeTask.execute(start, end);
            }
        }

        @Override
        public void run() {
            if (isMapReady) {
                Map.Speed speed = map.getSpeed();
                settings.distanceInKMeter += speed.getDistanceInKMeter();
                activity.showSpeed((int) speed.kmPerHour, 110);
                tvDistanceInKMeter.setText("" + (int)settings.distanceInKMeter + " km");
                updateNeiborhoodTrafficStick();
                updateNearestCamera();
                Log.d(TAG, speed.getKmPerHour() + " km/h, " +
                        settings.distanceInKMeter + " km");
            } else if (map.getIsLocateFinished()) {
                counter = counter + 1;
                if (counter > 10000 / UPDATE_UI_PERIOD) {
                    isMapReady = true;
                    maxNeiborhoodDistance = map.getOneMeterDistance();
                    drawRoute();
                    Log.d(TAG, "Get all trafficSticks.");
                    db.getAllTrafficStick();
                    Log.d(TAG, "TrafficStick done.");
                }
            }

            postDelayed(updateUi, UPDATE_UI_PERIOD);
        }
    }
}
