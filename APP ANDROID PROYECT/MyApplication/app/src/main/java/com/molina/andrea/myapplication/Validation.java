package com.molina.andrea.myapplication;

import android.support.design.widget.TextInputLayout;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by amoli on 8/29/2017.
 */


class Validation {
    /**
     * Regular Expressions
     */
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=_-])(?=\\S+$).{8,}$";
    private static final String NAME_PATTERN = "^[\\p{L} .'-]+$";
    private static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";
    /**
     * Patterns
     */
    private Pattern patternEmail = Pattern.compile(EMAIL_PATTERN);
    private Pattern patternPassword = Pattern.compile(PASSWORD_PATTERN);
    private Pattern patternName = Pattern.compile(NAME_PATTERN);
    private Pattern patternUserName = Pattern.compile(USERNAME_PATTERN);

    private Matcher matcher;

    //************************************** METHODS **************************************//

    /**
      * @desc validates that the email has the correct format
      * @param string email - the email to be validated
      * @return bool - success or failure
     */
    public boolean validateEmail(String email) {
        matcher = patternEmail.matcher(email);
        return matcher.matches();
    }

    /**
      * @desc validates that the password has the correct format
      * @param string password - the password to be validated
     * @format 8+ characters, 1+ uppercase letters, 1+ lowercase, 1+ {@#$%^&+=_-} , 1+ numbers
      * @return bool - success or failure
     */
    public boolean validatePassword(String password) {
        matcher = patternPassword.matcher(password);
        return (password.length() >= 8) && (matcher.matches());
    }

    /**
      * @desc validates that the password and the confirmation password are equal
      * @param string password & string confirmPassword - to be compare
      * @return bool - success or failure
     */
    public boolean validateConfirmationPassword(String password, String confirmPassword){
        if((password.equals(confirmPassword))&&(!password.equals(""))&&(!confirmPassword.equals("")))
            return true;
        return false;
    }

    /**
      * @desc validates that the name has only letters, spaces and isn't empty
      * @param string name - to be validated
      * @return bool - success or failure
     */
    public boolean validateFirstNameAndLastName(String name){
        matcher = patternName.matcher(name);
        return matcher.matches();
    }

    /**
     * @desc validates that the username is min 3 and max 15 characteres and is
     * composed by letters, numbers and underscore
     * @param username
     * @return
     */
    public boolean validateUserName(String username){
        matcher = patternUserName.matcher(username);
        return matcher.matches();
    }

    /**
      * @desc validates that the phone has 8 digits
      * @param string phone - to be validated
      * @return bool - success or failure
     */
    public boolean validatePhone(String phone){
        if(phone.length() == 8)
            return true;
        return false;
    }

    /**
     * @desc This method will check if the TextInputLayout is empty.
     * @param myeditText
     * @return
     */
    boolean isEmpty(TextInputLayout myeditText) {
        return myeditText.getEditText().getText().toString().trim().length() == 0;
    }

    /**
     * @desc This method will check if the editText is empty.
     * @param myeditText
     * @return
     */
    boolean isEmpty(EditText myeditText) {
        return myeditText.getText().toString().trim().length() == 0;
    }
}
