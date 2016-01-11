package ntust.speedcameradetection;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity {
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);

        ImageView ivWelcome = (ImageView)findViewById(R.id.ivWelcome);
        ivWelcome.setOnClickListener(new WelcomeOnClickListener(this));
        permissionManager = new PermissionManager(this);
        permissionManager.checkMapPermissions();
    }

    class WelcomeOnClickListener implements View.OnClickListener {
        WelcomeActivity welcomeActivity;
        public WelcomeOnClickListener(WelcomeActivity welcomeActivity) {
            this.welcomeActivity = welcomeActivity;
        }

        @Override
        public void onClick(View v) {
            startActivity(new Intent(welcomeActivity, OptionActivity.class));
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
