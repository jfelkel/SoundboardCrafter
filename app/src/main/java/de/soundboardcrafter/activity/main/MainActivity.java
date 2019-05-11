package de.soundboardcrafter.activity.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Supplier;

import de.soundboardcrafter.R;
import de.soundboardcrafter.activity.audiofile.list.AudioFileListFragment;
import de.soundboardcrafter.activity.game.edit.GameEditFragment;
import de.soundboardcrafter.activity.game.list.GameListFragment;
import de.soundboardcrafter.activity.soundboard.list.SoundboardListFragment;

/**
 * The main activity, showing the soundboards.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final int REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1024;
    private static final int REQUEST_PERMISSIONS_READ_EXTERNAL_STORAGE = 0;
    private static final String KEY_SELECTED_PAGE = "selectedPage";
    private ViewPager pager;
    private ScreenSlidePagerAdapter pagerAdapter;
    private Page selectedPage;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pager = findViewById(R.id.viewPagerMain);
        pager.clearOnPageChangeListeners();
        TabLayout tabLayout = findViewById(R.id.tabLayoutMain);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                @Nullable Page tmp = pagerAdapter.getPage(position);
                if (tmp != null) {
                    selectedPage = tmp;
                }
            }
        });

        tabLayout.setupWithViewPager(pager);
        if (savedInstanceState != null) {
            @Nullable String selectedPage = savedInstanceState.getString(KEY_SELECTED_PAGE);
            this.selectedPage = selectedPage != null ? Page.valueOf(selectedPage) : null;
        }
        Intent calledIntent = getIntent();
        if (calledIntent.getExtras() != null
                && calledIntent.getExtras().get(GameEditFragment.EXTRA_EDIT_FRAGMENT) != null) {
            selectedPage = Page.GAMES;
        }
        if (selectedPage == null) {
            selectedPage = Page.SOUNDBOARDS;
        }
        int index = pagerAdapter.getIndexOf(selectedPage);
        pager.setCurrentItem(index != -1 ? index : 0, false);

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            return;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (pagerAdapter != null) {
            pagerAdapter.notifyDataSetChanged();
        }

    }

    public enum Page {
        GAMES(R.string.games_tab_title, GameListFragment::new),
        SOUNDBOARDS(R.string.soundboards_tab_title, SoundboardListFragment::new),
        SOUNDS(R.string.sounds_tab_title, AudioFileListFragment::new);

        public final int title;
        public final Supplier<Fragment> createNew;

        Page(int title, Supplier<Fragment> createNew) {
            this.title = title;
            this.createNew = createNew;
        }
    }


    class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private final List<Page> pages = Lists.newArrayList(Page.GAMES, Page.SOUNDBOARDS, Page.SOUNDS);

        ScreenSlidePagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @Override
        public @NonNull
        Fragment getItem(int position) {
            Page page = pages.get(position);
            return page.createNew.get();
        }

        Page getPage(int position) {
            return pages.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return getString(pages.get(position).title);
        }

        @Override
        public int getCount() {
            return pages.size();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            // https://medium.com/inloopx/adventures-with-fragmentstatepageradapter-4f56a643f8e0
            return POSITION_NONE;
        }

        int getIndexOf(Page selectedPage) {
            return pages.indexOf(selectedPage);
        }
    }

    @Override
    @UiThread
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_READ_EXTERNAL_STORAGE || requestCode == REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // User denied. Stop the app.
                finishAndRemoveTask();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    @UiThread
    public void onResume() {
        super.onResume();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        int selectedTab = pager.getCurrentItem();
        @Nullable Page selectedPage = pagerAdapter.getPage(selectedTab);
        @Nullable String selectedPageName = selectedPage != null ?
                selectedPage.name() : null;

        outState.putString(KEY_SELECTED_PAGE, selectedPageName);
    }


}