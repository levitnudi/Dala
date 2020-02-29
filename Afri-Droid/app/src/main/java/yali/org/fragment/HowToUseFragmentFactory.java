package yali.org.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import yali.org.R;
import yali.org.utils.UiUtils;
public class HowToUseFragmentFactory extends Fragment {
 SharedPreferences shared;
 SharedPreferences.Editor editor;
 //private RewardedVideoAd rewardedVideoAd;
 private String ref1Text = "";
 private InterstitialAd interstitialAd;
 ProgressDialog pd;
    public static HowToUseFragmentFactory newInstance(int position) {
        Bundle args = new Bundle();
        HowToUseFragmentFactory fragment = new HowToUseFragmentFactory();
        args.putInt("htu_fragment", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UiUtils.setPreferenceTheme(getActivity());
        int layoutResource = getProperLayout(getArguments().getInt("htu_fragment", 0));
        View v = inflater.inflate(layoutResource, container, false);
        //MobileAds.initialize(getActivity(),  getString(R.string.app_id));

        /*interstitialAd = new InterstitialAd(getActivity());
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        AdRequest inter_adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(inter_adRequest);*/

        shared = getContext().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        editor = shared.edit();

        //initRewardedAds();
        //initInterstitialAds();


        if(getArguments().getInt("htu_fragment", 0)==2) {
            pd = new ProgressDialog(getActivity());
            pd.setMessage(getString(R.string.loading));
            pd.setCancelable(true);
            pd.setIndeterminate(true);

            //initRewardedVideo();
            initInterstitialAds();

            Button btnSave = v.findViewById(R.id.btnSave);
            Button btnOptOut = v.findViewById(R.id.btnOptOut);
            EditText ref1, ref2;
            ref1 = v.findViewById(R.id.refLink1);
            ref1.setText(shared.getString("ref1", "")
                    .replace("opt-out", ""));
            ref2 = v.findViewById(R.id.refLink2);
            ref2.setText(shared.getString("ref2", "")
                    .replace(getString(R.string.default_referral_id), ""));

            TextView tv = v.findViewById(R.id.termsAdfly);
            tv.setText(Html.fromHtml(getString(R.string.disclaimer)+" "+"<a href=\"https://ay.gy/terms\">READ TERMS AND CONDTIONS</a>"));
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
               public void onClick(View v) {

               if(!TextUtils.isEmpty(ref1.getText().toString())) {
                  ref1Text = ref1.getText().toString();
                   showDialog("Success!", "Adfly earning has been activated successfully. You will see Adfly ads when you click links in articles.");
                   editor.putString("ref1", ref1Text);
                   editor.commit();

                  /* AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                   builder.setTitle("Activate Adfly")
                           .setMessage(getString(R.string.reward_message));
                   builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int which) {

                       }
                   });
                   builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int which) {
                           //pd.show();
                           //initInterstitialAds();
                           *//*rewardedVideoAd.loadAd(getString(R.string.rewarded_ad_unit_id),
                                   new AdRequest.Builder().build());*//*


                           dialog.dismiss();
                           pd.show();
                       }
                   });

                   builder.show();*/

               }else {
                   editor.putString("ref1", "opt-out");
                   editor.commit();
               }

               if(!TextUtils.isEmpty(ref2.getText().toString())){
                        editor.putString("ref2", ref2.getText().toString());
                       editor.commit();
              }else {
                   editor.putString("ref2", getString(R.string.default_referral_id));
                   editor.commit();
               }
                }
            });


            btnOptOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ref1.setText(null);
                    ref2.setText(null);
                    editor.putString("ref1", "opt-out");
                    editor.putString("ref2", getString(R.string.default_referral_id));
                    editor.commit();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if(interstitialAd.isAdLoaded()) {
                                interstitialAd.show();
                            }
                        }
                    };
                    new Handler().postDelayed(runnable, 3000);
                    Toast.makeText(getContext(), "Link ads will not show", Toast.LENGTH_LONG).show();
                }
            });

        }else if(getArguments().getInt("htu_fragment", 0)==1){
            TextView tv = v.findViewById(R.id.textSignUp);
            tv.setText(Html.fromHtml(getString(R.string.welcome_2)+" "+"<a href=\"https://join-adf.ly/21012519\">Sign up here</a>"));
            tv.setMovementMethod(LinkMovementMethod.getInstance());

        }else if(getArguments().getInt("htu_fragment", 0)==3){
            Button btnShare = v.findViewById(R.id.btnShare);

            btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String appPackageName = getContext().getPackageName(); // getPackageName() from Context or Activity object
                    Uri url = Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName);
                    String redID = shared.getString("ref1", getString(R.string.default_referral_id)).replace("opt-out", "");
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Hi, I'm using "+getString(R.string.app_name)+" to make money from clicks. It's easy, " +
                            "download the app here "+url+"\n"+"1. Go to settings\n2. Enter my referral ID : "
                            +redID+"\nStart earning with AfriRise!");
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
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
        }

     //mInterstitialAd.show();
        return v;
    }

    int getProperLayout(int position){
        switch(position){
            case 0:
                return R.layout.htu_fragment1;
            case 1:
                return R.layout.htu_fragment2;
            case 2:
                return R.layout.htu_fragment3;
            case 3:
                return R.layout.htu_fragment4;
        }
        return 0;
    }
