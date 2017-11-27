package com.molina.andrea.myapplication;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "EditProfileFragment";
    private EditText textNombre;
    private EditText textApellido;
    private EditText textUsername;
    private EditText textPhoneNumber;
    private RadioButton radioButton;
    private RadioButton radioButton2;
    private RadioGroup radioGroupGender;
    Button buttonNext;
    Button buttonDatePicker;
    EditText editTextDate;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference databaseReference;

    private static final String ERROR_OBLIGATORY_FIELD = "Campo obligatorio";
    private static final String ERROR_FIRST_NAME = "Invalido (solo letras).";
    private static final String ERROR_LAST_NAME = "Invalido (solo letras).";
    private static final String ERROR_PHONE_NUMBER = "Numero de telefono invalido";
    private static final String ERROR_USER_NAME = "Nombre de usuario invalido";

    Validation validate; // class validate: has all the validations of the different fields

    public EditProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textNombre = (EditText) getView().findViewById(R.id.textNombre);
        textApellido = (EditText) getView().findViewById(R.id.textApellido);
        textUsername = (EditText) getView().findViewById(R.id.textUsername);
        textPhoneNumber = (EditText) getView().findViewById(R.id.textPhoneNumber);
        radioButton = (RadioButton) getView().findViewById(R.id.radioButton);
        radioButton2 = (RadioButton) getView().findViewById(R.id.radioButton2);
        editTextDate = (EditText) getView().findViewById(R.id.editTextDate);
        editTextDate.setFocusable(false);
        editTextDate.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
        editTextDate.setClickable(false);
        buttonNext = (Button) getView().findViewById(R.id.buttonNext);
        radioGroupGender = (RadioGroup) getView().findViewById(R.id.radioGroupGender);

        validate = new Validation();

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(); // Getting the instance of the database service

        FirebaseUser user = mAuth.getCurrentUser(); // getting the info of the user
        final String userId = user.getUid();
        databaseReference.child(userId).child("Profile").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);
                        Log.d(TAG, "Username: " + user.getUserName());
                        textNombre.setText(user.getFirstName());
                        textApellido.setText(user.getLastName());
                        textUsername.setText(user.getUserName());
                        textPhoneNumber.setText(user.getPhoneNumber());
                        editTextDate.setText(user.getBirthDate());
                        if(user.getUserGender().equals("Mujer")){
                            radioButton.setChecked(true);
                        }else{
                            radioButton2.setChecked(true);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
        buttonNext.setOnClickListener(this);
    }

    /**
     * @desc Validate all the information given by the user
     * @param firstName
     * @param lastName
     * @param phoneNumber
     * @param userName
     * @param date
     * @param gender
     * @return
     */
    public boolean validation(EditText firstName, EditText lastName, EditText phoneNumber, EditText userName, EditText date, RadioGroup gender){
        boolean result = false;
        int cont = 0;
        String firstNameStr;
        String lastNameStr;
        String phoneNumberStr;
        String userNameStr;

        if(validate.isEmpty(firstName)){
            firstName.setError(ERROR_OBLIGATORY_FIELD);
        }else{
            firstNameStr = firstName.getText().toString().trim();
            if(!validate.validateFirstNameAndLastName(firstNameStr)){
                firstName.setError(ERROR_FIRST_NAME);
            }else {
                cont++;
            }
        }
        if(validate.isEmpty(lastName)){
            lastName.setError(ERROR_OBLIGATORY_FIELD);
        }else{
            lastNameStr = lastName.getText().toString().trim();
            if(!validate.validateFirstNameAndLastName(lastNameStr)){
                lastName.setError(ERROR_FIRST_NAME);
            }else {
                cont++;
            }
        }
        if(validate.isEmpty(phoneNumber)){
            phoneNumber.setError(ERROR_OBLIGATORY_FIELD);
        }else{
            phoneNumberStr = phoneNumber.getText().toString().trim();
            if(!validate.validatePhone(phoneNumberStr)){
                phoneNumber.setError(ERROR_PHONE_NUMBER);
            }else {
                cont++;
            }
        }
        if(validate.isEmpty(userName)){
            userName.setError(ERROR_OBLIGATORY_FIELD);
        }else{
            userNameStr = userName.getText().toString().trim();
            if(!validate.validateUserName(userNameStr)){
                userName.setError(ERROR_USER_NAME);
            }else {
                cont++;
            }
        }
        if(validate.isEmpty(date)){
            date.setError(ERROR_OBLIGATORY_FIELD);
        }else{
            cont++;
            date.setError(null);
        }

        int selectedId = gender.getCheckedRadioButtonId(); // get selected radio button from radioGroup
        if(selectedId == -1){
            radioButton2.setError("Select Item");
        }else{
            cont++;
            radioButton2.setError(null);
        }

        if(cont == 6){
            result = true;
        }

        return result;
    }

    /**
     * @desc Save the user information in the database
     */
    public void saveProfileInformation(){
        String firstNameStr;
        String lastNameStr;
        String phoneNumberStr;
        String userNameStr;
        String gender;
        String date;
        int selectedId;
        RadioButton rbSelected;

        if(validation(textNombre, textApellido, textPhoneNumber, textUsername, editTextDate, radioGroupGender)){
            firstNameStr = textNombre.getText().toString().trim();
            lastNameStr = textApellido.getText().toString().trim();
            phoneNumberStr = textPhoneNumber.getText().toString().trim();
            userNameStr = textUsername.getText().toString().trim();
            selectedId = radioGroupGender.getCheckedRadioButtonId();
            rbSelected = (RadioButton) getView().findViewById(selectedId);
            gender = rbSelected.getText().toString();
            date = editTextDate.getText().toString().trim();

            User userInfo = new User(firstNameStr, lastNameStr, phoneNumberStr, userNameStr, gender, date);
            FirebaseUser user = mAuth.getCurrentUser();
            databaseReference.child(user.getUid()).child("Profile").setValue(userInfo);
            Toast.makeText(getContext(), "Su información ha sido actualizada", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        if(view == buttonNext){
            saveProfileInformation();
        }
    }
}
