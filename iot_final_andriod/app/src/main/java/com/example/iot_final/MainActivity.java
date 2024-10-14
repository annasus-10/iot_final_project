package com.example.iot_final;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity {
    private GraphView graphView;
    private LineGraphSeries<DataPoint> seriesPV; // For PV data
    private LineGraphSeries<DataPoint> seriesSP; // For SP data
    private int xValue = 0; // To keep track of the X axis points

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

        seriesPV = new LineGraphSeries<>();
        seriesSP = new LineGraphSeries<>();

        graphView.addSeries(seriesPV); // Add PV series
        graphView.addSeries(seriesSP); // Add SP series

        // Customize the X and Y axes
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(100);
        graphView.getViewport().setXAxisBoundsManual(true);

        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMaxY(100);
        graphView.getViewport().setYAxisBoundsManual(true);

        // Set labels for axes
        graphView.getGridLabelRenderer().setHorizontalAxisTitle("Time (seconds)");
        graphView.getGridLabelRenderer().setVerticalAxisTitle("Value");

        // Set labels for axes
        graphView.getGridLabelRenderer().setHorizontalAxisTitle("Setpoint Value (SP)"); // SP on X-axis
        graphView.getGridLabelRenderer().setVerticalAxisTitle("Process Variable (PV)"); // PV on Y-axis

        // Reference to Firebase Database
        DatabaseReference refPV = database.getReference("currentValue");
        DatabaseReference refSP = database.getReference("setpointValue");

        // Listener for PV data
        refPV.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    double pv = snapshot.getValue(Double.class); // Get the PV value
                    
                    // You need to fetch the current SP value to plot (this is the assumption)
                    refSP.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot spSnapshot) {
                            if (spSnapshot.exists()) {
                                double sp = spSnapshot.getValue(Double.class); // Get the SP value
                                // Append the new DataPoint for SP vs PV
                                seriesSP.appendData(new DataPoint(sp, pv), true, 100); // Use a single series
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle errors here
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvHeader), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
    }
}