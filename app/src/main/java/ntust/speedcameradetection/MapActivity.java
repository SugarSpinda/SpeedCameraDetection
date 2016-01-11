package ntust.speedcameradetection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MapActivity extends AppCompatActivity {
    private final static String TAG = "MapActivity";
    private PermissionManager permissionManager;
    private Map map;
    private UiHandler updateUiHandler;
    private ImageView ivHund,ivTen,ivDig;
    private static TextView tvDistanceInKMeter;
    private int numImage[];
    private static TextView tvTime;

    private void removeTitleBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        removeTitleBar();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        findViews();
        showCurrentTime();
        setupNumberImage();

        permissionManager = new PermissionManager(this);
        permissionManager.checkMapPermissions();
        map = new Map(this);
        updateUiHandler = new UiHandler(this, map);

        permissionManager.requestLocationUpdates(Map.MIN_TIME_MS, Map.MIN_DISTANCE_METER, map);
    }

    private void setupNumberImage() {
        numImage = new int[20];
        numImage[0] = R.drawable.number0;
        numImage[1] = R.drawable.number1;
        numImage[2] = R.drawable.number2;
        numImage[3] = R.drawable.number3;
        numImage[4] = R.drawable.number4;
        numImage[5] = R.drawable.number5;
        numImage[6] = R.drawable.number6;
        numImage[7] = R.drawable.number7;
        numImage[8] = R.drawable.number8;
        numImage[9] = R.drawable.number9;
        numImage[10] = R.drawable.numberr0;
        numImage[11] = R.drawable.numberr1;
        numImage[12] = R.drawable.numberr2;
        numImage[13] = R.drawable.numberr3;
        numImage[14] = R.drawable.numberr4;
        numImage[15] = R.drawable.numberr5;
        numImage[16] = R.drawable.numberr6;
        numImage[17] = R.drawable.numberr7;
        numImage[18] = R.drawable.numberr8;
        numImage[19] = R.drawable.numberr9;
    }

    public void makeText(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void findViews() {
        ivHund = (ImageView) findViewById(R.id.ivHund);
        ivTen = (ImageView) findViewById(R.id.ivTen);
        ivDig = (ImageView) findViewById(R.id.ivDig);
        tvDistanceInKMeter = (TextView) findViewById(R.id.tvDistanceInKMeter);
        tvTime = (TextView) findViewById(R.id.tvTime);
    }

    private static void showCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        tvTime.setText(new StringBuilder().append(padLeadingZero(hour)).append(":")
                .append(padLeadingZero(minute)));
    }

    private static String padLeadingZero(int number) {
        if (number >= 10)
            return String.valueOf(number);
        else
            return "0" + String.valueOf(number);
    }

    public void showSpeed(int speed, int limit) {
        if (speed < limit) {
            setDigital(ivHund, speed / 100, 0);
            setDigital(ivTen, speed / 10 % 10, 0);
            setDigital(ivDig, speed % 10, 0);
        } else {
            setDigital(ivHund, speed / 100, 10);
            setDigital(ivTen, speed / 10 % 10, 10);
            setDigital(ivDig, speed % 10, 10);
        }
    }

    public void setDigital(ImageView imageView, int value, int option){
        if (value >= 0 && value <= 9) {
            imageView.setImageResource(numImage[value + option]);
        }
    }

    public UiHandler getUpdateUiHandler() {
        return updateUiHandler;
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.initMap();
        if (updateUiHandler != null) {
            updateUiHandler.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (updateUiHandler != null) {
            updateUiHandler.onPause();
        }
    }

    public void onMileageCkick(View view) {
        AlertDialogFragment alertFragment = new AlertDialogFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        alertFragment.show(fragmentManager, "alert");
    }

    public static class AlertDialogFragment
            extends DialogFragment implements DialogInterface.OnClickListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title1)
                    .setIcon(R.drawable.alert)
                    .setMessage(R.string.msg_Alert1)
                    .setPositiveButton(R.string.text_btYes1, this)
                    .setNegativeButton(R.string.text_btNo1, this)
                    .create();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    tvDistanceInKMeter.setText("0km");
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.cancel();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (permissionManager.checkPermissionsResult(requestCode)) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Enable: " + permissions, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Disable: " + permissions, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
