package com.example.ashraf.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }




    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
            setHasOptionsMenu(true);
        }
        private final String LOG_TAG =DetailActivity.class.getSimpleName();
        private static final String FORECAST_SHARE_HASHTAG="#ShunshieApp";
        private String mForecastStr ;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            TextView details=(TextView)rootView.findViewById(R.id.detail_tv);
            mForecastStr=getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT) ;
            details.setText(mForecastStr);
            return rootView;
        }
        private Intent createShareForecastIntent()
        {
            Intent shareIntent =new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,mForecastStr+FORECAST_SHARE_HASHTAG);
            return shareIntent;
        }
        ShareActionProvider mShareActionProvider;
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
           inflater.inflate(R.menu.detail , menu);
            MenuItem menuItem=menu.findItem(R.id.action_share);
             mShareActionProvider =(ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            if (mShareActionProvider!=null)
            {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
            else
            {
                Log.d(LOG_TAG,"Share Action Provider is null");
            }


        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_share:
                    Toast.makeText(getContext(),"Share",Toast.LENGTH_LONG).show();

                    return true;

                default:
                    Toast.makeText(getContext() ,"default",Toast.LENGTH_LONG).show();
                    return super.onOptionsItemSelected(item);
            }
        }
        }
    }
