package de.soundboardcrafter.activity.game.list;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.fragment.app.Fragment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.annotation.Nonnull;

import de.soundboardcrafter.R;
import de.soundboardcrafter.activity.game.edit.GameCreateActivity;
import de.soundboardcrafter.dao.GameDao;
import de.soundboardcrafter.model.GameWithSoundboards;

/**
 * Shows Games in a Grid
 */
public class GameListFragment extends Fragment {
    private static final String TAG = GameListFragment.class.getName();
    private ListView listView;
    private Button addNewGame;
    private GameListItemAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GameListFragment.FindGamesTask(getContext()).execute();
    }

    @Override
    @UiThread
    public View onCreateView(@Nonnull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game_list,
                container, false);
        listView = rootView.findViewById(R.id.listview_games);
        addNewGame = rootView.findViewById(R.id.new_game);
        addNewGame.setOnClickListener(e -> {
            startActivityForResult(GameCreateActivity.newIntent(getContext()));
        });

        return rootView;
    }


    private void startActivityForResult(@SuppressLint("UnknownNullness") Intent intent) {
        startActivityForResult(intent, 0, null);
    }

    @UiThread
    private void initGameItemAdapter(ImmutableList<GameWithSoundboards> games) {
        List<GameWithSoundboards> list = Lists.newArrayList(games);
        list.sort((g1, g2) -> g1.getGame().getName().compareTo(g2.getGame().getName()));
        adapter = new GameListItemAdapter(list);
        listView.setAdapter(adapter);
        updateUI();
    }

    @UiThread
    private void updateUI() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * A background task, used to retrieve games from the database.
     */
    class FindGamesTask extends AsyncTask<Void, Void, ImmutableList<GameWithSoundboards>> {
        private final String TAG = GameListFragment.FindGamesTask.class.getName();

        private final WeakReference<Context> appContextRef;

        FindGamesTask(Context context) {
            super();
            appContextRef = new WeakReference<>(context.getApplicationContext());
        }

        @Override
        @WorkerThread
        protected ImmutableList<GameWithSoundboards> doInBackground(Void... voids) {
            Context appContext = appContextRef.get();
            if (appContext == null) {
                cancel(true);
                return null;
            }

            GameDao gameDao = GameDao.getInstance(appContext);

            Log.d(TAG, "Loading games...");

            ImmutableList<GameWithSoundboards> res = gameDao.findAllGamesWithSoundboards();

            Log.d(TAG, "Games loaded.");

            return res;
        }

        @Override
        @UiThread
        protected void onPostExecute(ImmutableList<GameWithSoundboards> gameWithSoundboards) {
            Context appContext = appContextRef.get();

            if (appContext == null) {
                // application context no longer available, I guess that result
                // will be of no use to anyone
                return;
            }
            initGameItemAdapter(gameWithSoundboards);
        }
    }
}