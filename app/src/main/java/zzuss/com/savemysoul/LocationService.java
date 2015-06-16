package zzuss.com.savemysoul;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;


public class LocationService extends Service implements LocationListener {

    int radius = 10000;//in m
    boolean isrunning = false;
    LocationManager lm;
    String provider;
    Geocoder gc;
    SmsManager sm;
    List<GetPlacesTask> threadlist;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
//        Log.d("loc","serv binded");
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        Log.d("loc", "LOC serv created");
        threadlist = new ArrayList<GetPlacesTask>();
        lm = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        provider = lm.getBestProvider(new Criteria(), true);
        gc = new Geocoder(getBaseContext(), Locale.ENGLISH);
        sm = SmsManager.getDefault();

        
        //      Log.d("loc","provider:"+provider);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        if (!isrunning) {
            Log.d("loc", "on start service");
            lm.requestLocationUpdates(provider, Utils.MIN_DIST, Utils.MIN_TIME, this);
            isrunning = true;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    //On Location update run a thread to get nearby hospitals and send them msg
    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
//Log.d("loc","location changed");
        try {
            Log.d("loc", location.getLatitude() + ":" + location.getLongitude());
            GetPlacesTask task = new GetPlacesTask(new String[]{"hospital"}, location, radius, sm, gc);
            task.execute();
            threadlist.add(task);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("loc", "!!!!!!!!!!! getplaces exception");
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
        lm.removeUpdates(this);
        provider = lm.getBestProvider(new Criteria(), true);
        lm.requestLocationUpdates(provider, 400, 1, this);
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
        lm.removeUpdates(this);
        provider = lm.getBestProvider(new Criteria(), true);
        lm.requestLocationUpdates(provider, 400, 1, this);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDestroy() {
        Log.d("loc", "LOC SERV STOPD");
        isrunning = false;
        Iterator<GetPlacesTask> i = threadlist.iterator();
        while (i.hasNext())
            i.next().cancel(true);

        super.onDestroy();
    }
}
