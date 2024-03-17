package pl.edu.agh.gpsdosimeter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import pl.edu.agh.gpsdosimeter.databinding.ActivityMainBinding;

/* callbacks implementing application-specific functions */
class RadAppCB implements JRadicom.RCCallbacks {
    RadAppCB (MainActivity parent)
    {
        this.parent = parent;
    }
    public MainActivity parent;

    public void gps_error_cb()
    {
        System.out.println("GPS ERROR!!");
    }

    public void rtc_error_cb()
    {
        System.out.println("RTC ERROR!!");
    }

    public void alarm_cb()
    {
        System.out.println("ALARM!!");
    }

    public void read_r_cb(JRadicom.rcfdataupck_t fdata)
    {
        String radiation = Integer.toString(fdata.radiation);
        String day = Integer.toString(fdata.day);
        String month = Integer.toString(fdata.month);
        String year = Integer.toString(fdata.year);
        String hours = Integer.toString(fdata.hours);
        String minutes = Integer.toString(fdata.minutes);
        String seconds = Integer.toString(fdata.seconds);
        String resStr = radiation + ", " + day + "." + month + "." + year + ":" + hours + ":" + minutes + ":" + seconds + ", GPS: " + fdata.gpsdata;
        parent.tv.setText(resStr);
    }
}

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    //GUI elements
    public TextView tv;

    JRadicom jradicom;
    RadAppCB radappcb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // GUI elements setup
        tv = binding.sampleText;

        // Initialise Radicom
        jradicom = new JRadicom();
        radappcb = new RadAppCB(this);

        int[] frame = jradicom.rc_q_read(); //send query
        //some send function
        //some wait for response
        frame = jradicom.rc_r_read(); //for testing - reply to ourselves

        jradicom.decode(frame, radappcb); //decode response
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.calibrate_btn)
        {
            Toast.makeText(this, "Calibration..", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}