package cz.zcu.kiv.jsmahy.minesweeper;

import android.content.Intent;
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
            lang.setDefaultValue(PreferenceManager.getDefaultSharedPreferences(lang.getContext()).getString("lang", "en"));
            lang.setOnPreferenceChangeListener(this::onLanguageChange);
            Objects.requireNonNull((Preference) findPreference("diff")).setOnPreferenceChangeListener(this::onDifficultyChange);
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
            FragmentActivity activity = getActivity();
            if (activity == null) {
                return commit;
            }
            Intent intent = new Intent(activity, activity.getClass());
            startActivity(intent);
            activity.finish();
            return commit;
        }
    }
}