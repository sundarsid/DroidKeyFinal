package com.example.sundar.droidkey;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.telephony.SmsManager;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

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
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    static SimpleCursorAdapter mAdapter_admin;
    static DBHelper mydb;
    static ListView mlockList;
    static Cursor cursor_admin;
    static DrawerLayout drawer;
    public ActionBarDrawerToggle mDrawerToggle;
    static FragmentManager fragmentManager ;
    static TextView noLocks;
    static LinearLayout drawerLayout;
    static int flagerror;
    static WifiManager wifi;
    static AlertDialog alertDialog;
    static ImageView statusview;
    static DownloadManager downloadManager;

    static List<ScanResult> wifiScanList;
    NetworkInfo networkInfo;
    static ConnectivityManager connectivityManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navdraw_main);
        fragmentManager = getFragmentManager();
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout = (LinearLayout) findViewById(R.id.navLayout);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);


        String[] columns = new String[]{DBHelper.LOCKS_COLUMN_NAME, DBHelper.LOCKS_COLUMN_IP};
        int[] to = new int[]{R.id.lock_list_textview_name, R.id.lock_list_textview_ip};
        mydb = new DBHelper(this);
        cursor_admin = mydb.populate_list_admin();
        noLocks = (TextView) findViewById(R.id.textViewNoLocks);
        mlockList = (ListView) findViewById(R.id.left_drawer);
        mAdapter_admin = new SimpleCursorAdapter(this, R.layout.lock_list, cursor_admin, columns, to);
        mlockList.setAdapter(mAdapter_admin);
        mlockList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ) {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }
        };
        drawer.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        startService(new Intent(getBaseContext(), MyService.class));

        alertDialog = new AlertDialog.Builder(this)
                .setTitle("Please wait")
                .setCancelable(true)
                .create();



    }





    private class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            wifiScanList = wifi.getScanResults();

        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        cursor_admin= mydb.populate_list_admin();
        mAdapter_admin.changeCursor(cursor_admin);
        if(cursor_admin.getCount()>0) {
            noLocks.setVisibility(View.GONE);
            mlockList.setVisibility(View.VISIBLE);
            selectItem(0);

        }
        else {
            noLocks.setVisibility(View.VISIBLE);
            mlockList.setVisibility(View.GONE);
            selectItem(-1);

        }
        Log.e("Hi", "Resuming");
    }

    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            startActivity(new Intent(this, AddLock.class));
            //finish();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
            Log.e("Hi","ListItem"+String.valueOf(position));
        }
    }
