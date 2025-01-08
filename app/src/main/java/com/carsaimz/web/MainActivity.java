package com.carsaimz;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.*;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String BASE_URL = "https://carsaimz.blogspot.com";
    private static final int PERMISSION_REQUEST_CODE = 1234;
    
    private WebView webView;
    private ProgressBar progressBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;
    private FirebaseAnalytics firebaseAnalytics;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private DownloadManager downloadManager;
    private long lastDownloadId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        initializeFirebase();
        initializeDownloadManager();
        setupWebView();
        setupNavigationBar();
        setupDrawerMenu();
    }

    private void initializeViews() {
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void initializeDownloadManager() {
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
    }

    private void initializeFirebase() {
        // Initialize Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize and setup Remote Config
        setupRemoteConfig();

        // Setup Firebase Cloud Messaging
        setupCloudMessaging();
    }

    private void setupRemoteConfig() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        // Set default Remote Config values
        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put("maintenance_mode", false);
        defaults.put("welcome_message", "Bem-vindo ao CarsaiMz! Aguarde enquanto a Página está sendo carregada...");
        defaults.put("enable_new_features", false);
        firebaseRemoteConfig.setDefaultsAsync(defaults);

        // Fetch and activate Remote Config values
        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        applyRemoteConfig();
                    } else {
                        Log.e(TAG, "Remote config fetch failed");
                    }
                });
    }

    private void applyRemoteConfig() {
        boolean maintenanceMode = firebaseRemoteConfig.getBoolean("maintenance_mode");
        String welcomeMessage = firebaseRemoteConfig.getString("welcome_message");
        boolean newFeaturesEnabled = firebaseRemoteConfig.getBoolean("enable_new_features");

        if (maintenanceMode) {
            Toast.makeText(this, "App em manutenção", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show();
        }

        if (newFeaturesEnabled) {
            enableNewFeatures();
        }
    }

    private void setupCloudMessaging() {
        FirebaseMessaging.getInstance().subscribeToTopic("news")
                .addOnCompleteListener(task -> {
                    String msg = task.isSuccessful() ? "Subscribed to news" : "Subscribe failed";
                    Log.d(TAG, msg);
                });
    }

    private void setupWebView() {
        configureWebViewSettings();
        configureWebViewClient();
        configureWebChromeClient();
        webView.loadUrl(BASE_URL);
    }

    private void configureWebViewSettings() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    }

    private void configureWebViewClient() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                
                // Log navigation to Firebase Analytics
                logNavigationEvent(url);

                // Handle special URLs
                if (shouldOpenInExternalApp(url)) {
                    openInExternalApp(url);
                    return true;
                }
                
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, 
                                      WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    handleWebViewError(error.getErrorCode());
                }
            }
        });

        // Configure DownloadListener
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            if (checkStoragePermission()) {
                startDownload(url, contentDisposition, mimetype);
            } else {
                requestStoragePermission();
            }
        });
    }

    private void configureWebChromeClient() {
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(TAG, "Console: " + consoleMessage.message());
                return true;
            }
        });
    }

    private boolean shouldOpenInExternalApp(String url) {
        return url.startsWith("tel:") || 
               url.startsWith("mailto:") || 
               url.startsWith("whatsapp:") ||
               url.contains("mediafire.com") ||
               url.contains("play.google.com") ||
               url.endsWith(".apk");
    }

    private void openInExternalApp(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening external app", e);
            Toast.makeText(this, "Erro ao abrir link externo", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão concedida. Por favor, tente baixar novamente.", 
                    Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Permissão necessária para downloads", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startDownload(String url, String contentDisposition, String mimeType) {
        String filename = URLUtil.guessFileName(url, contentDisposition, mimeType);
        
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
            .setTitle(filename)
            .setDescription("Baixando arquivo...")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true);

        try {
            lastDownloadId = downloadManager.enqueue(request);
            Toast.makeText(this, "Download iniciado", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao iniciar download", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error starting download", e);
        }
    }

    private void logNavigationEvent(String url) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, url);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
    }

    private void handleWebViewError(int errorCode) {
        String errorMessage;
        switch (errorCode) {
            case WebViewClient.ERROR_HOST_LOOKUP:
                errorMessage = "Sem conexão com a internet";
                break;
            case WebViewClient.ERROR_TIMEOUT:
                errorMessage = "Tempo de conexão esgotado";
                break;
            default:
                errorMessage = "Erro ao carregar a página";
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void setupNavigationBar() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_back) {
                if (webView.canGoBack()) webView.goBack();
                return true;
            } else if (itemId == R.id.nav_refresh) {
                webView.reload();
                return true;
            } else if (itemId == R.id.nav_forward) {
                if (webView.canGoForward()) webView.goForward();
                return true;
            } else if (itemId == R.id.nav_menu) {
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
            return false;
        });
    }

    private void setupDrawerMenu() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_about) {
                loadLocalHtml("about.html");
            } else if (itemId == R.id.nav_contact) {
                loadLocalHtml("contact.html");
            } else if (itemId == R.id.nav_thanks) {
                loadLocalHtml("thanks.html");
            } else if (itemId == R.id.nav_blog_mods) {
                webView.loadUrl("https://carsaimods.blogspot.com");
            } else if (itemId == R.id.nav_support) {
                loadLocalHtml("support.html");
            } else if (itemId == R.id.nav_report) {
                loadLocalHtml("report.html");
            } else if (itemId == R.id.nav_bot) {
                webView.loadUrl("https://bots.easy-peasy.ai/bot/64a9676d-62fc-4261-917a-dcdb1cff2280");
            } else if (itemId == R.id.nav_home) {
                webView.loadUrl("https://carsaimz.blogspot.com");
            } else if (itemId == R.id.nav_our_book) {
                loadLocalHtml("book.html");
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void loadLocalHtml(String fileName) {
        webView.loadUrl("file:///android_asset/" + fileName);
    }
  
    private void enableNewFeatures() {
        // Implementar novas funcionalidades aqui quando habilitadas via Remote Config
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }
}
