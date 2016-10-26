package com.example.ashraf.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by ashraf on 10/5/2016.
 */

public class  ForecastFragment extends Fragment
{
    public ForecastFragment()
    {

    }

    ListView listView_forecast ;
    ArrayAdapter arrayAdapter ;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);
        listView_forecast = (ListView) rootView.findViewById(R.id.listview_forecast);
        String[] forecastArray = {"Today -Sunny - 88/63", "Today -Sunny - 88/63", "Today -Sunny - 88/63", "Today -Sunny - 88/63"};
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));
        arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekForecast);
        listView_forecast.setAdapter(arrayAdapter);
        listView_forecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected=((TextView) view.findViewById(R.id.list_item_forecast_textview)).getText().toString();
                startActivity(new Intent(getActivity(),DetailActivity.class).putExtra(Intent.EXTRA_TEXT,selected));
            }
        });

        return rootView;
    }
    @Override
    public void onStart()
    {
        super.onStart();
        updateWeather();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
           case R.id.action_refresh:
              updateWeather();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(getActivity(),SettingsActivity.class));
                return true;
            case R.id.action_Map:
                openPreferenceLocationMap();
                return true;

            default:
               Toast.makeText(getContext() ,"default",Toast.LENGTH_LONG).show();
                return super.onOptionsItemSelected(item);
        }
    }
    private void openPreferenceLocationMap()
    {
        SharedPreferences shredPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location= shredPrefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default)) ;
        Uri geoLocation=Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",location).build();
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity((getActivity().getPackageManager()))!=null)
        {
            getActivity().startActivity(intent);
        }
        else
        {
            Log.d(LOG_TAG,"Couldn't call "+location+", no ");
        }
    }

    public void updateWeather()
    {
        new FetchWeatherTask().execute(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default)));
    }

    private final String LOG_TAG =FetchWeatherTask.class.getSimpleName();
    private class FetchWeatherTask extends AsyncTask<String, Void, String[]>{

         @Override
         protected String []doInBackground(String... params) {
             // These two need to be declared outside the try/catch
             // so that they can be closed in the finally block.
             if (params==null || params.length==0 )
             {
                 return null ;
             }

             HttpURLConnection urlConnection = null;
             BufferedReader reader = null;

             // Will contain the raw JSON response as a string.
             String forecastJsonStr = null;
             String format="json";
             String units="metric";
             int numDays=7 ;

             try {
                 // Construct the URL for the OpenWeatherMap query
                 // Possible parameters are avaiable at OWM's forecast API page, at
                 // http://openweathermap.org/API#forecast
                 final String FORECAST_BASE_URL=
                         "http://api.openweathermap.org/data/2.5/forecast/daily?";
                 final String QUERY_PARAM="q";
                 final String FORMAT_PARAM="mode";
                 final String UNITS_PARAM="units";
                 final String DAYS_PARAM="cnt";
                 Uri builturl = Uri.parse(FORECAST_BASE_URL).buildUpon()
                         .appendQueryParameter(QUERY_PARAM,params[0])
                         .appendQueryParameter(FORMAT_PARAM,format)
                         .appendQueryParameter(UNITS_PARAM,units)
                         .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                         .appendQueryParameter("appid","bbbcbe725f6685f109434c450b919b7a").build();
                 URL url=new URL(builturl.toString());
                 // Create the request to OpenWeatherMap, and open the connection
                 urlConnection = (HttpURLConnection) url.openConnection();
                 urlConnection.setRequestMethod("GET");
                 urlConnection.connect();

                 // Read the input stream into a String
                 InputStream inputStream = urlConnection.getInputStream();
                 StringBuffer buffer = new StringBuffer();
                 if (inputStream == null) {
                     // Nothing to do.
                     return null;
                 }
                 reader = new BufferedReader(new InputStreamReader(inputStream));

                 String line;
                 while ((line = reader.readLine()) != null) {
                     // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                     // But it does make debugging a *lot* easier if you print out the completed
                     // buffer for debugging.
                     buffer.append(line + "\n");
                 }

                 if (buffer.length() == 0) {
                     // Stream was empty.  No point in parsing.
                     return null;
                 }
                 forecastJsonStr = buffer.toString();
             } catch (IOException e) {
                 Log.e(LOG_TAG, "Error ", e);
                 // If the code didn't successfully get the weather data, there's no point in attemping
                 // to parse it.
                 return null;
             } finally {
                 if (urlConnection != null) {
                     urlConnection.disconnect();
                 }
                 if (reader != null) {
                     try {
                         reader.close();
                     } catch (final IOException e) {
                         Log.e(LOG_TAG, "Error closing stream", e);
                     }
                 }
             }
             try {
                 return getWeatherDataFromJson(forecastJsonStr,numDays) ;
             } catch (JSONException e) {
                 e.printStackTrace();
             }
             return null ;
         }

         @Override
         protected void onPostExecute(String[] result) {
             if (result != null) {
                 arrayAdapter.clear();
                 for (String dayForecastStr : result) {
                     arrayAdapter.add(dayForecastStr);
                 }
             }


         }

         @Override
         protected void onPreExecute() {}

         @Override
         protected void onProgressUpdate(Void... values) {}





        /* The date/time conversion code is going to be moved outside the asynctask later,
                * so for convenience we're breaking it out into its own method now.
                */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            Date date =new Date(time*1000);
            SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
            return format.format(date).toString();
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {

            SharedPreferences sharedPrefs=PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType=sharedPrefs.getString(getString(R.string.pref_units_key),getString(R.string.pref_units_metric));
            if (unitType.equals(getString(R.string.pref_units_imperial)))
            {
                high= (high * 1.8) +32 ;
                low = (low * 1.8) +32 ;
            }
            else if (!unitType.equals(getString(R.string.pref_units_metric)))
            {
                Log.d(LOG_TAG , "Units type not found "+unitType);
            }
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        public String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v("", "Forecast entry: " + s);
            }
            return resultStrs;

        }


    }

}

