package pl.edu.agh.gpsdosimeter;

import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

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
    public ListView data_list_view = null;
    TextView working_file_txt = null;
    ArrayAdapter<String> adapter = null;
    ArrayList<String> measurements = null;

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

        //Load config
        fileManager = new FileManager();
        try {
            String configPath = new File(getApplicationContext().getFilesDir(), "config.xml").getAbsolutePath();
            appConfig = fileManager.loadAppConfig(configPath);
        } catch (Exception e){
            Log.d("ERROR", e.toString());
        }

        //Load working file
        List<String> recvContents = new ArrayList<String>();
        boolean fileExists = false;
        List<String> lines = new ArrayList<String>();
        if (appConfig != null)
        {
            try {
                Scanner myReader = new Scanner(new File(getApplicationContext().getFilesDir(), appConfig.getWorkingFilePath()));
                while (myReader.hasNextLine()) {
                    String line = myReader.nextLine();
                    lines.add(line);
                }
                myReader.close();
                fileExists = true;
            } catch (FileNotFoundException e) {
                recvContents.add(getResources().getString(R.string.not_found));
                Log.d("ERROR", e.toString());
            }
        } else {
            recvContents.add(getResources().getString(R.string.not_found));
        }

        jradicom = new JRadicom();
        manappcb = new ManageAppCB(this);

        //GUI elements
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.rad_yellow_dark));
        working_file_txt = findViewById(R.id.working_file_txt);
        String working_file = getResources().getString(R.string.not_found);
        if (appConfig != null)
        {
            working_file = getResources().getString(R.string.working_file_prompt) + " " + appConfig.getWorkingFilePath();
        }
        working_file_txt.setText(working_file);
        fetch_data_button = findViewById(R.id.fetch_data_btn);
        data_list_view = findViewById(R.id.data_list_view);

        measurements = new ArrayList<String>();

        if (fileExists)
        {
            List<FileManager.Measurement> measTempList = fileManager.parseMeasurements((new File(getApplicationContext().getFilesDir(), appConfig.getWorkingFilePath())).getAbsolutePath());
            if (measTempList != null)
            {
                for (FileManager.Measurement meas : measTempList)
                {
                    String com = "";
                    if (!Objects.equals(meas.getComment(), ""))
                    {
                        com += ", " + meas.getComment();
                    }
                    measurements.add(meas.getGPS() + ", " + Integer.toString(meas.getRadiation()) + ", " + meas.getDateTime() + com);
                }
            } else {
                measurements.add (getResources().getString(R.string.empty_or_unrecognisable));
            }
        } else {
            measurements.add (getResources().getString(R.string.not_found));
        }

        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.data_list_row, R.id.list_content, measurements);
        data_list_view.setAdapter(adapter);

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

    private String entered_text = "";

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.new_file_btn)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.new_file_title));

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton(getResources().getString(R.string.new_file_create), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    entered_text = input.getText().toString();
                    String fileName = entered_text;
                    if (!fileName.endsWith(".xml"))
                    {
                        fileName += ".xml";
                    }
                    createNewFile(fileName);
                    String working_file = getResources().getString(R.string.working_file_prompt) + " " + appConfig.getWorkingFilePath();
                    working_file_txt.setText(working_file);
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();


        } else if (id == R.id.open_file_btn)
        {
            ArrayList<String> files = new ArrayList<String>();
            File folder = new File(getApplicationContext().getFilesDir().getAbsolutePath());
            File[] filesArray = folder.listFiles();

            for (int i = 0; i < filesArray.length; i++) {
                if (filesArray[i].isFile()) {
                    if (filesArray[i].getName().contains(".xml") && !filesArray[i].getName().equals("config.xml"))
                    {
                        Log.d("DEBUG", "File " + filesArray[i].getName());
                        files.add(filesArray[i].getName());
                    }
                }
            }

            String[] names = new String[files.size()];
            files.toArray(names);
            ListView listView;
            ArrayAdapter<String> myAdapter;
            AlertDialog.Builder alertDialog = new
                    AlertDialog.Builder(this);
            View rowList = getLayoutInflater().inflate(R.layout.open_file_row, null);
            listView = rowList.findViewById(R.id.open_dialog_listView);
            myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
            listView.setAdapter(myAdapter);
            myAdapter.notifyDataSetChanged();
            alertDialog.setView(rowList);
            AlertDialog dialog = alertDialog.create();
            dialog.show();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("OpenFileDialog", Integer.toString(position));
                    openFile(names[position]);
                    String working_file = getResources().getString(R.string.working_file_prompt) + " " + appConfig.getWorkingFilePath();
                    working_file_txt.setText(working_file);
                    dialog.dismiss();
                }
            });
        }
        return true;
    }

    void createNewFile (String fileName)
    {
        FileManager fileManager = new FileManager();
        appConfig = fileManager.createAppConfig("", "");
        try {
            String configPath = new File(getApplicationContext().getFilesDir(), "config.xml").getAbsolutePath();
            appConfig = fileManager.loadAppConfig(configPath);
        } catch (Exception e){
            Log.d("ERROR", e.toString());
        }
        appConfig.setWorkingFilePath(fileName);
        appConfig.saveConfig(new File(getApplicationContext().getFilesDir(), "config.xml").getAbsolutePath());

        try {
            fileManager.createNewFile(new File(getApplicationContext().getFilesDir(), appConfig.getWorkingFilePath()).getAbsolutePath());
            Log.d("DEBUG", new File(getApplicationContext().getFilesDir(), appConfig.getWorkingFilePath()).getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Log.d("DEBUG", "Cleared");

        measurements.clear();
        adapter.notifyDataSetChanged();
    }

    void openFile (String fileName)
    {
        measurements.clear();

        FileManager fileManager = new FileManager();
        appConfig = fileManager.createAppConfig("", "");
        try {
            String configPath = new File(getApplicationContext().getFilesDir(), "config.xml").getAbsolutePath();
            appConfig = fileManager.loadAppConfig(configPath);
        } catch (Exception e){
            Log.d("ERROR", e.toString());
        }
        appConfig.setWorkingFilePath(fileName);
        appConfig.saveConfig(new File(getApplicationContext().getFilesDir(), "config.xml").getAbsolutePath());

        List<String> recvContents = new ArrayList<String>();
        boolean fileExists = false;
        List<String> lines = new ArrayList<String>();
        try {
            Scanner myReader = new Scanner(new File(getApplicationContext().getFilesDir(), appConfig.getWorkingFilePath()));
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                lines.add(line);
            }
            myReader.close();
            fileExists = true;
        } catch (FileNotFoundException e) {
            recvContents.add(getResources().getString(R.string.not_found));
            Log.d("ERROR", e.toString());
        }

        if (fileExists)
        {
            List<FileManager.Measurement> measTempList = fileManager.parseMeasurements((new File(getApplicationContext().getFilesDir(), appConfig.getWorkingFilePath())).getAbsolutePath());
            if (measTempList != null)
            {
                for (FileManager.Measurement meas : measTempList)
                {
                    String com = "";
                    if (!Objects.equals(meas.getComment(), ""))
                    {
                        com += ", " + meas.getComment();
                    }
                    measurements.add(meas.getGPS() + ", " + Integer.toString(meas.getRadiation()) + ", " + meas.getDateTime() + com);
                }
            } else {
                measurements.add (getResources().getString(R.string.empty_or_unrecognisable));
            }
        } else {
            measurements.add (getResources().getString(R.string.not_found));
        }
        adapter.notifyDataSetChanged();
    }
}