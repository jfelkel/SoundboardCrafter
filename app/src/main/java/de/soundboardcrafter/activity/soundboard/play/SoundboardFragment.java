package de.soundboardcrafter.activity.soundboard.play;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.common.collect.ImmutableCollection;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.UUID;

import javax.annotation.Nonnull;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.fragment.app.Fragment;
import de.soundboardcrafter.R;
import de.soundboardcrafter.activity.common.mediaplayer.MediaPlayerService;
import de.soundboardcrafter.activity.common.mediaplayer.SoundboardMediaPlayer;
import de.soundboardcrafter.activity.sound.edit.SoundEditActivity;
import de.soundboardcrafter.activity.sound.edit.SoundEditFragment;
import de.soundboardcrafter.dao.SoundboardDao;
import de.soundboardcrafter.model.Sound;
import de.soundboardcrafter.model.Soundboard;

/**
 * Shows Soundboard in a Grid
 */
public class SoundboardFragment extends Fragment implements ServiceConnection {
    private static final String TAG = SoundboardFragment.class.getName();

    private static final int REQUEST_EDIT_SOUND = 1;

    private GridView gridView;
    // TODO Allow for zero or more than one soundboards
    private SoundboardItemAdapter soundboardItemAdapter;
    private MediaPlayerService mediaPlayerService;
    private Soundboard soundboard;

    static SoundboardFragment createTab(Soundboard soundboard) {
        Bundle thisTabArguments = new Bundle();
        thisTabArguments.putSerializable("Soundboard", soundboard);
        SoundboardFragment thisTab = new SoundboardFragment();
        thisTab.setArguments(thisTabArguments);
        return thisTab;
    }

    @Override
    @UiThread
    public void onServiceConnected(ComponentName name, IBinder binder) {
        MediaPlayerService.Binder b = (MediaPlayerService.Binder) binder;
        mediaPlayerService = b.getService();
        //as soon the mediaplayerservice is connected, the play/stop icons can be set correctly
        updateUI();

        Log.d(TAG, "MediaPlayerService is connected");
    }

    @Override
    @UiThread
    public void onServiceDisconnected(ComponentName name) {
        // TODO What to do on Service Disconnected?
    }

    @Override
    @UiThread
    public void onBindingDied(ComponentName name) {
        // TODO What to do on Service Died?
    }

    @Override
    @UiThread
    public void onNullBinding(ComponentName name) {
        // TODO What to do on Null Binding?
    }


