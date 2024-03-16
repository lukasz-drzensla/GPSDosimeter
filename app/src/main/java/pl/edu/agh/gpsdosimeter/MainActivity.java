package pl.edu.agh.gpsdosimeter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import pl.edu.agh.gpsdosimeter.databinding.ActivityMainBinding;

// A class used for data exchange between radicom callbacks and the activity
class RadicomResults {
        public static String resStr;
}

/* callbacks implementing application-specific functions */
class RadAppCB implements JRadicom.RCCallbacks {

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
        /*System.out.printf("Radiation: %s, Day: %s, Month: %s, Year: %s, Hours: %s, Minutes: %s, Seconds: %s, GPS: %s\n", radiation, day,
                month, year, hours, minutes, seconds, fdata.gpsdata);
        */
        RadicomResults.resStr = radiation + ", " + day + "." + month + "." + year + ", " + hours + ", " + minutes + ", " + seconds + ", GPS: " + fdata.gpsdata;
    }
}

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    JRadicom jradicom;
    RadAppCB radappcb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialise Radicom
        jradicom = new JRadicom();
        radappcb = new RadAppCB();

        int[] frame = jradicom.rc_q_read(); //send query
        //some send function
        //some wait for response
        frame = jradicom.rc_r_read(); //for testing - reply to ourselves

        jradicom.decode(frame, radappcb); //decode response

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        tv.setText(RadicomResults.resStr);
    }
}