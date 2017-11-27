package com.molina.andrea.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.icu.util.Calendar;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "ProfileActivity";
    static final int DIALOG_ID = 0;
    private static final String ERROR_OBLIGATORY_FIELD = "Campo obligatorio";
    private static final String ERROR_FIRST_NAME = "Invalido (solo letras).";
    private static final String ERROR_LAST_NAME = "Invalido (solo letras).";
    private static final String ERROR_PHONE_NUMBER = "Numero de telefono invalido";
    private static final String ERROR_USER_NAME = "Nombre de usuario invalido";
    private static final int CHOSE_IMAGE = 101;
    private static final String ERROR_PROFILE_PICTURE = "Es necesario ingresar una foto de perfil.";
    private static final String ERROR_EMAIL_VERIFIED = "Necesita validar su cuenta para proseguir. Si no tiene en su bandeja de entrada o de spam un correo con el link para verificar su cuenta presione ENVIAR DE NUEVO.";
    private static final String SENT_EMAIL_VERIFICATION = "Se ha enviado un mensaje a su correo electronico con el link para verificar su cuenta. Si no se encuentra en su vandeja de entrada revisar su folder de spam.";
    private static final String ERROR_SENT_EMAIL_VERIFICATION = "No se logro enviar el correo electronico. Intentar de nuevo más tarde.";


    private RadioButton radioButtonM;
    private RadioGroup radioGroupGender;
    private ImageView imageViewProfilePicture;
    private Uri uriProfilePicture;
    private TextInputLayout textInputLayoutName;
    private TextInputLayout textInputLayoutLastName;
    private TextInputLayout textInputLayoutUserName;
    private TextInputLayout textInputLayoutPhoneNumber;
    private TextInputLayout textInputLayoutDate;
    private Button buttonSave;
    private Button buttonDatePicker;

    CheckBox checkBoxVerified;

    ProgressBar progressBar;
    ProgressDialog progress;
    String profileImageUrl;

    int year_x, month_x, day_x;

    Validation validate; // class validate: has all the validations of the different fields

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageViewProfilePicture = (ImageView) findViewById(R.id.imageViewProfilePicture);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        imageViewProfilePicture.setOnClickListener(this);
        textInputLayoutName = (TextInputLayout) findViewById(R.id.textInputLayoutName);
        textInputLayoutLastName = (TextInputLayout) findViewById(R.id.textInputLayoutLastName);
        textInputLayoutUserName = (TextInputLayout) findViewById(R.id.textInputLayoutUserName);
        textInputLayoutPhoneNumber = (TextInputLayout) findViewById(R.id.textInputLayoutPhoneNumber);
        textInputLayoutDate = (TextInputLayout) findViewById(R.id.textInputLayoutDate);
        textInputLayoutDate.setEnabled(false);
        buttonDatePicker = (Button) findViewById(R.id.buttonDatePicker);
        radioGroupGender = (RadioGroup) findViewById(R.id.radioGroupGender);
        buttonSave = (Button) findViewById(R.id.buttonSave);
        checkBoxVerified = (CheckBox) findViewById(R.id.checkBoxVerified);

        validate = new Validation();

        final Calendar calendar;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            calendar = Calendar.getInstance();
            year_x = calendar.get(Calendar.YEAR) - 18;
            month_x = calendar.get(Calendar.MONTH);
            day_x = calendar.get(Calendar.DAY_OF_MONTH);
        }

        showDatePickerDialog();

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        final FirebaseUser user = mAuth.getCurrentUser();
        if(user.isEmailVerified()){
            checkBoxVerified.setChecked(true);
        }

        textInputLayoutDate.getEditText().setText("");

        buttonSave.setOnClickListener(this);
    }

    public void showImageChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar foto de perfil"), CHOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CHOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriProfilePicture = data.getData();
            Log.d(TAG, "onActivityResult: "+uriProfilePicture);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfilePicture);
                imageViewProfilePicture.setImageBitmap(getCroppedBitmap(bitmap));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /**
     * This method will show a dialog containing a datepicker.
     */
    public void showDatePickerDialog(){
        buttonDatePicker = (Button) findViewById(R.id.buttonDatePicker);
        buttonDatePicker.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDialog(DIALOG_ID);
                    }
                }
        );
    }

    @Override
    protected Dialog onCreateDialog(int id){
        if(id == DIALOG_ID){
            DatePickerDialog dialog = new DatePickerDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK, dpickerListener, year_x, month_x, day_x);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            return dialog;
        }else{
            return null;
        }
    }

    /**
     * @desc
     */
    private DatePickerDialog.OnDateSetListener dpickerListener
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            year_x = i;
            month_x = i1 + 1;
            day_x = i2;
            textInputLayoutDate.getEditText().setText(day_x + " / " + month_x + "/ " + year_x);
        }
    };

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
    public boolean validation(TextInputLayout firstName, TextInputLayout lastName, TextInputLayout phoneNumber, TextInputLayout userName, TextInputLayout date, RadioGroup gender){
        boolean result = false;
        int cont = 0;
        String firstNameStr;
        String lastNameStr;
        String phoneNumberStr;
        String userNameStr;

        if(validate.isEmpty(firstName)){
            firstName.setError(ERROR_OBLIGATORY_FIELD);
        }else{
            firstNameStr = firstName.getEditText().getText().toString().trim();
            if(!validate.validateFirstNameAndLastName(firstNameStr)){
                firstName.setError(ERROR_FIRST_NAME);
            }else {
                firstName.setError(null);
                cont++;
            }
        }
        if(validate.isEmpty(lastName)){
            lastName.setError(ERROR_OBLIGATORY_FIELD);
        }else{
            lastNameStr = lastName.getEditText().getText().toString().trim();
            if(!validate.validateFirstNameAndLastName(lastNameStr)){
                lastName.setError(ERROR_FIRST_NAME);
            }else {
                lastName.setError(null);
                cont++;
            }
        }
        if(validate.isEmpty(phoneNumber)){
            phoneNumber.setError(ERROR_OBLIGATORY_FIELD);
        }else{
            phoneNumberStr = phoneNumber.getEditText().getText().toString().trim();
            if(!validate.validatePhone(phoneNumberStr)){
                phoneNumber.setError(ERROR_PHONE_NUMBER);
            }else {
                phoneNumber.setError(null);
                cont++;
            }
        }
        if(validate.isEmpty(userName)){
            userName.setError(ERROR_OBLIGATORY_FIELD);
        }else{
            userNameStr = userName.getEditText().getText().toString().trim();
            if(!validate.validateUserName(userNameStr)){
                userName.setError(ERROR_USER_NAME);
            }else {
                userName.setError(null);
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
            radioButtonM = (RadioButton) findViewById(R.id.radioButtonM);
            radioButtonM.setError("Select Item");
        }else{
            cont++;
            radioButtonM = (RadioButton) findViewById(R.id.radioButtonM);
            radioButtonM.setError(null);
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
        final String firstNameStr;
        final String lastNameStr;
        final String phoneNumberStr;
        final String userNameStr;
        final String gender;
        final String date;
        int selectedId;
        RadioButton rbSelected;
        firstNameStr = textInputLayoutName.getEditText().getText().toString().trim();
        lastNameStr = textInputLayoutLastName.getEditText().getText().toString().trim();
        phoneNumberStr = textInputLayoutPhoneNumber.getEditText().getText().toString().trim();
        userNameStr = textInputLayoutUserName.getEditText().getText().toString().trim();
        selectedId = radioGroupGender.getCheckedRadioButtonId();
        rbSelected = (RadioButton) findViewById(selectedId);
        gender = rbSelected.getText().toString();
        date = textInputLayoutDate.getEditText().getText().toString().trim();
        final FirebaseUser user = mAuth.getCurrentUser();
        if(user != null && profileImageUrl!= null){
            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(userNameStr)
                    .setPhotoUri(Uri.parse(profileImageUrl))
                    .build();
            user.updateProfile(profileChangeRequest)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                User userInfo = new User(firstNameStr, lastNameStr, phoneNumberStr, userNameStr, gender, date);
                                databaseReference.child(user.getUid()).child("Profile").setValue(userInfo);
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);
                                finish();
                            }
                        }
                    });
        }
    }

    public void uploadProfilePicture(){
        progress = new ProgressDialog(ProfileActivity.this);
        progress.setMessage("Almacenando información de perfil...");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        final FirebaseUser user = mAuth.getCurrentUser();

        if(user.isEmailVerified()){
            if(validation(textInputLayoutName, textInputLayoutLastName, textInputLayoutPhoneNumber, textInputLayoutUserName, textInputLayoutDate, radioGroupGender)){
                if(uriProfilePicture != null){
                    StorageReference riversRef = mStorageRef.child("ProfilePictures/"+user.getUid()+"_profile_picture.jpg");
                    riversRef.putFile(uriProfilePicture)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    profileImageUrl = taskSnapshot.getDownloadUrl().toString();
                                    saveProfileInformation();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    progressBar.setVisibility(View.GONE);
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progressData = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                    progress.setProgress((int)progressData);
                                    progress.show();
                                    Log.d(TAG, "Upload is " + progressData + "% done");
                                }
                            });
                }else{
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setMessage(ERROR_PROFILE_PICTURE).setPositiveButton("Ok", dialogClickListener).show();
                }
            }
        }else{
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            sendVerificationEmail();
                            mAuth.signOut();
                            startActivity(new Intent(getApplicationContext(), SignUpOrSignIn.class));
                            finish();
                            break;
                        case DialogInterface.BUTTON_NEUTRAL:
                            mAuth.signOut();
                            startActivity(new Intent(getApplicationContext(), SignUpOrSignIn.class));
                            finish();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setMessage(ERROR_EMAIL_VERIFIED+"\n\n Correo: "+user.getEmail())
                    .setPositiveButton("ENVIAR DE NUEVO", dialogClickListener)
                    .setNeutralButton("OK", dialogClickListener).show();
        }
    }

    public void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case DialogInterface.BUTTON_POSITIVE:
                                            break;
                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                            builder.setMessage(SENT_EMAIL_VERIFICATION).setPositiveButton("Ok", dialogClickListener).show();
                        } else {
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case DialogInterface.BUTTON_POSITIVE:
                                            break;
                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                            builder.setMessage(ERROR_SENT_EMAIL_VERIFICATION).setPositiveButton("Ok", dialogClickListener).show();
                        }
                    }
                });
    }
    @Override
    public void onClick(View view) {
        if(view == buttonSave){
            Log.d(TAG,"Save profile information");
            uploadProfilePicture();
        }

        if(view == imageViewProfilePicture){
            showImageChooser();
        }
    }
}
