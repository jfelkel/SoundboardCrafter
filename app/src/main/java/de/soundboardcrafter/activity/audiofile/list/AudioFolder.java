package de.soundboardcrafter.activity.audiofile.list;

import java.util.Objects;

/**
 * A folder containing audio files - or folders with (folders with...) audio files.
 */
class AudioFolder extends AbstractAudioFolderEntry {
    private String path;
    private int numAudioFiles;

    AudioFolder(String path, int numAudioFiles) {
        this.path = path;
        this.numAudioFiles = numAudioFiles;
    }

    public String getPath() {
        return path;
    }

    int getNumAudioFiles() {
        return numAudioFiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AudioFolder that = (AudioFolder) o;
        return numAudioFiles == that.numAudioFiles &&
                Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path, numAudioFiles);
    }

    @Override
    public String toString() {
        return "AudioFolder{" +
                "path='" + path + '\'' +
                ", numAudioFiles=" + numAudioFiles +
                '}';
    }
}
