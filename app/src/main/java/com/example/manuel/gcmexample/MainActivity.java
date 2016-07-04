package com.example.manuel.gcmexample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Manuel on 02/06/2016.
 */

public class MainActivity extends Activity {
    private BroadcastReceiver mRegitrationBroadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRegitrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //check type of intent filter
                if(intent.getAction().endsWith(GCMRegistrationIntenService.REGISTRATION_SUCCESS)){
                     String token = intent.getStringExtra("token");
                    Toast.makeText(getApplicationContext(),"GCM token:" +token, Toast.LENGTH_LONG).show();
                    ConnectivityManager connMgr = (ConnectivityManager)
                            getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                       // new postTask().execute(token);
                    } else {
                        // Mostrar errores
                    }
                }else if (intent.getAction().equals(GCMRegistrationIntenService.REGISTRATION_ERROR)){
                    //Registration error
                    Toast.makeText(getApplicationContext(),"GCM registration error!!!",Toast.LENGTH_LONG).show();
                }else{
                    // To be define
                }
            }
        };
        //Check status of Google play service in device
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (ConnectionResult.SUCCESS !=resultCode){
            // Check type of error
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                Toast.makeText(getApplicationContext(),"Google Play Services is not install/enabled in this device", Toast.LENGTH_LONG).show();
                // So notification
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());
            }else{
                Toast.makeText(getApplicationContext(),"This device does not support for Google Play Service!",Toast.LENGTH_LONG).show();
            }
        }else{
            //Start service
            Intent itent = new Intent(this,GCMRegistrationIntenService.class);
            startService(itent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w("MainActivity", "onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegitrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntenService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegitrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntenService.REGISTRATION_ERROR));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w("MainActivity", "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegitrationBroadcastReceiver);
    }
    private class postTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String token = params[0];
            HashMap hm = new HashMap();
            hm.put("token",token);
            hm.put("idUsuario","a0c55983-c7bd-48c3-997e-8c0699d9f08b");

            String respuesta = performPostCall(hm);
            return respuesta;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
        }

        public String performPostCall(HashMap<String, String> postDataParams) {

            URL url;
            String response = "";
            try {
                url = new URL("http://babysmart.cloudapp.net/api/moviljs");

                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
                writer.write(getPostDataString(postDataParams));
                writer.flush();
                writer.close();
                os.close();
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK){
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine())!=null){
                        response+=line;
                        Log.d("output lines",line);
                    }

                }else{
                    response = "";
                }

                } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (ProtocolException e1) {
                e1.printStackTrace();
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return  response;
        }

        private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, String> entry : params.entrySet()){
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }

            return result.toString();
        }

        }




}

