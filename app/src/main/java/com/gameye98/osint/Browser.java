package com.gameye98.osint;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Browser extends AppCompatActivity {

    // Preserved fields for backward compatibility
    View dloadbtn, dsktpbtn;
    boolean isdesktop = false;
    WebView webview;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private static final int FILECHOOSER_RESULTCODE = 1;
    String url;

    // Modern UI Elements
    private FrameLayout webContainer;
    private TextView tvBrowserTitle;
    private TextView tvBrowserUrl;
    private TextView tvDesktopPill;
    private ProgressBar browserProgress;
    private ImageView imgSslLock;
    private View browserLoadingView;
    private View browserErrorView;
    private TextView tvErrorDescription;
    private ImageButton btnBrowserReloadStop;
    private ImageButton btnWebBack;
    private ImageButton btnWebForward;

    private boolean isLoading = false;
    private String defaultUserAgent = null;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void toast(String text) {
        if (!isFinishing()) {
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Preserved method signature for compatibility.
     * Returns a modern standard user agent instead of broken obsolete strings.
     */
    public String getUserAgent() {
        if (defaultUserAgent != null && !defaultUserAgent.isEmpty()) {
            return defaultUserAgent;
        }
        return "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36";
    }

    public void setDesktopMode(WebView webView, boolean enabled) {
        if (webView == null) return;
        this.isdesktop = enabled;

        String baseUa = (defaultUserAgent != null && !defaultUserAgent.isEmpty())
                ? defaultUserAgent
                : "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36";

        String targetUa;
        if (enabled) {
            targetUa = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";
            toast("Desktop Mode Activated");
        } else {
            targetUa = baseUa;
            toast("Mobile Mode Activated");
        }

        try {
            WebSettings ws = webView.getSettings();
            ws.setUserAgentString(targetUa);
            ws.setUseWideViewPort(enabled);
            ws.setLoadWithOverviewMode(enabled);
            ws.setSupportZoom(true);
            ws.setBuiltInZoomControls(true);
            ws.setDisplayZoomControls(false);
            updateDesktopModeUi(enabled);
            webView.reload();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDesktopModeUi(boolean enabled) {
        if (tvDesktopPill != null) {
            tvDesktopPill.setText(enabled ? "Desktop Mode" : "Mobile Mode");
            tvDesktopPill.setTextColor(ContextCompat.getColor(this, enabled ? R.color.accent_secondary : R.color.text_secondary));
        }
        if (dsktpbtn instanceof ImageButton) {
            ((ImageButton) dsktpbtn).setImageTintList(
                    ContextCompat.getColorStateList(this, enabled ? R.color.accent_secondary : R.color.text_secondary)
            );
        }
    }

    private void updateNavigationButtons() {
        if (btnWebBack != null) {
            boolean canBack = (webview != null && webview.canGoBack());
            btnWebBack.setEnabled(canBack);
            btnWebBack.setAlpha(canBack ? 1.0f : 0.35f);
        }
        if (btnWebForward != null) {
            boolean canForward = (webview != null && webview.canGoForward());
            btnWebForward.setEnabled(canForward);
            btnWebForward.setAlpha(canForward ? 1.0f : 0.35f);
        }
    }

    private void updateLoadingState(boolean loading) {
        this.isLoading = loading;
        if (btnBrowserReloadStop != null) {
            btnBrowserReloadStop.setImageResource(loading ? R.drawable.ic_clear : R.drawable.ic_refresh);
            btnBrowserReloadStop.setContentDescription(loading ? "Stop Loading" : "Reload");
        }
        if (browserProgress != null) {
            browserProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void showErrorView(String message) {
        if (browserErrorView != null) {
            browserErrorView.setVisibility(View.VISIBLE);
        }
        if (tvErrorDescription != null && message != null) {
            tvErrorDescription.setText(message);
        }
        if (webview != null) {
            webview.setVisibility(View.INVISIBLE);
        }
        updateLoadingState(false);
    }

    private void hideErrorView() {
        if (browserErrorView != null) {
            browserErrorView.setVisibility(View.GONE);
        }
        if (webview != null) {
            webview.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webview != null) {
            webview.onResume();
        }
    }

    @Override
    public void onPause() {
        if (webview != null) {
            webview.onPause();
        }
        CookieManager.getInstance().flush();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        if (uploadMessage != null) {
            uploadMessage.onReceiveValue(null);
            uploadMessage = null;
        }
        if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(null);
            mUploadMessage = null;
        }
        if (webview != null) {
            try {
                webview.stopLoading();
                webview.setWebChromeClient(null);
                webview.setWebViewClient(null);
                webview.setDownloadListener(null);
                if (webContainer != null) {
                    webContainer.removeView(webview);
                }
                webview.destroy();
            } catch (Exception ignored) {
            }
            webview = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.browser);

        // Bind preserved and modern views
        dloadbtn = findViewById(R.id.wtpsave);
        dsktpbtn = findViewById(R.id.wtpdesktop);
        webview = findViewById(R.id.webview);
        webContainer = findViewById(R.id.webContainer);

        tvBrowserTitle = findViewById(R.id.tvBrowserTitle);
        tvBrowserUrl = findViewById(R.id.tvBrowserUrl);
        tvDesktopPill = findViewById(R.id.tvDesktopPill);
        browserProgress = findViewById(R.id.browserProgress);
        imgSslLock = findViewById(R.id.imgSslLock);
        browserLoadingView = findViewById(R.id.browserLoadingView);
        browserErrorView = findViewById(R.id.browserErrorView);
        tvErrorDescription = findViewById(R.id.tvErrorDescription);

        btnBrowserReloadStop = findViewById(R.id.btnBrowserReloadStop);
        btnWebBack = findViewById(R.id.btnWebBack);
        btnWebForward = findViewById(R.id.btnWebForward);

        ImageButton btnBrowserBack = findViewById(R.id.btnBrowserBack);
        ImageButton btnBrowserShare = findViewById(R.id.btnBrowserShare);
        ImageButton btnBrowserExternal = findViewById(R.id.btnBrowserExternal);
        ImageButton btnWebReload = findViewById(R.id.btnWebReload);
        View btnErrorRetry = findViewById(R.id.btnErrorRetry);
        View btnErrorExternal = findViewById(R.id.btnErrorExternal);

        url = getIntent().getStringExtra("url");
        if (url == null || url.trim().isEmpty()) {
            url = "https://github.com/margacis2/OSINT-Tools";
        }

        if (tvBrowserUrl != null) {
            tvBrowserUrl.setText(url);
        }

        setupWebView();

        // Header Actions
        if (btnBrowserBack != null) {
            btnBrowserBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // Reload / Stop Action
        View.OnClickListener reloadStopAction = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webview == null) return;
                if (isLoading) {
                    webview.stopLoading();
                    updateLoadingState(false);
                } else {
                    hideErrorView();
                    webview.reload();
                }
            }
        };

        if (btnBrowserReloadStop != null) btnBrowserReloadStop.setOnClickListener(reloadStopAction);
        if (btnWebReload != null) btnWebReload.setOnClickListener(reloadStopAction);

        // Retry on Error
        if (btnErrorRetry != null) {
            btnErrorRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideErrorView();
                    if (webview != null) {
                        webview.reload();
                    }
                }
            });
        }

        // Open external on Error
        View.OnClickListener openExternalAction = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInExternalBrowser();
            }
        };
        if (btnErrorExternal != null) btnErrorExternal.setOnClickListener(openExternalAction);
        if (btnBrowserExternal != null) btnBrowserExternal.setOnClickListener(openExternalAction);

        // Desktop Mode Toggle
        if (dsktpbtn != null) {
            dsktpbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setDesktopMode(webview, !isdesktop);
                }
            });
        }

        // PDF Print / Save Action
        if (dloadbtn != null) {
            dloadbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    printCurrentPageToPdf();
                }
            });
        }

        // Share current URL
        if (btnBrowserShare != null) {
            btnBrowserShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareCurrentPage();
                }
            });
        }

        // Bottom Navigation Controls
        if (btnWebBack != null) {
            btnWebBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (webview != null && webview.canGoBack()) {
                        hideErrorView();
                        webview.goBack();
                    }
                }
            });
        }

        if (btnWebForward != null) {
            btnWebForward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (webview != null && webview.canGoForward()) {
                        hideErrorView();
                        webview.goForward();
                    }
                }
            });
        }

        updateDesktopModeUi(isdesktop);
        updateNavigationButtons();

        // Load the initial URL safely
        hideErrorView();
        webview.loadUrl(url);
    }

    private void setupWebView() {
        if (webview == null) return;

        WebSettings settings = webview.getSettings();
        defaultUserAgent = settings.getUserAgentString();

        // Core Rendering & Compatibility Settings
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        // Security & File Access Settings
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);

        // Mixed Content Mode (Essential for OSINT sites with HTTP/HTTPS assets)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        // Cookies
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webview, true);
        }

        // Download Listener (File downloads for OSINT exports: CSV, JSON, PCAP, etc.)
        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String downloadUrl, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                handleDownload(downloadUrl, contentDisposition, mimeType);
            }
        });

        // WebChromeClient
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (browserProgress != null) {
                    if (newProgress < 100) {
                        browserProgress.setVisibility(View.VISIBLE);
                        browserProgress.setProgress(newProgress);
                        updateLoadingState(true);
                    } else {
                        browserProgress.setVisibility(View.GONE);
                        updateLoadingState(false);
                    }
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (tvBrowserTitle != null && title != null && !title.isEmpty()) {
                    tvBrowserTitle.setText(title);
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // Grant geolocation cleanly when investigative tools request it
                callback.invoke(origin, true, false);
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                // Handle popups / window.open cleanly by opening in current WebView
                WebView.HitTestResult result = view.getHitTestResult();
                String data = result.getExtra();
                if (data != null && !data.isEmpty()) {
                    view.loadUrl(data);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                if (isFinishing()) return false;
                new AlertDialog.Builder(Browser.this)
                        .setTitle("Website Notice")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                if (isFinishing()) return false;
                new AlertDialog.Builder(Browser.this)
                        .setTitle("Website Confirmation")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }

            // File Chooser for Android 5.0+
            @Override
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }
                uploadMessage = filePathCallback;

                Intent intent = null;
                if (fileChooserParams != null) {
                    try {
                        intent = fileChooserParams.createIntent();
                    } catch (Exception ignored) {
                    }
                }
                if (intent == null) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                }

                try {
                    startActivityForResult(Intent.createChooser(intent, "Select File to Upload"), REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null);
                        uploadMessage = null;
                    }
                    toast("No file chooser application found on device.");
                    return false;
                }
                return true;
            }

            // Legacy file chooser methods
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "*/*");
            }
        });

        // Crash-proof WebViewClient
        webview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                // Prevent application termination on renderer memory exhaustion / crash
                boolean didCrash = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && detail != null && detail.didCrash());
                showErrorView(didCrash
                        ? "The website renderer terminated unexpectedly due to high memory usage. Tap Retry to reload."
                        : "Website process suspended by system. Tap Retry to reload.");
                return true; // Host application handled the termination
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && request != null) {
                    return handleExternalScheme(request.getUrl().toString());
                }
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String targetUrl) {
                return handleExternalScheme(targetUrl);
            }

            @Override
            public void onPageStarted(WebView view, String currentUrl, Bitmap favicon) {
                super.onPageStarted(view, currentUrl, favicon);
                hideErrorView();
                updateLoadingState(true);

                if (tvBrowserUrl != null && currentUrl != null) {
                    tvBrowserUrl.setText(currentUrl);
                }
                if (imgSslLock != null) {
                    boolean isHttps = (currentUrl != null && currentUrl.startsWith("https://"));
                    imgSslLock.setImageResource(R.drawable.ic_lock);
                    imgSslLock.setColorFilter(isHttps ? Color.parseColor("#22C55E") : Color.parseColor("#EF4444"));
                }
                updateNavigationButtons();
            }

            @Override
            public void onPageFinished(WebView view, String currentUrl) {
                super.onPageFinished(view, currentUrl);
                updateLoadingState(false);
                if (browserLoadingView != null) {
                    browserLoadingView.setVisibility(View.GONE);
                }
                updateNavigationButtons();
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                // Do NOT crash or silently bypass; prompt user safely
                if (isFinishing()) {
                    handler.cancel();
                    return;
                }

                String sslMessage = "SSL Certificate Warning: The website security certificate is invalid or untrusted.";
                if (error != null) {
                    switch (error.getPrimaryError()) {
                        case SslError.SSL_EXPIRED:
                            sslMessage = "The website security certificate has expired.";
                            break;
                        case SslError.SSL_IDMISMATCH:
                            sslMessage = "The website hostname does not match its certificate.";
                            break;
                        case SslError.SSL_UNTRUSTED:
                            sslMessage = "The certificate authority is not trusted by your device.";
                            break;
                        case SslError.SSL_NOTYETVALID:
                            sslMessage = "The website certificate is not yet valid.";
                            break;
                    }
                }

                new AlertDialog.Builder(Browser.this)
                        .setTitle("Security Warning")
                        .setMessage(sslMessage + "\n\nWould you like to proceed anyway?")
                        .setPositiveButton("Proceed (Unsafe)", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handler.proceed();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handler.cancel();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            }

            // Modern Error Handling (API 23+)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && request != null && request.isForMainFrame()) {
                    String desc = "Failed to connect to the server.";
                    if (error != null && error.getDescription() != null) {
                        desc = error.getDescription().toString();
                    }
                    showErrorView("Connection Error: " + desc);
                }
            }

            // Legacy Error Handling (API < 23)
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                showErrorView("Unable to reach website: " + description);
            }
        });
    }

    public static boolean isSafeWebScheme(String url) {
        if (url == null) return false;
        String lower = url.trim().toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    public static boolean isSupportedExternalScheme(String url) {
        if (url == null) return false;
        String lower = url.trim().toLowerCase();
        return lower.startsWith("intent://") || lower.startsWith("mailto:") || lower.startsWith("tel:")
                || lower.startsWith("tg:") || lower.startsWith("market:");
    }

    public boolean handleExternalScheme(String targetUrl) {
        if (targetUrl == null) return false;

        // Standard Web Schemes: Allow WebView to navigate
        if (isSafeWebScheme(targetUrl)) {
            return false;
        }

        // Android Intent Scheme (e.g. intent://...)
        if (targetUrl.startsWith("intent://")) {
            try {
                Intent intent = Intent.parseUri(targetUrl, Intent.URI_INTENT_SCHEME);
                if (intent != null) {
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setComponent(null);
                    intent.setSelector(null);
                    if (getPackageManager().resolveActivity(intent, 0) != null) {
                        startActivity(intent);
                        return true;
                    }
                    // Fallback to browser URL if available in intent extras
                    String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                    if (fallbackUrl != null && !fallbackUrl.isEmpty()) {
                        if (webview != null) webview.loadUrl(fallbackUrl);
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        // Block dangerous local file / javascript execution schemes
        String lower = targetUrl.toLowerCase();
        if (lower.startsWith("file:") || lower.startsWith("javascript:") || lower.startsWith("content:") || lower.startsWith("data:")) {
            toast("Blocked unsafe URL scheme");
            return true;
        }

        // External Schemes: mailto, tel, market, tg, etc.
        try {
            Intent externalIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl));
            startActivity(externalIntent);
            return true;
        } catch (Exception e) {
            toast("Cannot handle URL: " + targetUrl);
            return true;
        }
    }

    private void handleDownload(String downloadUrl, String contentDisposition, String mimeType) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            request.setMimeType(mimeType);

            String cookies = CookieManager.getInstance().getCookie(downloadUrl);
            request.addRequestHeader("cookie", cookies);
            request.addRequestHeader("User-Agent", webview.getSettings().getUserAgentString());

            String filename = URLUtil.guessFileName(downloadUrl, contentDisposition, mimeType);
            request.setDescription("Downloading " + filename);
            request.setTitle(filename);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm != null) {
                dm.enqueue(request);
                toast("Downloading " + filename + " to Downloads folder...");
            }
        } catch (Exception e) {
            // Fallback: Launch external browser for download
            try {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                startActivity(i);
            } catch (Exception ex) {
                toast("Unable to download file.");
            }
        }
    }

    private void openInExternalBrowser() {
        try {
            String current = (webview != null && webview.getUrl() != null) ? webview.getUrl() : url;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(current));
            startActivity(intent);
        } catch (Exception e) {
            toast("Cannot open external browser");
        }
    }

    private void shareCurrentPage() {
        String shareUrl = (webview != null && webview.getUrl() != null) ? webview.getUrl() : url;
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share OSINT Tool URL"));
    }

    private void printCurrentPageToPdf() {
        if (webview == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);
            if (printManager == null) {
                toast("Printing service unavailable");
                return;
            }

            String currentUrl = webview.getUrl();
            String jobName = (currentUrl != null && !currentUrl.isEmpty()) ? currentUrl : url;
            if (jobName.contains("?")) jobName = jobName.split("\\?")[0];
            while (jobName.endsWith("/")) jobName = jobName.substring(0, jobName.length() - 1);
            jobName = jobName.substring(jobName.lastIndexOf("/") + 1);
            if (jobName.contains(".")) jobName = jobName.split("\\.")[0];

            String datetime = new SimpleDateFormat("ddMMyyyy-HHmmss", Locale.US).format(new Date());
            jobName = (jobName.isEmpty() ? "OSINT" : jobName) + "-" + datetime;

            PrintDocumentAdapter adapter = webview.createPrintDocumentAdapter(jobName);
            PrintAttributes printAttributes = new PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build();

            final String filename = jobName;
            final PrintJob printJob = printManager.print(jobName, adapter, printAttributes);

            // Safe lifecycle status observer
            final Runnable checkStatus = new Runnable() {
                @Override
                public void run() {
                    if (isFinishing() || isDestroyed()) return;
                    if (printJob.isCompleted()) {
                        toast("File saved as " + filename + ".pdf");
                    } else if (printJob.isFailed()) {
                        toast("Print failed");
                    } else if (printJob.isCancelled()) {
                        toast("Print cancelled");
                    } else {
                        mainHandler.postDelayed(this, 600);
                    }
                }
            };
            mainHandler.post(checkStatus);
        } else {
            toast("Your device does not support PDF printing.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage != null) {
                    Uri[] results = WebChromeClient.FileChooserParams.parseResult(resultCode, intent);
                    uploadMessage.onReceiveValue(results);
                    uploadMessage = null;
                }
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (mUploadMessage != null) {
                Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            if (browserErrorView != null && browserErrorView.getVisibility() == View.VISIBLE) {
                hideErrorView();
                if (webview != null && webview.canGoBack()) {
                    webview.goBack();
                    return true;
                }
            }
            if (webview != null && webview.canGoBack()) {
                webview.goBack();
                return true;
            } else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
