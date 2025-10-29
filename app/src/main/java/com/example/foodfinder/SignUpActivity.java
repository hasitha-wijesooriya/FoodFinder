package com.example.foodfinder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import model.Validation;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textViewGoToLogin = findViewById(R.id.textViewGoToLogin);
        textViewGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        EditText editText1 = findViewById(R.id.textInputName);
        EditText editText2 = findViewById(R.id.textInputEmail);
        EditText editText3 = findViewById(R.id.textInputPassword);
        EditText editText4 = findViewById(R.id.textInputMobile);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){

            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            SensorEventListener listener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float values[] = event.values;
                    float z = values[2];

                    if (z < -9){
                        editText1.setText("");
                        editText2.setText("");
                        editText3.setText("");
                        editText4.setText("");
                        editText1.requestFocus();
                    }
                }
                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }
            };

            sensorManager.registerListener(listener,sensor,SensorManager.SENSOR_DELAY_NORMAL);

        }

        AlertDialog.Builder dialog =  new AlertDialog.Builder(SignUpActivity.this);

        LayoutInflater inflater1 = LayoutInflater.from(SignUpActivity.this);
        View alert_info_view = inflater1.inflate(R.layout.alert_info,null,false);
        TextView alertMessage = alert_info_view.findViewById(R.id.textErrorMessage);
        dialog.setView(alert_info_view);
        AlertDialog alertDialog = dialog.create();
        Button alertActionButton = alert_info_view.findViewById(R.id.buttonAlertAction);
        alertActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });


        Toast toast = new Toast(SignUpActivity.this);

        LayoutInflater inflater2 = LayoutInflater.from(SignUpActivity.this);
        View toast_error_view = inflater2.inflate(R.layout.toast_error,null,false);
        TextView toastMessage = toast_error_view.findViewById(R.id.toastMessage);
        toast.setView(toast_error_view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);

        Button buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

                String name = String.valueOf(editText1.getText());
                String email = String.valueOf(editText2.getText()).trim();
                String password = String.valueOf(editText3.getText()).trim();
                String mobile = String.valueOf(editText4.getText()).trim();

                if (name.trim().isBlank()){
                    toastMessage.setText("Please Enter Your Name!");
                    toast.show();

                }else if (email.isBlank()){
                    toastMessage.setText("Please Enter Your Email!");
                    toast.show();
                }
//                else if (Validation.isValidEmail(email)){
//                    toastMessage.setText("Invalid Email Address.Please check your Email!");
//                    toast.show();
//                }
                else if (password.isBlank()){
                    toastMessage.setText("Please Enter Your Password!");
                    toast.show();
                }else if (Validation.isPasswordValid(password)){
                    toastMessage.setText("Password must include at least one uppercase letter,number, special character, and not less than 8 characters!");
                    toast.show();
                }else if (mobile.isBlank()){
                    toastMessage.setText("Please Enter Your Phone Number!");
                    toast.show();
                }else {

                    firebaseFirestore.collection("user")
                            .whereEqualTo("email",email)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                    if (task.isSuccessful()){

                                        QuerySnapshot document = task.getResult();

                                        if (document != null && !document.isEmpty()){
                                            toastMessage.setText("This Email Already in Used!");
                                            toast.show();

                                        }else {

                                            HashMap<String,Object> usermap = new HashMap<>();
                                            usermap.put("name",name);
                                            usermap.put("email",email);
                                            usermap.put("password",password);
                                            usermap.put("mobile",mobile);

                                            firebaseFirestore.collection("user").add(usermap)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {

                                                            alertMessage.setText("Successfull User Registed");
                                                            alertDialog.show();

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e("FoodFinderLog","Failed user registered");
                                                        }
                                                    });

                                            editText1.setText("");
                                            editText2.setText("");
                                            editText3.setText("");
                                            editText4.setText("");
                                            editText1.requestFocus();

                                            insertUserEmail(email);

//                                            Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
//                                            startActivity(intent);

                                        }
                                    }

                                }
                            });




                }

            }
        });

    }

    private void insertUserEmail(String email){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("email", email);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.get("application/json"));

                Request request = new Request.Builder()
                        .url("http://192.168.8.195/Food_Finder/userEmail.php")
                        .post(requestBody)
                        .build();

                try {

                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();

                    JSONObject jsonResponse = new JSONObject(responseData);
                    String status = jsonResponse.getString("status");

                    if (status.equals("exists")) {
                        Log.d("foodfinder_test", "Email already exists in the database.");
                    } else if (status.equals("success")) {
                        Log.d("foodfinder_test", "Email inserted successfully.");
                    }



                } catch (RuntimeException | IOException | JSONException e) {
                    throw new RuntimeException(e);
                }

            }

        }).start();
    }

}