/*
    public void initRewardedAds(){
        // Instantiate a RewardedVideoAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
        rewardedVideoAd = new RewardedVideoAd(getActivity(), "VID_HD_16_9_46S_APP_INSTALL#"+getString(R.string.fb_rewarded_placement_id));
        rewardedVideoAd.setAdListener(new RewardedVideoAdListener() {
            @Override
            public void onError(Ad ad, AdError error) {
                // Rewarded video ad failed to load
                //Log.e(TAG, "Rewarded video ad failed to load: " + error.getErrorMessage());
                showDialog("Error!", "Adfly earning was not activated. Please check your internet and try again.");

            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Rewarded video ad is loaded and ready to be displayed
                //Log.d(TAG, "Rewarded video ad is loaded and ready to be displayed!");
                pd.hide();
                pd.cancel();
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Rewarded video ad clicked
                //Log.d(TAG, "Rewarded video ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Rewarded Video ad impression - the event will fire when the
                // video starts playing
                //Log.d(TAG, "Rewarded video ad impression logged!");
            }

            @Override
            public void onRewardedVideoCompleted() {
                // Rewarded Video View Complete - the video has been played to the end.
                // You can use this event to initialize your reward
                //Log.d(TAG, "Rewarded video completed!");

                // Call method to give reward
                // giveReward();
                editor.putString("ref1", ref1Text);
                editor.commit();
               showDialog("Success!", "Adfly earning has been activated successfully. You will see Adfly ads when you click links in articles.");
            }

            @Override
            public void onRewardedVideoClosed() {
                // The Rewarded Video ad was closed - this can occur during the video
                // by closing the app, or closing the end card.
                //Log.d(TAG, "Rewarded video ad closed!");
            }
        });
    }*/

    private void showDialog(String title, String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(msg);
        builder.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
                interstitialAd.loadAd();
            }
        });

        builder.show();
    }

    private final String TAG = HowToUseFragmentFactory.class.getSimpleName();
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
               // pd.cancel();
               // showDialog("Success!", "Adfly earning has been activated successfully. You will see Adfly ads when you click links in articles.");
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
                //pd.cancel();
                //showDialog("Error! "+adError.getErrorCode(), "AD loading failed! Please try again later.");
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
                // Show the ad
                //pd.cancel();
                interstitialAd.show();
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
        //interstitialAd.loadAd();
    }

/*
 public void initRewardedVideo(){
     rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getActivity());
     rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
         @Override
         public void onRewarded(RewardItem rewardItem) {
             //Toast.makeText(getContext(), "Saved!", Toast.LENGTH_LONG).show();
             pd.cancel();
             showDialog("Success!", "Adfly earning has been activated successfully. You will see Adfly ads when you click links in articles.");
             editor.putString("ref1", ref1Text);
             editor.commit();
         }//2880

         @Override
         public void onRewardedVideoAdLoaded() {
             pd.cancel();
             rewardedVideoAd.show();
         }

         @Override
         public void onRewardedVideoAdOpened() {
             // Toast.makeText(getActivity(), "Ad opened.", Toast.LENGTH_SHORT).show();
             pd.cancel();
         }

         @Override
         public void onRewardedVideoStarted() {
             //Toast.makeText(getBaseContext(), "Ad started.", Toast.LENGTH_SHORT).show();
             pd.cancel();
             // handler.postDelayed(runing, 1000);
         }

         @Override
         public void onRewardedVideoAdClosed() {
             pd.cancel();
             //Toast.makeText(getBaseContext(), "Ad closed.", Toast.LENGTH_SHORT).show();
             //playVideo();
             //handler.removeCallbacks(runing);
         }

         @Override
         public void onRewardedVideoAdLeftApplication() {
             pd.cancel();
             //   Toast.makeText(getActivity(), "Ad left application.", Toast.LENGTH_SHORT).show();
             // handler.removeCallbacks(runing);
         }

         @Override
         public void onRewardedVideoAdFailedToLoad(int i) {
             pd.cancel();
             showDialog("Error! "+i, "AD loading failed! Please try again later.");
         }
     });

 }*/
}
