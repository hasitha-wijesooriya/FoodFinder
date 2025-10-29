package com.example.foodfinder;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodfinder.ui.cart.GalleryFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodfinder.databinding.ActivityUserBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class UserActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityUserBinding binding;

    private TextView textViewLoginName;

    private NavController navController;

    private  String loginEmail;

    public static TextView cartBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarUser.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_order,R.id.nav_profile)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_user);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View headerView = navigationView.getHeaderView(0);



        SharedPreferences sharedPreferences = getSharedPreferences("com.example.foodfinder.data",Context.MODE_PRIVATE);
        loginEmail = sharedPreferences.getString("key_Email",null);

        TextView textViewLoginEmail = headerView.findViewById(R.id.textViewHeaderEmail);
        textViewLoginName = headerView.findViewById(R.id.textViewHeaderName);
        textViewLoginEmail.setText(loginEmail);

        loadHeaderData();


        //navigationView.setNavigationItemSelectedListener(this);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_logout) {
                // Clear SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("key_Email", null);
                editor.putString("key_Password", null);
                editor.apply();

                // Navigate to Login Screen
                Intent intent = new Intent(UserActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            // Handle other menu items if needed
            else if (id == R.id.nav_home) {
                navController.navigate(R.id.nav_home);
            } else if (id == R.id.nav_gallery) {
                navController.navigate(R.id.nav_gallery);
            } else if (id == R.id.nav_profile) {
                navController.navigate(R.id.nav_profile);
            } else if (id == R.id.nav_order) {
                navController.navigate(R.id.nav_order);
            } else if (id == R.id.nav_map) {
                Intent intent = new Intent(UserActivity.this,MapActivity.class);
                startActivity(intent);
            }

            // Close drawer after selection
            drawer.closeDrawer(GravityCompat.START);

            return true;
        });

    }

    private void loadHeaderData(){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("user")
                .whereEqualTo("email",loginEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            QuerySnapshot documentSnapshots = task.getResult();

                            if (!documentSnapshots.isEmpty()){
                                DocumentSnapshot document = documentSnapshots.getDocuments().get(0);
                                textViewLoginName.setText(String.valueOf(document.get("name")));
                            }
                        }
                    }
                });
    }



    @Override
    protected void onResume() {
        super.onResume();



    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user, menu);

        MenuItem cartItem = menu.findItem(R.id.action_cart);
        cartItem.setActionView(R.layout.cart_badge);

        View actionView = cartItem.getActionView();

        cartBadge = actionView.findViewById(R.id.cart_badge);
        cartBadge.setText("0"); // Example item count



        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                navController.navigate(R.id.nav_gallery);

            }
        });

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_user);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();

    }
}