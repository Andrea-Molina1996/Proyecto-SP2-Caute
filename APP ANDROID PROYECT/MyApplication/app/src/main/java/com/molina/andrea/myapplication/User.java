package com.molina.andrea.myapplication;

/**
 * Created by amoli on 9/1/2017.
 */

class User {
    String firstName;
    String lastName;
    String phoneNumber;
    String userName;
    String userGender;
    String birthDate;

    public User(){}
    public User(String firstName, String lastName, String phoneNumber, String userName, String userGender, String birthDate){
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.userName = userName;
        this.userGender = userGender;
        this.birthDate = birthDate;
    }

    /**
     * @desc this method will return the user first name
     * @return String fisrtName
     */
    public String getFirstName(){
        return firstName;
    }

    /**
     * @desc this method will return the user last name
     * @return String lastName
     */
    public String getLastName(){
        return lastName;
    }

    /**
     * @desc this method will return the user phone number
     * @return
     */
    public String getPhoneNumber(){
        return phoneNumber;
    }

    /**
     * @desc this method will return the user nickname
     * @return String username
     */
    public String getUserName(){
        return userName;
    }

    /**
     * @desc this method will return the user gender
     * @return String userGender
     */
    public String getUserGender(){
        return userGender;
    }

    /**
     * @desc this method will return the user birth date
     * @return String birthDate
     */
    public String getBirthDate(){
        return birthDate;
    }

    /**
     * @desc this method will modify the user first name information
     * @param firstName
     */
    public void changeFirstName(String firstName){
        this.firstName = firstName;
    }

    /**
     * @desc this method will modify the user last name information
     * @param lastName
     */
    public void changeLastName(String lastName){
        this.lastName = lastName;
    }

    /**
     * @desc this method will modify the user phone number information
     * @param phoneNumber
     */
    public void changePhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    /**
     * @desc this method will modify the user nickname information
     * @param userName
     */
    public void changeUserName(String userName){
        this.userName = userName;
    }

    /**
     * @desc this method will modify the user gender information
     * @param userGender
     */
    public void changeUserGender(String userGender){
        this.userGender = userGender;
    }

    /**
     * @desc this method will modify the user birth date information
     * @param birthDate
     */
    public void changeBirthDate(String birthDate){
        this.birthDate = birthDate;
    }
}