//
    private static void selectItem(int position) {
        // update the main content by replacing fragments
        if (position == -1) {
            Fragment fragment = new FragmentEmpty();

            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        } else {

            Fragment fragment = new LockFragment();
            Bundle args = new Bundle();
            args.putInt("ListItem", position);
            fragment.setArguments(args);


            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

            // update selected item and title, then close the drawer
            mlockList.setItemChecked(position, true);
            //setTitle(mPlanetTitles[position]);
            drawer.closeDrawer(drawerLayout);
        }
    }

    public static class FragmentEmpty extends Fragment {

        public FragmentEmpty() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_main_empty, container, false);
        }
    }



    public static class LockFragment extends Fragment  {

        EditText addKey;
        EditText usrname;
        EditText phNumber;
        Button sendInvite;
        ImageButton unlock;
        Cursor cursor1;
        Button viewLog;
        Button delLock;

        CardView KeyCard;
        CardView buttonCard;
        CardView listCard;
        ListView userList;
        Cursor usrcursor;
        SimpleCursorAdapter lockcursor_adapter;
        TextView disname;
        ShowcaseView showcaseView;
        int counter=0;




        public LockFragment() {
            // Empty constructor required for fragment subclasses
        }





        //
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            final int position = getArguments().getInt("ListItem");
            Log.e("Hi","ListItemincall "+String.valueOf(position));
            String[] columns = new String[] { DBHelper.USERS_COLUMN_NAME, DBHelper.USERS_COLUMN_KEY };
            int[] to = new int[] { R.id.lock_list_textview_name, R.id.lock_list_textview_ip };
            KeyCard=(CardView) rootView.findViewById(R.id.card_view_share);
            unlock = (ImageButton) rootView.findViewById(R.id.imageButton_unlock);
            buttonCard = (CardView) rootView.findViewById(R.id.card_view_buttons);
            listCard = (CardView) rootView.findViewById(R.id.card_view_list);
            disname = (TextView) rootView.findViewById(R.id.textView_Lockname);
            statusview = (ImageView)rootView.findViewById(R.id.imageView_status);
            viewLog = (Button) rootView.findViewById(R.id.button_log);
            delLock = (Button) rootView.findViewById(R.id.button_del);
            sendInvite = (Button) rootView.findViewById(R.id.button_send);
            ViewTarget target = new ViewTarget(unlock);

            View.OnClickListener showcaseonclick = new View.OnClickListener() {
                public void onClick(View v) {
                    switch (counter) {
                        case 0:
                            showcaseView.setShowcase(new ViewTarget(sendInvite), true);
                            showcaseView.setContentTitle("Share a Key");
                            showcaseView.setContentText("Enter the number and press send to send a virtual key to others");
                            showcaseView.setButtonText("Fine");
                            break;

                        case 1:
                            showcaseView.setShowcase(new ViewTarget(viewLog), true);
                            showcaseView.setContentTitle("Log");
                            showcaseView.setContentText("Press it to view who opened your lock and when");
                            showcaseView.setButtonText("Got it!!");
                            break;

                        case 2:
                            showcaseView.setTarget(Target.NONE);
                            showcaseView.setContentTitle("Get Started");
                            showcaseView.setContentText("Let's start unlocking");
                            showcaseView.setButtonText("Hurray!!");

                            break;

                        case 3:
                            showcaseView.hide();

                            break;
                    }
                    counter++;
                }
                // do something when the button is clicked

            };
            TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextSize(30);
            paint.setColor(Color.YELLOW);


            TextPaint title = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            title.setTextSize(20);
            title.setColor(Color.RED);






            alertDialog.setMessage("Connecting to the Lock...");

//            if(!alertDialog.isShowing())
//            {
//                alertDialog.show();
//            }

            showcaseView = new ShowcaseView.Builder(getActivity())
                    .setContentTitlePaint(title)
                    .setTarget(target)
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setContentTitle("Unlock Button")
                    .setContentText("Press this to unlock the door")
                    .setContentTextPaint(paint)
                    .setContentTitlePaint(title)
                    .setOnClickListener(showcaseonclick)
                    .build();
            showcaseView.setButtonText("Okay");





             cursor1 = (Cursor) MainActivity.mAdapter_admin.getItem(position);
            final String ipaddress;
            final String key;
            final int id;
            int admin;

            ipaddress = cursor1.getString(3);
            Log.e("Hi",ipaddress);
            key = cursor1.getString(2);
            Log.e("Hi",key);
            id = Integer.parseInt(cursor1.getString(0));
            Log.e("Hi",cursor1.getString(0));
            admin = Integer.parseInt(cursor1.getString(4));
            wifi.startScan();
//            Cursor c = downloadManager.query(new DownloadManager.Query()
//                    .setFilterByStatus(DownloadManager.STATUS_PAUSED
//                            | DownloadManager.STATUS_PENDING
//                            | DownloadManager.STATUS_RUNNING));
            //int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            //Log.e("Hi","status"+status);
//            if(c.moveToFirst()) {


//                Log.e("Hi", "Download count" + c.getCount());
                manageWifi manage= new manageWifi(getActivity());
                manage.execute(ipaddress);
                disname.setText(cursor1.getString(1));
//            }



//            while(true)
//            {
//
//                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//                WifiInfo wifiinfo1 = wifi.getConnectionInfo();
//                Log.e("Hi",wifiinfo1.getSSID());
//
//                if(networkInfo!=null)
//                {
//                    Log.e("Hi",networkInfo.getState().toString());
//
//
//                if(wifiinfo1.getSSID().contains(ipaddress) && networkInfo.getState()==NetworkInfo.State.CONNECTED)
//                {
//                    Log.e("Hi",networkInfo.getState().toString());
//                    break;
//                }}
//                int x;
//                int netid=-1;
//                List<WifiConfiguration> conflist = wifi.getConfiguredNetworks();
//                for(x=0;x<conflist.size();x++)
//                {
//                    if(conflist.get(x).SSID.contains(ipaddress)){
//                     netid=conflist.get(x).networkId;
//                        break;
//                }
//
//                }
//                if(netid==-1)
//                {
//                    WifiConfiguration wifiConfiguration = new WifiConfiguration();
//
//                    wifiConfiguration.SSID = "\""+ipaddress+"\"";
//                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//                    //wifiConfiguration.BSSID = wifiScanList.get(i).BSSID;
//                    wifiConfiguration.hiddenSSID = false;
//                     netid = wifi.addNetwork(wifiConfiguration);
//
//                }
//                wifi.startScan();
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
////                for(int i=0;i<wifiScanList.size();i++)
////                {
////                    if(wifi)
////                }
//
//
//                Log.e("Hi","*");
//                Log.e("Hi", "" + wifi.enableNetwork(netid, true));
//
//                Log.e("Hi", "" + wifi.reconnect());
//                Log.e("Hi", "" + wifi.reassociate());
//            }
//            alertDialog.dismiss();

            if(admin == 1) {

                userList = (ListView) rootView.findViewById(R.id.listView_users);
                usrcursor = MainActivity.mydb.populate_list_users(id);
                lockcursor_adapter = new SimpleCursorAdapter(getActivity(),R.layout.lock_list_user,usrcursor,columns,to);
                userList.setAdapter(lockcursor_adapter);

            //addKey = (EditText) rootView.findViewById(R.id.key);
            usrname = (EditText) rootView.findViewById(R.id.usrname);
            phNumber = (EditText) rootView.findViewById(R.id.phnumber);

                delLock.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        MainActivity.mydb.deleteLock(id);
                        MainActivity.mydb.delete_alluser(id);
                        cursor_admin= mydb.populate_list_admin();
                        mAdapter_admin.changeCursor(cursor_admin);
                        if(cursor_admin.getCount()>0) {
                            selectItem(0);
                            noLocks.setVisibility(View.GONE);
                            mlockList.setVisibility(View.VISIBLE);
                        }
                        else {
                            noLocks.setVisibility(View.VISIBLE);
                            mlockList.setVisibility(View.GONE);
                            selectItem(-1);
                        }


                    }

                });
                viewLog.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        URI url;
                        String[] params = new String[2];
                        try {
                            url = new URI("http://"+"192.168.4.1"+"/?Log");
                            sendData msendData = new sendData(v.getContext());
                            params[0]=url.toString();
                            params[1]="Could not reach the lock. Check if the lock is in your network and turned on";
                            msendData.execute(params);
                            Log.e("Hi", url.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
                unlock.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        URI url;
                        String[] params = new String[2];
                        try {
                            url = new URI("http://"+"192.168.4.1"+"/?CKey="+key+"&Status=0");
                            sendData msendData = new sendData(v.getContext());
                            params[0]=url.toString();
                            params[1]="Could not reach the lock. Check if the lock is in your network and turned on";
                            msendData.execute(params);
                            Log.e("Hi", url.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }

                });
            sendInvite.setOnClickListener(new View.OnClickListener()
            {
                URI url;
                String[] params = new String[2];
                @Override
                public void onClick(View v)
                {
                    Random r = new Random();
                    int keyToAdd = r.nextInt(9999);
                    String phoneNo = phNumber.getText().toString();
                    //String keyToAdd;
                    //keyToAdd=addKey.getText().toString().trim();
                    String usrnm = usrname.getText().toString().trim();
                    String message;
                    message = "DROIDKEY Code:"+keyToAdd;



                    try {
                        url = new URI("http://" + "192.168.4.1" + "/?AKey=" + keyToAdd + "&");
                        sendData msendData = new sendData(v.getContext());
                        params[0]=url.toString();
                        params[1]="Could not reach the lock. Check if the lock is in your network and turned on";
                        msendData.execute(params);
                        Log.e("Hi", url.toString());
                        if(flagerror==1) {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(phoneNo, null, message, null, null);
                            Toast.makeText(getActivity(), "SMS sent.", Toast.LENGTH_LONG).show();
                            MainActivity.mydb.insertUser(usrnm, keyToAdd, id);
                            usrcursor = MainActivity.mydb.populate_list_users(id);
                            lockcursor_adapter.changeCursor(usrcursor);
                        }
                    }

                    catch (Exception e) {
                        Toast.makeText(getActivity(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
        }else if(admin == 0){
                KeyCard.setVisibility(View.GONE);
                buttonCard.setVisibility(View.GONE);
                listCard.setVisibility(View.GONE);
                unlock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        URI url;
                        String[] params = new String[2];
                        try {
                            url = new URI("http://" + "192.168.4.1" + "/?CKey=" + key + "&Status=0");
                            sendData msendData = new sendData(v.getContext());
                            params[0]=url.toString();
                            params[1]="Could not reach the lock. Check if the lock is in your network and turned on";
                            msendData.execute(params);
                            Log.e("Hi", url.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });



            }


            return rootView;
        }

    }

    static class manageWifi extends AsyncTask<String,Void,Void>{

        WifiManager wifi;
        ConnectivityManager connectivityManager;
        int mode=0;





        public manageWifi(Context context){
            wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        }


        @Override
        protected Void doInBackground(String... params) {
            long starttime = System.currentTimeMillis();

            while(true)
            {

                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                WifiInfo wifiinfo1 = wifi.getConnectionInfo();
                Log.e("Hi",wifiinfo1.getSSID());
                if((System.currentTimeMillis()-starttime)>10000){
                    mode=1;
                    break;
                }

                if(networkInfo!=null)
                {
                    Log.e("Hi",networkInfo.getState().toString());


                    if(wifiinfo1.getSSID().contains(params[0]) && networkInfo.getState()==NetworkInfo.State.CONNECTED)
                    {
                        Log.e("Hi",networkInfo.getState().toString());
                        mode=2;
                        break;
                    }}
                int x;
                int netid=-1;
                List<WifiConfiguration> conflist = wifi.getConfiguredNetworks();
                for(x=0;x<conflist.size();x++)
                {
                    if(conflist.get(x).SSID.contains(params[0])){
                        netid=conflist.get(x).networkId;
                        break;
                    }

                }
                if(netid==-1)
                {
                    WifiConfiguration wifiConfiguration = new WifiConfiguration();

                    wifiConfiguration.SSID = "\""+params[0]+"\"";
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    //wifiConfiguration.BSSID = wifiScanList.get(i).BSSID;
                    wifiConfiguration.hiddenSSID = false;
                    netid = wifi.addNetwork(wifiConfiguration);

                }
                wifi.startScan();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                for(int i=0;i<wifiScanList.size();i++)
//                {
//                    if(wifi)
//                }


                Log.e("Hi","*");
                Log.e("Hi", "" + wifi.enableNetwork(netid, true));

                Log.e("Hi", "" + wifi.reconnect());
                Log.e("Hi", "" + wifi.reassociate());
            }
            return null;


        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //alertDialog.setMessage("Lock Connected");
            if(mode==1){
                alertDialog.setMessage("Cannot connect to lock. Check connection");
            }
            if(mode==2){
                alertDialog.setMessage("Lock Connected");
                statusview.setImageResource(R.drawable.oie_transparent);

            }
            if(!alertDialog.isShowing())
            {
                alertDialog.dismiss(); // show dialog
            }
        }

    }


}


class sendData extends AsyncTask<String, Void, Long> {

    private AlertDialog alertDialog;
    String serverResponse="";
    String line;
    String responsemsg;
    Long flag;

    public sendData(Context context)
    {


        alertDialog = new AlertDialog.Builder(context)
                .setTitle("Response from Lock")
                .setCancelable(true)
                .create();
    }



    @Override
    protected void onPreExecute() {




        alertDialog.setMessage("Sending command to server, please wait");
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
        alertDialog.setMessage("Command sent, waiting for reply from lock");
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

        if(flag==0) {
            alertDialog.setMessage(responsemsg + serverResponse);
        }else if(flag==1)
            alertDialog.setMessage(serverResponse);

        if(!alertDialog.isShowing())
        {
            alertDialog.show(); // show dialog
        }
    }



}
