package kr.ds.webviewplatfrom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;


public class MainActivity extends Activity {
    private WebView WebHomepage;
    private LinearLayout ProgressArea;
    private BackPressCloseHandler backPressCloseHandler = new BackPressCloseHandler(this);
    private String Url = "http://www.kmhousing.com";

    public class BackPressCloseHandler {

        private long backKeyPressedTime = 0;
        private Toast toast;
        private Activity activity;

        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }
        public void onBackPressed() {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                showGuide();
                return;
            }

            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                activity.finish();
                toast.cancel();
            }

        }

        private void showGuide() {
            toast = Toast.makeText(activity, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebHomepage = (WebView) findViewById(R.id.web_homepage);
        ProgressArea = (LinearLayout) findViewById(R.id.progress_area);

        try {
            getWebView(Url);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }





    public void getWebView(final String url) {
        final Handler handler = new Handler();
        Runnable doInit = new Runnable() {
            public void run() {
                WebHomepage.getSettings().setLoadWithOverviewMode(true);// 축소된상태
                WebHomepage.getSettings().setUseWideViewPort(true); // 뷰포트
                WebHomepage.setVerticalScrollbarOverlay(true);
                // WebHomepage.getSettings().setSupportZoom(true);// 줌컨트롤
                // WebHomepage.getSettings().setBuiltInZoomControls(true);
                WebHomepage.getSettings().setJavaScriptEnabled(true);
                WebHomepage.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                WebHomepage.getSettings().setSaveFormData(true);
                WebHomepage.getSettings().setSavePassword(false);//해당비밀번호 창
                WebHomepage.getSettings().setSupportMultipleWindows(false); // popup설정
                WebHomepage.loadUrl(url);
                WebHomepage.setWebViewClient(new WebViewClients());

                WebHomepage.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public boolean onJsAlert(WebView view, String url,
                                             String message, final JsResult result) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage(message)
                                .setPositiveButton(android.R.string.ok,
                                        new AlertDialog.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                result.confirm();
                                            }
                                        }).setCancelable(false).create().show();
                        return true;

                    };



                });
            }
        };
        ProgressArea.setVisibility(View.VISIBLE);
        handler.postDelayed(doInit, 0);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }



    private class WebViewClients extends WebViewClient {

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,SslError error) {
            super.onReceivedSslError(view, handler, error);
            handler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("tel:")) {
                Intent call_phone = new Intent(Intent.ACTION_CALL,	Uri.parse(url));
                startActivity(call_phone);
                return true;
            }else if(url.startsWith("market:")){
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }else{
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            ProgressArea.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            ProgressArea.setVisibility(View.GONE);
        }
    }
    public boolean onKeyDown(int KeyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (KeyCode == KeyEvent.KEYCODE_BACK) {
                if(WebHomepage.canGoBack()){
                    WebHomepage.goBack();
                }else{
                    backPressCloseHandler.onBackPressed();
                }
                return true;
            }
        }
        return super.onKeyDown(KeyCode, event);
    }
}
