package com.molina.andrea.myapplication;

import android.app.DatePickerDialog;
import android.app.Dialog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by amoli on 11/5/2017.
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "DatePickerFragment";
    int year_x = 0;
    int month_x = 0;
    int day_x = 0;
    String dateSelected = "";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c;

            c = Calendar.getInstance();
            year_x = c.get(Calendar.YEAR);
            month_x = c.get(Calendar.MONTH);
            day_x = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        Log.d(TAG, " initialDate: " + day_x + " - " + month_x + " - " + year_x);
        return new DatePickerDialog(getActivity(), this, year_x, month_x, day_x);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar c = null;
        c = Calendar.getInstance();
        c.set(year, month, day);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = sdf.format(c.getTime());
        FragmentManager manager = getFragmentManager();
        StatisticsFragment frag = (StatisticsFragment)manager.findFragmentById(R.id.mainLayout);
        frag.setText(formattedDate);
        Log.d(TAG, " dateSelected: " + formattedDate);
        }
    }

