package edu.sjsu.sensor.app;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;

import android.hardware.SensorEventListener;
import android.widget.EditText;

public class MainActivity extends Activity implements SensorEventListener{

    private SensorManager sensorManager;
    private int stepCount = 0;
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        enableSensors(null);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        stopSensor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = System.currentTimeMillis();
        if (accelationSquareRoot >= 2) //
        {
            // Turn on camera torch

            if (stepCount < 0) {
                stepCount = -1;
            } else {
                stepCount--;
            }

            startTorch();
        }
    }

    private void startTorch() {
        if (stepCount == 0) {
            try {
                if (camera == null) {
                    camera = Camera.open();
                }

                Parameters p = camera.getParameters();
                p.setFlashMode(Parameters.FLASH_MODE_TORCH);
                camera.setParameters(p);
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
                finish();

            }
        }
    }

    private void stopTorch() {

        if (camera == null) {
            camera = Camera.open();
        }
        Parameters p = camera.getParameters();
        p.setFlashMode(Parameters.FLASH_MODE_OFF);
        camera.setParameters(p);
        camera.stopPreview();
        
        camera.release();
        camera = null;
    }

    public void stopSensor (View view) {
        stopTorch();
        sensorManager.unregisterListener(this);

    }

    public void enableSensors (View view) {
        // register this class as a listener for the orientation and
        // accelerometer sensors

        EditText stepBox = (EditText)findViewById(R.id.stepInput);
        String stepText = stepBox.getText().toString();
        try {
            // Set step count
            stepCount = Integer.parseInt(stepText);

            // Register for accelerometer listen
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);


        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
    }

}
