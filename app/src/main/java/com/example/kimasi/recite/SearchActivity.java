package com.example.kimasi.recite;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    Handler handler;
    SearchView searchView;
    String strUTF8;
    String jieguo;
    TextView jieguo111;
    TextView sousuo111;
    InputStream inputStream;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchView=(SearchView)findViewById(R.id.search_aa);
        jieguo111=(TextView)findViewById(R.id.jieguo111);
        sousuo111=(TextView)findViewById(R.id.sousuo111);
        handler = new Handler(){
            public void handleMessage(Message msg) {
                if (msg.what==0x1233) {

                    Bundle bundle=msg.getData();
                    String sou=bundle.getString("sousuo");
                    String ss=bundle.getString("ss");
                    System.out.println(sou);
                    jieguo111.setText(sou);
                    sousuo111.setText(ss);

                }
                super.handleMessage(msg);
            }
        };

        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("在线搜索");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)){//搜索框空

                }
                else{  //


                    Log.v("dddddd","==============");
                    new Thread(new MyThread3(newText)).start();
                    Log.v("dddddd","-------");

                }
                return true;
            }
        });
    }


    public class MyThread3 implements Runnable {
        String sss;
        public  MyThread3(String ss){
            sss=ss;
        }

        public void run() {
            URL url=null;
            try {
                url=new URL( "http://fanyi.youdao.com/openapi.do?keyfrom=" +
                        "ligoudan&key=998459297&type=data&doctype=json&version=1.1&q="+sss );
            }catch (MalformedURLException e){}

            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setConnectTimeout(3000);        //设置连接超时时间
                httpURLConnection.setDoInput(true);               //打开输入流，以便从服务器获取数据
                // httpURLConnection.setDoOutput(true);           //打开输出流，以便向服务器提交数据
                httpURLConnection.setRequestMethod("GET");        //设置以get方式提交数据
                // httpURLConnection.setUseCaches(false);         //使用Post方式不能使用缓存
                //设置请求体的类型是文本类型
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                //设置请求体的长度
                //获得输出流，向服务器写入数据

                Log.v("响应吗","8====");
                int response = httpURLConnection.getResponseCode();  //获得服务器的响应码
                Log.v("响应吗",response+"");
                inputStream = httpURLConnection.getInputStream();
                byte[] data2 = new byte[1024];
                int len = 0;
                while ((len = inputStream.read(data2)) != -1) {
                    String sousuo = new String(data2);
                    Log.e("ssssss===",sousuo);
                    try {
                        JSONObject jsonObject = new JSONObject(sousuo);
                        JSONObject jsonObject1=jsonObject.getJSONObject("basic");
                        JSONArray jsonArray=jsonObject1.getJSONArray("explains");

                        String ss="";
                        for (int i=0;i<jsonArray.length();i++){
                            String s=jsonArray.getString(i);
                            ss+=s+"\n";
                        }
                        Message message=new Message();
                        message.what=0x1233;
                        Bundle bundle=new Bundle();
                        bundle.putString("sousuo",ss);
                        bundle.putString("ss",sss);
                        message.setData(bundle);
                        handler.sendMessage(message);

                    }catch (JSONException e){
                        Log.e("+++++++++++","666666");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public class MyThread implements Runnable {//百度翻译api，不好用，只有一个词
        String sss;
        public  MyThread(String ss){
            sss=ss;
        }
        public void run() {
            Log.v("点击", "dddddd");
            Map<String, String> params = new HashMap<String, String>();
            String q = sss;
            String appid = "20160129000009766";
            String salt = "4684864884";
            String miyao =  "bQ2aZ6_yNY0LY24XcciP";
            String sign = "";
            try {
                System.out.println("sign=="+appid + q + salt + miyao);
                sign = getMD5( appid + q + salt + miyao);
            } catch (NoSuchAlgorithmException e) {
            }
            params.put("q", q);
            params.put("from", "en");
            params.put("to", "zh");
            params.put("appid", appid);
            params.put("salt", salt);
            params.put("sign", sign);
            jieguo =submitPostData(params, "utf-8");

            try {
                JSONObject jsonObject = new JSONObject(jieguo);
                JSONArray jsonArray=jsonObject.getJSONArray("trans_result");
                JSONObject jsonObject1= jsonArray.getJSONObject(0);
                String ss=jsonObject1.getString("dst");
                Log.e("sssssssss",ss);
                Message message=new Message();
                message.what=0x1233;
                Bundle bundle=new Bundle();
                bundle.putString("ss",ss);
                message.setData(bundle);
                handler.sendMessage(message);
            }catch (JSONException e){}
            try {
                strUTF8 = URLDecoder.decode(jieguo, "UTF-8");
            }catch (UnsupportedEncodingException e){}
            Log.v("结果==", strUTF8);


        }
    }
    public static String submitPostData(Map<String, String> params, String encode) {

        URL url=null;
        try {
            url=new URL("http://api.fanyi.baidu.com/api/trans/vip/translate");
        }catch (MalformedURLException e){}

        byte[] data = getRequestData(params, encode).toString().getBytes();//获得请求体
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(3000);        //设置连接超时时间
            httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
            httpURLConnection.setRequestMethod("POST");     //设置以Post方式提交数据
            httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
            //设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);
            Log.v("响应吗","====");
            int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
            Log.v("响应吗",response+"");
            if(response == HttpURLConnection.HTTP_OK) {
                InputStream inptStream = httpURLConnection.getInputStream();
                return dealResponseResult(inptStream);                     //处理服务器的响应结果
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static String dealResponseResult(InputStream inputStream) {
        String resultData = null;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }
    public static String getMD5(String val) throws NoSuchAlgorithmException
    {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(val.getBytes());
        byte[] m = md5.digest();//加密
        //       return getString(m);

        int i2;
        StringBuffer buf = new StringBuffer("");
        for (int offset = 0; offset < m.length; offset++) {
            i2 = m[offset];
            if (i2 < 0) i2 += 256;
            if (i2 < 16)
                buf.append("0");
            buf.append(Integer.toHexString(i2));
        }

        System.out.println("result32: " + buf.toString());//32位的加密

        System.out.println("result: " + buf.toString().substring(8, 24));//16位的加密
        return buf.toString();
    }
    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
            StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
              try {
                    for(Map.Entry<String, String> entry : params.entrySet()) {
                               stringBuffer.append(entry.getKey())
                                       .append("=")
                                       .append(URLEncoder.encode(entry.getValue(), encode))
                                       .append("&");
                         }
                    stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
                  } catch (Exception e) {
                e.printStackTrace();
                   }
          return stringBuffer;
         }
    public static String sendPost(String fanyi){
        String canshu="";
        try {
                canshu=URLEncoder.encode(fanyi, "UTF-8");
        }catch (IOException e ){}
        String params="";
        String sss="";

             params = "type=AUTO" +
                    "&i=" +canshu +        //register"  +
                    "&doctype=json" +
                    "&xmlVersion=1.8" +
                    "&keyfrom=fanyi.web" +
                    "&ue=UTF-8" +
                    "&action=FY_BY_CLICKBUTTON" +
                    "&typoResult=true";

        InputStream inputStream;
        OutputStream out;
        try{

            Log.v("canshu====",params);
            URL realUrl=new URL("http://fanyi.youdao.com/translate?smartresult=dict&smartresult=rule&smartresult=ugc&sessionFrom=null");
            byte[] data = params.getBytes();
            HttpURLConnection conn=(HttpURLConnection)realUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(3000);
            conn.setRequestProperty("Accept","*/*");
            conn.setRequestProperty("Connection","keep-alive");
            conn.setRequestProperty("Accept-Language", "zh-CN");
            conn.setRequestProperty("Content-Length", "116");
            //        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");//会没数据
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            out=conn.getOutputStream();
            out.write(data);
            Log.v("senPost= = ", "++++");

            Integer response = conn.getResponseCode();
            Log.v("senPost= = ", "----");

            Log.v("senPost= = ", response.toString());

            inputStream = conn.getInputStream();
            byte[] data2=new byte[1024];
            int len=0;
            while ((len=inputStream.read(data2))!=-1) {
                sss = new String(data2);
            }
        }catch( Exception e){
        }


        finally {

        } Log.v("senPost= ", sss);
        return sss;
    }
/*
 {
"type":"EN2ZH_CN",
"errorCode":0,
"elapsedTime":1,
"translateResult":[[{"src":"register","tgt":"注册"}]],  //大括号算一个对象，中括号是数组

"smartResult":{"type":1,
"entries":["",
"n. 登记；注册；记录；寄存器；登记簿",
"vt. 登记；注册；记录；挂号邮寄；把\u2026挂号；正式提出",
"vi. 登记；注册；挂号"]  }  }

应该是包含两个json，
 */

    public String Jsonjiexi(String json){
        String aa1="";
        try{

            JSONObject jsonObject=new JSONObject(json);
            String ss=jsonObject.getString("type");
            JSONArray jsonArray=jsonObject.getJSONArray("translateResult");
            JSONArray jsonArray1=jsonArray.getJSONArray(0);
            JSONObject jsonObject1=jsonArray1.getJSONObject(0);
            String ss1=jsonObject1.getString("src");
            String ss2=jsonObject1.getString("tgt");

            JSONObject jsonObject11=jsonObject.getJSONObject("smartResult");
            JSONArray jsonArray2=jsonObject11.getJSONArray("entries");

            jsonArray2.length();//记得用长度来取值
             aa1=jsonArray2.getString(0);
            String aa2=jsonArray2.getString(1);
            String aa3=jsonArray2.getString(2);
            String aa4=jsonArray2.getString(3);

            Log.v("=translateResult= ",jsonObject.getString("translateResult"));//返回数组
            Log.v("=smartResult= ",jsonObject.getString("smartResult"));  //返回jsonObject，再解析
            Log.v("=== ",aa1+" "+aa2+" "+aa3+" "+aa4);

            return aa1;


        }catch (JSONException e){

        }
        return aa1;
    }




}
