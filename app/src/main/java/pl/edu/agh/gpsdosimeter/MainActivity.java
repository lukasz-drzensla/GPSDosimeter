package pl.edu.agh.gpsdosimeter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;

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

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
    /* GUI elements */
    public TextView value_txt;
    public TextView tv;
    public TextView rad_result_txt;
    public ShapeableImageView rad_status_led;
    public TextView gps_status_txt;
    public ShapeableImageView gps_status_led;
    private ActivityMainBinding binding;
    /* Radicom objects */
    JRadicom jradicom;
    RadAppCB radappcb;
    /* Dosimeter status */
    public boolean device_connected = true;

    private void send_read_query() {

        Handler handler = new Handler();
        Runnable runnable = () -> {

            while (device_connected) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable(){
                    public void run() {
                        /* setup frame */
                        int[] frame = jradicom.rc_q_read();
                        /* some send frame function */
                        int val = getRandomNumber(0, 4);

                        //set_rad_level(val);
                        //set_gps_status(val);

                        value_txt.setText(Integer.toString(val)); //dummy function
                    }
                });
            }
        };
        new Thread(runnable).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FileManager fileManager = new FileManager();
        FileManager.AppConfig appConfig;
        try {
            String configPath = new File(getApplicationContext().getFilesDir(), "config.xml").getAbsolutePath();
            appConfig = fileManager.loadAppConfig(configPath);
        } catch (Exception e){
            Log.d("ERROR", e.toString());
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // GUI elements setup
        tv = binding.sampleText;
        value_txt = binding.valueTxt;
        rad_result_txt = binding.radStatusTxt;
        rad_status_led = binding.radStatusLed;
        gps_status_txt = binding.gpsStatusTxt;
        gps_status_led = binding.gpsStatusLed;


        // Initialise Radicom
        jradicom = new JRadicom();
        radappcb = new RadAppCB(this);

        int[] frame = jradicom.rc_q_read(); //send query
        //some send function
        //some wait for response
        frame = jradicom.rc_r_read(); //for testing - reply to ourselves

        jradicom.decode(frame, radappcb); //decode response

        send_read_query(); //setup request query thread
        /* some listen function */ //setup receive and decode thread

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings_btn)
        {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        } else if (id == R.id.manage_btn)
        {
            startActivity(new Intent(MainActivity.this, ManageActivity.class));
        }
        return true;
    }

    void set_rad_level(int level)
    {
        switch (level)
        {
            case 0:
            {
                //safe
                rad_result_txt.setText(R.string.rad_status_OK);
                rad_status_led.setBackgroundColor((getResources().getColor(R.color.rad_green)));
            }
            case 1:
            {
                //bad, alarm
                rad_result_txt.setText(R.string.rad_status_BAD);
                rad_status_led.setBackgroundColor((getResources().getColor(R.color.rad_red)));
            }
            default:
            {
                //no measurement
                rad_result_txt.setText(R.string.rad_status_NO_MEAS);
                rad_status_led.setBackgroundColor((getResources().getColor(R.color.rad_yellow)));
            }
        }
    }

    void set_gps_status(int level)
    {
        switch (level)
        {
            case 0:
            {
                //safe
                gps_status_txt.setText(R.string.gps_status_OK);
                gps_status_led.setBackgroundColor((getResources().getColor(R.color.rad_green)));
            }
            case 1:
            {
                //bad, alarm
                gps_status_txt.setText(R.string.gps_status_ERR);
                gps_status_led.setBackgroundColor((getResources().getColor(R.color.rad_red)));
            }
            default:
            {
                //no measurement
                gps_status_txt.setText(R.string.gps_status_disconnected);
                gps_status_led.setBackgroundColor((getResources().getColor(R.color.rad_yellow)));
            }
        }
    }
}