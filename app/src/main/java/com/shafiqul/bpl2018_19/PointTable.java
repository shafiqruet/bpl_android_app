package com.shafiqul.bpl2018_19;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class PointTable extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    HttpResponse httpResponse;
    Button button;
    WebView textView;
    JSONObject jsonObject = null;
    String StringHolder = "";
    private AdView mAdView;
    InterstitialAd mInterstitialAd;
    public static final String LIVE_POINT_TABLE = "MyPointScoresFile";

    String HttpURL = "http://ksslbd.com/temp/bpl_point_table.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Points Table");

        mAdView = findViewById(R.id.adView);
        AdRequest request = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                // Check the LogCat to get your test device ID
                //.addTestDevice("7913E282C5FF4C6DB46D6E7C56A2094E")
                .build();
        mAdView.loadAd(request);

        // remove code here
        /*mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
        mInterstitialAd.loadAd(request);*/
        // remove code here
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        textView = (WebView) findViewById(R.id.webView);
        if (haveNetworkConnection()) {
            new GetDataFromServerIntoTextView(PointTable.this).execute();
        } else {
            Toast.makeText(getApplicationContext(), "Please check your internet connection!", Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.color_statusbar));
        }

        SharedPreferences prefs = getSharedPreferences(LIVE_POINT_TABLE, MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreferences(LIVE_POINT_TABLE, MODE_PRIVATE).edit();

        int count = prefs.getInt("point_scores_count", 0);
        count++;
        if (count == 2) {

            // remove code here
           /* mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    showInterstitial();
                }
            });*/
            // remove code here
            editor.putInt("point_scores_count", 0);
            editor.apply();
        } else {
            editor.putInt("point_scores_count", count);
            editor.apply();
        }
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    // Declaring GetDataFromServerIntoTextView method with AsyncTask.
    public class GetDataFromServerIntoTextView extends AsyncTask<Void, Void, Void> {
        // Declaring CONTEXT.
        public Context context;

        private ProgressDialog mDialog;

        public GetDataFromServerIntoTextView(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(PointTable.this, "Please wait...", "", true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(HttpURL);
            try {
                httpResponse = httpClient.execute(httpPost);
                StringHolder = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                JSONArray jsonArray = new JSONArray(StringHolder);
                jsonObject = jsonArray.getJSONObject(0);


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            try {

                textView.loadDataWithBaseURL(null, jsonObject.getString("TextViewServerData"), "text/html", "utf-8", null);
                mDialog.dismiss();

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.rate_apps) {
            rateApp();
        }  else if(id == R.id.about_apps){
            Intent help = new Intent(this, AboutUs.class);
            startActivity(help);
        } else if (id == R.id.more_apps) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Techparkbd")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/developer?id=Techparkbd")));
            }
        } if (id == R.id.check_update) {
            rateApp();
        }

        return super.onOptionsItemSelected(item);
    }

    public void rateApp()
    {
        try
        {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        }
        catch (ActivityNotFoundException e)
        {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }

    private Intent rateIntentForUrl(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
        {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
        else
        {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent newAct = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(newAct);
        } else if (id == R.id.nav_about_bpl_2019) {
            Intent newAct = new Intent(getApplicationContext(), BPL_2018.class);
            startActivity(newAct);
        } else if (id == R.id.nav_about_bpl) {
            Intent newAct = new Intent(getApplicationContext(), About_bpl.class);
            startActivity(newAct);
        } else if (id == R.id.nav_bpl_statistics) {
            Intent newAct = new Intent(getApplicationContext(), BPLStatistics.class);
            startActivity(newAct);
        } else if (id == R.id.nav_live_scores) {
            Intent newAct = new Intent(getApplicationContext(), LiveScores.class);
            startActivity(newAct);
        } else if (id == R.id.nav_point_table) {
            Intent newAct = new Intent(getApplicationContext(), PointTable.class);
            startActivity(newAct);
        } else if (id == R.id.nav_schedule_part) {
            Intent newAct = new Intent(getApplicationContext(), MatchFixture.class);
            startActivity(newAct);
        } else if (id == R.id.nav_schedule_part_two) {
            Intent newAct = new Intent(getApplicationContext(), MatchFixtureTwo.class);
            startActivity(newAct);
        } else if (id == R.id.nav_team_squad) {
            Intent newAct = new Intent(getApplicationContext(), TeamSquad.class);
            startActivity(newAct);
        } else if (id == R.id.nav_exit_apps) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory( Intent.CATEGORY_HOME );
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}