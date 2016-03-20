package com.example.sundar.droidkey;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class OnSmsReceive extends AppCompatActivity {

    Bundle bundle;
    String message;
    TextView sender;
    Button Add;
    Button Discard;
    EditText lkName;
    String code;
    String factname;
    DBHelper keydb;
    Cursor myCursor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_sms_receive);
        keydb = new DBHelper(this);
        myCursor = keydb.populate_list_admin();

        Add=(Button)findViewById(R.id.button_addnew);
        Discard = (Button)findViewById(R.id.button_discard);
        lkName = (EditText)findViewById(R.id.editText_name);



        sender = (TextView)findViewById(R.id.textview_sender);
        View mview = findViewById(R.id.onsmsrecieveview);

        Intent intent = this.getIntent();
        bundle = intent.getBundleExtra("mySms");
        int index;




        if (bundle != null) {
            Log.e("Hi", "smsReceiver : Reading Message");



            Object[] pdus = (Object[])bundle.get("pdus");
            SmsMessage sms = SmsMessage.createFromPdu((byte[])pdus[0]);

            if(sms.getMessageBody().contains("*DK*")){
                //launchIntent();
                message = sms.getMessageBody();
                index = message.indexOf(':');
                code=message.substring(index + 1, index + 5);
                index=message.indexOf("*DK*");
                factname = message.substring(index,index+8);


                sender.setText(sms.getOriginatingAddress());
                Log.e("Hi",code);




            }
        }else{
            Log.e("Hi","Bundle is null");
        }

        for(int i=0;i<myCursor.getCount();i++){
            if(myCursor.getString(3).equals(factname)){
                Add.setVisibility(View.GONE);
                Snackbar.make(mview, "Key already exists. Press Discard", Snackbar.LENGTH_LONG).show();
                break;
            }
            myCursor.moveToNext();
        }



    }

    public void addNew(View v){
        MainActivity.mydb.insertLock(lkName.getText().toString().trim(), factname, Integer.parseInt(code), 0);
        Log.e("Hi","Key added");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void discard(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
