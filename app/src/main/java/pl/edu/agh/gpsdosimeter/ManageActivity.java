package pl.edu.agh.gpsdosimeter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        Log.d("ManageActivity", resStr);
    }
}

public class ManageActivity extends AppCompatActivity {
    FileManager.AppConfig appConfig = null;

    //Radicom protocol objects
    JRadicom jradicom;
    ManageAppCB manappcb;

    private List<Measurement> measurementList;

    //GUI elements
    public Button fetch_data_button = null;
    public Button create_map_btn = null;
    public ListView data_list_view = null;
    TextView working_file_txt = null;
    ArrayAdapter<String> adapter = null;
    ArrayList<String> measurements = null;

    private boolean loadConfig(@NonNull FileManager fManager)
    {
        try {
            String configPath = new File(getApplicationContext().getFilesDir(), fManager.configName).getAbsolutePath();
            appConfig = fManager.loadAppConfig(configPath);
        } catch (Exception e){
            Log.e("ManageActivity", "loadConfig: " + e);
        }
        return appConfig != null;
    }

    private boolean contentExists()
    {
        boolean result = false;
        if (appConfig != null) {
            File file = new File(getApplicationContext().getFilesDir(), appConfig.getWorkingFileName());
            result = file.exists();
        }
        return result;
    }
    private void loadCollection(@NonNull FileManager fManager)
    {
        measurementList = fManager.parseMeasurements((new File(getApplicationContext().getFilesDir(), appConfig.getWorkingFileName())).getAbsolutePath());
        if (measurementList != null)
        {
            for (Measurement meas : measurementList)
            {
                String com = "";
                if (!Objects.equals(meas.getComment(), ""))
                {
                    com += ", " + meas.getComment();
                }
                measurements.add(meas.getGPS() + ", " + meas.getRadiation() + ", " + meas.getDateTime() + com);
            }
        } else {
            measurements.add (getResources().getString(R.string.empty_or_unrecognisable));
        }
    }

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

        //load config
        FileManager fileManager = new FileManager();
        loadConfig(fileManager);

        jradicom = new JRadicom();
        manappcb = new ManageAppCB(this);

