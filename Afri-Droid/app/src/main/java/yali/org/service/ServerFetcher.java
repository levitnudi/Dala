/*
package yali.org.service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.preference.PreferenceManager;
//import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import yali.org.R;
import yali.org.provider.FeedData;
import yali.org.provider.FeedDataContentProvider;
import yali.org.utils.PrefUtils;

import static yali.org.Constants.URL_TOPICS;

public class ServerFetcher extends Service {
    SQLiteDatabase db;
    //String URL_MSGS = "https://www.api.afririse.com/2show/rss.txt";
    boolean IS_EXIST = false, IS_MSG=false;
    Context context;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    public ServerFetcher() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
       // Toast.makeText(this, "Service was Created", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onStart(Intent intent, int startId) {
        context = getApplicationContext();
        pref =  getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        editor = pref.edit();
        fetchRss();

    }

    @Override
    public void onDestroy() {
        //System.exit(0);
    }

    private void startRefresh() {
        if (!PrefUtils.getBoolean(PrefUtils.IS_REFRESHING, false)) {
                startService(new Intent(this, FetcherService.class).setAction(FetcherService.ACTION_REFRESH_FEEDS));
             }
    }


    //Fetch Rss
    public void fetchRss() {
        // Create AsycHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        // Http Request Params Object
        RequestParams params = new RequestParams();
        client.post(URL_TOPICS, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                syncJSON(response);
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content) {
                if (statusCode == 404) {
                    //fetchRss();
                    //Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    //fetchRss();
                    //Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                } else {
                    //fetchRss();
                    //Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]",
                    //	Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void  syncJSON(String response){
        ArrayList<HashMap<String, String>> usersynclist;
        try {
             // Extract JSON array from the response
            JSONArray arr = new JSONArray(response);
            //only load if there are changes
            if(!response.equals(pref.getString("response", null))) {

                if (arr.length() != 0) {
                 // clearApplicationData();
                    // Loop through each array element, get JSON object which has userid and username
                    for (int i = 0; i < arr.length(); i++) {
                        // Get JSON object
                        JSONObject object = (JSONObject) arr.get(i);
                        try {

                            String title = object.getString("title").replaceAll("'", "''");
                            String url = object.getString("url").replaceAll("'", "''");

                            editor.putString("version", object.getString("version").replaceAll("'", "''")).apply();

                            editor.putString("features", object.getString("features").replaceAll("'", "''")).apply();


                           */
/* if(pref.getString("response", null).contains(title) ||
                                    pref.getString("response", null).contains(url))*//*

                            FeedDataContentProvider.addFeed(this, url, title, true);
                            //FeedDataContentProvider.addFeed(this, "http://archive.org/services/collection-rss.php", "Archive", true);
                            //this.getLoaderManager().initLoader(0, null, this);
                            //AutoRefreshService.initAutoRefresh(this);

                            editor.putString("response", response);
                            editor.commit();

                        } catch (JSONException | OutOfMemoryError e) {
                            // Toast.makeText(getApplicationContext(), "Error now... "+e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                }
            }
        } catch (JSONException e) {}
    }


   */
/* public void clearApplicationData()
    {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));//Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }*//*


   */
/* public static boolean deleteDir(File dir)
    {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public void trimCache() {
        try {
            File dir = getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }*//*



}*/
