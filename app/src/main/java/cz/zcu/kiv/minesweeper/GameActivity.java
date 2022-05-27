package cz.zcu.kiv.minesweeper;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        /*if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragments, new DetailsMenuFragment())
                    .commit();
        }*/
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }
        // TODO tohle použít na layering
        Log.i("GameActivity", String.valueOf(AppCompatResources.getDrawable(this, R.drawable.ic_icon_tile_white)));
//        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable});
    }
}