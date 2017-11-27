package com.molina.andrea.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "SignInActivity";
    private static final String ERROR_AUTH_FAIL = "No se pudo iniciar sesion, intentelo de nuevo";
    private static final String ERROR_EMAIL = "Correo electronico no valido";
    private static final String ERROR_PASSWORD = "Contraseña no valida (8+ caracteres, 1+ mayúscula, 1+ minuscula, 1+ digito, 1+ caracter (@#$%^&_))";
    private static final String ERROR_PASSWORD_INVALID = "La contraseña es invalida o el usuario no tiene contraseña";
    private static final String ERROR_EMPTY_FIELD = "Campo vacío";
    private static final String ERROR_WEAK_PASSWORD = "Contraseña debil";
    private static final String ERROR_INVALID_USER = "No hay registro de usuario correspondiente a este identificador. El usuario puede haber sido eliminado.";
    private static final String ERROR_EMPTY_EMAIL_PASSWORD_RESET = "Se necesita este campo para restablecer su contraseña";
    private static final String SENT_EMAIL_RESET_PASSWORD = "Hemos enviado un correo electrónico, debe obtenerlo en breve. Si no recibe un mensaje, revise su carpeta de spam.";

    Button signInButton;
    Button signInButtonGoogle;

    TextView signUpButton;
    TextView textViewForgotPassword;

    TextInputLayout editTextEmail;
    TextInputLayout editTextPassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    Validation validate; // class validate: has all the validations of the different fields

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        // Checking if the user is already sign in, if it's sign in it will re-direct to the profile activity for now.
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            public static final String TAG = "onCreate()";
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //check if it's the first time loggin in
                    final String userId = user.getUid();
                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child(userId);
                    rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.hasChild("Profile")) {
                                Log.d(TAG, " It's not the first time login in");
                                finish();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            }else{
                                Log.d(TAG, " It's the first time login in");
                                finish();
                                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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

        signInButton = (Button) findViewById(R.id.signInButton);
        signInButtonGoogle = (Button) findViewById(R.id.signInButtonGoogle);
        signUpButton = (TextView) findViewById(R.id.signUpButton);
        textViewForgotPassword = (TextView) findViewById(R.id.textViewForgotPassword);

        editTextEmail = (TextInputLayout) findViewById(R.id.editTextEmail);
        editTextPassword = (TextInputLayout) findViewById(R.id.editTextPassword);

        signInButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
        signInButtonGoogle.setOnClickListener(this);
        textViewForgotPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == signInButton){
            Log.d(TAG, "signInButton pressed");
            signInUser();
        }
        if(view == signUpButton){
            Log.d(TAG, "signUpButton pressed");
            Intent i = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(i);
        }
        if(view == signInButtonGoogle){
            Log.d(TAG, "signInButtonGoogle pressed");
            signIn();
        }
        if(view == textViewForgotPassword){
            Log.d(TAG, "textViewForgotPassword pressed");
            String email = editTextEmail.getEditText().getText().toString();
            if(!email.equals("")){
                editTextEmail.setError(null);
                sendResetPasswrdEmail(email);
            }else{
                editTextEmail.setError(ERROR_EMPTY_EMAIL_PASSWORD_RESET);
            }
        }
    }

    private void sendResetPasswrdEmail(String email) {
        mAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case DialogInterface.BUTTON_POSITIVE:
                                            //Yes button clicked
                                            editTextEmail.getEditText().setText("");
                                            editTextPassword.getEditText().setText("");
                                            editTextEmail.setError(null);
                                            editTextPassword.setError(null);
                                            break;
                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                            builder.setMessage(SENT_EMAIL_RESET_PASSWORD).setPositiveButton("Ok", dialogClickListener).show();
                        }
                    }
                });
    }

    /**
     * @desc method to handle sign-in by creating a sign-in intent with the getSignInIntent() method, and starting the intent with startActivityForResult().
     * @parm none
     * @return none
     */
    private void signIn() {
        /* finish();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));*/
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        Log.d(TAG, "signIn(): ");
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
      * @desc register the user to the system
      * @param none
      * @return none
     */
    public void signInUser(){
        editTextEmail.setError(null);
        editTextPassword.setError(null);
        String emailAddress = editTextEmail.getEditText().getText().toString().trim();
        String password = editTextPassword.getEditText().getText().toString().trim();
        if(validate.validatePassword(password) && validate.validateEmail(emailAddress)){
            mAuth.signInWithEmailAndPassword(emailAddress, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithEmail:failed", task.getException());
                                try {
                                    throw task.getException();
                                } catch(FirebaseAuthWeakPasswordException e) {
                                    editTextPassword.setError(ERROR_WEAK_PASSWORD);
                                    editTextPassword.requestFocus();
                                } catch(FirebaseAuthInvalidUserException e) {
                                    editTextEmail.setError(ERROR_INVALID_USER);
                                    editTextEmail.requestFocus();
                                } catch(FirebaseAuthInvalidCredentialsException e) {
                                    editTextPassword.setError(ERROR_PASSWORD_INVALID);
                                    editTextPassword.requestFocus();
                                } catch(Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                                Toast.makeText(SignInActivity.this, ERROR_AUTH_FAIL, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else{
            if(password.equals("")){
                editTextPassword.setError(ERROR_EMPTY_FIELD);
            }else if(!validate.validatePassword(password)){
                editTextPassword.setError(ERROR_PASSWORD);
            }
            if(emailAddress.equals("")){
                editTextEmail.setError(ERROR_EMPTY_FIELD);
            }else if(!validate.validateEmail(emailAddress)){
                editTextEmail.setError(ERROR_EMAIL);
            }
        }
    }
}
