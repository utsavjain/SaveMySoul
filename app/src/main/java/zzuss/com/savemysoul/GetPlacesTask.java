package zzuss.com.savemysoul;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.JsonReader;
import android.util.Log;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestFactory;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GetPlacesTask extends AsyncTask{

    private URL url;
    ArrayList<String> places_id;
    StringBuilder sb;
    SmsManager sm;
    Geocoder gc;
    AsyncTask sms;
    Location loc;
    String phno;

    public GetPlacesTask(String[] types,Location loc,double radius,SmsManager sm,Geocoder gc) throws IOException{
        super();
        Log.d("loc","places task constructor");
        places_id=new ArrayList<String>();
        this.sm=sm;
        this.gc=gc;
        this.loc=loc;
//Build a url for get nearby hospital place_id from GOOGLEPLACES API
        sb = new StringBuilder(Utils.GP_URL);//utils.GP_URL is url to Googleplaces API
        sb.append("location=" + loc.getLatitude() + "," + loc.getLongitude());
        //maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&types=food&name=cruise&key=API_KEY

        sb.append("&radius=" + radius);
        sb.append("&types=");
        for(String i:types)
            if(!i.equals(types[types.length-1]))
                sb.append(i+"|");
            else
                sb.append(i);
        sb.append("&sensor=true");
        sb.append("&key=" + Utils.GP_KEY);
        url=new URL(sb.toString());
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            Log.d("loc","tsk running");
            JSONObject response=getResults(url);
            if(response.has("results")) {
                JSONArray results = response.getJSONArray("results");
             //   Log.d("loc", "res-len-" + results.length());
                 for (int i = 0; i < results.length(); i++) {
                    JSONObject place = results.getJSONObject(i);
                    if(place.has("place_id")) {
                        String id = place.getString("place_id");
                        places_id.add(id);
                        //Build a new url for detailed place search of each place_id of nearby search
                        //to get their contact no.
                        sb = new StringBuilder(Utils.GP_URL_DET);
                        sb.append("placeid=" + id);
                        sb.append("&key=" + Utils.GP_KEY);
                        //Log.d("loc","DT:"+sb.toString());
                        //get JSON response for each place_id
                        response = getResults(new URL(sb.toString()));
                        JSONObject res = response.getJSONObject("result");
                        if (res.has("formatted_address") && res.has("formatted_phone_number")) {
                            //res.getString("formatted_address");
                            phno = res.getString("formatted_phone_number");
                            Log.d("loc", "phnono: " + res.getString("formatted_phone_number"));
                            //send message to the retrieved contact
                            String add=getAddress();
                            Log.d("loc",add);
                           // sm.sendTextMessage(phno, null, "SEND HELP @" + add, null, null);
                        }
                    }
                }
            }
sta
        }
        catch(Exception e){
            e.printStackTrace();
        }
            return null;
    }
    String getAddress(){
        try {
            List<Address> addressList = gc.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append(",");
                }
                sb.append(address.getLocality()).append("-");
                sb.append(address.getPostalCode()).append(",");
                sb.append(address.getCountryName());

                return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loc.getLongitude()+"|"+loc.getLongitude();
    }
//function to get JSON response from the url
    JSONObject getResults(URL url)throws Exception{
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        Log.d("loc","url conn open");
        con.setDoOutput(true);
        con.setDoInput(true);
        BufferedReader r=new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb=new StringBuilder();
        String reply;
        while((reply=r.readLine())!=null)
        {
            Log.d("loc",reply);
            sb.append(reply);
        }

        return new JSONObject(sb.toString());

    }
    @Override
    protected void onPostExecute(Object o) {
        Log.d("loc","task stopd");super.onPostExecute(o);
    }
}
