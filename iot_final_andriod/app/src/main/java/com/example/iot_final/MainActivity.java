package com.example.iot_final;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//
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


    // Reference to ImageViews for high and low alarms
    private ImageView hiAlarmImageView;
    private ImageView loAlarmImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the ImageViews for high and low alarms
        hiAlarmImageView = findViewById(R.id.HiAlarmImage);
        loAlarmImageView = findViewById(R.id.LoAlarmImage);

        // Firebase Database references for high and low alarm statuses
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference highAlarmRef = database.getReference("highAlarmStatus");
        DatabaseReference lowAlarmRef = database.getReference("lowAlarmStatus");

        // Listen for changes in high alarm status
        highAlarmRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean isHighAlarmOn = dataSnapshot.getValue(Boolean.class);

                    if (isHighAlarmOn != null && isHighAlarmOn) {
                        // High alarm is ON, show LED ON image
                        hiAlarmImageView.setImageResource(R.drawable.ledon);
                    } else {
                        // High alarm is OFF, show LED OFF image
                        hiAlarmImageView.setImageResource(R.drawable.ledoff);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });

        // Listen for changes in low alarm status
        lowAlarmRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean isLowAlarmOn = dataSnapshot.getValue(Boolean.class);

                    if (isLowAlarmOn != null && isLowAlarmOn) {
                        // Low alarm is ON, show LED ON image
                        loAlarmImageView.setImageResource(R.drawable.ledon);
                    } else {
                        // Low alarm is OFF, show LED OFF image
                        loAlarmImageView.setImageResource(R.drawable.ledoff);
                    }
                }
            }
    private GraphView graphView;
    LineGraphSeries<DataPoint> pvSeries = new LineGraphSeries<>();
    LineGraphSeries<DataPoint> spSeries = new LineGraphSeries<>();
    private double latestSP = 0.0;
    private double latestPV = 0.0;

    Button btnOn, btnOff;
    SeekBar seekBarSP, seekBarHA, seekBarLA;
    TextView textViewSP, textViewHA, textViewLA;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        graphView = findViewById(R.id.idGraphView);
        btnOn = findViewById(R.id.ONbutton);
        btnOff = findViewById(R.id.OFFbutton);
        seekBarSP = findViewById(R.id.SetPointSeekBar);
        seekBarHA = findViewById(R.id.HiAlarmSeekBar);
        seekBarLA = findViewById(R.id.LowAlarmSeekBar);
        textViewSP = findViewById(R.id.SetPointValue);
        textViewHA = findViewById(R.id.HiAlarmValue);
        textViewLA = findViewById(R.id.LowAlarmValue);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference refStatus = database.getReference("status");

        btnOn.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refStatus.setValue(true);
                }
            }
        );

        btnOff.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refStatus.setValue(false);
                    }
                }
        );

        seekBarSP.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        DatabaseReference refValue = database.getReference("setpointValue");
                        textViewSP.setText(String.valueOf(progress));
                        refValue.setValue(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );


        seekBarHA.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        DatabaseReference refValue = database.getReference("highAlarmValue");
                        textViewHA.setText(String.valueOf(progress));
                        refValue.setValue(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );


        seekBarLA.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        DatabaseReference refValue = database.getReference("lowAlarmValue");
                        textViewLA.setText(String.valueOf(progress));
                        refValue.setValue(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

        graphView.addSeries(pvSeries);
        graphView.addSeries(spSeries);

        pvSeries.setColor(Color.RED);  // Set PV series to red
        pvSeries.setTitle("PV");
        spSeries.setColor(Color.BLUE); // Set SP series to blue
        spSeries.setTitle("SP");


        // Enable the legend to distinguish between PV and SP
        graphView.getLegendRenderer().setVisible(true);
        graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);


        // Customize the x-axis to display only the current time
        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // Only show the current time in a readable format
                    return new SimpleDateFormat("HH:mm:ss").format(new Date((long) value));
                } else {
                    // Show normal y-axis values (PV and SP values)
                    return super.formatLabel(value, isValueX);
                }
            }
        });

// Customize the number of x-axis labels to avoid crowding
        graphView.getGridLabelRenderer().setNumHorizontalLabels(3); // Show 3 labels to avoid clutter

// Disable automatic scaling of the x-axis (if you want to keep the range static)
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(System.currentTimeMillis()); // Start from the current time
        graphView.getViewport().setMaxX(System.currentTimeMillis() + 20000); // 1 minute window


        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMaxY(100);
        graphView.getViewport().setYAxisBoundsManual(true);

        // Set labels for axes
        graphView.getGridLabelRenderer().setHorizontalAxisTitle("Time (seconds)");
        graphView.getGridLabelRenderer().setVerticalAxisTitle("Value");

        // Reference to Firebase Database
        DatabaseReference refPV = database.getReference("currentValue");
        DatabaseReference refSP = database.getReference("setpointValue");
        // Variables to store PV and SP values


        // Listener for PV (currentValue)
        refPV.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final double currentValue = dataSnapshot.getValue(Double.class);
                    updateGraph(currentValue, latestSP);  // Update graph with current values
                    latestPV = currentValue;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

// Listener for SP (setpointValue)
        refSP.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final double setpointValue = dataSnapshot.getValue(Double.class);  // Assuming values are doubles
                    updateGraph(latestPV, setpointValue);  // Update graph with current values
                    latestSP = setpointValue;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvHeader), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
    }

    private void updateGraph(double pv, double sp) {
        // Add a timestamp for x-axis (assuming real-time plotting)
        long time = System.currentTimeMillis();

        // Update PV series
        pvSeries.appendData(new DataPoint(time, pv), true, 100);

        // Update SP series
        spSeries.appendData(new DataPoint(time, sp), true, 100);

        // Optionally, compare PV and SP for additional logic
        if (pv > sp) {
            // Perform some action if PV is greater than SP
        } else if (pv < sp) {
            // Perform some action if PV is less than SP
        }
    }

}