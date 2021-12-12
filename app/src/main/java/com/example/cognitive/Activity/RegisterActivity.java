package com.example.cognitive.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cognitive.R;
import com.example.cognitive.Utils.DestroyActivityUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import mehdi.sakout.fancybuttons.FancyButton;

public class RegisterActivity extends AppCompatActivity {
    private SharedPreferences sp; // sharedPerferences实现记住密码和自动登录
    private String userPhoneValue, passwordValue;

    String TAG = LoginActivity.class.getCanonicalName();
    private EditText et_data_uphone;
    private EditText et_data_upass;
    private HashMap<String, String> stringHashMap;
    private FancyButton button_signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //DestroyActivityUtil.addActivity(RegisterActivity.this);
        //获取输入框数据
        et_data_uphone = (EditText) findViewById(R.id.et_data_uphone);
        et_data_upass = (EditText) findViewById(R.id.et_data_upass);
        stringHashMap = new HashMap<>();
        sp = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        // button
        button_signup = findViewById(R.id.btn_signup);
        button_signup.setOnClickListener(MyListener);
    }
    private View.OnClickListener MyListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            stringHashMap.put("userphone", et_data_uphone.getText().toString());
            stringHashMap.put("password", et_data_upass.getText().toString());
            new Thread(postRun).start();
        }
    };
    Runnable postRun = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            requestPost(stringHashMap);
        }
    };
    /**
     * post提交数据
     *
     * @param paramsMap
     */
    private void requestPost(HashMap<String, String> paramsMap) {
        int code = 20;
        try {
            String baseUrl = "http://101.132.97.43:8080/ServiceTest/servlet/RegisterServlet";
            //合成参数
            StringBuilder tempParams = new StringBuilder();
            int pos = 0;
            for (String key : paramsMap.keySet()) {
                if (pos >0) {
                    tempParams.append("&");
                }
                tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
                pos++;
            }
            String params = tempParams.toString();
            Log.e(TAG,"params--post-->>"+params);
            // 请求的参数转换为byte数组
//            byte[] postData = params.getBytes();
            // 新建一个URL对象
            URL url = new URL(baseUrl);
            // 打开一个HttpURLConnection连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            // 设置连接超时时间
            urlConn.setConnectTimeout(5 * 1000);
            //设置从主机读取数据超时
            urlConn.setReadTimeout(5 * 1000);
            // Post请求必须设置允许输出 默认false
            urlConn.setDoOutput(true);
            //设置请求允许输入 默认是true
            urlConn.setDoInput(true);
            // Post请求不能使用缓存
            urlConn.setUseCaches(false);
            // 设置为Post请求
            urlConn.setRequestMethod("POST");
            //设置本次连接是否自动处理重定向
            urlConn.setInstanceFollowRedirects(true);
            //配置请求Content-Type
//            urlConn.setRequestProperty("Content-Type", "application/json");//post请求不能设置这个
            // 开始连接
            urlConn.connect();

            // 发送请求参数
            PrintWriter dos = new PrintWriter(urlConn.getOutputStream());
            dos.write(params);
            dos.flush();
            dos.close();
            // 判断请求是否成功
            if (urlConn.getResponseCode() == 200) {
                // 获取返回的数据
                String result = streamToString(urlConn.getInputStream());
                Log.e(TAG, "Post方式请求成功，result--->" + result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject != null) {
                        code = jsonObject.optInt("code");
                    }
                    switch (code){
                        case -1 : // 已有账号，注册失败
                            Looper.prepare();
                            Toast.makeText(RegisterActivity.this,"该手机号已注册过账号", Toast.LENGTH_LONG).show();
                            Looper.loop();
                            break;
                        case 0: // 注册成功
                            Looper.prepare();
                            Toast.makeText(RegisterActivity.this,"注册成功", Toast.LENGTH_LONG).show();
                            Intent intent2 = new Intent();
                            intent2.setClassName(this,"com.example.cognitive.Activity.LoginActivity");
                            this.startActivity(intent2);
                            Looper.loop();
                            break;
                        default:
                            //Toast.makeText(RegisterActivity.this,"用户名或密码错误，请重新登录", Toast.LENGTH_LONG).show();
                            break;
                    }
                }catch (JSONException e)
                {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Post方式请求失败");
            }
            // 关闭连接
            urlConn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
    /**
     * 将输入流转换成字符串
     *
     * @param is 从网络获取的输入流
     * @return
     */
    public String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] byteArray = baos.toByteArray();
            return new String(byteArray);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}