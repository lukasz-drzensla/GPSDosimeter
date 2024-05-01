package pl.edu.agh.gpsdosimeter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.IOException;

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
        String date = day + "." + month + "." + year + ":" + hours + ":" + minutes + ":" + seconds;
        String resStr = radiation + ", " + date + ", GPS: " + fdata.gpsdata;
        parent.current_measurement.setGpsData(fdata.gpsdata);
        parent.current_measurement.setRadiation(fdata.radiation);
        parent.current_measurement.setDateTime(date);
        parent.value_txt.post(new Runnable() {
            @Override
            public void run() {
                parent.value_txt.setText(radiation);
            }
        });

        parent.tv.post(new Runnable() {
            @Override
            public void run() {
                parent.tv.setText(resStr);
            }
        });
    }
}

public class MainActivity extends AppCompatActivity {

    public Measurement current_measurement = new Measurement("", 0, "", "");

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
    private EditText comment_txt;
    private Button measure_and_save_btn;
    private ActivityMainBinding binding;

    /* BT objects */
    BTTools btTools = null;
    BTCb btCb = null;

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
                            byte[] msg = new byte[JRadicom.RC.FRAME_SIZE];
                            for (int i = 0; i < JRadicom.RC.FRAME_SIZE; i++)
                            {
                                msg[i] = (byte) frame[i];
                            }
                            if (btTools.isConnected())
                            {
                                btTools.write(msg);
                            }
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
        FileManager.AppConfig appConfig = null;
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
        comment_txt = binding.commentTxt;
        measure_and_save_btn = binding.measureAndSaveBtn;

        if (appConfig != null && !appConfig.getAddComments())
        {
            comment_txt.setVisibility(View.INVISIBLE);
        }

        FileManager.AppConfig finalAppConfig = appConfig;
        measure_and_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = "";
                if (finalAppConfig != null && finalAppConfig.getAddComments())
                {
                    comment = comment_txt.getText().toString();
                }
                current_measurement.setComment(comment);
                //trigger radicom function for measure and save
            }
        });

        // Initialise Radicom
        jradicom = new JRadicom();
        radappcb = new RadAppCB(this);
        btCb = new BTCb(jradicom, radappcb);

        // Initialise BT
        checkPermission(android.Manifest.permission.BLUETOOTH, BTTools.permission_codes.BT.ordinal());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkPermission(android.Manifest.permission.BLUETOOTH_CONNECT, BTTools.permission_codes.BT.ordinal());
        }
        btTools = new BTTools();
        try {
            BTTools.ConnInfo connInfo = btTools.connect(this, btCb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //some send function
        //some wait for response
        //frame = jradicom.rc_r_read(); //for testing - reply to ourselves

        //jradicom.decode(frame, radappcb); //decode response

        send_read_query(); //setup request query thread
        /* some listen function */ //setup receive and decode thread

    }

    private void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
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