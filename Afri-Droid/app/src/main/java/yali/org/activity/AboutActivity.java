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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;
import yali.org.R;
import yali.org.utils.UiUtils;

public class AboutActivity extends BaseActivity {
    //private GifImageView gifImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.setPreferenceTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

       //initFBAds();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setLogo(R.mipmap.ic_launcher);
       // getSupportActionBar().setDisplayUseLogoEnabled(true);

        String title;
        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            title = "Version " + info.versionName;
        } catch (NameNotFoundException unused) {
            title = getString(R.string.app_name);
        }

          /*gifImageView = (GifImageView) findViewById(R.id.GifImageView);
        gifImageView.setGifImageResource(R.drawable.ainimoto);*/
       /* GifView gifView1 = (GifView) findViewById(R.id.gif1);
        gifView1.setVisibility(View.VISIBLE);
        gifView1.play();
        gifView1.pause();
        //gifView1.setGifResource(R.drawable.ainimoto);
        gifView1.getGifResource();
        gifView1.play();*/
        // gifView1.setMovieTime(10000);
        // gifView1.getMovie();

        TextView titleView = findViewById(R.id.about_title);
        titleView.setText(title);
        String info = "<p><a href=\"https://www.afririse.com\"> <b>AfriRise</b></a> is your one stop shop for valuable information. We collect the latest information on " +
                "<b>Scholarships</b>, <b>Jobs & Internships</b>, <b>Investments</b>, <b>Latest News Alerts</b> near you and <b>Infortainment</b> from <b>trusted</b> sources" +
                "<p><a href=\"https://www.afririse.com\"> <b>AfriRise</b></a>, we <b>inform</b>, we <b>arise</b>, <b>together</b>...</p>"
                +"<p>"+"Follow us on<a href=\"https://facebook.com/afririse\"> <b>facebook</b></a> and "
                +"<a href=\"https://play.google.com/store/apps/details?id=yali.org\"><b>rate</b> </a>this app</p>";
        TextView contentView = findViewById(R.id.about_content);
        contentView.setText(Html.fromHtml(info));
        contentView.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.about_content:

               return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }
}

