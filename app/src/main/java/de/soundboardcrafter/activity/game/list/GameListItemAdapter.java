package de.soundboardcrafter.activity.game.list;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import java.util.List;

import de.soundboardcrafter.R;
import de.soundboardcrafter.activity.common.AbstractTutorialListAdapter;
import de.soundboardcrafter.dao.TutorialDao;
import de.soundboardcrafter.model.GameWithSoundboards;

/**
 * Adapter for a GameItem.
 */
class GameListItemAdapter extends AbstractTutorialListAdapter {
    private final List<GameWithSoundboards> gamesWithSoundboards;

    GameListItemAdapter(List<GameWithSoundboards> gamesWithSoundboards) {
        this.gamesWithSoundboards = gamesWithSoundboards;
    }

    @Override
    public int getCount() {
        return gamesWithSoundboards.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public GameWithSoundboards getItem(int position) {
        return gamesWithSoundboards.get(position);
    }

    @Override
    @UiThread
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        if (!(convertView instanceof GameListItemRow)) {
            convertView = new GameListItemRow(parent.getContext());
        }
        GameListItemRow itemRow = (GameListItemRow) convertView;

        TutorialDao tutorialDao = TutorialDao.getInstance(itemRow.getContext());

        itemRow.setGameWithSoundboards(gamesWithSoundboards.get(position));

        showTutorialHintIfNecessary(position, itemRow, itemRow.getTvGameName(),
                () -> tutorialDao.isChecked(TutorialDao.Key.SOUNDBOARD_PLAY_START_SOUND)
                        && !tutorialDao.isChecked(TutorialDao.Key.GAME_LIST_CONTEXT_MENU),
                R.string.tutorial_game_list_context_menu_description);

        return convertView;
    }

    public void remove(GameWithSoundboards gameWithSoundboards) {
        gamesWithSoundboards.stream()
                .filter(g -> g.getGame().getId().equals(gameWithSoundboards.getGame().getId()))
                .findFirst()
                .ifPresent(obj -> {
                    gamesWithSoundboards.remove(obj);
                    notifyDataSetChanged();
                });
    }
}