    @Override
    @UiThread
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        soundboard = (Soundboard) getArguments().getSerializable("Soundboard");
        Intent intent = new Intent(getActivity(), MediaPlayerService.class);
        getActivity().startService(intent);
        // TODO Necessary?! Also done in onResume()
        bindService();


    }

    private void bindService() {
        Intent intent = new Intent(getActivity(), MediaPlayerService.class);
        getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    @UiThread
    public void onPause() {
        super.onPause();
        getActivity().unbindService(this);
    }


    @Override
    @UiThread
    public View onCreateView(@Nonnull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_soundboard,
                container, false);

        gridView = rootView.findViewById(R.id.grid_view_soundboard);
        initSoundboardItemAdapter();
        registerForContextMenu(gridView);
        return rootView;
    }

    private SoundBoardItemRow.MediaPlayerServiceCallback newMediaPlayerServiceCallback() {
        SoundBoardItemRow.MediaPlayerServiceCallback mediaPlayerServiceCallback = new SoundBoardItemRow.MediaPlayerServiceCallback() {
            @Override
            public boolean isConnected() {
                return getService() != null;
            }

            @Override
            public boolean shouldBePlaying(Soundboard soundboard, Sound sound) {
                MediaPlayerService service = getService();
                if (service == null) {
                    return false;
                }
                return service.shouldBePlaying(soundboard, sound);
            }

            @Override
            public void initMediaPlayer(Soundboard soundboard, Sound sound, SoundboardMediaPlayer.InitializeCallback initializeCallback,
                                        SoundboardMediaPlayer.StartPlayCallback playCallback, SoundboardMediaPlayer.StopPlayCallback stopPlayCallback) {
                MediaPlayerService service = getService();
                if (service != null) {
                    service.initMediaPlayer(soundboard, sound, initializeCallback, playCallback, stopPlayCallback);
                }
            }

            @Override
            public void startPlaying(Soundboard soundboard, Sound sound) {
                MediaPlayerService service = getService();
                if (service != null) {
                    service.startPlaying(soundboard, sound);
                }
            }

            @Override
            public void setMediaPlayerCallbacks(Soundboard soundboard, Sound sound, SoundboardMediaPlayer.StartPlayCallback startPlayCallback,
                                                SoundboardMediaPlayer.StopPlayCallback stopPlayCallback) {
                MediaPlayerService service = getService();
                if (service != null) {
                    service.setMediaPlayerCallbacks(soundboard, sound, startPlayCallback, stopPlayCallback);
                }
            }

            @Override
            public void stopPlaying(Soundboard soundboard, Sound sound) {
                MediaPlayerService service = getService();
                if (service != null) {
                    service.stopPlaying(soundboard, sound);
                }

            }
        };
        return mediaPlayerServiceCallback;
    }


    @Override
    @UiThread
    // Called especially when the SoundEditActivity returns.
    public void onResume() {
        super.onResume();
        updateUI();
        bindService();
    }

    /**
     * Starts reading the data for the UI (first time) or
     * simple ensure that the grid shows the latest information.
     */
    @UiThread
    private void updateUI() {
        if (soundboardItemAdapter != null) {
            soundboardItemAdapter.notifyDataSetChanged();
        }
    }

    @UiThread
    private void initSoundboardItemAdapter() {
        // TODO Start without any soundboard
        soundboardItemAdapter =
                new SoundboardItemAdapter(newMediaPlayerServiceCallback(), soundboard);
        gridView.setAdapter(soundboardItemAdapter);
        updateUI();
    }


    @Override
    @UiThread
    public void onCreateContextMenu(@Nonnull ContextMenu menu, @Nonnull View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.fragment_main_context, menu);

        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo =
                (AdapterView.AdapterContextMenuInfo) menuInfo;
        SoundBoardItemRow itemRow = (SoundBoardItemRow) adapterContextMenuInfo.targetView;

        menu.setHeaderTitle(itemRow.getSoundName());
    }

    @Override
    @UiThread
    public boolean onContextItemSelected(@Nonnull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_menu_edit_sound:
                AdapterView.AdapterContextMenuInfo menuInfo =
                        (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                SoundBoardItemRow itemRow = (SoundBoardItemRow) menuInfo.targetView;
                Sound sound = itemRow.getSound();

                Log.d(TAG, "Editing sound \"" + sound.getName() + "\"");

                Intent intent = SoundEditActivity.newIntent(getActivity(), sound);
                startActivityForResult(intent, REQUEST_EDIT_SOUND);
                return true;
            case R.id.context_menu_remove_sound:
                menuInfo =
                        (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

                Log.d(TAG, "Removing sound " + menuInfo.position);
                soundboardItemAdapter.remove(menuInfo.position);
                new RemoveSoundsTask(getActivity()).execute(menuInfo.position);
                return true;
            default:
                return false;
        }
    }

    @Override
    @UiThread
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_EDIT_SOUND:
                Log.d(TAG, "Returned from sound edit fragment with OK");

                final UUID soundId = UUID.fromString(
                        data.getStringExtra(SoundEditFragment.EXTRA_SOUND_ID));
                new UpdateSoundsTask(getActivity()).execute(soundId);

                break;
        }
    }

    @Override
    @UiThread
    public void onDestroy() {
        super.onDestroy();
        // TODO: 17.03.2019 destroy service save state
    }

    private MediaPlayerService getService() {
        if (mediaPlayerService == null) {
            // TODO Necessary?! Also done in onResume()
            bindService();
        }
        return mediaPlayerService;
    }


    /**
     * A background task, used to retrieve some sounds from the database and update the GUI.
     */
    public class UpdateSoundsTask extends AsyncTask<UUID, Void, ImmutableCollection<Sound>> {
        private final String TAG = UpdateSoundsTask.class.getName();

        private final WeakReference<Context> appContextRef;
        private final SoundboardDao soundboardDao = SoundboardDao.getInstance(getActivity());

        UpdateSoundsTask(Context context) {
            super();
            appContextRef = new WeakReference<>(context.getApplicationContext());
        }

        @Override
        @WorkerThread
        protected ImmutableCollection<Sound> doInBackground(UUID... soundIds) {
            Context appContext = appContextRef.get();
            if (appContext == null) {
                cancel(true);
                return null;
            }

            Log.d(TAG, "Loading sounds: " + Arrays.toString(soundIds));

            return soundboardDao.findSounds(soundIds);
        }

        @Override
        @UiThread
        protected void onPostExecute(ImmutableCollection<Sound> sounds) {
            if (!isAdded()) {
                // fragment is no longer linked to an activity
                return;
            }
            Context appContext = appContextRef.get();

            if (appContext == null) {
                // application context no longer available, I guess that result
                // will be of no use to anyone
                return;
            }

            soundboardItemAdapter.updateSounds(sounds);
        }
    }

    /**
     * A background task, used to remove sounds with the given indexes from the soundboard
     */
    public class RemoveSoundsTask extends AsyncTask<Integer, Void, Void> {
        private final String TAG = RemoveSoundsTask.class.getName();

        private final WeakReference<Context> appContextRef;
        private final SoundboardDao soundboardDao = SoundboardDao.getInstance(getActivity());

        RemoveSoundsTask(Context context) {
            super();
            appContextRef = new WeakReference<>(context.getApplicationContext());
        }

        @Override
        @WorkerThread
        protected Void doInBackground(Integer... indexes) {
            Context appContext = appContextRef.get();
            if (appContext == null) {
                cancel(true);
                return null;
            }

            for (int index : indexes) {
                Log.d(TAG, "Removing sound + " + index + " from soundboard");

                soundboardDao.unlinkSound(soundboardItemAdapter.getSoundboard(), index);
            }

            return null;
        }
    }


}