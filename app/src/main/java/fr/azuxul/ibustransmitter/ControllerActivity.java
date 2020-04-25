package fr.azuxul.ibustransmitter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class ControllerActivity extends AppCompatActivity {

    private final static int LOOP_DELAY = 15; // in ms

    private final Handler handler;
    private final Runnable dataSend;
    private final JoystickView.OnMoveListener stickListenerLeft;
    private final JoystickView.OnMoveListener stickListenerRight;
    private final CommandChannels commandChannels;

    private Client client;
    private JoystickView joystickLeft;
    private JoystickView joystickRight;
    private Button armButton;
    private TextView rssiText;
    private WifiManager wifiManager;
    private ConnectTask connectTask;
    private long lastLeftMove;
    private long lastRightMove;
    private boolean buttonArmPress;
    private boolean rssiTransmit;
    private boolean inBackground;
    private boolean infoDisplayed;

    public ControllerActivity() {
        this.handler = new Handler();
        this.commandChannels = new CommandChannels();

        this.dataSend = new Runnable() {

            @Override
            public void run() {

                int rssi = 0;

                if (wifiManager != null) {
                    rssi = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 100);
                    rssiText.setText(getString(R.string.rssi, rssi));
                }

                if (client != null && !client.isClose()) {
                    byte data[] = new byte[24];
                    int rawData[] = new int[12];

                    for (int i = 0; i < 12; i++) {
                        rawData[i] = 1500;
                    }

                    if (lastLeftMove > System.currentTimeMillis() - LOOP_DELAY * 2) {
                        rawData[commandChannels.getLeftStickX()] = 1000 + joystickLeft.getNormalizedX() * 10;
                        rawData[commandChannels.getLeftStickY()] = 1000 + (100 - joystickLeft.getNormalizedY()) * 10;
                    }

                    if (lastRightMove > System.currentTimeMillis() - LOOP_DELAY * 2) {
                        rawData[commandChannels.getRightStickX()] = 1000 + joystickRight.getNormalizedX() * 10;
                        rawData[commandChannels.getRightStickY()] = 1000 + (100 - joystickRight.getNormalizedY()) * 10;
                    }

                    rawData[4] = 1000 + (buttonArmPress ? 1000 : 0);

                    if (wifiManager != null && rssiTransmit) {
                        rawData[11] = 1000 + rssi * 10;
                    }

                    for (int i = 0; i < 12; i++) {
                        data[i * 2 + 1] = (byte) rawData[i];
                        data[i * 2] = (byte) (rawData[i] >> 8);
                    }

                    if(!inBackground) {
                        client.sendData(data);
                    }
                } else if (connectTask != null) {
                    client = connectTask.getClient();
                }
            }

        };


        this.stickListenerLeft = new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                handler.post(dataSend);
                lastLeftMove = System.currentTimeMillis();
            }
        };

        this.stickListenerRight = new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                handler.post(dataSend);
                lastRightMove = System.currentTimeMillis();
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_controller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if(id == R.id.action_licenses) {
            startActivity(new Intent(this, OssLicensesMenuActivity.class));
            return true;
        } else if(id == R.id.action_help) {
            startActivity(new Intent(this, HelpActivity.class));
            return true;
        } else if(id == R.id.action_connect) {

            Toast.makeText(this, getText(R.string.reconnect_toast), Toast.LENGTH_SHORT).show();
            if(client != null) {
                client.stop();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(client != null) {
            client.stop();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        inBackground = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        inBackground = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        inBackground = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inBackground = false;

        setContentView(R.layout.activity_controller);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(!infoDisplayed && preferences.getBoolean("info_show", true)) {
            startActivity(new Intent(this, InfoActivity.class));
        }

        infoDisplayed = true;

        Log.d("Stick mode", preferences.getString("mode", "1"));
        commandChannels.setMode(Integer.valueOf(preferences.getString("mode", "1")));
        this.rssiTransmit = preferences.getBoolean("rssi", true);

        connectTask = new ConnectTask();
        connectTask.execute(new ConnectTask.ConnectTaskArguments("192.168.1.1", 1081));

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        joystickLeft = findViewById(R.id.joystickView_left);
        joystickRight = findViewById(R.id.joystickView_right);
        armButton = findViewById(R.id.button_arm);
        rssiText = findViewById(R.id.rssi_text);

        armButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonArmPress = !buttonArmPress;

                armButton.setText(buttonArmPress ? R.string.armed : R.string.arm);
            }
        });

        joystickLeft.setOnMoveListener(stickListenerLeft, LOOP_DELAY);
        joystickRight.setOnMoveListener(stickListenerRight, LOOP_DELAY);

        handler.post(new Runnable() {
            @Override
            public void run() {
                handler.post(dataSend);
                handler.postDelayed(this, 50);
            }
        });

        if (Build.VERSION.SDK_INT >= 18) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

    }
}
