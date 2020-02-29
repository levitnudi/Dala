/**
 * Flym
 *
 * Copyright (c) 2012-2013 Frederic Julian
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package yali.org;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.ads.MobileAds;

import com.facebook.ads.AudienceNetworkAds;
//import com.google.android.gms.ads.MobileAds;

import yali.org.utils.PrefUtils;
import yali.org.utils.UiUtils;


public class MainApplication extends Application {

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        // Initialize the Audience Network SDK
        AudienceNetworkAds.initialize(this);
        //MobileAds.initialize(this,  getString(R.string.app_id));
        PrefUtils.putBoolean(PrefUtils.IS_REFRESHING, false); // init
    }
}
