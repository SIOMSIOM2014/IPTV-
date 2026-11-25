package com.iptvworld.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.graphics.Color;

public class MainActivity extends Activity {
  private WebView webView;
  private ProgressBar progress;
  private TextView errorView;
  private static final String PLAYER_URL = "https://siomsiom2014.github.io/IPTV-/?app=android";

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setStatusBarColor(Color.BLACK);
    getWindow().setNavigationBarColor(Color.BLACK);
    FrameLayout root = new FrameLayout(this);
    root.setBackgroundColor(Color.BLACK);
    webView = new WebView(this);
    webView.setBackgroundColor(Color.BLACK);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.addJavascriptInterface(new ExternalPlayerBridge(), "IPTVNative");
    webView.getSettings().setDomStorageEnabled(true);
    webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
    webView.getSettings().setLoadsImagesAutomatically(true);
    webView.setWebChromeClient(new WebChromeClient() {
      @Override public void onProgressChanged(WebView view, int newProgress) {
        progress.setProgress(newProgress);
        progress.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
      }
    });
    webView.setWebViewClient(new WebViewClient() {
      @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String host = request.getUrl().getHost();
        return host != null && !host.equals("siomsiom2014.github.io")
          && !host.equals("iptv-org.github.io") && !host.endsWith("jsdelivr.net");
      }
      @Override public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
        if (request.isForMainFrame()) {
          errorView.setVisibility(View.VISIBLE);
          errorView.setText("تعذر فتح المشغل. تحقق من اتصال الإنترنت ثم أعد المحاولة.");
        }
      }
    });
    progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
    progress.setMax(100);
    errorView = new TextView(this);
    errorView.setTextColor(Color.WHITE);
    errorView.setTextSize(16);
    errorView.setGravity(17);
    errorView.setBackgroundColor(Color.BLACK);
    errorView.setVisibility(View.GONE);
    root.addView(webView, new FrameLayout.LayoutParams(-1, -1));
    FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(-1, 5, 48);
    root.addView(progress, pp);
    root.addView(errorView, new FrameLayout.LayoutParams(-1, -1));
    setContentView(root);
    if (savedInstanceState != null) webView.restoreState(savedInstanceState);
    else webView.loadUrl(PLAYER_URL);
  }

  private class ExternalPlayerBridge {
    @JavascriptInterface public void openExternalPlayer(String rawUrl, String title) {
      runOnUiThread(() -> {
        try {
          Uri uri = Uri.parse(rawUrl);
          String scheme = uri.getScheme();
          if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            Toast.makeText(MainActivity.this, "رابط البث غير صالح", Toast.LENGTH_LONG).show();
            return;
          }
          String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase();
          String mime = path.endsWith(".m3u8") ? "application/vnd.apple.mpegurl" : "video/*";
          Intent intent = new Intent(Intent.ACTION_VIEW);
          intent.setDataAndType(uri, mime);
          intent.putExtra(Intent.EXTRA_TITLE, title == null ? "IPTV WORLD" : title);
          startActivity(Intent.createChooser(intent, "اختر مشغّل الفيديو"));
        } catch (ActivityNotFoundException e) {
          Toast.makeText(MainActivity.this, "لا يوجد مشغّل خارجي متوافق مثبت على الجهاز", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
          Toast.makeText(MainActivity.this, "تعذر فتح الرابط في مشغّل خارجي", Toast.LENGTH_LONG).show();
        }
      });
    }
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    webView.saveState(outState);
    super.onSaveInstanceState(outState);
  }

  @Override public void onBackPressed() {
    if (webView != null && webView.canGoBack()) webView.goBack();
    else super.onBackPressed();
  }

  @Override protected void onDestroy() {
    if (webView != null) { webView.loadUrl("about:blank"); webView.destroy(); }
    super.onDestroy();
  }
}