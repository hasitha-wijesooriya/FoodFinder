package com.example.foodfinder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.io.Serializable;
import adapter.FoodAdapter;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SingleViewActivity extends AppCompatActivity {

    private int quantity = 1;
    private double total;
    private double normalPrice = 0;
    private String name;
    private String mobile;
    private String loginEmail;
    private String item_id;

    ImageView itemImage;
    TextView itemName;
    TextView itemNormalPrice;
    TextView itemFullPrice;
    TextView itemDescription;
    TextView totalPrice;
    EditText qtyCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_single_view);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("com.example.foodfinder.data", Context.MODE_PRIVATE);
        loginEmail = sharedPreferences.getString("key_Email", null);

        loadUserData();

        itemImage = findViewById(R.id.itemImage);
        itemName = findViewById(R.id.itemName);
        itemNormalPrice = findViewById(R.id.itemNormal_price);
        itemFullPrice = findViewById(R.id.itemFull_price);
        itemDescription = findViewById(R.id.itemDescription);
        totalPrice = findViewById(R.id.totalPrice);
        qtyCount = findViewById(R.id.cartItemQty);

        item_id = getIntent().getStringExtra("item_id");
        loadItemData(item_id);

        ImageView goToHome = findViewById(R.id.goToHome);
        goToHome.setOnClickListener(v -> {
            Intent intent = new Intent(SingleViewActivity.this, UserActivity.class);
            startActivity(intent);
            finish();
        });

        ImageButton qtyReduce = findViewById(R.id.cartViewQtyReduce);
        ImageButton qtyAdd = findViewById(R.id.cartViewQtyAdd);

        qtyCount.setText(String.valueOf(quantity));

        qtyReduce.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                qtyCount.setText(String.valueOf(quantity));
                updateTotalPrice();
            }
        });

        qtyAdd.setOnClickListener(v -> {
            quantity++;
            qtyCount.setText(String.valueOf(quantity));
            updateTotalPrice();
        });

        Button itemPlaceOrder = findViewById(R.id.itemPlaceOrder);
        itemPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initiatePayment();
            }
        });

        Button itemAddToCart = findViewById(R.id.itemAddToCart);
        itemAddToCart.setOnClickListener(v -> FoodAdapter.AddToCart(v.getContext(), item_id, loginEmail));
    }

    private void loadUserData() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("user")
                .whereEqualTo("email", loginEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documentSnapshots = task.getResult();
                        if (documentSnapshots != null && !documentSnapshots.isEmpty()) {
                            DocumentSnapshot document = documentSnapshots.getDocuments().get(0);
                            name = String.valueOf(document.get("name"));
                            mobile = String.valueOf(document.get("mobile"));
                        }
                    }
                });
    }

    @SuppressLint("DefaultLocale")
    private void updateTotalPrice() {
        try {
            normalPrice = Double.parseDouble(itemNormalPrice.getText().toString());
            total = normalPrice * quantity;
            totalPrice.setText(String.format("%.2f", total));
        } catch (NumberFormatException e) {
            totalPrice.setText("0.00");
        }
    }

    private final ActivityResultLauncher<Intent> payHereLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                        Serializable serializable = data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
                        if (serializable instanceof PHResponse) {
                            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) serializable;
                            if (response.isSuccess()) {
                                // Payment successful - insert order into database
                                insertOrderToDatabase(String.valueOf(response.getData().getPaymentNo()));
                                Toast.makeText(this, "Payment Success: " + response.getData(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Payment Failed: " + response, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void insertOrderToDatabase(String paymentId) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();



            RequestBody requestBody = new FormBody.Builder()
                    .add("email", loginEmail)
                    .add("item_id", item_id)
                    .add("quantity", String.valueOf(quantity))
                    .add("total", String.valueOf(total))
                    .add("payment_id", paymentId)
                    .add("status", "completed")
                    .build();

            Request request = new Request.Builder()
                    .url("http://192.168.8.195/Food_Finder/insert_order.php")
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                runOnUiThread(() -> {
                    Toast.makeText(SingleViewActivity.this, "Order placed successfully", Toast.LENGTH_SHORT).show();
                    // Navigate back to home or cart after successful order
                    Intent intent = new Intent(SingleViewActivity.this, UserActivity.class);
                    startActivity(intent);
                    finish();
                });
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(SingleViewActivity.this,
                        "Failed to save order: " + e.getMessage(), Toast.LENGTH_LONG).show());
                e.printStackTrace();
            }
        }).start();
    }

    private void initiatePayment() {
        InitRequest req = new InitRequest();
        req.setMerchantId("1221343");       // Replace with your Merchant ID
        req.setCurrency("LKR");
        req.setAmount(Double.parseDouble(totalPrice.getText().toString()));
        req.setOrderId("ORDER" + System.currentTimeMillis()); // Unique order ID
        req.setItemsDescription(itemName.getText().toString());
        req.setCustom1("Single Item Purchase");
        req.getCustomer().setFirstName(name);
        req.getCustomer().setLastName("");
        req.getCustomer().setEmail(loginEmail);
        req.getCustomer().setPhone(mobile);
        req.getCustomer().getAddress().setAddress("C104/3, Daswatta");
        req.getCustomer().getAddress().setCity("Mawanella");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        String it_name = itemName.getText().toString();
        int it_qty = Integer.parseInt(qtyCount.getText().toString());
        req.getItems().add(new Item(item_id, it_name, it_qty, total));

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        payHereLauncher.launch(intent);
    }

    private void loadItemData(String item_id) {
        int food_id = Integer.parseInt(item_id);
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://192.168.8.195/Food_Finder/get_singleProduct.php?item_id=" + food_id)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONArray jsonResponse = new JSONArray(responseData);
                JSONObject obj = jsonResponse.getJSONObject(0);

                String img_url = obj.getString("image_url");
                String item_name = obj.getString("item_name");
                double normal_price = obj.getDouble("item_normal_price");
                double full_price = obj.getDouble("item_full_price");
                String description = obj.getString("item_description");

                runOnUiThread(() -> {
                    Picasso.get().load(img_url).into(itemImage);
                    itemName.setText(item_name);
                    itemNormalPrice.setText(String.format("%.2f", normal_price));
                    itemFullPrice.setText(String.format("%.2f", full_price));
                    itemDescription.setText(description);
                    updateTotalPrice();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}