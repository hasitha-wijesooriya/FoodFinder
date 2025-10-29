package com.example.foodfinder.ui.myprofile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodfinder.R;
import com.example.foodfinder.SignUpActivity;
import com.example.foodfinder.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding profileBinding;
    private CircleImageView profileImage;

    private EditText profileEmail;

    private EditText profileName;

    private EditText profileMobile;

    private EditText currentPassword;
    private EditText newPassword;

    private static final int CAMERA_REQUEST_CODE = 0;
    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private CircleImageView getProfileImage;
    private String loginEmail;

    public ProfileFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        profileBinding = FragmentProfileBinding.inflate(inflater, container, false);
        return profileBinding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    FirebaseFirestore  firestore = FirebaseFirestore.getInstance();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileImage = profileBinding.profileImageView;
        profileEmail = profileBinding.profileUserEmail;
        profileName = profileBinding.profileUserName;
        profileMobile = profileBinding.profileUserMobile;
        newPassword = profileBinding.newPassword;
        currentPassword = profileBinding.currentPassword;
        getProfileImage = profileBinding.profileImageView;

        //profileEmail.setEnabled(false);
        profileEmail.setFocusable(false);
        profileEmail.setClickable(false);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("com.example.foodfinder.data", Context.MODE_PRIVATE);
        loginEmail = sharedPreferences.getString("key_Email", null);

        if (loginEmail != null) {

            loadData(loginEmail);
        }

        Button saveData = profileBinding.profileDataSave;
        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserData(loginEmail);
            }
        });



        Button savePassword = profileBinding.savePassword;
        savePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePassword();

            }
        });

        ImageButton selectImage = profileBinding.profileImageButton;
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                            CAMERA_PERMISSION_CODE);
                } else {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE);

                }

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE);
            } else {
                showToast("Permission denied! Cannot access camera or gallery.");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data != null) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        getProfileImage.setImageBitmap((android.graphics.Bitmap) extras.get("data"));
                    }
                    break;
                case GALLERY_REQUEST_CODE:
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        getProfileImage.setImageURI(selectedImage);
                    } else {
                        showToast("Failed to load image from gallery!");
                    }
                    break;
            }
        }
    }

    private void loadData(String email){
        firestore.collection("user")
                .whereEqualTo("email",email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()){
                            QuerySnapshot documentSnapshots = task.getResult();

                            if (!documentSnapshots.isEmpty()){
                                DocumentSnapshot document = documentSnapshots.getDocuments().get(0);
                                profileEmail.setText(String.valueOf(document.get("email")));
                                profileName.setText(String.valueOf(document.get("name")));
                                profileMobile.setText(String.valueOf(document.get("mobile")));
                            }
                        }

                    }
                });
    }

    private void updatePassword(){

        String currentPasswordText = String.valueOf(currentPassword.getText());
        String newPasswordText = String.valueOf(newPassword.getText());

        if (currentPasswordText.trim().isBlank()){
            showToast("Please enter Current Password");

        }else if (newPasswordText.trim().isBlank()){
            showToast("Please enter New Password");
        }else {

            HashMap<String, Object> newPasswordData = new HashMap<>();
            newPasswordData.put("password", newPasswordText);

            firestore.collection("user")
                    .whereEqualTo("password",currentPasswordText)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()){
                                QuerySnapshot documentSnapshots = task.getResult();

                                if (!documentSnapshots.isEmpty()){
                                    for (QueryDocumentSnapshot document : documentSnapshots) {
                                        firestore.collection("user")
                                                .document(document.getId())
                                                .update(newPasswordData)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        showToast("Succesfull Password Changes");
                                                        currentPassword.setText("");
                                                        newPassword.setText("");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        showToast("Error Password Changes");
                                                    }
                                                });
                                    }
                                }else {
                                    showToast("Incorrect Current Password");
                                }
                            }

                        }
                    });

        }
    }

    private void updateUserData(String email){

        String name = String.valueOf(profileName.getText());
        String mobile = String.valueOf(profileMobile.getText());


        if (name.trim().isBlank()){
            showToast("Please enter Name");

        }else if (mobile.trim().isBlank()){
            showToast("Please enter Mobile");
        }else {

            HashMap<String,Object> newData = new HashMap<>();
            newData.put("name",name);
            newData.put("mobile",mobile);

            firestore.collection("user")
                    .whereEqualTo("email",email)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()){
                                QuerySnapshot documentSnapshots = task.getResult();

                                if (!documentSnapshots.isEmpty()){
                                    for (QueryDocumentSnapshot document : documentSnapshots) {
                                        firestore.collection("user")
                                                .document(document.getId())
                                                .update(newData)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        showToast("Succesfull Profile Updated");
                                                        loadData(loginEmail);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        showToast("Error Profile Updated");
                                                    }
                                                });
                                    }
                                }
                            }

                        }
                    });

        }

    }

    private void showToast(String message) {
        if (requireActivity().getBaseContext() != null) {

            Toast toast = new Toast(requireActivity().getBaseContext());

            LayoutInflater inflater2 = LayoutInflater.from(requireContext());
            View toast_error_view = inflater2.inflate(R.layout.toast_error,null,false);
            TextView toastMessage = toast_error_view.findViewById(R.id.toastMessage);
            toast.setView(toast_error_view);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toastMessage.setText(message);
            toast.show();
        }
    }

}