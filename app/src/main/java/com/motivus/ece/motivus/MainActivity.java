package com.motivus.ece.motivus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {
    /**
     * The refresh time
     */
    private final long LOCATION_REFRESH_TIME = 1000;
    /**
     * The refresh distance is set to 0 meter, which means the update does not depend on distance
     */
    private final float LOCATION_REFRESH_DISTANCE = 0;
    /**
     * The distance threshold in meter.
     * For example, A is current location, and B is the target location
     *              If A and B are as close as 100 meters, then we consider them as one location
     */
    private final float LOCATION_THRESHOLD_DISTANCE = 200;
    private final float LOCATION_LOG_THRESHOLD_DISTANCE = 50;
    /**
     *
     */
    double mLastLatitude = 0;
    double mLastLongitude = 0;

    private LocationManager mLocationManager;

    private Database db_appointment;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
            Date date = new Date();
            GPSlocation gpsLocation = new GPSlocation();
            gpsLocation.time = dateFormat.format(date);
            gpsLocation.latitude = latitude;
            gpsLocation.longitude = longitude;
            double logDiffDistance = GoogleMaps.checkDistance(latitude, longitude, mLastLatitude, mLastLongitude);
            if (logDiffDistance >= LOCATION_LOG_THRESHOLD_DISTANCE) {
                Database.getInstance(getApplicationContext()).addGPS(gpsLocation);
                mLastLatitude = latitude;
                mLastLongitude = longitude;
            }

            //Compare appointment location
            ArrayList<Appointment> appointments = Database.getInstance(getApplicationContext()).getAllAppointments();
            for(int i = 0; i <  appointments.size(); i++) {
                double diffDistance = GoogleMaps.checkDistance(latitude, longitude, appointments.get(i).latitude, appointments.get(i).longitude);
                if (diffDistance <= LOCATION_THRESHOLD_DISTANCE) {
                    appointments.get(i).check = true;
                    Toast.makeText(getApplicationContext(),
                            "\"" + appointments.get(i).title + "\" appointment DONE!" , Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up the database
        db_appointment = Database.getInstance(this);

        //Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //get the location manager
        mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        //Set up the ViewPager with the sections adapter.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
               //
               // mSectionsPagerAdapter.notifyDataSetChanged();
            }
            public void onPageScrollStateChanged(int state) {
            }
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Constantly monitoring the GPS location
        this.mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE,mLocationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.mLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        int tabPosition = tab.getPosition();
        mViewPager.setCurrentItem(tabPosition);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return AppointmentFragment.newInstance(position + 1);
                case 1:
                    return CalendarFragment.newInstance(position + 1);
                case 2:
                    return ReportFragment.newInstance(position + 1);
                default:
                    return AppointmentFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public int getItemPosition(Object object) {
            if(object instanceof  AppointmentFragment ) {
                AppointmentFragment f = (AppointmentFragment) object;
                if (f != null) {
                    f.update();
                }
            }
            return super.getItemPosition(object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    private void alertTurnOnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to enable GPS?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                startActivity(new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public static class AppointmentFragment extends ListFragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public List<Appointment> appointmentList = new ArrayList<Appointment>();
        public AppointmentAdapter mAdapter;
        private final int NewAppointmentIndex = 0;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static AppointmentFragment newInstance(int sectionNumber) {
            AppointmentFragment fragment = new AppointmentFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public AppointmentFragment() {
        }

        public void update() {
            appointmentList = Database.getInstance(getActivity()).getAllAppointments();
            mAdapter = new AppointmentAdapter(getActivity(), appointmentList);
            setListAdapter(mAdapter);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_appointment, container, false);

            //Get all the appointment and put them into
            appointmentList = Database.getInstance(getActivity()).getAllAppointments();
            mAdapter = new AppointmentAdapter(getActivity(), appointmentList);
            setListAdapter(mAdapter);

            FragmentManager fm = getFragmentManager();
            fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    //Update after delete or modify any appointment; even just return by back button
                    if(getFragmentManager().getBackStackEntryCount() == 0) {
                        update();
                    }
                }
            });

            //Add new appointment button
            Button newAppointment = (Button) rootView.findViewById(R.id.button_newappointment);
            newAppointment.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent newActivity = new Intent(v.getContext(), NewAppointment.class);
                            startActivityForResult(newActivity, NewAppointmentIndex);
                        }
                    }
            );

            return rootView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch(requestCode) {
                case (NewAppointmentIndex) : {
                    if (resultCode == Activity.RESULT_OK) {
                        update();
                    }
                    break;
                }
            }
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Appointment appointment = (Appointment)l.getItemAtPosition(position);
            DetailFragment detailFragment = new DetailFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("appointment", appointment);
            detailFragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, detailFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    public static class CalendarFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static CalendarFragment newInstance(int sectionNumber) {
            CalendarFragment fragment = new CalendarFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public CalendarFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);
            return rootView;
        }
    }

    public static class ReportFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ReportFragment newInstance(int sectionNumber) {
            ReportFragment fragment = new ReportFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public ReportFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_report, container, false);
            Button trackButton = (Button) rootView.findViewById(R.id.button_tracking);
            trackButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent map = new Intent(v.getContext(), TrackingMap.class);
                            startActivity(map);
                        }
                    }
            );

            return rootView;
        }
    }

    public static class DetailFragment extends Fragment {
        public Appointment appointment;
        public DetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            Bundle args = getArguments();
            appointment  = args.getParcelable("appointment");

            TextView editText_name = (TextView)(rootView.findViewById(R.id.textView_title));
            editText_name.setText(appointment.title, TextView.BufferType.EDITABLE);

            TextView editText_detail = (TextView)(rootView.findViewById(R.id.textView_detail));
            editText_detail.setText(appointment.detail, TextView.BufferType.EDITABLE);

            TextView editText_latitude = (TextView)(rootView.findViewById(R.id.textView_latitude));
            editText_latitude.setText("" + appointment.latitude, TextView.BufferType.EDITABLE);

            TextView editText_longitude = (TextView)(rootView.findViewById(R.id.textView_longitude));
            editText_longitude.setText("" + appointment.longitude, TextView.BufferType.EDITABLE);
            /*
            ImageView imageView_pic = (ImageView)(rootView.findViewById(R.id.imageView_pic));
            byte[] imageByteArray = appointment.pic;
            ByteArrayInputStream imageStream = new ByteArrayInputStream(imageByteArray);
            Bitmap image = BitmapFactory.decodeStream(imageStream);
            imageView_pic.setImageBitmap(image);
            Button button_back = (Button) rootView.findViewById(R.id.button_back);
            button_back.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
            */
            //Add new appointment button
            Button deleteAppointment = (Button) rootView.findViewById(R.id.button_delete);
            deleteAppointment.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getActivity().getSupportFragmentManager().popBackStack();
                            Database.getInstance(getActivity()).deleteAppointment(appointment);
                        }
                    }
            );

            return rootView;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db_appointment.close();
    }
}
