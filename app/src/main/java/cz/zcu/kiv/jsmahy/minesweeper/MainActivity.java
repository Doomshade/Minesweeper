package cz.zcu.kiv.jsmahy.minesweeper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class MainActivity extends AppCompatActivity {

    private ImageButton playBtn = null;
    private ImageButton scoreBoardBtn = null;
    private ImageButton exitBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }
        hideSystemBars();
        playBtn = findViewById(R.id.play);
        playBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
            startActivity(intent);
        });
        scoreBoardBtn = findViewById(R.id.scoreboard);
        scoreBoardBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ScoreboardActivity.class);
            startActivity(intent);
        });
        exitBtn = findViewById(R.id.exit);
        exitBtn.setOnClickListener(view -> finish());
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

}