        //GUI elements
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.rad_yellow_dark));
        working_file_txt = findViewById(R.id.working_file_txt);
        String working_file = getResources().getString(R.string.not_found);
        if (appConfig != null)
        {
            working_file = getResources().getString(R.string.working_file_prompt) + " " + appConfig.getWorkingFileName();
        }
        working_file_txt.setText(working_file);
        fetch_data_button = findViewById(R.id.fetch_data_btn);
        create_map_btn = findViewById(R.id.create_map_btn);
        data_list_view = findViewById(R.id.data_list_view);

        measurements = new ArrayList<>();
        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.data_list_row, R.id.list_content, measurements);
        data_list_view.setAdapter(adapter);

        //load working file
        if (null != appConfig)
        {
            openWorkingFile(appConfig.getWorkingFileName());
        }

        fetch_data_button.setOnClickListener(v -> {
            int[] frame = jradicom.rc_q_memread();
            //some send frame function
        });
        create_map_btn.setOnClickListener(v -> {
            //start activity with parameter passed
            Bundle bundle = new Bundle();
            bundle.putSerializable("measurements", (ArrayList<Measurement>)measurementList);
            Intent intent = new Intent(ManageActivity.this, RadiationMap.class);
            intent.putExtras(bundle);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage_menu, menu);
        return true;
    }

    private String entered_text = "";

    void newFileDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.new_file_title));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton(getResources().getString(R.string.new_file_create), (dialog, which) -> {
            entered_text = input.getText().toString();
            String fileName = entered_text;
            if (!fileName.endsWith(".xml"))
            {
                fileName += ".xml";
            }
            createNewFile(fileName);
            String working_file = getResources().getString(R.string.working_file_prompt) + " " + appConfig.getWorkingFileName();
            working_file_txt.setText(working_file);
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.cancel());

        builder.show();
    }

    void openFileDialog()
    {
        FileManager fManager = new FileManager();
        ArrayList<String> files = new ArrayList<>();
        File folder = new File(getApplicationContext().getFilesDir().getAbsolutePath());
        File[] filesArray = folder.listFiles();

        if (filesArray != null) {
            for (File file : filesArray) {
                if (file.isFile()) {
                    if (file.getName().contains(".xml") && !file.getName().equals(fManager.configName)) {
                        Log.d("ManageActivity", "Open file " + file.getName());
                        files.add(file.getName());
                    }
                }
            }
        } else {
            Log.e ("ManageActivity", "No files found");
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
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d("OpenFileDialog", Integer.toString(position));
            openWorkingFile(names[position]);
            String working_file = getResources().getString(R.string.working_file_prompt) + " " + appConfig.getWorkingFileName();
            working_file_txt.setText(working_file);
            dialog.dismiss();
        });
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.remove) + " " + names[position] + "?");
            builder.setPositiveButton(getResources().getString(R.string.remove), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    File toRemove = new File(getApplicationContext().getFilesDir(), names[position]);
                    toRemove.delete();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog removeDialog = builder.create();
            removeDialog.show();

            return true;
        });
    }

    void exportDialog()
    {
        FileManager fManager = new FileManager();
        if (0 == fManager.exportCSV(fManager.loadMeasurements(new File(getApplicationContext().getFilesDir(), "").getAbsolutePath()), (new File(getFilesDir(), "export.csv")).getAbsolutePath()))
        {
            final File csvFile = new File(getFilesDir(), "export.csv");
            Uri csvFileUri = FileProvider.getUriForFile(ManageActivity.this,"pl.edu.agh.gpsdosimeter", csvFile);

            ArrayList<Uri> exportFiles = new ArrayList<>();
            exportFiles.add(csvFileUri);

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, exportFiles);
            shareIntent.setType("text/csv");
            startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.export_menu_item)));
        } else {
            Log.e("EXPORT", "Error creating CSV file");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            finish();
        } else if (id == R.id.new_file_btn)
        {
            newFileDialog();
        } else if (id == R.id.open_file_btn)
        {
            openFileDialog();
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.rem_file_info), Toast.LENGTH_LONG).show();
        } else if (id == R.id.export_as)
        {
            exportDialog();
        }
        return true;
    }

    void createNewFile (String fileName)
    {
        FileManager fileManager = new FileManager();
        appConfig = fileManager.createAppConfig("", "", "3.6");
        try {
            String configPath = new File(getApplicationContext().getFilesDir(), fileManager.configName).getAbsolutePath();
            appConfig = fileManager.loadAppConfig(configPath);
        } catch (Exception e){
            Log.e("ManageActivity", e.toString());
        }
        appConfig.setWorkingFilePath(fileName);
        appConfig.saveConfig(new File(getApplicationContext().getFilesDir(), fileManager.configName).getAbsolutePath());

        try {
            fileManager.createNewFile(new File(getApplicationContext().getFilesDir(), appConfig.getWorkingFileName()).getAbsolutePath());
            Log.d("ManageActivity", new File(getApplicationContext().getFilesDir(), appConfig.getWorkingFileName()).getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        measurements.clear();
        adapter.notifyDataSetChanged();
    }

    void openWorkingFile(String fileName)
    {
        if (measurements != null)
        {
            measurements.clear();
        } else {
            return;
        }

        FileManager fManager = new FileManager();
        appConfig = fManager.createAppConfig();
        if (!loadConfig(fManager))
        {
            Log.e ("ManageActivity", "Error loading config");
            return;
        }

        if (null != appConfig) //redundant but kept for readability
        {
            appConfig.setWorkingFilePath(fileName);
            appConfig.saveConfig(new File(getApplicationContext().getFilesDir(), fManager.configName).getAbsolutePath());
            if (contentExists())
            {
                loadCollection(fManager);
            } else {
                measurements.add (getResources().getString(R.string.not_found));
            }
        } else {
            measurements.add (getResources().getString(R.string.not_found));
        }

        adapter.notifyDataSetChanged();
    }
}