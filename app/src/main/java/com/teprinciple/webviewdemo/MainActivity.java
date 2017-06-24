package com.teprinciple.webviewdemo;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private RelativeLayout root;
    private boolean isGoBack = false;
    private boolean isFirst = true;
    private ImageView iamgeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.webView);
        root = (RelativeLayout) findViewById(R.id.root);
        initoperation();
    }

    private void initoperation() {
        initWebView();
        mWebView.loadUrl("http://www.jianshu.com/u/7c36ad462572");
    }

    private void initWebView() {


        mWebView.setWebChromeClient(new WebChromeClient() {

            //url加载进度改变的监听
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (isFirst)return; //刚进入页面不需要模拟效果，app自己有

                view.setVisibility(View.GONE);//先隐藏webview

                if (newProgress == 100) {

                    //加载完毕，显示webview 隐藏imageview
                    view.setVisibility(View.VISIBLE);
                    if (iamgeView != null)iamgeView.setVisibility(View.GONE);

                    //页面进入效果的动画
                    Animation translate_in = AnimationUtils.loadAnimation(MainActivity.this, R.anim.translate_in_go);
                    Animation translate_out = AnimationUtils.loadAnimation(MainActivity.this, R.anim.translate_out_go);

                    //页面退出的动画
                    if (isGoBack){
                        translate_in = AnimationUtils.loadAnimation(MainActivity.this, R.anim.translate_in_back);
                        translate_out = AnimationUtils.loadAnimation(MainActivity.this, R.anim.translate_out_back);
                    }
                    translate_in.setFillAfter(true);
                    translate_in.setDetachWallpaper(true);
                    translate_out.setFillAfter(true);
                    translate_out.setDetachWallpaper(true);

                    //开启动画
                    if(null!=iamgeView)iamgeView.startAnimation(translate_out);
                    view.startAnimation(translate_in);

                    //动画结束后，移除imageView
                    translate_out.setAnimationListener(new Animation.AnimationListener(){
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if(null!=iamgeView){
                                root.removeView(iamgeView);
                                iamgeView=null;
                                isGoBack = false;
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void onAnimationStart(Animation animation) {
                            // TODO Auto-generated method stub
                        }
                    });

                }else{
                    //url没加载好之前，隐藏webview，在主布局中，加入imageview显示当前页面快照

                    if(null==iamgeView){
                        iamgeView=new ImageView(MainActivity.this);
                        view.setDrawingCacheEnabled(true);
                        Bitmap bitmap=view.getDrawingCache();
                        if(null!=bitmap){
                            Bitmap b=   Bitmap.createBitmap(bitmap);
                            iamgeView.setImageBitmap(b);
                        }
                        root.addView(iamgeView);
                    }
                }

            }
        });


        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                isFirst = false;
                return super.shouldOverrideUrlLoading(view, url);
            }
        });


        //webView的相关设置

        WebSettings websettings = mWebView.getSettings();
        websettings.setJavaScriptEnabled(true); // Warning! You can have XSS
        websettings.setSupportZoom(true);
//        websettings.setBlockNetworkImage(true);  //使把图片加载放在最后来加载渲染
        websettings.setAppCacheEnabled(true);    //开启H5(APPCache)缓存功能
        websettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        websettings.setAllowFileAccess(true);    // 可以读取文件缓存(manifest生效)
        websettings.setDatabaseEnabled(true);    //webSettings.setDatabaseEnabled(true);
        websettings.setDomStorageEnabled(true);  // 开启 DOM storage 功能
        websettings.setDisplayZoomControls(false);
        websettings.setDefaultTextEncodingName("utf-8");
        websettings.setRenderPriority(WebSettings.RenderPriority.HIGH); //提高渲染的优先级
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            isGoBack = true;
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
