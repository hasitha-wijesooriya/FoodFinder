package com.example.foodfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {
//    private static final String key_Email = "";
//    private static final String key_Password = "";

    private String loginEmail;
    private String loginName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textViewGoSignUp = findViewById(R.id.textViewGoSignUp);
        textViewGoSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        EditText editText1 = findViewById(R.id.userLoginEmail);
        EditText editText2 = findViewById(R.id.profileUserEmail);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){

            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            SensorEventListener listener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float valuees[] = event.values;
                    float z = valuees[2];

                    if (z < -9){
                        editText1.setText("");
                        editText2.setText("");
                        editText1.requestFocus();
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }
            };

            sensorManager.registerListener(listener,sensor,SensorManager.SENSOR_DELAY_NORMAL);

        }

        Toast toast = new Toast(MainActivity.this);

        LayoutInflater inflater2 = LayoutInflater.from(MainActivity.this);
        View toast_error_view = inflater2.inflate(R.layout.toast_error,null,false);
        TextView toastMessage = toast_error_view.findViewById(R.id.toastMessage);
        toast.setView(toast_error_view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("com.example.foodfinder.data",Context.MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString("key_Email",null);
        String savedPassword = sharedPreferences.getString("key_Password",null);

        if (savedEmail != null && savedPassword != null){
            openDashboard();

        }else{

            Button buttonLogin = findViewById(R.id.buttonLogin);
            buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String email = String.valueOf(editText1.getText()).trim();
                    String password = String.valueOf(editText2.getText()).trim();

                    if (email.isBlank()){
                        toastMessage.setText("Please Enter Your Email!");
                        toast.show();

                    }else if (password.isBlank()){
                        toastMessage.setText("Please Enter Your Password!");
                        toast.show();

                    }else {

                        firestore.collection("user")
                                .where(
                                        Filter.and(
                                                Filter.equalTo("email",email),
                                                Filter.equalTo("password",password)
                                        )
                                )
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                        if (task.isSuccessful()){

                                            QuerySnapshot documentSnapshots = task.getResult();

                                            if (documentSnapshots != null && !documentSnapshots.isEmpty()){

                                                DocumentSnapshot document = documentSnapshots.getDocuments().get(0);


                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString("key_Email",email);
                                                editor.putString("key_Password",password);
                                                editor.apply();


                                                openDashboard();

                                            }else {

                                                toastMessage.setText("Incorrect Email or Password.Please Check!");
                                                toast.show();

                                            }
                                        }

                                    }
                                });

                    }

                }
            });

        }



    }

    //method to open Dashboard
    private  void openDashboard(){
        Intent intent = new Intent(MainActivity.this, UserActivity.class);
        startActivity(intent);
        finish();
    }

}