package com.molina.andrea.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "SignUpActivity" ;
    private static final String ERROR_EMPTY_FIELD = "Campo vacío";
    private static final String ERROR_EMAIL = "Correo electronico no valido";
    private static final String ERROR_PASSWORD = "Contraseña no valida (8+ caracteres, 1+ mayúscula, 1+ minuscula, 1+ digito, 1+ caracter (@#$%^&_))";
    private static final String ERROR_PASSWORD_MATCH = "Contraseñas no coinciden";
    private static final String ERROR_AUTH_FAIL_REG = "Fallo el registro, por favor intentelo de nuevo";
    private static final String ERROR_EMAIL_VERIFICATION = "Error al enviar correo de verificación";
    private static final String ERROR_AUTH_FAIL = "No se pudo crear cuenta, intentelo de nuevo";
    private static final String ERROR_EMAIL_ALREADY_IN_USE = "Dirección de correo electrónico ya está en uso";
    private static final String ERROR_EMAIL_VERIFIED = "Necesita validar su cuenta para proseguir. Si no tiene en su bandeja de entrada o de spam un correo con el link para verificar su cuenta presione ENVIAR DE NUEVO.";
    private static final String SENT_EMAIL_VERIFICATION = "Se ha enviado un mensaje a su correo electronico con el link para verificar su cuenta. Si no se encuentra en su vandeja de entrada revisar su folder de spam.";
    private static final String ERROR_SENT_EMAIL_VERIFICATION = "No se logro enviar el correo electronico. Intentar de nuevo más tarde.";
    private GoogleApiClient mGoogleApiClient;

    Validation validate; // class validate: has all the validations of the different fields

    Button buttonSignUp;
    Button buttonGoogle;
    TextView textViewSignIn;

    EditText textEmailAddress;
    EditText textPassword;
    EditText textPasswordConfirm;

    TextInputLayout editTextEmail;
    TextInputLayout editTextPassword;
    TextInputLayout editTextConfPassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        // Checking if the user is already sign in, if it's sign in it will re-direct to the profile activity for now.
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            public static final String TAG = "onCreate()";
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent i = new Intent(SignUpActivity.this, ProfileActivity.class);
                    startActivity(i);
                    finish();
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        /**
         * Configure Google Sign In
         */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        /**
         * GoogleApiClient object with access to the Google Sign-In API
         */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this , new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getApplicationContext(), "Google API connection fail.", Toast.LENGTH_SHORT).show();
                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        validate = new Validation();

        buttonSignUp = (Button) findViewById(R.id.buttonSignUp);
        buttonGoogle = (Button) findViewById(R.id.buttonGoogle);
        editTextEmail = (TextInputLayout) findViewById(R.id.textInputLayoutEmail);
        editTextPassword = (TextInputLayout) findViewById(R.id.textInputLayoutPassword);
        editTextConfPassword = (TextInputLayout) findViewById(R.id.textInputLayoutConfPasswrod);
        textViewSignIn = (TextView) findViewById(R.id.textViewSignIn);

        buttonSignUp.setOnClickListener(this);
        buttonGoogle.setOnClickListener(this);
        textViewSignIn.setOnClickListener(this);
    }

    /**
     * @desc method to handle sign-in by creating a sign-in intent with the getSignInIntent() method, and starting the intent with startActivityForResult().
     * @parm none
     * @return none
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    /**
     * @desc this method will make the authentication with google
     * @param acct
     * @result none
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
      * @desc manages the action when a button is pressed
      * @param View view - the view of the button pressed
      * @return none
     */
    @Override
    public void onClick(View view) {
        if(view == textViewSignIn){
            Log.d(TAG, "signInButton pressed");
            Intent i = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(i);
        }
        if(view == buttonSignUp){
            Log.d(TAG, "signUpButton pressed");
            registerUser();
        }
        if(view == buttonGoogle){
            Log.d(TAG, "signInButtonGoogle pressed");
            signIn();
        }
    }

    /**
      * @desc makes the keyboard vanish
      * @param none
      * @return none
     */
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
      * @desc makes the validations of the fields before creating the user account
      * @param string mail, string password and string confirmPassword - to be validated
      * @return boolean - success or failure
     */
    public boolean validateFields(String emailaddress, String password, String confirmPassword){
        boolean result = false;
        boolean test = validate.validateEmail(emailaddress) && validate.validatePassword(password) && validate.validateConfirmationPassword(password, confirmPassword);
        if(validate.validateEmail(emailaddress) && validate.validatePassword(password) && validate.validateConfirmationPassword(password, confirmPassword)){
            Log.d(TAG, "validateFields: correct" + validate.validateEmail(emailaddress) + validate.validatePassword(password) + validate.validateConfirmationPassword(password, confirmPassword));
            editTextEmail.setError(null);
            editTextPassword.setError(null);
            editTextConfPassword.setError(null);
            result = true;
        }else{
            Log.d(TAG, "validateFields: Email validation " + validate.validateEmail(emailaddress));
            if(!validate.validateEmail(emailaddress)){
                editTextEmail.setError(ERROR_EMAIL);
            }else{
                editTextEmail.setError(null);
            }
            Log.d(TAG, "validateFields: Password validation " + validate.validatePassword(password));
            if(!validate.validatePassword(password)){
                editTextPassword.setError(ERROR_PASSWORD);
            }else{
                editTextPassword.setError(null);
            }

            Log.d(TAG, "validateFields: Confir Password validation " + validate.validateConfirmationPassword(password, confirmPassword));
            if(!validate.validateConfirmationPassword(password, confirmPassword)){
                editTextConfPassword.setError(ERROR_PASSWORD_MATCH);
            }else{
                editTextConfPassword.setError(null);
            }
        }
        return result;
    }

    /**
      * @desc register the user to the system
      * @param none
      * @return none
     */
    public void registerUser(){
        Log.d(TAG, "registerUser(): In the method");
        String emailAddress = editTextEmail.getEditText().getText().toString().trim();
        Log.d(TAG, "emailAddress: " + emailAddress);
        String password = editTextPassword.getEditText().getText().toString().trim();
        Log.d(TAG, "password: "+ password);
        String passwordConfirm = editTextConfPassword.getEditText().getText().toString().trim();
        Log.d(TAG, "passwordConfirm: " + passwordConfirm);
        Log.d(TAG, "First validation: "+ validateFields(emailAddress, password, passwordConfirm));
        if(validateFields(emailAddress, password, passwordConfirm)){
            mAuth.createUserWithEmailAndPassword(emailAddress, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        public static final String TAG = "registerUser()";
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                            /**
                             * If sign in fails, display a message to the user. If sign in succeeds
                             * the auth state listener will be notified and logic to handle the
                             * signed in user can be handled in the listener.
                             */
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithEmail:failed", task.getException());
                                try {
                                    throw task.getException();
                                } catch(FirebaseAuthUserCollisionException e) {
                                    editTextEmail.setError(ERROR_EMAIL_ALREADY_IN_USE);
                                    editTextEmail.requestFocus();
                                }catch(Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            case DialogInterface.BUTTON_POSITIVE:
                                                break;
                                        }
                                    }
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                builder.setMessage(ERROR_AUTH_FAIL).setPositiveButton("Ok", dialogClickListener).show();
                            }else{
                                sendVerificationEmail();
                            }
                        }
                    });
        }else{
            if(password.equals("")){
                editTextPassword.setError(ERROR_EMPTY_FIELD);
            }else if(!validate.validatePassword(password)){
                editTextPassword.setError(ERROR_PASSWORD);
            }
            if(passwordConfirm.equals("")){
                editTextConfPassword.setError(ERROR_EMPTY_FIELD);
            }
            if(emailAddress.equals("")){
                editTextEmail.setError(ERROR_EMPTY_FIELD);
            }else if(!validate.validateEmail(emailAddress)){
                editTextEmail.setError(ERROR_EMAIL);
            }
        }
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
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

                            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                            builder.setMessage(ERROR_SENT_EMAIL_VERIFICATION).setPositiveButton("Ok", dialogClickListener).show();
                        }
                    }
                });
    }
}
