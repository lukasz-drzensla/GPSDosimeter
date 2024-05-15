package pl.edu.agh.gpsdosimeter;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import pl.edu.agh.gpsdosimeter.databinding.ActivityRadiationMapBinding;

public class RadiationMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityRadiationMapBinding binding;

    private ArrayList<Measurement> measurements = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRadiationMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get parameter
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        measurements = (ArrayList<Measurement>)bundle.getSerializable("measurements");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (measurements != null)
        {
            for (Measurement m : measurements)
            {
                RMCParser rmcParser = new RMCParser();
                RMCParser.GPSDataUnpacked gpsDataUnpacked = rmcParser.parse(m.getGPS());
                LatLng latLng = new LatLng(gpsDataUnpacked.getLat(), gpsDataUnpacked.getLon());
                mMap.addMarker(new MarkerOptions().position(latLng).title(m.getComment() + ": " + Integer.toString(m.getRadiation()) + " mSv/h").icon(BitmapDescriptorFactory.defaultMarker(new RadiGrader().grade(m.getRadiation()))));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }
}