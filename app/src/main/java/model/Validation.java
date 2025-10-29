package model;

public class Validation {

    public static boolean isValidEmail(String email){
        return email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+");
    }

//    public static boolean isValidEmail(CharSequence email){
//        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
//    }

    public static boolean isPasswordValid(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$");
    }

    public static boolean isValidMobile(String text){
        return text.matches("^07[01245678]{1}[0-9]{7}$");
    }

}
