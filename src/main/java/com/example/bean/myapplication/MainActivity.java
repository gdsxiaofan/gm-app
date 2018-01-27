package com.example.bean.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

public class MainActivity extends AppCompatActivity {

    private WebView webview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //实例化WebView对象
        webview = new WebView(this);
        //设置WebView属性，能够执行Javascript脚本
        webview.getSettings().setJavaScriptEnabled(true);
        //开启localstage
        webview.getSettings().setDomStorageEnabled(true);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webview.getSettings().setAppCachePath(appCachePath);
        webview.getSettings().setAllowFileAccess(true);
        webview.getSettings().setAppCacheEnabled(true);
        //关掉游览器提示
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setSupportMultipleWindows(true);
//        webview.setWebViewClient(new WebViewClient());
        webview.setWebViewClient(new MyWebViewClient());
        webview.setWebChromeClient(new WebChromeClient());
        webview.setDownloadListener(new MyWebViewDownLoadListener());
        //加载需要显示的网页
//        webview.loadUrl("file:///android_asset/index.html");
        webview.loadUrl("http://47.93.20.244:8090/app/index.html?#/");

          //本地调试地址
//        webview.loadUrl("http://10.208.137.25:8090//app/index.html?#/");
        //设置Web视图
        setContentView(webview);
//        setContentView(R.layout.activity_main);
    }

    @Override
    //设置回退
    //覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
            webview.goBack(); //goBack()表示返回WebView的上一页面
            return true;
        }
        return false;
    }

    /**
     * 重新定义webViewClient
     */
    public class MyWebViewClient extends  WebViewClient{
        /**
         * 如果页面中链接，需要点击链接在当前browser中响应
         * 而不是新开的Android的系统browser中响应谅解，必须覆盖WebViewClient对象
         */
        public boolean shouldOverviewUrlLoading(WebView view, String url) {
            Log.i("MyWebViewClient","shouldOverviewUrlLoading");
            view.loadUrl(url);
            return true;
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.i("MyWebViewClient","onPageStarted");
            showProgressDialog();
        }

        public void onPageFinished(WebView view, String url) {
            Log.i("MyWebViewClient","onPageFinished");
            closeProgressDialog();
        }

        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.i("MyWebViewClient","onReceivedError");
            closeProgressDialog();
        }
    }

    /**
     * 重新定义webViewDownLoadListener
     */

    private class MyWebViewDownLoadListener implements DownloadListener{

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            //自定义下载
//            if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
//                Toast t=Toast.makeText(MainActivity.this, "需要SD卡。", Toast.LENGTH_LONG);
//                t.setGravity(Gravity.CENTER, 0, 0);
//                t.show();
//                return;
//            }
//            DownloaderTask task=new DownloaderTask();
//            task.execute(url);
            //交由游览器下载
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(url));
            startActivity(intent);

        }
    }

    private class DownloaderTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            String url=params[0];
//          Log.i("tag", "url="+url);
            String fileName=url.substring(url.lastIndexOf("/")+1);
            fileName= URLDecoder.decode(fileName);
            Log.i("tag", "fileName="+fileName);

            File directory=Environment.getExternalStorageDirectory();
            File file=new File(directory,fileName);
            if(file.exists()){
                Log.i("tag", "The file has already exists.");
                return fileName;
            }
            try {
                HttpClient client = new DefaultHttpClient();
//                client.getParams().setIntParameter("http.socket.timeout",3000);//设置超时
                HttpGet get = new HttpGet(url);
                HttpResponse response = client.execute(get);
                if(HttpStatus.SC_OK==response.getStatusLine().getStatusCode()){
                    HttpEntity entity = response.getEntity();
                    InputStream input = entity.getContent();

                    writeToSDCard(fileName,input);

                    input.close();
//                  entity.consumeContent();
                    return fileName;
                }else{
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            closeProgressDialog();
            if(result==null){
                Toast t=Toast.makeText(MainActivity.this, "连接错误！请稍后再试！", Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                return;
            }

            Toast t=Toast.makeText(MainActivity.this, "已保存到SD卡。", Toast.LENGTH_LONG);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            File directory=Environment.getExternalStorageDirectory();
            File file=new File(directory,result);
            Log.i("tag", "Path="+file.getAbsolutePath());

            Intent intent = getFileIntent(file);

            startActivity(intent);

        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }
    }


    private ProgressDialog mDialog;
    private void showProgressDialog(){
        if(mDialog==null){
            mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置风格为圆形进度条
            mDialog.setMessage("正在加载 ，请等待...");
            mDialog.setIndeterminate(false);//设置进度条是否为不明确
            mDialog.setCancelable(true);//设置进度条是否可以按退回键取消
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    // TODO Auto-generated method stub
                    mDialog=null;
                }
            });
            mDialog.show();

        }
    }
    private void closeProgressDialog(){
        if(mDialog!=null){
            mDialog.dismiss();
            mDialog=null;
        }
    }
    public Intent getFileIntent(File file){
//       Uri uri = Uri.parse("http://m.ql18.com.cn/hpf10/1.pdf");
        Uri uri = Uri.fromFile(file);
        String type = getMIMEType(file);
        Log.i("tag", "type="+type);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, type);
        return intent;
    }

    public void writeToSDCard(String fileName,InputStream input){

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File directory=Environment.getExternalStorageDirectory();
            File file=new File(directory,fileName);
//          if(file.exists()){
//              Log.i("tag", "The file has already exists.");
//              return;
//          }
            try {
                FileOutputStream fos = new FileOutputStream(file);
                byte[] b = new byte[2048];
                int j = 0;
                while ((j = input.read(b)) != -1) {
                    fos.write(b, 0, j);
                }
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else{
            Log.i("tag", "NO SDCard.");
        }
    }

    private String getMIMEType(File f){
        String type="";
        String fName=f.getName();
      /* 取得扩展名 */
        String end=fName.substring(fName.lastIndexOf(".")+1,fName.length()).toLowerCase();

      /* 依扩展名的类型决定MimeType */
        if(end.equals("pdf")){
            type = "application/pdf";//
        }
        else if(end.equals("m4a")||end.equals("mp3")||end.equals("mid")||
                end.equals("xmf")||end.equals("ogg")||end.equals("wav")){
            type = "audio/*";
        }
        else if(end.equals("3gp")||end.equals("mp4")){
            type = "video/*";
        }
        else if(end.equals("jpg")||end.equals("gif")||end.equals("png")||
                end.equals("jpeg")||end.equals("bmp")){
            type = "image/*";
        }
        else if(end.equals("apk")){
        /* android.permission.INSTALL_PACKAGES */
            type = "application/vnd.android.package-archive";
        }
//      else if(end.equals("pptx")||end.equals("ppt")){
//        type = "application/vnd.ms-powerpoint";
//      }else if(end.equals("docx")||end.equals("doc")){
//        type = "application/vnd.ms-word";
//      }else if(end.equals("xlsx")||end.equals("xls")){
//        type = "application/vnd.ms-excel";
//      }
        else{
//        /*如果无法直接打开，就跳出软件列表给用户选择 */
            type="*/*";
        }
        return type;
    }
}
