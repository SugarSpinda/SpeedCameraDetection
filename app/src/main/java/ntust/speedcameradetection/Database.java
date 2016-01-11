package ntust.speedcameradetection;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Message;

public class Database {
    private static final String TAG = "MapActivity";
    private static final String DATABASE_NAME = "MapActivity.db";
    private static final String TABLE_NAME = "location";
    private static final String DELETE_TABLE = "drop table " + TABLE_NAME;
    private static final String CREATE_TABLE =
            "create table if not exists " + TABLE_NAME + "(" +
                    "no integer primary key, " +
                    "id integer, " +
                    "address text, " +
                    "latitude double, " +
                    "longitude double, " +
                    "speed integer);";
    private static final int ADDRESS_COL = 2;
    private static final int LATITUDE_COL = 3;
    private static final int LONGITUDE_COL = 4;
    MapActivity activity;
    SQLiteDatabase db;

    public Database(MapActivity activity) {
        this.activity = activity;
        db = activity.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        db.execSQL(CREATE_TABLE);
    }

    public boolean insert(String id, String address, String latitude, String longitude, int speed) {
        Cursor cursor = null;
        boolean isValid = false;

        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME +
                    " where id=" + id, null);
            if (cursor != null && cursor.getCount() == 0) {
                String sql = "insert into " + TABLE_NAME +
                        " (id, address, latitude, longitude, speed) values(" +
                        id + ", \'" + address + "\', " +
                        latitude + ", " + longitude + ", " + speed + ")";
                db.execSQL(sql);
                isValid = true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return isValid;
    }

    public int getCount() {
        Cursor cursor = null;
        int count = 0;

        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME, null);
            if (cursor != null) {
                count = cursor.getCount();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    public void getAllTrafficStick() {
        Cursor cursor = null;
        UiHandler handler = activity.getUpdateUiHandler();
        int id = 1;

        try {
            cursor = db.rawQuery("select * from " + TABLE_NAME, null);
            if (cursor != null && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    sendAddTrafficStickEvent(handler, id,
                            cursor.getDouble(LATITUDE_COL),
                            cursor.getDouble(LONGITUDE_COL),
                            cursor.getString(ADDRESS_COL));
                    cursor.moveToNext();
                    id += 1;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void sendAddTrafficStickEvent(UiHandler handler,
            int id, double latitude, double longitude, String address) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putDouble("latitude", latitude);
        bundle.putDouble("longitude", longitude);
        bundle.putString("address", address);
        Message msg = new Message();
        msg.what = UiHandler.ADD_TRAFFICSTICK;

        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public static class TrafficStick {
        public double latitude, longitude;
        public String address;
        private int id;

        public TrafficStick(int id, double latitude, double longitude, String address) {
            this.id = id;
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
        }

        public int getId() {
            return id;
        }

        public double distanceBetween(TrafficStick t) {
            return distanceBetween(t.latitude, t.longitude);
        }

        public double distanceBetween(double latitude, double longitude) {
            double x = this.latitude - latitude;
            double y = this.longitude - longitude;
            return x * x + y * y;
        }
    }
}
