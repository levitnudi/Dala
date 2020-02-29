/**
 * Flym
 * <p/>
 * Copyright (c) 2012-2015 Frederic Julian
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package yali.org.activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;
import yali.org.R;
import yali.org.fragment.EntryFragment;
import yali.org.utils.PrefUtils;
import yali.org.utils.UiUtils;

public class EntryActivity extends BaseActivity implements MaterialIntroListener {

    private EntryFragment mEntryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.setPreferenceTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        //MobileAds.initialize(this,  getString(R.string.app_id));

        //initFBAds();
        initBannerAds();
        //initNativeAds();
        //initInterstitialAds();

       /* rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        rewardedVideoAd.loadAd(getString(R.string.video_ad_unit_id), new AdRequest.Builder().build());*/


        mEntryFragment = (EntryFragment) getSupportFragmentManager().findFragmentById(R.id.entry_fragment);
    /*    getSupportFragmentManager()
                .beginTransaction()
                .detach(mEntryFragment)
                .attach(mEntryFragment)
                .commit();*/
        if (savedInstanceState == null) { // Put the data only the first time (the fragment will save its state)
            mEntryFragment.setData(getIntent().getData());

        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (PrefUtils.getBoolean(PrefUtils.DISPLAY_ENTRIES_FULLSCREEN, false)) {
            setImmersiveFullScreen(true);
        }

        /*mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        AdRequest inter_adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(inter_adRequest);*/



        // Load an ad into the AdMob banner view.
       /* mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);*/

        //Toast.makeText(this, "Success!", Toast.LENGTH_LONG).show();

        SharedPreferences shared = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        //final String MENU_FIRST_EARNING = "about_tag";
        if(shared.getBoolean("firstEarn", true)) {
            if(!shared.getString("ref1", "opt-out").equals("opt-out")) {
                //showIntro(mAdView, MENU_FIRST_EARNING, getString(R.string.earn_message), FocusGravity.CENTER);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Earning Activated")
                        .setMessage(getString(R.string.earn_message));

                builder.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();

                editor.putBoolean("firstEarn", false);
                editor.commit();
            }
        }

    }

    @Override
    public void onUserClicked(String materialIntroViewId) {
        switch (materialIntroViewId) {
            // case MENU_SEARCH_ID_TAG:
            //  showIntro(search_item.getActionView(), MENU_FIRST_EARNING, getString(R.string.afririse_intro), FocusGravity.LEFT);
            //   break;
           /* case MENU_FIRST_EARNING:
                showIntro(refresh_item.getActionView(), MENU_FIRST_EARNING, getString(R.string.intro_refresh), FocusGravity.LEFT);
                break;
            case MENU_MARK_ID_TAG:
                showIntro(mark_status, MENU_MARK_ID_TAG, getString(R.string.intro_mark), FocusGravity.LEFT);
                break;*/
            default:
                break;
        }
    }

    public void showIntro(View view, String id, String text, FocusGravity focusGravity) {
        new MaterialIntroView.Builder(this)
                .enableDotAnimation(true)
                .setFocusGravity(focusGravity)
                .setFocusType(Focus.MINIMUM)
                .setDelayMillis(100)
                .enableFadeAnimation(true)
                .performClick(true)
                .setInfoText(text)
                .setTarget(view)
                .setListener(this)
                .setUsageId(id)
                .show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            //rewardedVideoAd.show();
           // Bundle b = getIntent().getExtras();
            //if (b != null && b.getBoolean(Constants.INTENT_FROM_WIDGET, false)) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
           // }
            finish();
            return true;
        }/*else if(item.getItemId()==R.id.favorite_icon){
          rewardedVideoAd.show();
        }*/

        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mEntryFragment.setData(intent.getData());
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        /*if(interstitialAd!=null) {
            interstitialAd.destroy();
        }*/
    }

  /*  @Override
    public void onPause(){
        super.onPause();
    }*/


   /* @Override
    public void onResume(){
        super.onResume();
        UiUtils.setPreferenceTheme(this);
    }*/

   /* @Override
    public void onStart(){
        super.onStart();
        UiUtils.setPreferenceTheme(this);
    }*/

/*    public void initNativeAds(){
    // Instantiate a NativeBannerAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
        nativeBannerAd = new NativeBannerAd(this, getString(R.string.fb_test_ad)+getString(R.string.fb_native_placement_id));
        nativeBannerAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Native ad is loaded and ready to be displayed
                Log.d(TAG, "Native ad is loaded and ready to be displayed!");
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Native ad clicked
                Log.d(TAG, "Native ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
                Log.d(TAG, "Native ad impression logged!");
            }
        });
        // load the ad
        nativeBannerAd.loadAd();


    }*/


    public void initBannerAds(){
        // Instantiate an AdView object.
        // NOTE: The placement ID from the Facebook Monetization Manager identifies your App.
        // To get test ads, add IMG_16_9_APP_INSTALL# to your placement id. Remove this when your app is ready to serve real ads.

        AdView adView = new AdView(this,
                getString(R.string.fb_test_ad) +
                        getString(R.string.fb_banner_placement_id), AdSize.BANNER_HEIGHT_90);

        // Find the Ad Container
        LinearLayout adContainer = findViewById(R.id.banner_container);

        // Add the ad view to your activity layout
        adContainer.addView(adView);

        // Request an ad
        adView.loadAd();
    }


  /*  public void initInterstitialAds(){
        // Instantiate an InterstitialAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
        interstitialAd = new InterstitialAd(this, getString(R.string.fb_test_ad)+getString(R.string.fb_interstitial_placement_id));
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
               // interstitialAd.show();
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
*/
 }