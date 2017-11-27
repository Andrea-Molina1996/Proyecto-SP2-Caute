package com.molina.andrea.myapplication;


import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatisticsFragment extends Fragment /*implements AdapterView.OnItemSelectedListener, View.OnClickListener*/
{

    private static final String TAG = "StatisticsFragment";
    static final int DIALOG_ID = 0;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference databaseReference;
    private List<AlertInfo> alertsList;

    private Spinner spinnerTypeOfChart;
    private ImageButton imageButtonDate;
    private EditText textViewDate;
    private Button buttonGenerate;
    private RadioGroup radioGroupRangeTime;
    private RadioButton radioButtonDay;
    private RadioButton radioButtonWeek;
    private RadioButton radioButtonMonth;
    private RelativeLayout relativeLayoutChartsl;
    private Button btnSubmit;
    private String[] months_selection = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic", "All"};
    private String month_selected;
    private String typeChartSelected;
    int year_x, month_x, day_x;


    public StatisticsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_statistics, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(); // Getting the instance of the database service
        FirebaseUser user = mAuth.getCurrentUser(); // getting the info of the user
        final String userId = user.getUid();
        alertsList = new ArrayList<>();

        spinnerTypeOfChart = (Spinner) getView().findViewById(R.id.spinnerTypeOfChart);

        // Initializing a String Array
        final String[] typeReport = new String[]{
                "Cantidad de Alertas",
                "Riesgo",
                "Horas Riesgosas",
                "Por Ubicación"
        };

        // Initializing an ArrayAdapter
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(),R.layout.spinner_item, typeReport);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerTypeOfChart.setAdapter(spinnerArrayAdapter);

        imageButtonDate = (ImageButton) getView().findViewById(R.id.imageButtonDate);
        imageButtonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment picker = new DatePickerFragment();
                picker.show(getFragmentManager(), "datePicker");
            }
        });

        spinnerTypeOfChart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, " Tipo de grafica "+typeReport[i]);
                typeChartSelected = typeReport[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                typeChartSelected = " ";
            }
        });

        buttonGenerate = (Button) getView().findViewById(R.id.buttonGenerate);
        buttonGenerate.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                /*textViewDate = (EditText) getView().findViewById(R.id.textViewDate);
                final String str_date= textViewDate.getText().toString();
                final DateFormat formatter ;
                final Date date ;
                formatter = new SimpleDateFormat("dd-MM-yyyy");
                try {
                    date = (Date)formatter.parse(str_date);
                    Log.d(TAG, "Date: "+date.getMonth());
                    Log.d(TAG, "Month: "+months_selection[date.getMonth()]);
                    month_selected = months_selection[date.getMonth()];
                    Log.d(TAG, "Type chart: "+typeChartSelected);
                    radioGroupRangeTime = (RadioGroup) getView().findViewById(R.id.radioGroupRangeTime);
                    int checkedRadioButtonId = radioGroupRangeTime.getCheckedRadioButtonId();
                    if (checkedRadioButtonId == -1) {
                        Toast.makeText(getActivity(), "Debe seleccionar un tipo de reporte a generar.", Toast.LENGTH_SHORT).show();
                    }else{
                        if (checkedRadioButtonId == R.id.radioButtonMonth) {
                            if(typeChartSelected.equals("Riesgo")){
                                final BarChart chart = new BarChart(getContext());
                                // get a layout defined in xml
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                                RelativeLayout rl = (RelativeLayout) getView().findViewById(R.id.relativeLayoutCharts);
                                rl.addView(chart, params);
                                Log.d(TAG, "Selecciono mensual");
                                List<AlertInfo> alertList = new ArrayList<>();
                                final ArrayList<BarEntry> BarEntry = new ArrayList<>();
                                databaseReference.child(userId).child("Alerts").child(month_selected).addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                // Get user value
                                                int i =0;
                                                for(DataSnapshot alertSnapshot: dataSnapshot.getChildren()){
                                                    AlertInfo alert = alertSnapshot.getValue(AlertInfo.class);
                                                    alertsList.add(alert);
                                                    BarEntry.add(new BarEntry(i, alert.getSeriousness()));
                                                    i++;
                                                }
                                                BarDataSet dataSet = new BarDataSet(BarEntry, "Seriedad");
                                                dataSet.setColor(Color.parseColor("#E53935"));
                                                BarData data = new BarData(dataSet);
                                                data.setBarWidth(0.9f); // set custom bar width
                                                chart.setFitBars(true); // make the x-axis fit exactly all bars
                                                chart.invalidate();
                                                chart.setData(data);
                                                chart.invalidate();
                                                chart.setDescription(null);
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                                            }
                                        });
                            }
                        }else if (checkedRadioButtonId == R.id.radioButtonDay){
                            Log.d(TAG, "Selecciono por dia");
                        }else if(checkedRadioButtonId == R.id.radioButtonWeek){
                            Log.d(TAG, "Selecciono por semana");
                            final Calendar c = Calendar.getInstance();
                            c.setTime(date); // yourdate is an object of type Date
                            final int weekNumber = c.get(Calendar.WEEK_OF_YEAR); // this will for example return 2 for tuesday
                            Log.d(TAG,"Week number " + weekNumber);
                            if(typeChartSelected.equals("Riesgo")){
                                final BarChart chart = new BarChart(getContext());
                                // get a layout defined in xml
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                                RelativeLayout rl = (RelativeLayout) getView().findViewById(R.id.relativeLayoutCharts);
                                rl.addView(chart, params);
                                Log.d(TAG, "Selecciono mensual");
                                List<AlertInfo> alertList = new ArrayList<>();
                                final ArrayList<BarEntry> BarEntry = new ArrayList<>();
                                databaseReference.child(userId).child("Alerts").child(month_selected).addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                // Get user value
                                                int i =0;
                                                for(DataSnapshot alertSnapshot: dataSnapshot.getChildren()){
                                                    AlertInfo alert = alertSnapshot.getValue(AlertInfo.class);
                                                    try {
                                                        Date dateAlert = (Date)formatter.parse(alert.getDate().toString());
                                                        c.setTime(dateAlert); // yourdate is an object of type Date
                                                        int weekNumberAlert = c.get(Calendar.WEEK_OF_YEAR); // this will for example return 2 for tuesday
                                                        if(weekNumber == weekNumberAlert){
                                                            BarEntry.add(new BarEntry(i, alert.getSeriousness()));
                                                            i++;
                                                        }
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                                BarDataSet dataSet = new BarDataSet(BarEntry, "Seriedad");
                                                dataSet.setColor(Color.parseColor("#E53935"));
                                                BarData data = new BarData(dataSet);
                                                data.setBarWidth(0.9f); // set custom bar width
                                                chart.setFitBars(true); // make the x-axis fit exactly all bars
                                                chart.invalidate();
                                                chart.setData(data);
                                                chart.invalidate();
                                                chart.setDescription(null);
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                                            }
                                        });
                            }
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }*/
                getAlertsPerDay("Nov", "23-11-2107",userId);
            }
        });

    }

    /**
     * @desc Gets the date parameter from the DatePickerFragment
     * @param text
     */
    public void setText(String text) {
        EditText t = (EditText) getView().findViewById(R.id.textViewDate);  //UPDATE
        t.setText(text);
    }

    private void getAlertsPerDay(String month, String date, String userId){
        final TaskCompletionSource<DataSnapshot> dbSource = new TaskCompletionSource<>();
        Task<DataSnapshot> dbTask = dbSource.getTask();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(userId).child("Alerts").child(month);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dbSource.setResult(dataSnapshot);
                Log.d(TAG, "dbSource result: " + dbSource.getTask());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                dbSource.setException(databaseError.toException());
                Log.d(TAG, "dbSource result: " + dbSource.toString());
            }
        });
    }

}
