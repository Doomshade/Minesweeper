package cz.zcu.kiv.jsmahy.minesweeper;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class MinesweeperApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        LocaleHelper.updateLocale(this, prefs.getString("lang", "en"));
        int themeId = prefs.getInt("themeId", R.style.Theme_AppCompat_Light_NoActionBar_FullScreen_Fulbo);
        setTheme(themeId);
        getApplicationContext().setTheme(themeId);
    }

}
