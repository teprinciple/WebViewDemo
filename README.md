# WebViewDemo
webview模拟原生页面切换效果

#### 前言
在开发中，有时候我们会在app中使用WebView加载一个web页面。这样可以适当减轻我们开发的难度。但是弊端是WebView中切换html，没有像原生页面的切换效果。这里我们就利用动画，以及
获取网页的快照来实现android activity 默认的切换效果（页面从右往左进入，从左往右退出）。

先看看效果：
![](http://upload-images.jianshu.io/upload_images/2368611-45679aa3388f4aa7.gif?imageMogr2/auto-orient/strip)


#### 大体思路
监听webview加载url的进度，在没到100%之前先获取当前页面的截图，并用imageview显示在手机屏幕上。网页加载好的时候。让imageview和webView分别加载对应的动画效果，以达到从右到左或者从左到右的效果，模拟原生页面的切换效果。

#### 第一步、创建相关动画资源
在animal文件夹下新建以下补间动画：
1、页面进入相关动画
translate_in_go.xml
```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android" >
    <translate android:fromXDelta="100%" android:toXDelta="0%p"
        android:duration="300" />
</set>
```

translate_out_go.xml
```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <translate android:fromXDelta="0%" android:toXDelta="-100%p"
        android:duration="300" />
</set>
```

2、页面退出相关动画
translate_in_back.xml
```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android" >
    <translate
        android:fromXDelta="-100%"   android:toXDelta="0%p"
        android:duration="300" />
</set>
```

translate_out_back.xml
```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <translate android:fromXDelta="0%" android:toXDelta="100%p"
        android:duration="300" />
</set>
```

#### 第二步、在webView中调用动画达到效果

```
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

```

activity的布局文件：
```
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:id="@+id/root"
    tools:context="com.teprinciple.webviewdemo.MainActivity">
   <WebView
       android:id="@+id/webView"
       android:layout_width="match_parent"
       android:layout_height="match_parent"/>
</RelativeLayout>
```

这样就模仿出了app原生的切换效果。但是当url页面的很丰富，加载时间会很长，这样来实现就会有的卡顿的效果，不过一般的页面效果还是很好的。

#### 文章地址
http://www.jianshu.com/p/b7f44ba49270
