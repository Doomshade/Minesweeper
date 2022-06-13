package cz.zcu.kiv.jsmahy.minesweeper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(PreferenceManager.getDefaultSharedPreferences(this).getInt("themeId", R.style.Theme_AppCompat_Light_NoActionBar_FullScreen_Fulbo));

        setContentView(R.layout.activity_main);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }
        // hideSystemBars();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public void startGameActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        startActivity(intent);
    }

    public void startScoreboardActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), ScoreboardActivity.class);
        startActivity(intent);
    }

    public void startSettingsActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    public void exit(View view) {
        finish();
    }

}