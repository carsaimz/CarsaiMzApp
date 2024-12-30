package com.carsaimz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.carsaimz.BuildConfig;

public class SplashActivity extends AppCompatActivity {
  private ProgressBar progressBar;
  private ImageView logoImage;
  private TextView welcomeText;
  private TextView creditsText;
  private TextView versionText;
  private Handler handler;
  private int progress = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    initializeViews();
    setupProgressBar();
    setVersionText();
    simulateLoading();
  }

  private void initializeViews() {
    progressBar = findViewById(R.id.progressBar);
    logoImage = findViewById(R.id.logoImage);
    welcomeText = findViewById(R.id.welcomeText);
    creditsText = findViewById(R.id.creditsText);
    versionText = findViewById(R.id.versionText);
  }

  private void setupProgressBar() {
    progressBar.setMax(100);
    handler = new Handler(Looper.getMainLooper());
  }

  private void setVersionText() {
    String version = BuildConfig.VERSION_NAME;
    versionText.setText(getString(R.string.version_format, version));
  }

  private void simulateLoading() {
    Runnable progressRunnable =
        new Runnable() {
          @Override
          public void run() {
            if (progress < 100) {
              progress += 5;
              progressBar.setProgress(progress);
              handler.postDelayed(this, 100);
            } else {
              navigateToMainActivity();
            }
          }
        };
    handler.post(progressRunnable);
  }

  private void navigateToMainActivity() {
    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
    startActivity(intent);
    finish();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (handler != null) {
      handler.removeCallbacksAndMessages(null);
    }
  }
}
