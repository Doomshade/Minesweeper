package cz.zcu.kiv.jsmahy.minesweeper;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.menu_item_settings, new SettingsFragment())
                    .commit();
        }
        /*SharedPreferences.Editor edit = getSharedPreferences(GameImpl.GAME_VALUES, 0).edit();
        edit.putInt(GameImpl.S_DIFFICULTY, Game.Difficulty.EASY.ordinal());
        edit.putInt(GameImpl.S_WIDTH, 8);
        edit.apply();*/

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}