package de.soundboardcrafter.activity.soundboard.play;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.common.collect.ImmutableList;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import de.soundboardcrafter.R;
import de.soundboardcrafter.activity.common.mediaplayer.MediaPlayerService;
import de.soundboardcrafter.dao.SoundboardDao;
import de.soundboardcrafter.model.SoundboardWithSounds;

/**
 * The main activity, showing the soundboards.
 */
public class SoundboardPlayActivity extends AppCompatActivity
        implements ServiceConnection, ResetAllDialogFragment.OnOkCallback {
    private static final String TAG = SoundboardPlayActivity.class.getName();
    private static final String KEY_SELECTED_SOUNDBOARD_ID = "selectedSoundboardId";
    private static final String KEY_GAME_ID = "gameId";
    private static final String SHARED_PREFERENCES = SoundboardPlayActivity.class.getName() + "_Prefs";
    private static final String DIALOG_RESET_ALL = "DialogResetAll";
    private static final int REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1024;
    private ScreenSlidePagerAdapter pagerAdapter;
    private static final int REQUEST_PERMISSIONS_READ_EXTERNAL_STORAGE = 0;

    private static final String EXTRA_SOUNDBOARD_ID = "SoundboardId";
    public static final String EXTRA_GAME_ID = "GameId";

    private MediaPlayerService mediaPlayerService;
    private TextView gameNameTextView;
    private ViewPager pager;

    /**
     * ID of the chose game - or <code>null</code>, if all soundboards shall be displayed.
     */
    private @Nullable
    UUID gameId;

    private @Nullable
    UUID selectedSoundboardId;

    @Override
    @UiThread
    public void onServiceConnected(ComponentName name, IBinder binder) {
        MediaPlayerService.Binder b = (MediaPlayerService.Binder) binder;
        mediaPlayerService = b.getService();

        Log.d(TAG, "MediaPlayerService is connected");
    }

    @Override
    @UiThread
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "MediaPlayerService is disconnected");

        // TODO What to do on Service Disconnected?
    }

    @Override
    @UiThread
    public void onBindingDied(ComponentName name) {
        Log.d(TAG, "MediaPlayerService binding has died");

        // TODO What to do on Service Died?
    }

    @Override
    @UiThread
    public void onNullBinding(ComponentName name) {
        Log.d(TAG, "MediaPlayerService has null binding");

        // TODO What to do on Null Binding?
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soundboards);

        Intent intent = new Intent(this, MediaPlayerService.class);
        startService(intent);
        // TODO Necessary?! Also done in onResume()
        bindService();

        gameNameTextView = findViewById(R.id.game_name);
        pager = findViewById(R.id.viewPager);
        pager.clearOnPageChangeListeners();
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                @Nullable UUID tmp = pagerAdapter.getSoundboardId(position);
                if (tmp != null) {
                    selectedSoundboardId = tmp;
                }
            }
        });

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(pager);

        if (savedInstanceState != null) {
            selectedSoundboardId = getUUID(savedInstanceState, KEY_SELECTED_SOUNDBOARD_ID);
            gameId = getUUID(savedInstanceState, KEY_GAME_ID);
        }
        if (selectedSoundboardId == null && getIntent() != null) {
            selectedSoundboardId = getUUIDExtra(getIntent(), EXTRA_SOUNDBOARD_ID);
        }
        if (selectedSoundboardId == null) {
            selectedSoundboardId = getUUIDPreference(KEY_SELECTED_SOUNDBOARD_ID);
        }
        if (gameId == null) {
            if (getIntent() != null) {
                gameId = getUUIDExtra(getIntent(), EXTRA_GAME_ID);
            } else {
                gameId = getUUIDPreference(KEY_GAME_ID);
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        if (gameId != null) {
            gameNameTextView.setVisibility(View.VISIBLE);
            gameNameTextView.setText("");
        } else {
            gameNameTextView.setVisibility(View.GONE);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Nullable
    private UUID getUUIDPreference(String key) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        String idPref = pref.getString(key, null);
        if (idPref == null) {
            return null;
        }
        UUID id = UUID.fromString(idPref);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(KEY_SELECTED_SOUNDBOARD_ID);
        editor.commit();
        return id;
    }

    @Nullable
    private static UUID getUUIDExtra(Intent intent, String extraKey) {
        String idString = intent.getStringExtra(extraKey);
        return idString != null ? UUID.fromString(idString) : null;
    }

    @Nullable
    private static UUID getUUID(Bundle savedInstanceState, String key) {
        @Nullable String idString = savedInstanceState.getString(key);
        return idString != null ? UUID.fromString(idString) : null;
    }

    private void bindService() {
        Intent intent = new Intent(this, MediaPlayerService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            return;
        }
        pagerAdapter.clear(false);
        new FindSoundboardsTask(this).execute();
    }

    @Override
    @UiThread
    public void doResetAll() {
        Log.i(TAG, "Resetting all data");
        pagerAdapter.clear(true);
        new ResetAllTask(this).execute();
    }

    @Override
    @UiThread
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_main, menu);
        if (gameId != null) {
            MenuItem item = menu.findItem(R.id.toolbar_menu_reset_all);
            item.setVisible(false);
            item.setEnabled(false);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_menu_reset_all:
                resetAllOrCancel();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @UiThread
    private void resetAllOrCancel() {
        ResetAllDialogFragment dialog = new ResetAllDialogFragment();
        dialog.show(getSupportFragmentManager(), DIALOG_RESET_ALL);
    }

    class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private final List<SoundboardWithSounds> soundboardList = new ArrayList<>();

        ScreenSlidePagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @Override
        public @NonNull
        Fragment getItem(int position) {
            return SoundboardFragment.createFragment(soundboardList.get(position));
        }

        /**
         * Add soundboards to the @link {@link #soundboardList} and refreshes the
         * view
         */
        void addSoundboards(Collection<SoundboardWithSounds> soundboards) {
            soundboardList.addAll(soundboards);
            soundboardList.sort((s1, s2) -> s1.getName().compareTo(s2.getName()));
            notifyDataSetChanged();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return soundboardList.get(position).getName();
        }

        /**
         * Returns the index of the soundboard with the given ID - or <code>null</code>,
         * if no soundboard with this ID exists.
         */
        @Nullable
        Integer getIndex(UUID soundboardId) {
            for (int index = 0; index < getCount(); index++) {
                if (soundboardList.get(index).getId().equals(soundboardId)) {
                    return index;
                }
            }

            return null;
        }

        /**
         * Returns the soundboard ID at this index - or <code>null</code>, if the index
         * was invalid.
         */
        @Nullable
        UUID getSoundboardId(int index) {
            @Nullable SoundboardWithSounds soundboard = getSoundboard(index);
            if (soundboard == null) {
                return null;
            }

            return soundboard.getId();
        }

        /**
         * Returns the soundboard at this index - or <code>null</code>, if the index
         * was invalid.
         */
        @Nullable
        SoundboardWithSounds getSoundboard(int index) {
            if (index >= getCount()) {
                return null;
            }

            return soundboardList.get(index);
        }

        @Override
        public int getCount() {
            return soundboardList.size();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            // https://medium.com/inloopx/adventures-with-fragmentstatepageradapter-4f56a643f8e0
            return POSITION_NONE;
        }

        void clear(boolean stopPlayingAllSoundboards) {
            if (stopPlayingAllSoundboards) {
                stopPlayingAllSoundboards();
            }

            soundboardList.clear();
            notifyDataSetChanged();
        }

        private void stopPlayingAllSoundboards() {
            MediaPlayerService service = getService();
            if (service == null) {
                return;
            }
            service.stopPlaying(soundboardList.stream()
                    .map(SoundboardWithSounds::getSoundboard)
                    .collect(Collectors.toList()));
        }
    }

    @Override
    @UiThread
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_READ_EXTERNAL_STORAGE || requestCode == REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // User denied. Stop the app.
                finishAndRemoveTask();
                return;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    @Override
    @UiThread
    public void onResume() {
        super.onResume();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        bindService();
    }

    private MediaPlayerService getService() {
        if (mediaPlayerService == null) {
            // TODO Necessary?! Also done in onResume()
            bindService();
        }
        return mediaPlayerService;
    }

    @Override
    protected void onDestroy() {
        savePreference(KEY_SELECTED_SOUNDBOARD_ID, selectedSoundboardId);
        savePreference(KEY_GAME_ID, gameId);
        super.onDestroy();
    }

    private void savePreference(String key, @Nullable Object value) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value != null ? value.toString() : null);
        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        int selectedTab = pager.getCurrentItem();
        @Nullable UUID selectedSoundboardId = pagerAdapter.getSoundboardId(selectedTab);
        putUUID(outState, KEY_SELECTED_SOUNDBOARD_ID, selectedSoundboardId);
        putUUID(outState, KEY_GAME_ID, gameId);
    }

    private void putUUID(@NonNull Bundle outState, String key, UUID id) {
        @Nullable String idString = id != null ?
                id.toString() : null;
        outState.putString(key, idString);
    }

    /**
     * A background task, used to retrieve soundboards from the database.
     */
    class FindSoundboardsTask extends AsyncTask<Void, Void, ImmutableList<SoundboardWithSounds>> {
        private final String TAG = FindSoundboardsTask.class.getName();

        private final WeakReference<Context> appContextRef;

        @Nullable
        private final UUID gameId;

        FindSoundboardsTask(Context context) {
            super();
            appContextRef = new WeakReference<>(context.getApplicationContext());
            gameId = SoundboardPlayActivity.this.gameId;
        }

        @Override
        @WorkerThread
        protected ImmutableList<SoundboardWithSounds> doInBackground(Void... voids) {
            Context appContext = appContextRef.get();
            if (appContext == null) {
                cancel(true);
                return null;
            }

            SoundboardDao soundboardDao = SoundboardDao.getInstance(appContext);

            // TODO Load Game by gameID (and set Game name later on)
            Log.d(TAG, "Loading soundboards...");

            ImmutableList<SoundboardWithSounds> res = soundboardDao.findAllWithSounds(gameId);

            Log.d(TAG, "Soundboards loaded.");

            return res;
        }

        @Override
        @UiThread
        protected void onPostExecute(ImmutableList<SoundboardWithSounds> soundboards) {
            Context appContext = appContextRef.get();

            if (appContext == null) {
                // application context no longer available, I guess that result
                // will be of no use to anyone
                return;
            }
            pagerAdapter.addSoundboards(soundboards);

            @Nullable Integer index = null;
            if (selectedSoundboardId != null) {
                index = pagerAdapter.getIndex(selectedSoundboardId);
            }

            pager.setCurrentItem(index != null ? index : 0, false);
        }
    }

    /**
     * A background task, used to reset games, soundboards and sounds.
     */
    class ResetAllTask extends AsyncTask<Void, Void, ImmutableList<SoundboardWithSounds>> {
        private final String TAG = ResetAllTask.class.getName();

        private final WeakReference<Context> appContextRef;

        ResetAllTask(Context context) {
            super();

            appContextRef = new WeakReference<>(context.getApplicationContext());
        }

        @Override
        @WorkerThread
        protected ImmutableList<SoundboardWithSounds> doInBackground(Void... voids) {
            Context appContext = appContextRef.get();
            if (appContext == null) {
                cancel(true);
                return null;
            }

            Log.d(TAG, "Resetting all data.");

            SoundboardDao soundboardDao = SoundboardDao.getInstance(appContext);
            soundboardDao.clearDatabase();


            Log.d(TAG, "Loading soundboards...");

            final ImmutableList<SoundboardWithSounds> res =
                    // Resetting if only enabled when no game is selected
                    soundboardDao.findAllWithSounds(null);

            Log.d(TAG, "Soundboards loaded.");

            return res;
        }

        @Override
        @UiThread
        protected void onPostExecute(ImmutableList<SoundboardWithSounds> soundboards) {

            Context appContext = appContextRef.get();

            if (appContext == null) {
                // application context no longer available, I guess that result
                // will be of no use to anyone
                return;
            }

            pagerAdapter.addSoundboards(soundboards);

            @Nullable Integer index = null;
            if (selectedSoundboardId != null) {
                index = pagerAdapter.getIndex(selectedSoundboardId);
            }

            pager.setCurrentItem(index != null ? index : 0, false);
        }

    }
}