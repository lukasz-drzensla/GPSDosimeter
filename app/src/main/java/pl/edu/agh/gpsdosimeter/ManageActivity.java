package pl.edu.agh.gpsdosimeter;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;

class ManageAppCB implements JRadicom.RCCallbacks {
    ManageAppCB(ManageActivity parent)
    {
        this.parent = parent;
    }
    public ManageActivity parent;

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

    }
}

public class ManageActivity extends AppCompatActivity {

    FileManager fileManager = null;
    FileManager.AppConfig appConfig = null;

    //Radicom protocol objects
    JRadicom jradicom;
    ManageAppCB manappcb;

    //GUI elements
    public Button fetch_data_button = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Load config
        fileManager = new FileManager();
        try {
            String configPath = new File(getApplicationContext().getFilesDir(), "config.xml").getAbsolutePath();
            appConfig = fileManager.loadAppConfig(configPath);
        } catch (Exception e){
            Log.d("ERROR", e.toString());
        }

        jradicom = new JRadicom();
        manappcb = new ManageAppCB(this);

        //GUI elements
        TextView working_file_txt = findViewById(R.id.working_file_txt);
        String working_file = getResources().getString(R.string.working_file_prompt) + " " + appConfig.getWorkingFilePath();
        working_file_txt.setText(working_file);
        fetch_data_button = findViewById(R.id.fetch_data_btn);
        fetch_data_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] frame = jradicom.rc_q_memread();
                //some send frame function
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.new_file_btn)
        {

        }
        return true;
    }
}