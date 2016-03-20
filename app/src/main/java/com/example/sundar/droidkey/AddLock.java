package com.example.sundar.droidkey;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;

public class AddLock extends AppCompatActivity {


    WifiManager wifi;

    TextView startscan;
    TextView lockname;
    EditText dblockname;
    EditText dbkey;
    LinearLayout lockfound;
    String wifis[];
    String defname;
    private AlertDialog alertDialog;
    WifiScanReceiver wifiReciever;
    int flag=1;
    int admin=1;
    ConnectivityManager connectivityManager;
    String serverResponse="";
    View mview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lock);
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("Please wait")
                .setCancelable(true)
                .create();
        startscan=(TextView)findViewById(R.id.startscan);
        lockfound=(LinearLayout)findViewById(R.id.resultlayout);
        lockname =(TextView)findViewById(R.id.lockfound);
        lockfound.setVisibility(View.GONE);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        dblockname = (EditText) findViewById(R.id.editText_lockname);
        dbkey = (EditText) findViewById(R.id.editText_master);
        //wifi.startScan();
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mview = findViewById(R.id.addlayout);





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_lock, menu);
        return true;
    }

    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_scan) {
            alertDialog.setMessage("Scanning for new locks...");

            if(!alertDialog.isShowing())
            {
                alertDialog.show();
            }
            flag=0;

            wifi.startScan();

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public void addlock(View v){
        if(dblockname.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Please enter a name",Toast.LENGTH_SHORT).show();
            Snackbar.make(v,"hi",Snackbar.LENGTH_SHORT).show();
        }
        else if(dbkey.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Please enter the Key",Toast.LENGTH_SHORT).show();
        }else {
            try {
                URI url;
                String[] params = new String[2];
                url = new URI("http://"+"192.168.4.1"+"/?MKey="+dbkey.getText().toString());
                findLock mfindLock = new findLock(v.getContext());
                params[0]=url.toString();
                params[1]="Could not reach the lock. Check if the lock is in your network and turned on";
                mfindLock.execute(params);
                Log.e("Hi", url.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }




        }


    }

    protected void onPause() {
        unregisterReceiver(wifiReciever);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }



    private class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            List<ScanResult> wifiScanList = wifi.getScanResults();
            wifis = new String[wifiScanList.size()];
            long starttime = System.currentTimeMillis();
            int mode=0;
            if (flag == 0) {




            for (int i = 0; i < wifiScanList.size(); i++) {
                wifis[i] = ((wifiScanList.get(i)).toString());
                if (wifiScanList.get(i).SSID.equals("DroidKey")) {
                    WifiInfo wifiinfo = wifi.getConnectionInfo();
                    Log.e("Hi",wifiinfo.getSSID());
                    if(wifiinfo.getSSID().equals(wifiScanList.get(i).SSID) && networkInfo.getState()==NetworkInfo.State.CONNECTED)
                    {
                        break;
                    }
                    lockname.setText(wifiScanList.get(i).SSID);

                    WifiConfiguration wifiConfiguration = new WifiConfiguration();

                    wifiConfiguration.SSID = "\""+wifiScanList.get(i).SSID+"\"";
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    //wifiConfiguration.BSSID = wifiScanList.get(i).BSSID;
                    wifiConfiguration.hiddenSSID = false;
                    int inetId = wifi.addNetwork(wifiConfiguration);
                    Log.e("Hi", "" + inetId);
                    wifi.disconnect();
                    Log.e("Hi", "" + wifi.enableNetwork(inetId, true));

                    Log.e("Hi", "" + wifi.reconnect());

                    Log.e("Hi", "" + wifi.reassociate());
                    while(true)
                    {
                        networkInfo = connectivityManager.getActiveNetworkInfo();

                        if((System.currentTimeMillis()-starttime)>10000){
                            mode=1;
                            break;
                        }
                        WifiInfo wifiinfo1 = wifi.getConnectionInfo();
                        Log.e("Hi",wifiinfo1.getSSID());
                        Log.e("Hi",wifiScanList.get(i).SSID);
                        if(networkInfo!=null) {
                            if (wifiinfo1.getSSID().contains(wifiScanList.get(i).SSID) && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                                mode = 2;
                                break;

                            }
                        }
                        Log.e("Hi","*");
                        Log.e("Hi", "" + wifi.enableNetwork(inetId, true));

                        Log.e("Hi", "" + wifi.reconnect());
                    }
                    if(mode==2) {

                        flag = 1;
                        lockfound.setVisibility(View.VISIBLE);
                        startscan.setVisibility(View.GONE);

                        alertDialog.dismiss();
                        defname = wifiScanList.get(i).SSID;


                        Log.e("Hi", wifis[i]);

                        break;
                    }else if(mode==1){
                        alertDialog.setMessage("Couldn't connect to lock. Please retry");
                    }
                }
                if(i==wifiScanList.size()-1){
                    alertDialog.setMessage("Couldn't find lock. Make sure the lock is turned on");

                }


            }
                flag=1;

            }
        }


    }
    class findLock extends AsyncTask<String, Void, Long> {



        String line;
        String responsemsg;
        Long flag;
        Context context;

        public findLock(Context context)
        {
            this.context=context;



        }



        @Override
        protected void onPreExecute() {




            alertDialog.setMessage("Authenticating the master key");
            if(!alertDialog.isShowing())
            {
                alertDialog.show();
            }
        }

        @Override
        protected Long doInBackground(String... params) {
            URI url = URI.create(params[0]);
            responsemsg = params[1];


            // HttpURLConnection urlConnection = null;
            alertDialog.setMessage("Authenticating the master key");
            if(!alertDialog.isShowing())
            {
                alertDialog.show();
            }

            try {
                HttpParams httpParameters = new BasicHttpParams();

                int timeoutConnection = 3000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
//
                int timeoutSocket = 15000;
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

                HttpClient httpclient = new DefaultHttpClient(httpParameters);

                HttpGet getRequest = new HttpGet();
                getRequest.setURI(url);
                HttpResponse response = httpclient.execute(getRequest);
                Log.v("Hi", url.toString());



                InputStream content = null;
                content = response.getEntity().getContent();

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        content
                ));
                line = in.readLine();
                while (!(line==null)) {
                    if (line.isEmpty()) {
                        break;
                    }
                    serverResponse += line;
                    line = in.readLine();
                }

                content.close();
                flag=new Long(1);
            } catch (ClientProtocolException e) {
                // HTTP error
                serverResponse = e.getMessage();
                e.printStackTrace();
                Log.v("Hi", e.toString());
                flag=new Long(1);
            } catch (IOException e) {
                // IO error
                serverResponse = e.getMessage();
                e.printStackTrace();
                Log.v("Hi", e.toString());
                flag=new Long(1);
            }

            return flag;
        }
        @Override
        protected void onPostExecute(Long aVoid) {
            MainActivity.flagerror = flag.intValue();

            if(serverResponse.equals("Invalid"))
            {
                alertDialog.setMessage("Invalid master key.Please enter the valid key");
            }else if(serverResponse.equals("Done")){
                MainActivity.mydb.insertLock(dblockname.getText().toString(), defname, Integer.parseInt(dbkey.getText().toString()), admin);
                Toast.makeText(context, "Lock Added Successfully",Toast.LENGTH_SHORT).show();
                Snackbar.make(mview,"Lock Added Successfully",Snackbar.LENGTH_SHORT).show();
                finish();

            }else{
                alertDialog.setMessage("Problem authenticating the key. Please try again"+serverResponse);
            }

            if(!alertDialog.isShowing())
            {
                alertDialog.show(); // show dialog
            }
        }



    }

}