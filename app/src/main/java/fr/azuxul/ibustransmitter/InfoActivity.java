package fr.azuxul.ibustransmitter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        if (Build.VERSION.SDK_INT >= 18) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final View agreeButton = findViewById(R.id.button_agree);
        final CheckBox showCheckBox = findViewById(R.id.check_show);

        agreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(showCheckBox.isChecked()) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("info_show", false);
                    editor.apply();
                }
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}
