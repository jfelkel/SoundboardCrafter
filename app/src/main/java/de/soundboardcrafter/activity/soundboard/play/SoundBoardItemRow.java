package de.soundboardcrafter.activity.soundboard.play;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import de.soundboardcrafter.R;
import de.soundboardcrafter.activity.common.mediaplayer.SoundboardMediaPlayer;
import de.soundboardcrafter.model.Sound;
import de.soundboardcrafter.model.Soundboard;

/**
 * Tile for a single sound in a soundboard, allows the sound to be played and stopped again.
 */
class SoundBoardItemRow extends RelativeLayout {
    @NonNull
    private final TextView soundItem;
    private Sound sound;

    SoundBoardItemRow(Context context) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the view into this object
        inflater.inflate(R.layout.soundboard_item, this, true);
        soundItem = findViewById(R.id.sound_item);
    }

    public interface MediaPlayerServiceCallback {
        boolean isConnected();

        boolean isPlaying(Soundboard soundboard, Sound sound);

        void play(Soundboard soundboard, Sound sound);

        void setOnPlayingStopped(Soundboard soundboard, Sound sound,
                                 SoundboardMediaPlayer.OnPlayingStopped onPlayingStopped);

        void stopPlaying(Soundboard soundboard, Sound sound);
    }

    /**
     * Set the data for the view.
     */
    @UiThread
    void setSound(Soundboard soundboard, Sound sound, MediaPlayerServiceCallback mediaPlayerServiceCallback) {
        if (!mediaPlayerServiceCallback.isConnected()) {
            return;
        }

        this.sound = sound;
        soundItem.setText(this.sound.getName());

        setImage(mediaPlayerServiceCallback.isPlaying(soundboard, sound) ?
                R.drawable.ic_stop : R.drawable.ic_play);

        setOnClickListener(l -> {
            if (!mediaPlayerServiceCallback.isPlaying(soundboard, sound)) {
                setImage(R.drawable.ic_stop);
                mediaPlayerServiceCallback.play(soundboard, sound);
            } else {
                mediaPlayerServiceCallback.stopPlaying(soundboard, this.sound);
            }
        });

        setOnLongClickListener(l -> {
            // Do NOT consume long clicks.
            // Without this, this context menu on the grid view won't work
            return false;
        });
    }

    @UiThread
    private void setImage(int p) {
        soundItem.setCompoundDrawablesWithIntrinsicBounds(p, 0, 0, 0);
    }

    String getSoundName() {
        return getSound().getName();
    }

    Sound getSound() {
        return sound;
    }
}
