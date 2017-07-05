package com.smartbeings.assignment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.MapView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class Home extends AppCompatActivity implements MessageFragment.OnFragmentInteractionListener,SearchFragment.OnFragmentInteractionListener,BookingsFragment.OnFragmentInteractionListener, profile.OnFragmentInteractionListener{

    private static final int CODE_ERROR = 1;
    private static final int CODE_OK = 0;
    private appList datalist;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.search:
                    switchToFragment1();
                    return true;
                case R.id.messages:
                    switchToFragment2();
                    return true;
                case R.id.bookings:
                    switchToFragment3();
                    return true;
                case R.id.you:
                    switchToFragment4();
                    return true;
            }
            return false;
        }

    };
    private BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        navigation = (BottomNavigationView) findViewById(R.id.navigation_tab);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        callService();
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
                datalist = new appList();
                boolean error = false;
                String webUrl = "https://app.legrooms.com/api/listing/Meeting/Bangalore";
                String response = "";
                try{
                    response = readStringFromUrl(webUrl);
                    System.out.println("response : "+response);
//                    datalist = new Gson().fromJson(response, appList.class);
                    JSONObject jsonObj = new JSONObject(response);
                    JSONArray listingsarray = jsonObj.getJSONArray("listings");
                    for (int i=0;i<listingsarray.length();i++)
                    {
                        JSONObject item = listingsarray.getJSONObject(i);
                        listings listings = new listings();
                        listings._id = item.getString("_id");
                        listings.activity = item.getString("activity");
                        listings.address = item.getString("address");
                        try {
                            listings.latitude = item.getString("latitude");
                            listings.longitude = item.getString("longitude");
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        datalist.getListingses().add(listings);
                    }
                }
                catch (IOException | IllegalStateException | JsonSyntaxException e) {
                    // IO exception
                    Log.e("", e.getMessage(), e);
                    error = true;
                } catch (JSONException e) {
                    e.printStackTrace();
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
//            dialog.dismiss();
            if (msg.what == CODE_ERROR) {
                Toast.makeText(Home.this, "Service error.", Toast.LENGTH_SHORT).show();
            }
            else if (datalist != null && datalist.getListingses() != null) {
                System.out.println("halooooooooooo");
                Log.i("", "activity address : " + datalist.getSuccess());
                Log.i("", "activity address : " + datalist.getListingses().size());
                navigation.setSelectedItemId(R.id.search);
//                buildList();
//                if (login_result.getSuccess().equals("true")) {
////                    Intent intent = new Intent(LoginPage.this,menu_page.class);
////                    startActivity(intent);
//                }
//                else{
//                    FlashMessage("Incorrect Email id or Password");
//                }
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

    public void switchToFragment2() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new MessageFragment()).commit();
    }

    public void switchToFragment3() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new BookingsFragment()).commit();
    }

    public void switchToFragment4() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new profile()).commit();
    }

    public void switchToFragment1() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new SearchFragment()).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public appList getYourObjects() {
        return datalist;
    }
}
