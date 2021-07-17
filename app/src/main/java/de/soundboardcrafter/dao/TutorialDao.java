package de.soundboardcrafter.dao;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.UiThread;

import javax.annotation.ParametersAreNonnullByDefault;

import static android.content.Context.MODE_PRIVATE;

/**
 * Used to read and write the state of the tutorial.
 */
@ParametersAreNonnullByDefault
@UiThread
public class TutorialDao {
    public enum Key {
        SOUNDBOARD_PLAY_START_SOUND,
        SOUNDBOARD_PLAY_CONTEXT_MENU,
        SOUNDBOARD_PLAY_SORT
    }

    private static final String SHARED_PREFERENCES =
            TutorialDao.class.getName() + "_Prefs";

    private static TutorialDao instance;
    private Context appContext;

    public static TutorialDao getInstance(final Context context) {
        if (instance == null) {
            instance = new TutorialDao(context);
        }
        return instance;
    }

    private TutorialDao(Context context) {
        appContext = context.getApplicationContext();
    }

    public boolean isChecked(Key key) {
        return getPrefs().getBoolean(key.name(), false);
    }

    public void check(Key key) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putBoolean(key.name(), true);
        editor.apply();
    }

    public void uncheckAll() {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.clear();
        editor.apply();
    }

    private SharedPreferences getPrefs() {
        return appContext.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
    }
}
