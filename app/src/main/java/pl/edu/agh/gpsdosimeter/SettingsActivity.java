package pl.edu.agh.gpsdosimeter;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* GUI elements */
        Switch en_comm_sw = findViewById(R.id.en_comm_sw);

        FileManager initFileManager = new FileManager();
        FileManager.AppConfig initAppConfig = initFileManager.createAppConfig("", "");
        try {
            String configPath = new File(getApplicationContext().getFilesDir(), "config.xml").getAbsolutePath();
            initAppConfig = initFileManager.loadAppConfig(configPath);
        } catch (Exception e){
            Log.d("ERROR", e.toString());
        }

        en_comm_sw.setChecked(initAppConfig.getAddComments());

        en_comm_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("DEBUG", String.valueOf(isChecked));
                FileManager fileManager = new FileManager();
                FileManager.AppConfig appConfig = fileManager.createAppConfig("", "");
                try {
                    String configPath = new File(getApplicationContext().getFilesDir(), "config.xml").getAbsolutePath();
                    appConfig = fileManager.loadAppConfig(configPath);
                } catch (Exception e){
                    Log.d("ERROR", e.toString());
                }
                appConfig.setAddComments(isChecked);
                appConfig.saveConfig(new File(getApplicationContext().getFilesDir(), "config.xml").getAbsolutePath());

            }
        });
    }
}