package ntust.speedcameradetection;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Net {
    private final static String TAG = "MapActivity";
    private final static String TRAFFIC_STICK_URL =
            "http://crimevideo.npa.gov.tw/TrafficStick/TrafficStick.asp?DB_Selt=Selt";

    public static void getTrafficStick(Database db) {
        InputStream is;
        try {
            URL url = new URL(TRAFFIC_STICK_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            is = conn.getInputStream();
            String contentAsString = convertInputStreamToString(is);
            Log.d(TAG, "" + contentAsString.length());
            parseTrafficStick(db, contentAsString);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public static String convertInputStreamToString(InputStream stream) throws IOException {
        Reader reader;
        reader = new InputStreamReader(stream, "UTF-8");
        StringBuilder response = new StringBuilder();
        char[] buffer = new char[2048];

        while (true) {
            int size = reader.read(buffer);
            if (size != -1) {
                response.append(buffer, 0, size);
            } else {
                break;
            }
        }

        return new String(response);
    }

    private static void parseTrafficStick(Database db, String html) {
        Pattern p = Pattern.compile("SetMarkers1\\((\\d+?),\\'(.*?)\\',(.*?),(.*?)\\);");
        int first = html.indexOf("SetMarkers1");
        Matcher m = p.matcher(html.substring(first));
        while (m.find()) {
            String id = m.group(1);
            String address = m.group(2);
            String latitude = m.group(3);
            String longitude = m.group(4);
            db.insert(id, address, latitude, longitude, 0);
        }
        Log.d(TAG, "database has " + db.getCount() + " data");
    }
}
