package com.molina.andrea.myapplication;


import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.widget.AdapterView.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlertsAddInfoFragment extends Fragment implements OnItemSelectedListener, View.OnClickListener{

    private static final String TAG = "AlertsAddInfoFragment";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference databaseReference;

    ListView listViewAlerts;
    List<AlertInfo> alertsList;

    private Spinner spinner;
    private Button btnSubmit;
    private String[] months_selection = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
    private String[] months = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
    private String month_selected;

    String alertId = "";

    public AlertsAddInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_alerts_add_info, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinner = (Spinner) getView().findViewById(R.id.spinner);
        // Initializing an ArrayAdapter
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(),R.layout.spinner_item, months);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(spinnerArrayAdapter);


        listViewAlerts = (ListView) view.findViewById(R.id.listViewAlerts);
        //spinner = (Spinner) getView().findViewById(R.id.spinner);
        btnSubmit = (Button) getView().findViewById(R.id.btnSubmit);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(); // Getting the instance of the database service
        alertsList = new ArrayList<>();

        spinner.setOnItemSelectedListener(this);
        btnSubmit.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if(view == btnSubmit){
            FirebaseUser user = mAuth.getCurrentUser(); // getting the info of the user
            final String userId = user.getUid();
            alertsList.clear();
            databaseReference.child(userId).child("Alerts").child(month_selected).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            // Get user value
                            for(DataSnapshot alertSnapshot: dataSnapshot.getChildren()){
                                Log.d(TAG, "Alerts Snapshot: " + alertSnapshot);
                                AlertInfo alert = alertSnapshot.getValue(AlertInfo.class);
                                Log.d(TAG, "Alerts: " + alert);
                                alertsList.add(alert);
                            }
                            Log.d(TAG, "Size: " + alertsList.size());

                            AlertListAddInfoAdapter adapter = new AlertListAddInfoAdapter(getContext(), alertsList);
                            listViewAlerts.setAdapter(adapter);
                            listViewAlerts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Log.d(TAG, "Alerta: " + alertsList.get(i).getTime());
                                    for(DataSnapshot alertSnapshot: dataSnapshot.getChildren()){
                                        AlertInfo alert = alertSnapshot.getValue(AlertInfo.class);
                                        AlertInfo alertA = alertsList.get(i);
                                        Log.d(TAG,"Alert: "+alert + " - AlertList: "+alertA);
                                        Log.d(TAG, "Alert: "+alert.getTime()+" "+alert.getDate()+" "+alert.getSeriousness()+ "AlertList: "+ alertA.getDate() + " "+ alertA.getTime()+" "+alertA.getSeriousness());
                                        if(alert.equals(alertA)){
                                            alertId = alertSnapshot.getKey();
                                            Log.d(TAG, "Alert key: " + alertId);
                                        }
                                    }
                                    Bundle bundle = new Bundle();
                                    bundle.putString("edttext", alertId+" "+month_selected);
                                    ViewAllInfoAlert viewAllInfoAlert = new ViewAllInfoAlert();
                                    viewAllInfoAlert.setArguments(bundle);
                                    FragmentManager manager = getActivity().getSupportFragmentManager();
                                    manager.beginTransaction().replace(R.id.mainLayout, viewAllInfoAlert).commit();

                                }
                            });
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        }
                    });

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        month_selected = months_selection[i];
        Log.d(TAG, "Mes seleccionado: "+month_selected);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}
