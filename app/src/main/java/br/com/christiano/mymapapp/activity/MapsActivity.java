package br.com.christiano.mymapapp.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.christiano.mymapapp.R;
import br.com.christiano.mymapapp.adapter.AddressAdapter;
import br.com.christiano.mymapapp.dao.MyMapAppDAO;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by Christiano on 31/03/2016.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DialogInterface.OnClickListener {

    private GoogleMap mMap;
    private LatLngBounds.Builder builder = new LatLngBounds.Builder();
    private EditText editTextAddress;
    private Button searchButton;
    private Button saveRemoveButton;
    private LatLng myPosition;
    private MyMapAppDAO dao;
    private String myLastSearch; //from DataBase
    private AlertDialog dialogConfirmation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        editTextAddress = (EditText) findViewById(R.id.editTextEndereco);
        searchButton = (Button) findViewById(R.id.searchButton);
        saveRemoveButton = (Button) findViewById(R.id.saveButton);

        dao = new MyMapAppDAO(this);
        myLastSearch = dao.find();
        if(myLastSearch!=null){
            editTextAddress.setText(myLastSearch);
            saveRemoveButton.setText("Delete");
        }else{
            editTextAddress.setText("");
            saveRemoveButton.setText("Save");
        }

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchAddressAsyncTask task = new SearchAddressAsyncTask();
                task.execute(editTextAddress.getText().toString());
            }
        });

        saveRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(saveRemoveButton.getText().equals("Save")) {
                    dao.save(editTextAddress.getText().toString());
                    saveRemoveButton.setText("Delete");
                    Toast.makeText(getApplication(), "Object saved.", LENGTH_SHORT).show();
                }else{
                    dialogConfirmation.show();
                }
            }
        });

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        dialogConfirmation = createConfirmationDialog();
    }

    @Override
    protected void onDestroy() {
        dao.close();
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No Permission to execute my location", LENGTH_SHORT).show();
            return;
        }
        mMap.setMyLocationEnabled(true);
        myPosition = getMyLocation();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 15));
    }

    /**
     * Getting Current Location from LocationManager object from System Service LOCATION_SERVICE
     * @return
     */
    private LatLng getMyLocation() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "No Permission to execute my location", LENGTH_SHORT).show();
                return null;
            }
            Location location = locationManager.getLastKnownLocation(provider);

            if(location!=null){
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng my = new LatLng(latitude, longitude);
                return my;
            }else{
                LatLng bh = new LatLng(-19.8157, -43.9542);
                return bh;
            }
        }catch(Exception e){
            Log.e("Error", "(getMyLocation) : "+e);
        }

        return null;
    }

    /**
     * Create a pop up  dialog for confirm the deletion.
     * @return
     */
    private AlertDialog createConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirmation_text);
        builder.setPositiveButton(getString(R.string.delete), this);
        builder.setNegativeButton(getString(R.string.cancel), this);
        return builder.create();
    }

    /**
     * Pop Up Events tratament.
     *
     * @param dialog
     * @param which
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: //delete
                dao.remove();
                editTextAddress.setText("");
                saveRemoveButton.setText("Save");
                Toast.makeText(getApplication(), "Object deleted.", LENGTH_SHORT).show();
            case DialogInterface.BUTTON_NEGATIVE://cancel
                dialogConfirmation.dismiss();
                break;
        }
    }


    /**
     * ======================================================================================================
     * inner class for assinc-task search address
     * makes the search through the call to the Google Maps API.
     */
    class SearchAddressAsyncTask extends AsyncTask<String, Void, List<Address>> {

        private String addressTitle;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            mMap.clear();
            progressDialog = ProgressDialog.show(MapsActivity.this, null, "Searching...");
        }

        @Override
        protected List<Address> doInBackground(String... params) {
            try {
                addressTitle = params[0];
                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocationName(addressTitle, 10);
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Address> listAddress) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (listAddress != null) {
                final Dialog dialog = new Dialog(MapsActivity.this);
                dialog.setContentView(R.layout.dialog_address);

                if(listAddress.size()>1 ){//if there are more than one results, show a row for "Display All on Map"
                    ImageView imageView = (ImageView) dialog.findViewById(R.id.imageIcon);
                    imageView.setImageResource(R.drawable.android_place_grey);
                    TextView txtDisplayAllOnMap = (TextView) dialog.findViewById(R.id.txtDisplayAllOnMap);
                    final List<Address> listAddress2 = new ArrayList<>(listAddress);
                    txtDisplayAllOnMap.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(v.getId() == R.id.txtDisplayAllOnMap){
                                dialog.dismiss();

                                List<Marker> markersList = new ArrayList<Marker>();
                                Marker marker;
                                for(Address address : listAddress2){
                                    marker = mMap.addMarker(new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())).title(addressTitle));
                                    markersList.add(marker);
                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                    for (Marker m : markersList) {
                                        builder.include(m.getPosition());
                                    }

                                    int padding = 50;
                                    LatLngBounds bounds = builder.build();
                                    final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                    mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                                        @Override
                                        public void onMapLoaded() {
                                            mMap.animateCamera(cu);
                                        }
                                    });
                                }
                            }
                        }
                    });
                }else{// only one result
                    LinearLayout layoutDisplayAll = (LinearLayout) dialog.findViewById(R.id.layoutDisplayAll);
                    layoutDisplayAll.setVisibility(LinearLayout.GONE);
                }

                ListView listView = (ListView) dialog.findViewById(R.id.listView);
                AddressAdapter adapter = new AddressAdapter(MapsActivity.this);
                adapter.addAll(listAddress);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                        dialog.dismiss();

                        Address item = (Address) adapter.getItemAtPosition(position);

                        StringBuilder descricao = new StringBuilder();
                        if (item.getAdminArea() != null) {
                            descricao.append(item.getAdminArea());
                        }
                        if (item.getCountryCode() != null) {
                            descricao.append(", ").append(item.getCountryCode());
                        }
                        descricao.append(", Lat:").append(item.getLatitude());
                        descricao.append(", Lon:").append(item.getLongitude());

                        LatLng location = new LatLng(item.getLatitude(), item.getLongitude());
                        builder.include(location);
                        LatLngBounds bounds = builder.build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 150);

                        mMap.moveCamera(cameraUpdate);
                        mMap.addMarker(new MarkerOptions().position(location)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                .title(addressTitle)
                                .snippet(descricao.toString()));
                    }

                });
                dialog.show();

            } else {
                Toast.makeText(MapsActivity.this, "No Results", Toast.LENGTH_LONG).show();
            }
        }

    }


}
