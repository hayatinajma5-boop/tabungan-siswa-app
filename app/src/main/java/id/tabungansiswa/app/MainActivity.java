package id.tabungansiswa.app;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;

public class MainActivity extends Activity {

    private static final int STORAGE_REQUEST = 501;

    private WebView webView;
    private String homeUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homeUrl = getString(R.string.website_url);

        getWindow().setStatusBarColor(Color.rgb(15, 23, 42));
        getWindow().setNavigationBarColor(Color.rgb(15, 23, 42));

        if (
                Build.VERSION.SDK_INT <= 28 &&
                checkSelfPermission(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    STORAGE_REQUEST
            );
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(
                Color.rgb(244, 247, 251)
        );

        webView = createWebView();

        root.addView(
                webView,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        setContentView(root);

        webView.loadUrl(homeUrl);
    }

    private WebView createWebView() {

        WebView w = new WebView(this);

        WebSettings settings = w.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);

        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        settings.setTextZoom(100);

        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);

        settings.setMediaPlaybackRequiresUserGesture(false);

        String userAgent = settings.getUserAgentString();

        settings.setUserAgentString(
                userAgent + " TabunganSiswaApp/1.0"
        );

        CookieManager.getInstance().setAcceptCookie(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance()
                    .setAcceptThirdPartyCookies(
                            w,
                            true
                    );
        }

        w.setBackgroundColor(
                Color.rgb(244, 247, 251)
        );

        w.addJavascriptInterface(
                new AndroidBridge(this, w),
                "AndroidApp"
        );

        w.setWebViewClient(
                new AppWebViewClient()
        );

        w.setWebChromeClient(
                new AppWebChromeClient(w)
        );

        w.setDownloadListener(
                new AppDownloadListener()
        );

