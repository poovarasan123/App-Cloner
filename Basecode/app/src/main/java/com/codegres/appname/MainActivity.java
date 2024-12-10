package com.codegres.appname;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    WebView webView;
    TextView textView;
    DrawerLayout drawer;
    Button retrybtn;
    AdView adView;
    NavigationView navigationView;
    private InterstitialAd interstitialAd;
    private boolean exit = false;
    private Handler handler;
    private Runnable adRunnable;
    private final long AD_INTERVAL_MS = 60000;
    private ValueCallback<Uri[]> uploadMessage;
    private static final int FILECHOOSER_RESULTCODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        webView = findViewById(R.id.webview);
        textView = findViewById(R.id.textview);
        retrybtn = findViewById(R.id.retrybtn);
        adView = findViewById(R.id.adView);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        if (getResources().getBoolean(R.bool.isBannerAds)) {
            adView.setVisibility(View.VISIBLE);
        } else {
            adView.setVisibility(View.GONE);
        }
        if (getResources().getBoolean(R.bool.isPortfolio)) {
            navigationView.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
        } else {
            navigationView.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);
        }

        checkConnection(); //method to check connection

        handler = new Handler();

        // Runnable to load and show interstitial ad
        adRunnable = new Runnable() {
            @Override
            public void run() {
                if (getResources().getBoolean(R.bool.isInterstitialAds)) {
                    loadAndShowAd();
                }

                handler.postDelayed(this, AD_INTERVAL_MS);
            }
        };

        handler.post(adRunnable);


        //Runtime External storage permission for saving download files
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_DENIED) {
//                Log.d("permission", "permission denied to WRITE_EXTERNAL_STORAGE - requesting it");
//                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
//                requestPermissions(permissions, 1);
//            }
//        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        retrybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConnection();
            }
        });
    }

    private void loadAndShowAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        if (getResources().getBoolean(R.bool.isInterstitialAds)) {
            InterstitialAd.load(MainActivity.this,
                    getResources().getString(R.string.interstitialAdId), adRequest, new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            Log.d("TAG", adError.toString());
                            interstitialAd = null;
                        }

                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd minterstitialAd) {
                            Log.d("TAG", "Ad was loaded....");
                            interstitialAd = minterstitialAd;
                            if (interstitialAd != null) {
                                interstitialAd.show(MainActivity.this);
                            }
                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_Email) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"naveen@codegres.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
            intent.putExtra(Intent.EXTRA_TEXT, "Mail Body");
            startActivity(Intent.createChooser(intent, "Email Via"));
        }
        if (id == R.id.action_Contact) {
            String phone = "+917204847987";
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
            startActivity(intent);
        }
        if (id == R.id.refresh) {
            if (isOnline()) {
                webView.loadUrl(webView.getUrl());
            } else {
                Toast.makeText(this, "Can't Connect to Internet.", Toast.LENGTH_SHORT).show();
            }
        }
        if (id == R.id.share) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, "Download Paint MS Version from https://play.google.com/store/apps/details?id=com.codegres.paint");
            startActivity(Intent.createChooser(i, "Share Via"));
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //paste your Social Media ID's here.
        String fb = "https://codegres.com";
        String twt = "https://codebuy.org";
        String ig = "https://codegres.org/free";
        String lin = "https://www.linkedin.com/in/naveen-codegres";
        String amzn = "https://play.google.com/store/apps/details?id=com.codegres.paintpro";
        String flpkrt = "tel:+917204847987";
        int id = item.getItemId();
        if (id == R.id.nav_facebook) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fb)));
        } else if (id == R.id.nav_twitter) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(twt)));
        } else if (id == R.id.nav_linkedin) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(lin)));
        } else if (id == R.id.nav_insta) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ig)));
        } else if (id == R.id.nav_amazon) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(amzn)));
        } else if (id == R.id.nav_flipkart) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(flpkrt)));
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                if (exit) {
                    next();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Press again to exit.", Toast.LENGTH_SHORT).show();
                }
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        exit = false;
                    }
                }, 2000);
                exit = true;
            }
        }
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }


    private void checkConnection() {
        if (isOnline()) {
            WebSettings webSettings = webView.getSettings();
            // Enable cookies
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            //cookieManager.setAcceptThirdPartyCookies(webView, true);
            // Other settings
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);


            // Allow content access
            webSettings.setAllowContentAccess(true);

            // Allow file access
            webSettings.setAllowFileAccess(true);

            // Allow file access from file URLs
            webSettings.setAllowFileAccessFromFileURLs(true);

            // Allow universal access from file URLs
            webSettings.setAllowUniversalAccessFromFileURLs(true);

            // Enable zoom controls
            webSettings.setBuiltInZoomControls(true);

            // Display the built-in zoom controls
            webSettings.setDisplayZoomControls(true);

            // Enable mixed content
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }

            webView.getSettings().setDefaultTextEncodingName("utf-8");


            webView.setWebViewClient(new WebViewClient());
            webView.setWebChromeClient(new WebChromeClient() {
                // For Android 5.0+
                @Override
                public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                    uploadMessage = filePathCallback;
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    startActivityForResult(Intent.createChooser(intent, ""), FILECHOOSER_RESULTCODE);
                    return true;
                }
            });


            webView.loadUrl("https://google.com");

            JavaScriptInterface javascriptInterface = new JavaScriptInterface(getApplicationContext());
            webView.addJavascriptInterface(new JavaScriptInterface(getApplicationContext()), "Android");
            webView.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String url, String userAgent,
                                            String contentDisposition, String mimeType,
                                            long contentLength) {
                    try {
                        if (url.startsWith("blob:")) {
                            webView.evaluateJavascript(javascriptInterface.getBase64StringFromBlobUrl(url), null);
                        }else{
                            System.out.println("Download URL: " + url);
                            javascriptInterface.processBase64Data(url);
                        }

                    } catch (Exception e) {
                        System.out.println("Exception: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                }

            });

            textView.setVisibility(View.INVISIBLE);
            retrybtn.setEnabled(false);
            retrybtn.setVisibility(View.INVISIBLE);
        } else {
            Toast.makeText(this, "Can't Connect to Internet.", Toast.LENGTH_SHORT).show();
            webView.loadUrl("file:///android_asset/offline.html");
            retrybtn.setEnabled(true);
        }
    }

    public void next() {
//        if(interstitialAd.isLoaded()) {
//            interstitialAd.show();
//        }else {
        finish();
        //   }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (uploadMessage != null) {
                Uri[] result = data == null || resultCode != RESULT_OK ? null : new Uri[]{data.getData()};
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        }
    }


    //fullscreen videos
    private static class Browser_Home extends WebViewClient {
        Browser_Home() {
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    private class ChromeClient extends WebChromeClient {
        private View customview;
        private CustomViewCallback customviewcallback;
        private int originalorientation;
        private int originalsystemvisibility;

        ChromeClient() {
        }

        public Bitmap getDefaultVideoPoster() {
            if (customview == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView() {
            ((FrameLayout) getWindow().getDecorView()).removeView(this.customview);
            this.customview = null;
            getWindow().getDecorView().setSystemUiVisibility(this.originalsystemvisibility);
            setRequestedOrientation(this.originalorientation);
            this.customviewcallback.onCustomViewHidden();
            this.customviewcallback = null;
        }

        public void onShowCustomView(View paramView, CustomViewCallback paramCustomViewCallback) {
            if (this.customview != null) {
                onHideCustomView();
                return;
            }
            this.customview = paramView;
            this.originalsystemvisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.originalorientation = getRequestedOrientation();
            this.customviewcallback = paramCustomViewCallback;
            ((FrameLayout) getWindow().getDecorView()).addView(this.customview, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }
}
