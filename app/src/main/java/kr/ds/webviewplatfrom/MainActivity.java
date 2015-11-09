package kr.ds.webviewplatfrom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
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
    private WebView mWebView;
    private LinearLayout ProgressArea;
    private BackPressCloseHandler backPressCloseHandler = new BackPressCloseHandler(this);
    private String Url = Config.WEBPAGE;

    public static final String INTENT_PROTOCOL_START = "intent:";
    public static final String INTENT_PROTOCOL_INTENT = "#Intent;";
    public static final String INTENT_PROTOCOL_END = ";end;";
    public static final String GOOGLE_PLAY_STORE_PREFIX = "market://details?id=";



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

        mWebView = (WebView) findViewById(R.id.web_homepage);
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
                mWebView.getSettings().setLoadWithOverviewMode(true);// 축소된상태
                mWebView.getSettings().setUseWideViewPort(true); // 뷰포트
                mWebView.setVerticalScrollbarOverlay(true);
                // mWebView.getSettings().setSupportZoom(true);// 줌컨트롤
                //mWebView.getSettings().setBuiltInZoomControls(true);
                mWebView.getSettings().setJavaScriptEnabled(true);
                mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                mWebView.getSettings().setSaveFormData(true);
                //mWebView.getSettings().setSavePassword(false);//해당비밀번호 창
                mWebView.getSettings().setSupportMultipleWindows(false); // popup설정
                mWebView.loadUrl(url);
                mWebView.setWebViewClient(new WebViewClients());

                mWebView.setWebChromeClient(new WebChromeClient() {
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

                    }


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
            //19버젼이후 체크 사항.
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                if (url.startsWith(INTENT_PROTOCOL_START)) {

                    final int customUrlStartIndex = INTENT_PROTOCOL_START.length();
                    final int customUrlEndIndex = url.indexOf(INTENT_PROTOCOL_INTENT);
                    if (customUrlEndIndex < 0) {
                        return false;
                    } else {
                        final String customUrl = url.substring(customUrlStartIndex, customUrlEndIndex);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            intent.setData(Uri.parse(customUrl));
                            getBaseContext().startActivity(intent);
                        } catch (ActivityNotFoundException e) {//액티비티가존재 하지 않을 경우.
                            final int packageStartIndex = customUrlEndIndex+ INTENT_PROTOCOL_INTENT.length();
                            final int packageEndIndex = url.indexOf(INTENT_PROTOCOL_END);

                            final String packageName = url.substring(packageStartIndex,	packageEndIndex < 0 ? url.length()	: packageEndIndex);
                            intent.setData(Uri.parse(GOOGLE_PLAY_STORE_PREFIX	+ packageName));
                            getBaseContext().startActivity( intent );
                        }
                        return true;
                    }
                } else {
                    return false;
                }
            }else{
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }else if (url.startsWith("mailto:")) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }else if (url.startsWith("sms:")) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }else if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:") ) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }else{
                    view.loadUrl(url);
                }
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
    @Override
    public boolean onKeyDown(int KeyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (KeyCode == KeyEvent.KEYCODE_BACK) {
                if(mWebView.canGoBack()){
                    mWebView.goBack();
                }else{
                    backPressCloseHandler.onBackPressed();
                }
                return true;
            }
        }
        return super.onKeyDown(KeyCode, event);
    }



}
