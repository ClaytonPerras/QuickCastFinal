 package com.cnit355.quickcast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    // API Variables
    public static String BaseUrl = "http://api.openweathermap.org/";
    public static String AppId = "5463c2ff4638d57cbc0f412f3fc9c5f6";

    // Locations ArrayList
    ArrayList<String> locations = new ArrayList<>();

    // Tag used for testing with Logs
    private static final String TAG = "Testing";

    // Default Settings
    private String city;
    private String countryCode="";
    String tempUnits[] = {"K", "C", "F"};
    private int tempIndex = 0;
    String speedUnits[] = {"mph", "kph", "knots", "m/s", "ft/s"};
    private int speedIndex = 0;
    private static String forecastDayNum = "3";
    private int countryUpdateCounter=0;

    boolean showTemp=true;
    boolean showMaxTemp=true;
    boolean showMinTemp=true;
    boolean showHumidity=true;
    boolean showWindSpeed=true;
    boolean showWindDirection=true;
    boolean showFeelsLike=true;
    boolean showCloudiness=true;
    boolean showPressure=true;

    // Intent Variables
    private static final int request_code = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get Saved Information
        try {
            readLocations();
            readUnits();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            locations.add("Lafayette,IN");
            try {
                writeLocations();
                writeUnits();
            } catch(IOException f) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        // Set Spinner Options
        final Spinner spinnerLocations = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocations.setAdapter(adapter);

        // Set Location Text
        TextView cityText = (TextView) findViewById(R.id.place);
        cityText.setText(spinnerLocations.getSelectedItem().toString());

        // Set Date Text
        TextView date = (TextView) findViewById(R.id.date);
        SimpleDateFormat currentDate = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        date.setText(currentDate.format(Calendar.getInstance().getTime()));



        spinnerLocations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Set Location Text
                TextView cityText = (TextView) findViewById(R.id.place);
                cityText.setText(spinnerLocations.getSelectedItem().toString());
                countryUpdateCounter=0;
                // Set City
                city = locations.get(spinnerLocations.getSelectedItemPosition());

                city = city.toLowerCase();
                // Call API Method
                getCurrentData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set Spinner Options
        Spinner spinnerLocations = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocations.setAdapter(adapter);

        // Call API Method
        getCurrentData();
    }



    public void openSettings(View view) {
        Intent mIntent = new Intent(this, SettingsActivity.class);
        mIntent.putExtra("qLocations", locations);
        mIntent.putExtra("qTempUnit", tempIndex);
        mIntent.putExtra("qSpeedUnit", speedIndex);
        mIntent.putExtra("qShowTemp",showTemp);
        mIntent.putExtra("qShowMaxTemp",showMaxTemp);
        mIntent.putExtra("qShowMinTemp",showMinTemp);
        mIntent.putExtra("qShowHumidity",showHumidity);
        mIntent.putExtra("qShowWindSpeed",showWindSpeed);
        mIntent.putExtra("qShowWindDirection",showWindDirection);
        mIntent.putExtra("qShowFeelsLike",showFeelsLike);
        mIntent.putExtra("qShowCloudiness",showCloudiness);
        mIntent.putExtra("qShowPressure",showPressure);
        startActivityForResult(mIntent, request_code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if((requestCode == request_code) && (resultCode == RESULT_OK)) {
            locations = data.getStringArrayListExtra("rLocations");
            tempIndex = data.getIntExtra("rTempUnit",0);
            speedIndex = data.getIntExtra("rSpeedUnit",0);
            showTemp = data.getBooleanExtra("rShowTemp",true);
            showMaxTemp = data.getBooleanExtra("rShowMaxTemp",true);
            showMinTemp = data.getBooleanExtra("rShowMinTemp",true);
            showHumidity = data.getBooleanExtra("rShowHumidity",true);
            showWindSpeed = data.getBooleanExtra("rShowWindSpeed",true);
            showWindDirection = data.getBooleanExtra("rShowWindDirection",true);
            showFeelsLike = data.getBooleanExtra("rShowFeelsLike",true);
            showCloudiness = data.getBooleanExtra("rShowCloudiness",true);
            showPressure = data.getBooleanExtra("rShowPressure",true);
            try {
                writeLocations();
                writeUnits();
                Toast.makeText(getApplicationContext(), "Settings Saved", Toast.LENGTH_SHORT).show();
            } catch(IOException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    void getCurrentData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.getCurrentWeatherData(city, AppId);

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (response.code() == 200) {
                    WeatherResponse weatherResponse = response.body();
                    assert weatherResponse != null;

                    // Set Temperature
                    EditText weatherHeader = (EditText) findViewById(R.id.weatherHeaders);
                    EditText weatherInfo = (EditText) findViewById(R.id.weatherInfo);
                    weatherHeader.setText("");
                    weatherInfo.setText("");
                    float tempMeasurement = weatherResponse.main.temp;
                    float maxTempMeasurement = weatherResponse.main.temp_max;
                    float minTempMeasurement = weatherResponse.main.temp_min;
                    float feelsLikeMeasurement = weatherResponse.main.feels_like;
                    if (tempUnits[tempIndex] != "K") {
                        tempMeasurement -= 273.15;
                        maxTempMeasurement -= 273.15;
                        minTempMeasurement -= 273.15;
                        feelsLikeMeasurement -= 273.15;
                        if (tempUnits[tempIndex] == "F") {
                            tempMeasurement = (float) ((tempMeasurement * 9.0 / 5) + 32);
                            maxTempMeasurement = (float) ((maxTempMeasurement * 9.0 / 5) + 32);
                            minTempMeasurement = (float) ((minTempMeasurement * 9.0 / 5) + 32);
                            feelsLikeMeasurement = (float) ((feelsLikeMeasurement * 9.0 / 5) + 32);
                        }
                    }
                    if (showTemp == true) {
                        String temp = String.format("%.1f", tempMeasurement);
                        weatherHeader.append("Temperature:\n");
                        weatherInfo.append(temp + "° " + tempUnits[tempIndex] + "\n");
                    }
                    if (showMaxTemp == true) {
                        String maxTemp = String.format("%.1f", maxTempMeasurement);
                        weatherHeader.append("Max Temperature:\n");
                        weatherInfo.append(maxTemp + "° " + tempUnits[tempIndex] + "\n");
                    }
                    if (showMinTemp == true) {
                        String minText = String.format("%.1f", minTempMeasurement);
                        weatherHeader.append("Min Temperature:\n");
                        weatherInfo.append(minText + "° " + tempUnits[tempIndex] + "\n");
                    }
                    if (showFeelsLike == true) {
                        String minText = String.format("%.1f", feelsLikeMeasurement);
                        weatherHeader.append("Feels Like:\n");
                        weatherInfo.append(minText + "° " + tempUnits[tempIndex] + "\n");
                    }

                    if (showCloudiness == true) {
                        String cloudiness=String.format("%.1f",weatherResponse.clouds.all);
                        weatherHeader.append("Cloudiness:\n");
                        weatherInfo.append(cloudiness+"%\n");
                    }
                    if (showPressure == true) {
                        String pressure=String.format("%.1f",weatherResponse.main.pressure);
                        weatherHeader.append("Pressure:\n");
                        weatherInfo.append(pressure+" hPa\n");
                    }
                    // Set Forecast
                    if(showHumidity==true)
                    {
                        String humidity=String.format("%.1f",weatherResponse.main.humidity);
                        weatherHeader.append("Humidity:\n");
                        weatherInfo.append(humidity+"%\n");
                    }

                    // Set Wind Speed
                    float windSpeedMeasurement=weatherResponse.wind.speed;
                    if(speedUnits[speedIndex].equals("mph"))
                    {
                        windSpeedMeasurement=(float)2.2369363*windSpeedMeasurement;
                    }
                    else if(speedUnits[speedIndex].equals("kph"))
                    {
                        windSpeedMeasurement=(float)3.6*windSpeedMeasurement;
                    }
                    else if(speedUnits[speedIndex].equals("knots"))
                    {
                        windSpeedMeasurement=(float)1.9438445*windSpeedMeasurement;
                    }
                    else if(speedUnits[speedIndex].equals("ft/s"))
                    {
                        windSpeedMeasurement=(float)3.2808399*windSpeedMeasurement;
                    }
                    if(showWindSpeed==true)
                    {
                        String windSpeed=String.format("%.1f",windSpeedMeasurement);
                        weatherHeader.append("Wind Speed:\n");
                        weatherInfo.append(windSpeed+" "+speedUnits[speedIndex]+"\n");
                    }

                    if(showWindDirection==true)
                    {
                        weatherHeader.append("Wind Direction:\n");
                        float windDeg=weatherResponse.wind.deg;
                        String windDirection="error";
                        if(windDeg>=348.75||windDeg<=11.24){
                            windDirection="N";
                        }
                        else if(windDeg>=11.25&&windDeg<=33.74)
                        {
                            windDirection="NNE";
                        }
                        else if(windDeg>=33.75&&windDeg<=56.24)
                        {
                            windDirection="NE";
                        }
                        else if(windDeg>=56.24&&windDeg<=78.74)
                        {
                            windDirection="ENE";
                        }
                        else if(windDeg>=78.75&&windDeg<=101.24)
                        {
                            windDirection="E";
                        }
                        else if(windDeg>=101.24&&windDeg<=123.74)
                        {
                            windDirection="ESE";
                        }
                        else if(windDeg>=123.75&&windDeg<=146.24)
                        {
                            windDirection="SE";
                        }
                        else if(windDeg>=146.25&&windDeg<=168.74)
                        {
                            windDirection="SSE";
                        }
                        else if(windDeg>=168.75&&windDeg<=191.24)
                        {
                            windDirection="S";
                        }
                        else if(windDeg>=191.24&&windDeg<=213.74)
                        {
                            windDirection="SSW";
                        }
                        else if(windDeg>=213.75&&windDeg<=236.24)
                        {
                            windDirection="SW";
                        }
                        else if(windDeg>=236.25&&windDeg<=258.74)
                        {
                            windDirection="WSW";
                        }
                        else if(windDeg>=258.75&&windDeg<=281.24)
                        {
                            windDirection="W";
                        }
                        else if(windDeg>=281.25&&windDeg<=303.74)
                        {
                            windDirection="WNW";
                        }
                        else if(windDeg>=303.75&&windDeg<=326.24)
                        {
                            windDirection="NW";
                        }
                        else if(windDeg>=326.25&&windDeg<=348.74)
                        {
                            windDirection="NNW";
                        }
                        weatherInfo.append(windDirection+"\n");
                    }



                    countryCode=weatherResponse.sys.country;
                    TextView cityAppend=(TextView)findViewById(R.id.place);
                    if (countryUpdateCounter == 0) {
                        cityAppend.append(", "+countryCode);
                        countryUpdateCounter++;
                    }



                    TextView forecast = (TextView) findViewById(R.id.forecast);
                    String tempText4=weatherResponse.weather.get(0).main.toUpperCase();
                    forecast.setText(tempText4);

                    ImageView fImage=(ImageView)findViewById(R.id.forecastImage);
                    if(tempText4.equals("CLEAR")){
                        Drawable clear= ResourcesCompat.getDrawable(getResources(),R.drawable.clear,null);
                        fImage.setImageDrawable(clear);
                    }
                    else if(tempText4.equals("CLOUDS")){
                        Drawable clouds= ResourcesCompat.getDrawable(getResources(),R.drawable.clouds,null);
                        fImage.setImageDrawable(clouds);
                    }
                    else if(tempText4.equals("THUNDERSTORM")){
                        Drawable thunderstorm= ResourcesCompat.getDrawable(getResources(),R.drawable.thunderstorm,null);
                        fImage.setImageDrawable(thunderstorm);
                    }
                    else if(tempText4.equals("DRIZZLE") || tempText4.equals("RAIN")){
                        Drawable rain= ResourcesCompat.getDrawable(getResources(),R.drawable.rain,null);
                        fImage.setImageDrawable(rain);
                    }
                    else if(tempText4.equals("SNOW")){
                        Drawable snow= ResourcesCompat.getDrawable(getResources(),R.drawable.snow,null);
                        fImage.setImageDrawable(snow);
                    }
                    else{
                        Drawable mist= ResourcesCompat.getDrawable(getResources(),R.drawable.mist,null);
                        fImage.setImageDrawable(mist);
                    }
                    Log.i(TAG, Integer.toString(weatherResponse.weather.size()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                Toast toast = Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    private void writeLocations() throws IOException{
        // Create Output Stream
        FileOutputStream outFs = openFileOutput("locations.txt", Context.MODE_PRIVATE);

        // Create List of Locations from the Array
        String str = "";
        for(int i = 0; i < locations.size(); i++) {
            if(locations.get(i).equals("")){
                continue;
            }
            str = str + locations.get(i) + "\n";
        }

        // Write and Close the Output Stream
        outFs.write(str.getBytes());
        outFs.close();
        Toast.makeText(getApplicationContext(), "Locations Saved", Toast.LENGTH_SHORT).show();

    }

    private void writeUnits() throws IOException{
        // Create Output Stream
        FileOutputStream outFs = openFileOutput("units.txt", Context.MODE_PRIVATE);

        // Create List of Units from the Array
        String str = tempIndex + "\n" + speedIndex;

        // Write and Close the Output Stream
        outFs.write(str.getBytes());
        outFs.close();
        Toast.makeText(getApplicationContext(), "Units Saved", Toast.LENGTH_SHORT).show();
    }

    private void readLocations() throws FileNotFoundException {
        File fileLocations = new File("locations.txt");
        FileInputStream fis = getApplicationContext().openFileInput(fileLocations.toString());
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                locations.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            // Error occurred when opening raw file for reading.
        }

        // Remove Blanks in the ArrayList
        for(int i = 0; i < locations.size(); i++) {
            if(locations.get(i).isEmpty()) {
                locations.remove(i);
                i--;
            }
        }
    }

    private void readUnits() throws IOException {
        File fileUnits = new File("units.txt");
        FileInputStream fis = getApplicationContext().openFileInput(fileUnits.toString());
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            tempIndex = Integer.parseInt(line);
            speedIndex = Integer.parseInt(line);
        } catch (IOException e) {
            // Error occurred when opening raw file for reading.
        } finally {
            locations.add(stringBuilder.toString());
        }
    }
}