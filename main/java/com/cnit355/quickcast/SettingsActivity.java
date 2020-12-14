package com.cnit355.quickcast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    // Locations ArrayList
    ArrayList<String> locations = new ArrayList<>();

    // Temperature Unit Array
    String tempUnits[] = {"K", "C", "F"};
    int tempIndex;

    // Speed Unit Array
    String speedUnits[] = {"mph", "kph", "knots", "m/s", "ft/s"};
    int speedIndex;

    boolean showTemp;
    boolean showMaxTemp;
    boolean showMinTemp;
    boolean showHumidity;
    boolean showWindSpeed;
    boolean showWindDirection;
    boolean showFeelsLike;
    boolean showCloudiness;
    boolean showPressure;

    // Spinners
    Spinner spinnerTemps;
    Spinner spinnerSpeeds;

    // Request Code
    private static final int request_code = 5;

    String TAG = "Testing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Intent Stuff
        Intent mIntent = getIntent();
        if(mIntent == null) {
            return;
        }
        locations = mIntent.getStringArrayListExtra("qLocations");
        tempIndex = mIntent.getIntExtra("qTempUnit", 0);
        speedIndex = mIntent.getIntExtra("qSpeedUnit", 0);
        showTemp = mIntent.getBooleanExtra("qShowTemp",true);
        showMaxTemp = mIntent.getBooleanExtra("qShowMaxTemp",true);
        showMinTemp = mIntent.getBooleanExtra("qShowMinTemp",true);
        showHumidity = mIntent.getBooleanExtra("qShowHumidity",true);
        showWindSpeed = mIntent.getBooleanExtra("qShowWindSpeed",true);
        showWindDirection = mIntent.getBooleanExtra("qShowWindDirection",true);
        showFeelsLike = mIntent.getBooleanExtra("qShowFeelsLike",true);
        showCloudiness = mIntent.getBooleanExtra("qShowCloudiness",true);
        showPressure = mIntent.getBooleanExtra("qShowPressure",true);
        CheckBox cB1=(CheckBox)findViewById(R.id.checkBox);
        cB1.setChecked(showTemp);
        CheckBox cB2=(CheckBox)findViewById(R.id.checkBox2);
        cB2.setChecked(showMaxTemp);
        CheckBox cB3=(CheckBox)findViewById(R.id.checkBox3);
        cB3.setChecked(showMinTemp);
        CheckBox cB4=(CheckBox)findViewById(R.id.checkBox4);
        cB4.setChecked(showHumidity);
        CheckBox cB5=(CheckBox)findViewById(R.id.checkBox5);
        cB5.setChecked(showWindSpeed);
        CheckBox cB6=(CheckBox)findViewById(R.id.checkBox6);
        cB6.setChecked(showWindDirection);
        CheckBox cB7=(CheckBox)findViewById(R.id.checkBox7);
        cB7.setChecked(showFeelsLike);
        CheckBox cB8=(CheckBox)findViewById(R.id.checkBox8);
        cB8.setChecked(showCloudiness);
        CheckBox cB9=(CheckBox)findViewById(R.id.checkBox9);
        cB9.setChecked(showPressure);
        // Setup Temperature Spinner
        spinnerTemps = (Spinner) findViewById(R.id.spinnerTemps);
        ArrayAdapter<String> tempAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, tempUnits);
        tempAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTemps.setAdapter(tempAdapter);
        spinnerTemps.setSelection(tempIndex);
        spinnerTemps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tempIndex=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Setup Speed Spinner
        spinnerSpeeds = (Spinner) findViewById(R.id.spinnerSpeeds);
        ArrayAdapter<String> speedAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, speedUnits);
        speedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpeeds.setAdapter(speedAdapter);
        spinnerSpeeds.setSelection(speedIndex);
        spinnerSpeeds.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                speedIndex=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Update the Scroll View
        final ListView listLocations = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, locations);
        listLocations.setAdapter(adapter);

        listLocations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int deleteIndex = i;
                deleteLocation((deleteIndex));
            }
        });
    }

    public void applySettings(View view) {
        Intent data = new Intent();
        CheckBox cB1=(CheckBox)findViewById(R.id.checkBox);
        showTemp=cB1.isChecked();
        CheckBox cB2=(CheckBox)findViewById(R.id.checkBox2);
        showMaxTemp=cB2.isChecked();
        CheckBox cB3=(CheckBox)findViewById(R.id.checkBox3);
        showMinTemp=cB3.isChecked();
        CheckBox cB4=(CheckBox)findViewById(R.id.checkBox4);
        showHumidity=cB4.isChecked();
        CheckBox cB5=(CheckBox)findViewById(R.id.checkBox5);
        showWindSpeed=cB5.isChecked();
        CheckBox cB6=(CheckBox)findViewById(R.id.checkBox6);
        showWindDirection=cB6.isChecked();
        CheckBox cB7=(CheckBox)findViewById(R.id.checkBox7);
        showFeelsLike=cB7.isChecked();
        CheckBox cB8=(CheckBox)findViewById(R.id.checkBox8);
        showCloudiness=cB8.isChecked();
        CheckBox cB9=(CheckBox)findViewById(R.id.checkBox9);
        showPressure=cB9.isChecked();
        data.putExtra("rLocations", locations);
        data.putExtra("rTempUnit", tempIndex);
        data.putExtra("rSpeedUnit", speedIndex);
        data.putExtra("rShowTemp",showTemp);
        data.putExtra("rShowMaxTemp",showMaxTemp);
        data.putExtra("rShowMinTemp",showMinTemp);
        data.putExtra("rShowHumidity",showHumidity);
        data.putExtra("rShowWindSpeed",showWindSpeed);
        data.putExtra("rShowWindDirection",showWindDirection);
        data.putExtra("rShowFeelsLike",showFeelsLike);
        data.putExtra("rShowCloudiness",showCloudiness);
        data.putExtra("rShowPressure",showPressure);
        setResult(RESULT_OK, data);
        finish();
    }

    public void newLocation(View view) {
        // Remove Blanks in the ArrayList
        for(int i = 0; i < locations.size(); i++) {
            if(locations.get(i).isEmpty()) {
                locations.remove(i);
                i--;
            }
        }

        EditText editLocation = (EditText) findViewById(R.id.editLocation);
        String newLocation = editLocation.getText().toString();

        // Remove Spaces in String
        String noSpaceLocation = newLocation.replaceAll("\\s", "");

        // Add new location to the ArrayList
        locations.add(noSpaceLocation);

        // Update the Scroll View
        ListView listLocations = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, locations);
        listLocations.setAdapter(adapter);

        // Clear EditView
        editLocation.setText("");
    }

    public void deleteLocation(final int index) {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Deleting Location").setMessage("Are you sure you want to delete " + locations.get(index) + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        locations.remove(index);
                        Toast.makeText(getApplicationContext(), "Location Deleted",Toast.LENGTH_SHORT).show();

                        // Update the Scroll View
                        ListView listLocations = (ListView) findViewById(R.id.listView);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, locations);
                        listLocations.setAdapter(adapter);
                    }
                }).setNegativeButton("No", null).show();

        // Remove Blanks in the ArrayList
        for(int i = 0; i < locations.size(); i++) {
            if(locations.get(i).isEmpty()) {
                locations.remove(i);
                i--;
            }
        }
        Log.i(TAG, "End of Delete");
    }
}