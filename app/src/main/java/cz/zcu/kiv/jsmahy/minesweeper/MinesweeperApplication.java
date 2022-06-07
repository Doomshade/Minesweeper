package cz.zcu.kiv.jsmahy.minesweeper;

import android.app.Application;

import androidx.preference.PreferenceManager;

public class MinesweeperApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LocaleHelper.updateLocale(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("lang", "en"));
    }

}