        return w;
    }

    private class AppWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(
                WebView view,
                WebResourceRequest request
        ) {

            Uri uri = request.getUrl();

            String scheme = uri.getScheme();

            if (
                    "http".equalsIgnoreCase(scheme) ||
                    "https".equalsIgnoreCase(scheme) ||
                    "blob".equalsIgnoreCase(scheme)
            ) {
                return false;
            }

            try {

                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        uri
                );

                startActivity(intent);

            } catch (Exception e) {

                Toast.makeText(
                        MainActivity.this,
                        "Tautan tidak dapat dibuka.",
                        Toast.LENGTH_SHORT
                ).show();
            }

            return true;
        }

        @Override
        public void onPageFinished(
                WebView view,
                String url
        ) {

            super.onPageFinished(
                    view,
                    url
            );

            injectNativeHelpers(view);
        }

        @Override
        public void onReceivedError(
                WebView view,
                WebResourceRequest request,
                WebResourceError error
        ) {

            super.onReceivedError(
                    view,
                    request,
                    error
            );

            if (request.isForMainFrame()) {

                Toast.makeText(
                        MainActivity.this,
                        "Koneksi bermasalah. Coba buka ulang aplikasi.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private class AppWebChromeClient extends WebChromeClient {

        private final WebView owner;

        AppWebChromeClient(
                WebView owner
        ) {
            this.owner = owner;
        }

        @Override
        public boolean onCreateWindow(
                WebView view,
                boolean isDialog,
                boolean isUserGesture,
                android.os.Message resultMsg
        ) {

            final Dialog dialog =
                    new Dialog(
                            MainActivity.this
                    );

            dialog.requestWindowFeature(
                    Window.FEATURE_NO_TITLE
            );

            WebView popup =
                    createWebView();

            popup.setWebChromeClient(
                    new PopupChromeClient(
                            popup,
                            dialog
                    )
            );

            popup.setWebViewClient(
                    new AppWebViewClient()
            );

            dialog.setContentView(
                    popup
            );

            Window window =
                    dialog.getWindow();

            if (window != null) {

                window.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                );

                window.setGravity(
                        Gravity.CENTER
                );
            }

            WebView.WebViewTransport transport =
                    (WebView.WebViewTransport)
                            resultMsg.obj;

            transport.setWebView(
                    popup
            );

            resultMsg.sendToTarget();

            dialog.show();

            return true;
        }

        @Override
        public boolean onJsAlert(
                WebView view,
                String url,
                String message,
                JsResult result
        ) {

            return super.onJsAlert(
                    view,
                    url,
                    message,
                    result
            );
        }
    }

    private class PopupChromeClient
            extends WebChromeClient {

        private final WebView popup;
        private final Dialog dialog;

        PopupChromeClient(
                WebView popup,
                Dialog dialog
        ) {

            this.popup = popup;
            this.dialog = dialog;
        }

        @Override
        public void onCloseWindow(
                WebView window
        ) {

            dialog.dismiss();

            popup.destroy();
        }

        @Override
        public boolean onCreateWindow(
                WebView view,
                boolean isDialog,
                boolean isUserGesture,
                android.os.Message resultMsg
        ) {

            AppWebChromeClient chrome =
                    new AppWebChromeClient(
                            view
                    );

            return chrome.onCreateWindow(
                    view,
                    isDialog,
                    isUserGesture,
                    resultMsg
            );
        }
    }

    private void injectNativeHelpers(
            WebView target
    ) {

        String js =
                "(function(){" +

                "try{" +

                "window.__TABUNGAN_ANDROID__=true;" +

                "if(typeof window.downloadBlob==='function'" +
                "&&!window.__nativeBlobPatched){" +

                "window.__nativeBlobPatched=true;" +

                "window.downloadBlob=function(blob,filename){" +

                "var r=new FileReader();" +

                "r.onloadend=function(){" +

                "AndroidApp.saveBase64(" +
                "filename,String(r.result)" +
                ");" +

                "};" +

                "r.readAsDataURL(blob);" +

                "};" +

                "}" +

                "if(!window.__nativePrintPatched){" +

                "window.__nativePrintPatched=true;" +

                "window.print=function(){" +

                "AndroidApp.printPage();" +

                "};" +

                "}" +

                "}catch(e){}" +

                "})();";

        target.evaluateJavascript(
                js,
                null
        );
    }

    private class AppDownloadListener
            implements DownloadListener {

        @Override
        public void onDownloadStart(
                String url,
                String userAgent,
                String contentDisposition,
                String mimetype,
                long contentLength
        ) {

            if (
                    url != null &&
                    url.startsWith("blob:")
            ) {

                Toast.makeText(
                        MainActivity.this,
                        "Menyiapkan file...",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            try {

                DownloadManager.Request request =
                        new DownloadManager.Request(
                                Uri.parse(url)
                        );

                request.setMimeType(
                        mimetype
                );

                request.addRequestHeader(
                        "User-Agent",
                        userAgent
                );

                String cookies =
                        CookieManager
                                .getInstance()
                                .getCookie(url);

                if (cookies != null) {

                    request.addRequestHeader(
                            "Cookie",
                            cookies
                    );
                }

                request.setNotificationVisibility(
                        DownloadManager.Request
                                .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                );

                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        "TabunganSiswa_" +
                                System.currentTimeMillis()
                );

                DownloadManager downloadManager =
                        (DownloadManager)
                                getSystemService(
                                        DOWNLOAD_SERVICE
                                );

                downloadManager.enqueue(
                        request
                );

                Toast.makeText(
                        MainActivity.this,
                        "File sedang diunduh.",
                        Toast.LENGTH_SHORT
                ).show();

            } catch (Exception e) {

                Toast.makeText(
                        MainActivity.this,
                        "Download gagal: " +
                                e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    public static class AndroidBridge {

        private final Activity activity;
        private final WebView webView;

        AndroidBridge(
                Activity activity,
                WebView webView
        ) {

            this.activity = activity;
            this.webView = webView;
        }

        @JavascriptInterface
        public void saveBase64(
                final String inputFilename,
                final String inputDataUrl
        ) {

            activity.runOnUiThread(
                    new Runnable() {

                        @Override
                        public void run() {

                            try {

                                String filename =
                                        inputFilename;

                                String dataUrl =
                                        inputDataUrl;

                                if (
                                        filename == null ||
                                        filename
                                                .trim()
                                                .isEmpty()
                                ) {

                                    filename =
                                            "TabunganSiswa_" +
                                            System.currentTimeMillis();
                                }

                                filename =
                                        sanitizeFilename(
                                                filename
                                        );

                                if (dataUrl == null) {

                                    throw new IllegalArgumentException(
                                            "Data file kosong."
                                    );
                                }

                                int comma =
                                        dataUrl.indexOf(',');

                                if (comma < 0) {

                                    throw new IllegalArgumentException(
                                            "Data file tidak valid."
                                    );
                                }

                                String meta =
                                        dataUrl.substring(
                                                0,
                                                comma
                                        );

                                String payload =
                                        dataUrl.substring(
                                                comma + 1
                                        );

                                byte[] bytes =
                                        Base64
                                                .getDecoder()
                                                .decode(
                                                        payload
                                                );

                                String mime =
                                        "application/octet-stream";

                                if (
                                        meta.startsWith(
                                                "data:"
                                        ) &&
                                        meta.contains(";")
                                ) {

                                    mime =
                                            meta.substring(
                                                    5,
                                                    meta.indexOf(';')
                                            );
                                }

                                saveBytes(
                                        filename,
                                        mime,
                                        bytes
                                );

                                Toast.makeText(
                                        activity,
                                        "Tersimpan di folder Download: " +
                                                filename,
                                        Toast.LENGTH_LONG
                                ).show();

                            } catch (Exception e) {

                                Toast.makeText(
                                        activity,
                                        "Gagal menyimpan file: " +
                                                e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                    }
            );
        }

        @JavascriptInterface
        public void printPage() {

            activity.runOnUiThread(
                    new Runnable() {

                        @Override
                        public void run() {

                            try {

                                PrintManager printManager =
                                        (PrintManager)
                                                activity.getSystemService(
                                                        Context.PRINT_SERVICE
                                                );

                                String jobName =
                                        "Tabungan Siswa";

                                printManager.print(
                                        jobName,
                                        webView
                                                .createPrintDocumentAdapter(
                                                        jobName
                                                ),
                                        null
                                );

                            } catch (Exception e) {

                                Toast.makeText(
                                        activity,
                                        "Print gagal: " +
                                                e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                    }
            );
        }

        private void saveBytes(
                String filename,
                String mime,
                byte[] bytes
        ) throws Exception {

            if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.Q
            ) {

                ContentResolver resolver =
                        activity
                                .getContentResolver();

                ContentValues values =
                        new ContentValues();

                values.put(
                        MediaStore.Downloads.DISPLAY_NAME,
                        filename
                );

                values.put(
                        MediaStore.Downloads.MIME_TYPE,
                        mime
                );

                values.put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS +
                                "/Tabungan Siswa"
                );

                values.put(
                        MediaStore.Downloads.IS_PENDING,
                        1
                );

                Uri uri =
                        resolver.insert(
                                MediaStore.Downloads
                                        .EXTERNAL_CONTENT_URI,
                                values
                        );

                if (uri == null) {

                    throw new Exception(
                            "Folder Download tidak tersedia."
                    );
                }

                OutputStream out =
                        resolver.openOutputStream(
                                uri
                        );

                if (out == null) {

                    throw new Exception(
                            "Tidak dapat membuka file."
                    );
                }

                out.write(
                        bytes
                );

                out.flush();
                out.close();

                values.clear();

                values.put(
                        MediaStore.Downloads.IS_PENDING,
                        0
                );

                resolver.update(
                        uri,
                        values,
                        null,
                        null
                );

            } else {

                File dir =
                        Environment
                                .getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_DOWNLOADS
                                );

                if (
                        !dir.exists() &&
                        !dir.mkdirs()
                ) {

                    throw new Exception(
                            "Tidak dapat membuat folder Download."
                    );
                }

                File file =
                        new File(
                                dir,
                                filename
                        );

                FileOutputStream out =
                        new FileOutputStream(
                                file
                        );

                out.write(
                        bytes
                );

                out.flush();
                out.close();
            }
        }

        private String sanitizeFilename(
                String name
        ) {

            return name.replaceAll(
                    "[\\\\/:*?\"<>|]",
                    "_"
            );
        }
    }

    @Override
    public void onBackPressed() {

        if (
                webView != null &&
                webView.canGoBack()
        ) {

            webView.goBack();

        } else {

            super.onBackPressed();
        }
    }
}
