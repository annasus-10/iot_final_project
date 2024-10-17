package com.example.iot_final;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // References for UI elements
    private ImageView hiAlarmImageView;
    private ImageView loAlarmImageView;
    private GraphView graphView;
    private Button btnOn, btnOff;
    private SeekBar seekBarSP, seekBarHA, seekBarLA;
    private TextView textViewSP, textViewHA, textViewLA;

    // Graph data series
    private LineGraphSeries<DataPoint> pvSeries = new LineGraphSeries<>();
    private LineGraphSeries<DataPoint> spSeries = new LineGraphSeries<>();
    private double latestSP = 0.0;
    private double latestPV = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        hiAlarmImageView = findViewById(R.id.HiAlarmImage);
        loAlarmImageView = findViewById(R.id.LoAlarmImage);
        graphView = findViewById(R.id.idGraphView);
        btnOn = findViewById(R.id.ONbutton);
        btnOff = findViewById(R.id.OFFbutton);
        seekBarSP = findViewById(R.id.SetPointSeekBar);
        seekBarHA = findViewById(R.id.HiAlarmSeekBar);
        seekBarLA = findViewById(R.id.LowAlarmSeekBar);
        textViewSP = findViewById(R.id.SetPointValue);
        textViewHA = findViewById(R.id.HiAlarmValue);
        textViewLA = findViewById(R.id.LowAlarmValue);

        // Firebase Database references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference refStatus = database.getReference("status");
        DatabaseReference highAlarmRef = database.getReference("highAlarmStatus");
        DatabaseReference lowAlarmRef = database.getReference("lowAlarmStatus");

        // Set up listeners for alarm statuses
        setupAlarmStatusListener(highAlarmRef, hiAlarmImageView, true);
        setupAlarmStatusListener(lowAlarmRef, loAlarmImageView, false);

        // Set up button click listeners for controlling the lamp
        setupButtons(refStatus);

        // Set up seek bars for user input
        setupSeekBars(database);

        // Set up the graph for visualizing values
        setupGraph();

        // Set up listeners for process variable (PV) and setpoint (SP) values
        setupValueListeners(database);
    }

    private void setupAlarmStatusListener(DatabaseReference alarmRef, ImageView alarmImageView, boolean isHighAlarm) {
        alarmRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean isAlarmOn = dataSnapshot.getValue(Boolean.class);
                    if (isAlarmOn != null) {
                        // Update the ImageView based on the alarm status
                        alarmImageView.setImageResource(isAlarmOn ? R.drawable.ledon : R.drawable.ledoff);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    private void setupButtons(DatabaseReference refStatus) {
        btnOn.setOnClickListener(v -> {
            // Set the status in Firebase to indicate the lamp should be on
            refStatus.setValue(true)
                    .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Lamp turned ON", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Log.e("FirebaseError", "Error updating status: " + e.getMessage()));
        });

        btnOff.setOnClickListener(v -> {
            // Set the status in Firebase to indicate the lamp should be off
            refStatus.setValue(false)
                    .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Lamp turned OFF", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Log.e("FirebaseError", "Error updating status: " + e.getMessage()));
        });
    }

    private void setupSeekBars(FirebaseDatabase database) {
        setupSeekBar(seekBarSP, textViewSP, database.getReference("setpointValue"));
        setupSeekBar(seekBarHA, textViewHA, database.getReference("highAlarmValue"));
        setupSeekBar(seekBarLA, textViewLA, database.getReference("lowAlarmValue"));
    }

    private void setupSeekBar(SeekBar seekBar, TextView textView, DatabaseReference ref) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.valueOf(progress));
                ref.setValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupGraph() {
        graphView.addSeries(pvSeries);
        graphView.addSeries(spSeries);
        pvSeries.setColor(Color.RED);
        pvSeries.setTitle("PV");
        spSeries.setColor(Color.BLUE);
        spSeries.setTitle("SP");
        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return new SimpleDateFormat("HH:mm:ss").format(new Date((long) value));
                } else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        graphView.getGridLabelRenderer().setNumHorizontalLabels(3);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(System.currentTimeMillis());
        graphView.getViewport().setMaxX(System.currentTimeMillis() + 20000);
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMaxY(100);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getGridLabelRenderer().setHorizontalAxisTitle("Time (seconds)");
        graphView.getGridLabelRenderer().setVerticalAxisTitle("Value");
    }

    private void setupValueListeners(FirebaseDatabase database) {
        DatabaseReference refPV = database.getReference("currentValue");
        DatabaseReference refSP = database.getReference("setpointValue");

        refPV.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Double currentValue = dataSnapshot.getValue(Double.class);
                    if (currentValue != null) {
                        updateGraph(currentValue, latestSP);
                        latestPV = currentValue;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        refSP.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Double setpointValue = dataSnapshot.getValue(Double.class);
                    if (setpointValue != null) {
                        updateGraph(latestPV, setpointValue);
                        latestSP = setpointValue;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Error while reading setpoint value: " + databaseError.getMessage());
                Toast.makeText(MainActivity.this, "Error accessing setpoint value. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGraph(double pv, double sp) {
        long time = System.currentTimeMillis();
        pvSeries.appendData(new DataPoint(time, pv), true, 100);
        spSeries.appendData(new DataPoint(time, sp), true, 100);
    }
}
