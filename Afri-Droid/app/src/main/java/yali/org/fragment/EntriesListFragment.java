/**
 * Flym
 * <p>
 * Copyright (c) 2012-2015 Frederic Julian
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package yali.org.fragment;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import yali.org.Constants;
import yali.org.MainApplication;
import yali.org.R;
import yali.org.adapter.EntriesCursorAdapter;
import yali.org.provider.FeedData;
import yali.org.provider.FeedDataContentProvider;
import yali.org.service.AutoRefreshService;
import yali.org.service.FetcherService;
import yali.org.utils.PrefUtils;
import yali.org.utils.UiUtils;

import static yali.org.Constants.URL_TOPICS;
import static yali.org.Constants.URL_VERSION;

public class EntriesListFragment extends SwipeRefreshListFragment {

    private static final String STATE_CURRENT_URI = "STATE_CURRENT_URI";
    private static final String STATE_ORIGINAL_URI = "STATE_ORIGINAL_URI";
    private static final String STATE_SHOW_FEED_INFO = "STATE_SHOW_FEED_INFO";
    private static final String STATE_LIST_DISPLAY_DATE = "STATE_LIST_DISPLAY_DATE";
    private boolean ISPROGRESSIVE = false;
    private static final int ENTRIES_LOADER_ID = 1;
    private static final int NEW_ENTRIES_NUMBER_LOADER_ID = 2;
    private SharedPreferences shared;
    private SharedPreferences.Editor editor;
    private Uri mCurrentUri, mOriginalUri;
    private boolean mShowFeedInfo = false;
    private EntriesCursorAdapter mEntriesCursorAdapter;
    private Cursor mJustMarkedAsReadEntries;
    private FloatingActionButton mFab;
    private ListView mListView;
    private long mListDisplayDate = new Date().getTime();
    private final LoaderManager.LoaderCallbacks<Cursor> mEntriesLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String entriesOrder = PrefUtils.getBoolean(PrefUtils.DISPLAY_OLDEST_FIRST, false) ? Constants.DB_ASC : Constants.DB_DESC;
            String where = "(" + FeedData.EntryColumns.FETCH_DATE + Constants.DB_IS_NULL + Constants.DB_OR + FeedData.EntryColumns.FETCH_DATE + "<=" + mListDisplayDate + ')';
            CursorLoader cursorLoader = new CursorLoader(getActivity(), mCurrentUri, null, where, null, FeedData.EntryColumns.DATE + entriesOrder);
            cursorLoader.setUpdateThrottle(150);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mEntriesCursorAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mEntriesCursorAdapter.swapCursor(Constants.EMPTY_CURSOR);
        }
    };
    private final OnSharedPreferenceChangeListener mPrefListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (PrefUtils.IS_REFRESHING.equals(key)) {
                refreshSwipeProgress();
            }
        }
    };
    private int mNewEntriesNumber, mOldUnreadEntriesNumber = -1;
    private boolean mAutoRefreshDisplayDate = false;
    private final LoaderManager.LoaderCallbacks<Cursor> mEntriesNumberLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            CursorLoader cursorLoader = new CursorLoader(getActivity(), mCurrentUri, new String[]{"SUM(" + FeedData.EntryColumns.FETCH_DATE + '>' + mListDisplayDate + ")", "SUM(" + FeedData.EntryColumns.FETCH_DATE + "<=" + mListDisplayDate + Constants.DB_AND + FeedData.EntryColumns.WHERE_UNREAD + ")"}, null, null, null);
            cursorLoader.setUpdateThrottle(150);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            data.moveToFirst();
            mNewEntriesNumber = data.getInt(0);
            mOldUnreadEntriesNumber = data.getInt(1);

            if (mAutoRefreshDisplayDate && mNewEntriesNumber != 0 && mOldUnreadEntriesNumber == 0) {
                mListDisplayDate = new Date().getTime();
                restartLoaders();
            } else {
                refreshUI();
            }

            mAutoRefreshDisplayDate = false;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private Button mRefreshListBtn;
    private InterstitialAd interstitialAd;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        UiUtils.setPreferenceTheme(getActivity());
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);


       shared = getContext().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
       editor = shared.edit();
       // MobileAds.initialize(getActivity(),  getString(R.string.app_id));

        initInterstitialAds();

        if (savedInstanceState != null) {
            mCurrentUri = savedInstanceState.getParcelable(STATE_CURRENT_URI);
            mOriginalUri = savedInstanceState.getParcelable(STATE_ORIGINAL_URI);
            mShowFeedInfo = savedInstanceState.getBoolean(STATE_SHOW_FEED_INFO);
            mListDisplayDate = savedInstanceState.getLong(STATE_LIST_DISPLAY_DATE);

            mEntriesCursorAdapter = new EntriesCursorAdapter(getActivity(), mCurrentUri, Constants.EMPTY_CURSOR, mShowFeedInfo);
        }

      /*  interstitialAd = new InterstitialAd(getActivity());
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        AdRequest inter_adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(inter_adRequest);*/


    }

    @Override
    public void onStart() {
        super.onStart();
        refreshUI(); // Should not be useful, but it's a security
        refreshSwipeProgress();
        PrefUtils.registerOnPrefChangeListener(mPrefListener);

        mFab = getActivity().findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markAllAsRead();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                   if(interstitialAd.isAdLoaded()) {
                       interstitialAd.show();
                   }
                    }
                };
               new Handler().postDelayed(runnable, 3000);
            }
        });

        if (mCurrentUri != null) {
            // If the list is empty when we are going back here, try with the last display date
            if (mNewEntriesNumber != 0 && mOldUnreadEntriesNumber == 0) {
                mListDisplayDate = new Date().getTime();
            } else {
                mAutoRefreshDisplayDate = true; // We will try to update the list after if necessary
            }

            restartLoaders();
        }

        // First open => we open the drawer for you
        //if (PrefUtils.getBoolean(PrefUtils.FIRST_OPEN, true)) {
            fetchRss();
         fetchVersion();
       // }
    }

    @Override
    public View inflateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_entry_list, container, true);

        if (mEntriesCursorAdapter != null) {
            setListAdapter(mEntriesCursorAdapter);
        }

        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mListView.setOnTouchListener(new SwipeGestureListener(mListView.getContext()));

        if (PrefUtils.getBoolean(PrefUtils.DISPLAY_TIP, true)) {
            final TextView header = new TextView(mListView.getContext());
            header.setMinimumHeight(UiUtils.dpToPixel(70));
            int footerPadding = UiUtils.dpToPixel(10);
            header.setPadding(footerPadding, footerPadding, footerPadding, footerPadding);
            header.setText(R.string.tip_sentence);
            header.setGravity(Gravity.CENTER_VERTICAL);
            header.setCompoundDrawablePadding(UiUtils.dpToPixel(5));
            header.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_about, 0, R.drawable.ic_action_cancel, 0);
            header.setClickable(true);
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListView.removeHeaderView(header);
                    PrefUtils.putBoolean(PrefUtils.DISPLAY_TIP, false);
                }
            });
            mListView.addHeaderView(header);
        }

        AdView adView = new AdView(getActivity(),
                getString(R.string.fb_test_ad) +
                        getString(R.string.fb_banner_placement_id), AdSize.BANNER_HEIGHT_90);
        mListView.addHeaderView(adView);
        mListView.addFooterView(adView);
        adView.loadAd();

      /*  AdView mAdView = new AdView(getActivity());//findViewById(R.id.adView);
        mAdView.setAdSize(AdSize.LARGE_BANNER);
        mAdView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);*/

     /*   RelativeLayout relativeLayout = new RelativeLayout(getActivity());
        RelativeLayout.LayoutParams adViewParams = new RelativeLayout.LayoutParams
                (AdView.LayoutParams.MATCH_PARENT, AdView.LayoutParams.MATCH_PARENT);
        //adViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adViewParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);*/

        // Load an ad into the AdMob banner view.

       // relativeLayout.addView(mAdView, adViewParams);

       /* mListView.addHeaderView(mAdView);
        mListView.addFooterView(mAdView);*/

        UiUtils.addEmptyFooterView(mListView, 90);

        mRefreshListBtn = rootView.findViewById(R.id.refreshListBtn);
        mRefreshListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNewEntriesNumber = 0;
                mListDisplayDate = new Date().getTime();

                refreshUI();
                if (mCurrentUri != null) {
                    restartLoaders();
                }
            }
        });

        //disableSwipe();

 /*       RelativeLayout relativeLayout = new RelativeLayout(getActivity());
        RelativeLayout.LayoutParams adViewParams = new RelativeLayout.LayoutParams
                (AdView.LayoutParams.MATCH_PARENT, AdView.LayoutParams.WRAP_CONTENT);
        adViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adViewParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        // Load an ad into the AdMob banner view.
        AdView mAdView = new AdView(getActivity());//findViewById(R.id.adView);
        mAdView.setAdSize(AdSize.LARGE_BANNER);
        mAdView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        relativeLayout.addView(mAdView, adViewParams);

        mListView.addView(mAdView);*/
        showMessage();
        return rootView;
    }


    @Override
    public void onStop() {
        PrefUtils.unregisterOnPrefChangeListener(mPrefListener);

        if (mJustMarkedAsReadEntries != null && !mJustMarkedAsReadEntries.isClosed()) {
            mJustMarkedAsReadEntries.close();
        }

        mFab = null;

        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(STATE_CURRENT_URI, mCurrentUri);
        outState.putParcelable(STATE_ORIGINAL_URI, mOriginalUri);
        outState.putBoolean(STATE_SHOW_FEED_INFO, mShowFeedInfo);
        outState.putLong(STATE_LIST_DISPLAY_DATE, mListDisplayDate);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRefresh() {
        startRefresh();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        if (id >= 0) { // should not happen, but I had a crash with this on PlayStore...
            Intent intent = new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(mCurrentUri, id));
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            //Toast.makeText(getContext(), mCurrentUri+"\n\n"+id, Toast.LENGTH_LONG).show();
          /*  Intent intent = new Intent(getActivity(), EntryActivity.class);
            intent.putExtra("mCurrentUri", ContentUris.withAppendedId(mCurrentUri, id));
            startActivity(intent);*/
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear(); // This is needed to remove a bug on Android 4.0.3

        inflater.inflate(R.menu.entry_list, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        if (FeedData.EntryColumns.isSearchUri(mCurrentUri)) {
            searchItem.expandActionView();
            searchView.post(new Runnable() { // Without that, it just does not work
                @Override
                public void run() {
                    searchView.setQuery(mCurrentUri.getLastPathSegment(), false);
                    searchView.clearFocus();
                }
            });

        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    setData(mOriginalUri, true);
                } else {
                    setData(FeedData.EntryColumns.SEARCH_URI(newText), true, true);
                }
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                setData(mOriginalUri, true);
                return false;
            }
        });

        if (FeedData.EntryColumns.FAVORITES_CONTENT_URI.equals(mCurrentUri)) {
            menu.findItem(R.id.menu_refresh).setVisible(false);
        } else {
            menu.findItem(R.id.menu_share_starred).setVisible(false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_share_starred: {
                if (mEntriesCursorAdapter != null) {
                    String starredList = "";
                    Cursor cursor = mEntriesCursorAdapter.getCursor();
                    if (cursor != null && !cursor.isClosed()) {
                        int titlePos = cursor.getColumnIndex(FeedData.EntryColumns.TITLE);
                        int linkPos = cursor.getColumnIndex(FeedData.EntryColumns.LINK);
                        if (cursor.moveToFirst()) {
                            do {
                                starredList += cursor.getString(titlePos) + "\n" + cursor.getString(linkPos) + "\n\n";
                            } while (cursor.moveToNext());
                        }
                        startActivity(Intent.createChooser(
                                new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_favorites_title))
                                        .putExtra(Intent.EXTRA_TEXT, starredList).setType(Constants.MIMETYPE_TEXT_PLAIN), getString(R.string.menu_share)
                        ));

                        startRefresh();
                    }
                }
                return true;
            }
            case R.id.menu_refresh: {
                startRefresh();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void markAllAsRead() {
        if (mEntriesCursorAdapter != null) {
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.coordinator_layout), R.string.marked_as_read, Snackbar.LENGTH_LONG)
                    .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.light_theme_color_primary))
                    .setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new Thread() {
                                @Override
                                public void run() {
                                    if (mJustMarkedAsReadEntries != null && !mJustMarkedAsReadEntries.isClosed()) {
                                        ArrayList<Integer> ids = new ArrayList<>();
                                        while (mJustMarkedAsReadEntries.moveToNext()) {
                                            ids.add(mJustMarkedAsReadEntries.getInt(0));
                                        }
                                        ContentResolver cr = MainApplication.getContext().getContentResolver();
                                        String where = BaseColumns._ID + " IN (" + TextUtils.join(",", ids) + ')';
                                        cr.update(FeedData.EntryColumns.CONTENT_URI, FeedData.getUnreadContentValues(), where, null);

                                        mJustMarkedAsReadEntries.close();
                                    }
                                }
                            }.start();
                        }
                    });
            snackbar.getView().setBackgroundResource(R.color.material_grey_900);
            snackbar.show();

            new Thread() {
                @Override
                public void run() {
                    ContentResolver cr = MainApplication.getContext().getContentResolver();
                    String where = FeedData.EntryColumns.WHERE_UNREAD + Constants.DB_AND + '(' + FeedData.EntryColumns.FETCH_DATE + Constants.DB_IS_NULL + Constants.DB_OR + FeedData.EntryColumns.FETCH_DATE + "<=" + mListDisplayDate + ')';
                    if (mJustMarkedAsReadEntries != null && !mJustMarkedAsReadEntries.isClosed()) {
                        mJustMarkedAsReadEntries.close();
                    }
                    mJustMarkedAsReadEntries = cr.query(mCurrentUri, new String[]{BaseColumns._ID}, where, null, null);
                    cr.update(mCurrentUri, FeedData.getReadContentValues(), where, null);
                }
            }.start();

            // If we are on "all items" uri, we can remove the notification here
            if (mCurrentUri != null && Constants.NOTIF_MGR != null && (FeedData.EntryColumns.CONTENT_URI.equals(mCurrentUri) || FeedData.EntryColumns.UNREAD_ENTRIES_CONTENT_URI.equals(mCurrentUri))) {
                Constants.NOTIF_MGR.cancel(0);
            }
        }
    }

    private void startRefresh() {
        if (!PrefUtils.getBoolean(PrefUtils.IS_REFRESHING, false)) {
            if (mCurrentUri != null && FeedDataContentProvider.URI_MATCHER.match(mCurrentUri) == FeedDataContentProvider.URI_ENTRIES_FOR_FEED) {
                getActivity().startService(new Intent(getActivity(), FetcherService.class).setAction(FetcherService.ACTION_REFRESH_FEEDS).putExtra(Constants.FEED_ID,
                        mCurrentUri.getPathSegments().get(1)));
            } else {
                getActivity().startService(new Intent(getActivity(), FetcherService.class).setAction(FetcherService.ACTION_REFRESH_FEEDS));
            }
        }

        refreshSwipeProgress();
    }

    public Uri getUri() {
        return mOriginalUri;
    }

    public void setData(Uri uri, boolean showFeedInfo) {
        setData(uri, showFeedInfo, false);
    }

    private void setData(Uri uri, boolean showFeedInfo, boolean isSearchUri) {
        mCurrentUri = uri;
        if (!isSearchUri) {
            mOriginalUri = mCurrentUri;
        }

        mShowFeedInfo = showFeedInfo;

        mEntriesCursorAdapter = new EntriesCursorAdapter(getActivity(), mCurrentUri, Constants.EMPTY_CURSOR, mShowFeedInfo);
        setListAdapter(mEntriesCursorAdapter);

        mListDisplayDate = new Date().getTime();
        if (mCurrentUri != null) {
            restartLoaders();
        }
        refreshUI();
    }

    private void restartLoaders() {
        LoaderManager loaderManager = getLoaderManager();

        //HACK: 2 times to workaround a hard-to-reproduce bug with non-refreshing loaders...
        loaderManager.restartLoader(ENTRIES_LOADER_ID, null, mEntriesLoader);
        loaderManager.restartLoader(NEW_ENTRIES_NUMBER_LOADER_ID, null, mEntriesNumberLoader);

        loaderManager.restartLoader(ENTRIES_LOADER_ID, null, mEntriesLoader);
        loaderManager.restartLoader(NEW_ENTRIES_NUMBER_LOADER_ID, null, mEntriesNumberLoader);
    }

    private void refreshUI() {
        if (mNewEntriesNumber > 0) {
            mRefreshListBtn.setText(getResources().getQuantityString(R.plurals.number_of_new_entries, mNewEntriesNumber, mNewEntriesNumber));
            mRefreshListBtn.setVisibility(View.VISIBLE);
        } else {
            mRefreshListBtn.setVisibility(View.GONE);
        }
    }

    private void refreshSwipeProgress() {
        if (PrefUtils.getBoolean(PrefUtils.IS_REFRESHING, false)) {
            showSwipeProgress();
        } else {
            hideSwipeProgress();
        }
    }

    private class SwipeGestureListener extends SimpleOnGestureListener implements OnTouchListener {
        static final int SWIPE_MIN_DISTANCE = 120;
        static final int SWIPE_MAX_OFF_PATH = 150;
        static final int SWIPE_THRESHOLD_VELOCITY = 150;

        private final GestureDetector mGestureDetector;

        public SwipeGestureListener(Context context) {
            mGestureDetector = new GestureDetector(context, this);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mListView != null && e1 != null && e2 != null && Math.abs(e1.getY() - e2.getY()) <= SWIPE_MAX_OFF_PATH && Math.abs(velocityX) >= SWIPE_THRESHOLD_VELOCITY) {
                long id = mListView.pointToRowId(Math.round(e2.getX()), Math.round(e2.getY()));
                int position = mListView.pointToPosition(Math.round(e2.getX()), Math.round(e2.getY()));
                View view = mListView.getChildAt(position - mListView.getFirstVisiblePosition());

                if (view != null) {
                    // Just click on views, the adapter will do the real stuff
                    if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
                        mEntriesCursorAdapter.toggleReadState(id, view);
                    } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
                        mEntriesCursorAdapter.toggleFavoriteState(id, view);
                    }

                    // Just simulate a CANCEL event to remove the item highlighting
                    mListView.post(new Runnable() { // In a post to avoid a crash on 4.0.x
                        @Override
                        public void run() {
                            MotionEvent motionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
                            mListView.dispatchTouchEvent(motionEvent);
                            motionEvent.recycle();
                        }
                    });
                    return true;
                }
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }
    }


    //Fetch Rss
    public void fetchRss() {
        // Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        // Http Request Params Object
        RequestParams params = new RequestParams();
        if (PrefUtils.getBoolean(PrefUtils.FIRST_OPEN, true)) {
            ISPROGRESSIVE = true;
            showSwipeProgress();
        }
        client.post(URL_TOPICS, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
               if(!getActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
                        .getString("response", response).equals(response)){
                   syncJSON(response);
                }
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content) {
                if (statusCode == 404) {
                    fetchRss();
                    //Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    fetchRss();
                    //Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                } else {
                    fetchRss();
                    //Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]",
                    //	Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public void fetchVersion() {
        AsyncHttpClient client = new AsyncHttpClient();
        // Http Request Params Object
        RequestParams params = new RequestParams();
        client.post(URL_VERSION, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
              syncVersion(response);
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content) {
                if (statusCode == 404) {
                   fetchVersion();
                    //Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                   fetchVersion();
                    //Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                } else {
                   fetchVersion();
                    //Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]",
                    //	Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void showToast(String str){
        Toast.makeText(getActivity(), str, Toast.LENGTH_LONG).show();
    }

    public void  syncJSON(String response){
        try {
            // Extract JSON array from the response
            JSONArray arr = new JSONArray(response);
            //only load if there are changes

                if (arr.length() != 0) {
                    // clearApplicationData();
                    // Loop through each array element, get JSON object which has userid and username
                    for (int i = 0; i < arr.length(); i++) {
                        // Get JSON object
                        JSONObject object = (JSONObject) arr.get(i);
                        try {

                            String title = object.getString("title").replaceAll("'", "''");
                            String url = object.getString("url").replaceAll("'", "''");

                            editor.putString("version", object.getString("version")
                                    .replaceAll("'", "''"));
                            //showToast(object.getString("version")+" from fetch");
                            editor.putString("features", object.getString("features")
                                    .replaceAll("'", "''"));
                            editor.putString("response", response);

                            editor.commit();

                           /* if(pref.getString("response", null).contains(title) ||
                                    pref.getString("response", null).contains(url))*/
                            FeedDataContentProvider.addFeed(getActivity(), url, title, true);
                            //FeedDataContentProvider.addFeed(this, "http://archive.org/services/collection-rss.php", "Archive", true);
                            //this.getLoaderManager().initLoader(0, null, this);
                            AutoRefreshService.initAutoRefresh(getActivity());


                        } catch (JSONException e) {
                            // Toast.makeText(getApplicationContext(), "Error now... "+e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    if(ISPROGRESSIVE) {
                        hideSwipeProgress();
                    }
                    getActivity().startService(new Intent(getActivity(), FetcherService.class).setAction(FetcherService.ACTION_REFRESH_FEEDS));
                   /* while (mListView.getAdapter().getCount()==0) {
                        mListView.invalidateViews();
                    }*/
                   //mEntriesCursorAdapter.notifyDataSetChanged();

            }
        } catch (JSONException e) {}
    }



    public void  syncVersion(String response){
                    try {
                        JSONObject object = new JSONObject(response);
                        String version = object.getString("version").replaceAll("'", "''");
                        String features = object.getString("features").replaceAll("'", "''");

                        //showToast(version);

                        editor.putString("version", version);
                        //showToast(object.getString("version")+" from fetch");
                        editor.putString("features", features);
                        editor.commit();

                    } catch (JSONException e) {
                       // showToast(e.getMessage());
                    }
    }


    private final String TAG = EntriesListFragment.class.getSimpleName();
    public void initInterstitialAds(){
        // Instantiate an InterstitialAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
        interstitialAd = new InterstitialAd(getContext(), getString(R.string.fb_test_ad)+
                getString(R.string.fb_interstitial_placement_id));
        // Set listeners for the Interstitial Ad
        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial ad displayed callback
                Log.e(TAG, "Interstitial ad displayed.");
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
                Log.e(TAG, "Interstitial ad dismissed.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                Log.e(TAG, "Interstitial ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
                // Show the ad
                //interstitialAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                Log.d(TAG, "Interstitial ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
                Log.d(TAG, "Interstitial ad impression logged!");
            }
        });

        // For auto play video ads, it's recommended to load the ad
        // at least 30 seconds before it is shown
        interstitialAd.loadAd();
    }

    public void showMessage(){
        String version =  "noversion";
        PackageManager manager = getActivity().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            version = info.versionName;

        } catch (PackageManager.NameNotFoundException unused) {
          version = "noversion";
        }
        //showToast(shared.getString("version", "noversion")+" vs real version is "+version);

        if(!version.equals("noversion")
                && !shared.getString("version", "noversion").equals(version)
                && !shared.getString("version", "noversion").equals("noversion")) {
            CFAlertDialog.Builder builder2 = new CFAlertDialog.Builder(getActivity())
                    .setDialogStyle(CFAlertDialog.CFAlertStyle.NOTIFICATION)
                    .setTitle("New Update!")
                    .setCornerRadius(32)
                    .setTextGravity(Gravity.LEFT)
                    .setTextColor(Color.BLACK)
                    //.setHeaderView(imageView)
                    .setIcon(R.drawable.logo)
                    .setMessage(shared.getString("features", "New features added, bug fixes..."))
                    .addButton("UPDATE NOW",-1, Color.parseColor("#4285F4"),
                            CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                            (dialog, which) -> {
                                dialog.dismiss();
                                updateApp();
                            })
                    .addButton("CANCEL",-1, -1,
                            CFAlertDialog.CFAlertActionStyle.DEFAULT, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                            (dialog, which) -> {
                                //updateApp();
                                dialog.dismiss();
                            });
            // Show the alert
            builder2.show();
        }




       // }

    }


    public void updateApp(){
        Uri rateLink = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Uri rateLinkNotFound = Uri.parse("https://play.google.com/store/apps/details?id=" + getActivity().getPackageName());
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, rateLink);
        Intent rateNotFound = new Intent(Intent.ACTION_VIEW, rateLinkNotFound);
        try{
            startActivity(rateIntent);
        }catch (ActivityNotFoundException e){
            startActivity(rateNotFound);
        }
    }

}
