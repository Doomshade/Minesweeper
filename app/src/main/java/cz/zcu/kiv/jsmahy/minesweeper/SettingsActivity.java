package cz.zcu.kiv.jsmahy.minesweeper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(PreferenceManager.getDefaultSharedPreferences(this).getInt("themeId", R.style.Theme_AppCompat_Light_NoActionBar_FullScreen_Fulbo));
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.menu_item_settings, new SettingsFragment())
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference lang = Objects.requireNonNull((Preference) findPreference("lang"));
            Preference font = Objects.requireNonNull((Preference) findPreference("font"));
            Preference diff = Objects.requireNonNull((Preference) findPreference("diff"));
            lang.setOnPreferenceChangeListener(this::onLanguageChange);
            font.setOnPreferenceChangeListener(this::onFontChange);
            diff.setOnPreferenceChangeListener(this::onDifficultyChange);

            FragmentActivity activity = getActivity();
            if (activity != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                String defaultLang = prefs.getString("lang", "en");
                lang.setDefaultValue(defaultLang);

                String defaultFont = prefs.getString("font", "fulbo");
                font.setDefaultValue(defaultFont);
            }
        }

        private boolean onFontChange(Preference preference, Object newValue) {
            Context context = preference.getContext();
            final int themeId;
            switch ((String) newValue) {
                case "dolce":
                    themeId = R.style.Theme_AppCompat_Light_NoActionBar_FullScreen_Dolce;
                    break;
                case "fulbo":
                    themeId = R.style.Theme_AppCompat_Light_NoActionBar_FullScreen_Fulbo;
                    break;
                default:
                    throw new UnsupportedOperationException("Invalid font " + newValue);
            }
            context.setTheme(themeId);
            boolean commit = preference.getSharedPreferences()
                    .edit()
                    .putInt("themeId", themeId)
                    .commit();
            updateActivity();
            return commit;
        }

        private boolean onDifficultyChange(Preference preference, Object newValue) {
            String difficultyValue = (String) newValue;
            final int difficulty;
            switch (difficultyValue) {
                case "easy":
                    difficulty = 0;
                    break;
                case "medium":
                    difficulty = 1;
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown difficulty");
            }
            return preference.getSharedPreferences()
                    .edit()
                    .putInt("difficulty", difficulty)
                    .commit();
        }

        private boolean onLanguageChange(Preference preference, Object newValue) {
            LocaleHelper.updateLocale(preference.getContext(), (String) newValue);

            boolean commit = preference.getSharedPreferences()
                    .edit()
                    .putString("lang", (String) newValue)
                    .commit();
            updateActivity();
            return commit;
        }

        private void updateActivity() {
            FragmentActivity activity = getActivity();
            if (activity == null) {
                return;
            }
            Intent intent = new Intent(activity, activity.getClass());
            startActivity(intent);
            activity.finish();
        }
    }
}