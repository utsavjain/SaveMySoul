package zzuss.com.savemysoul;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity implements View.OnTouchListener {


    HashMap<Time, Location> locations;
    LocationManager lm;
    Location curlc,bestlc;
    String provider;
    boolean isTouched=false;
    Geocoder gc;
    Intent locser;
    Menu menu;
    SharedPreferences pref;
    EditText input,pcur,pnew,pcon;
Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(android.R.id.content).setOnTouchListener(this);
        pref=getPreferences(Context.MODE_PRIVATE);
        context=getBaseContext();
        if (pref.getBoolean("firstrun", true)) {
            pref.edit().putBoolean("firstrun", false).commit();
            pref.edit().putString("PASSWORD","1234").commit();
           }
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent arg1) {
        // TODO Auto-generated method stub
        Log.d("loc", "onTouchEvent");
        locser=new Intent(getBaseContext(), LocationService.class);
        startService(locser);
        menu.findItem(R.id.action_off).setEnabled(true);
        
        return false;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu=menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()){
            case R.id.action_off:

                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Turn OFF!");
                alert.setMessage("Enter Password");
                input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
                alert.setView(input);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d("loc",input.getText().toString());
                        if(input.getText().toString().equals(pref.getString("password","1234"))){
                            stopService(locser);
                            menu.findItem(R.id.action_off).setEnabled(false);

                            Toast.makeText(getBaseContext(),"HELP CALL OFF!!",Toast.LENGTH_LONG).show();

                        }else
                            Toast.makeText(context,"Call to HELP will Continue",Toast.LENGTH_LONG).show();
                  }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getBaseContext(),"Call to Help will continue",Toast.LENGTH_LONG).show();
                    }
                });
                alert.show();
                break;
            case R.id.action_settings:
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                View v=new View(this);
                LinearLayout layout=new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);
                pcur=new EditText(this);
                pnew=new EditText(this);
                pcon=new EditText(this);

                pcur.setHint("current pass");
                pnew.setHint("new pass");
                pcon.setHint("confirm password");

                pcur.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
                pnew.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
                pcon.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);

                layout.addView(pcur);
                layout.addView(pnew);
                layout.addView(pcon);

                builder.setView(layout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (pcur.getText().toString().equals(pref.getString("password", "1234"))) {
                            if (pnew.getText().toString().equals(pcon.getText().toString())) {
                                pref.edit().putString("password", pcon.getText().toString()).commit();
                                Toast.makeText(getBaseContext(), "Password changed", Toast.LENGTH_LONG).show();
                            }
                            else{
                                Toast.makeText(getBaseContext(), "New PASSWRD and Current NOT Match!!", Toast.LENGTH_LONG).show();
                            }
                        }
                        else
                            Toast.makeText(getBaseContext(),"Password NOT changed Wrong CURRENT PASSWORD",Toast.LENGTH_LONG).show();
                    }
                    });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getBaseContext(), "Password NOT changed", Toast.LENGTH_LONG).show();
                    }
                });
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
