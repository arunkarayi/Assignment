package com.smartbeings.assignment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.http.util.ByteArrayBuffer;

public class Login extends AppCompatActivity {

    EditText usernameetxt,passwordetxt;
    Button signin_btn;
    String username=null,password=null;
    private Login_auth_data login_result;
    ProgressBar pb;

    private static final int CODE_ERROR = 1;
    private static final int CODE_OK = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameetxt = (EditText) findViewById(R.id.username_etxt);
        passwordetxt = (EditText) findViewById(R.id.password_etxt);
        signin_btn = (Button) findViewById(R.id.signin_btn);
        pb = (ProgressBar) findViewById(R.id.progbar);
        pb.setVisibility(View.INVISIBLE);

        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    username = usernameetxt.getText().toString();
                    password = passwordetxt.getText().toString();

                    if (username.equals(""))
                    {
                        FlashMessage("Enter username");
                    }
                    else if (password.equals(""))
                    {
                        FlashMessage("Enter password");
                    }
                    else {
                        if (checkConnectivity())
                        {
                            pb.setVisibility(View.VISIBLE);
                            signin_btn.setEnabled(false);
                            System.out.println("pressed");
                            callService();
                        }
                        else {
                            FlashMessage("No internet connection");
                        }
                    }

                }catch (Exception ex)
                {
                    FlashMessage("Error "+ex.toString());
                }

            }
        });
    }

    private boolean checkConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        NetworkInfo infoMobi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo infoWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo.State connectionMobi = NetworkInfo.State.DISCONNECTED;
        NetworkInfo.State connectionWifi = NetworkInfo.State.DISCONNECTED;
        if (infoMobi != null)
            connectionMobi = infoMobi.getState();
        if (infoWifi != null)
            connectionWifi = infoWifi.getState();
        return (connectionMobi == NetworkInfo.State.CONNECTED) || (connectionWifi == NetworkInfo.State.CONNECTED);
    }

    private void callService()
    {
//        dialog = ProgressDialog.show(this, "Loading", "Calling GeoNames web service...", true, false);
        Thread loader = new Thread()
        {
            public void run()
            {
                Looper.prepare();
                login_result = new Login_auth_data();
                boolean error = false;
                String webUrl = "https://app.legrooms.com/api/authenticate";
                String response = "";
                try{
                    response = readStringFromUrl(webUrl);
                    System.out.println("response : "+response);
                    login_result = new Gson().fromJson(response, Login_auth_data.class);
                }
                catch (IOException | IllegalStateException | JsonSyntaxException e) {
                    // IO exception
                    Log.e("", e.getMessage(), e);
                    error = true;
                }

                if (error) {
                    // error: notify the error to the handler.
                    handler.sendEmptyMessage(CODE_ERROR);
                }
                else {
                    // everything ok: tell the handler to show cities list.
                    handler.sendEmptyMessage(CODE_OK);
                }
            }
        };
        loader.start();
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            signin_btn.setEnabled(true);
            pb.setVisibility(View.INVISIBLE);
//            dialog.dismiss();
            if (msg.what == CODE_ERROR) {
                Toast.makeText(Login.this, "Service error.", Toast.LENGTH_SHORT).show();
            }
            else if (login_result != null) {
                Log.i("", "login success : " + login_result.getSuccess());
                String name = login_result.getSuccess();
                String company = login_result.getMessage();
//                buildList();
                if (login_result.getSuccess().equals("true")) {
                    Intent intent = new Intent(Login.this,Home.class);
                    startActivity(intent);
                }
                else{
                    FlashMessage("Incorrect Email id or Password");
                }
            }
        }
    };

    private void FlashMessage(final String error)
    {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    private String readStringFromUrl(String fileURL) throws IOException {

        InputStream is = null;
        BufferedInputStream bis = null;
        ByteArrayBuffer bufH = new ByteArrayBuffer(512);
        byte[] bufL = new byte[512];

        if (checkConnectivity()) {

            try {
                URL url = new URL(fileURL);

                long startTime = System.currentTimeMillis();

                Log.d("", "Started download from URL " + url);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                String urlParameters  = "{\"email\":\""+username+"\",\"password\":\""+password+"\"}";
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("email",username);
                urlConnection.setRequestProperty("password",password);
                urlConnection.setRequestProperty("Content-Type","application/json");

                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
                writer.write(urlParameters);
                writer.close();

                is = new BufferedInputStream(urlConnection.getInputStream());
                System.out.println("result : "+is.toString());

                int read;
                do {
                    read = is.read(bufL, 0, bufL.length);
                    if (read > 0) bufH.append(bufL, 0, read);
                } while (read >= 0);

                Log.d("", "completed download in " + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
                Log.d("", "downloaded " + bufH.length() + " byte");

                String text = new String(bufH.toByteArray()).trim();
                return text;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                throw ioe;
            }
            finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            throw new IOException("Download error: no connection");
        }
    }
}
