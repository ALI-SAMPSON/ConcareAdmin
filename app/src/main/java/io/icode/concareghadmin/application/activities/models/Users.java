package io.icode.concareghadmin.application.activities.models;

public class Users {

    //field in the database
    private String email;
    private String username;
    private String uid;
    private String gender;
    private String phoneNumber;
    private String imageUrl;


    //default constructor
    public Users(){}

    //constructor with one or more parameters
    public Users(String email, String username, String uid, String gender, String phoneNumber, String imageUrl){
        this.email = email;
        this.username = username;
        this.uid = uid;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.imageUrl = imageUrl;

    }

    //Getter and Setter Method for Username
    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail(){
        return email;
    }

    //Getter and Setter Method for Username
    public void setUsername(String username){
        this.username = username;
    }

    public String getUsername(){
        return username;
    }

    //Getter and Setter Method for uid
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    //Getter and Setter Method for Gender
    public void setGender(String gender){
        this.gender = gender;
    }

    public String getGender(){
        return gender;
    }

    //Getter and Setter Method for TelephoneNumber
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public String getImageUrl(){
        return imageUrl;
    }

}