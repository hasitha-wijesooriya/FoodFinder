package com.example.foodfinder.ui.cart;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodfinder.UserActivity;
import com.example.foodfinder.databinding.FragmentGalleryBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import adapter.CartAdapter;
import model.CartItem;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private static ArrayList<CartItem> cartItemArrayList = new ArrayList<>();
    private String loginEmail;
    private CartAdapter cartAdapter;
    private static TextView cartTotal;
    private String name;
    private String mobile;

    private static TextView cartBadge = UserActivity.cartBadge;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cartTotal = binding.cartTotalPrice;
        RecyclerView recyclerViewCartItem = binding.recyclerViewCartItem;

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewCartItem.setLayoutManager(layoutManager);

        cartAdapter = new CartAdapter(getContext(), cartItemArrayList);
        recyclerViewCartItem.setAdapter(cartAdapter);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("com.example.foodfinder.data", Context.MODE_PRIVATE);
        loginEmail = sharedPreferences.getString("key_Email", null);

        Log.i("foodfinderLog", "Email: " + loginEmail);

        loadUserData();
        loadCartItems(loginEmail);


        Button checkOut = binding.cartCheckoutBtn;
        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cartItemArrayList.isEmpty()) {
                    Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
                } else {
                    initiatePayment();
                }
            }
        });
    }

    private void loadUserData() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://192.168.8.195/Food_Finder/get_user.php?email=" + loginEmail)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONArray jsonArray = new JSONArray(responseData);
                JSONObject obj = jsonArray.getJSONObject(0);
                name = obj.getString("name");
                mobile = obj.getString("mobile");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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
                                Toast.makeText(requireContext(), "Payment Success: " + response.getData(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(requireContext(), "Payment Failed: " + response, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Toast.makeText(requireContext(), "Payment Cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void initiatePayment() {
        InitRequest req = new InitRequest();
        req.setMerchantId("1221343");       // Replace with your Merchant ID
        req.setCurrency("LKR");
        req.setAmount(Double.parseDouble(cartTotal.getText().toString()));
        req.setOrderId("ORDER" + System.currentTimeMillis());
        req.setItemsDescription("Cart Checkout");
        req.setCustom1("Cart Purchase");
        req.getCustomer().setFirstName(name);
        req.getCustomer().setLastName("");
        req.getCustomer().setEmail(loginEmail);
        req.getCustomer().setPhone(mobile);
        req.getCustomer().getAddress().setAddress("C104/3, Daswatta");
        req.getCustomer().getAddress().setCity("Mawanella");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        for (CartItem cartItem : cartItemArrayList) {
            req.getItems().add(new Item(
                    String.valueOf(cartItem.getPid()),
                    cartItem.getName(),
                    cartItem.getQty(),
                    cartItem.getPrice() * cartItem.getQty()
            ));

            Log.i("foodfinderLog",String.valueOf(cartItem.getPrice() * cartItem.getQty()));

        }

        Intent intent = new Intent(getContext(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        payHereLauncher.launch(intent);
    }

    private void insertOrderToDatabase(String paymentId) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("email", loginEmail)
                    .add("payment_id", paymentId)
                    .add("total", cartTotal.getText().toString())
                    .add("status", "completed");

            JSONArray itemsArray = new JSONArray();
            try {
                for (CartItem item : cartItemArrayList) {
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("item_id", item.getId());
                    itemObj.put("quantity", item.getQty());
                    itemObj.put("price", item.getPrice());
                    itemsArray.put(itemObj);
                }
                formBuilder.add("items", itemsArray.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            RequestBody requestBody = formBuilder.build();

            Request request = new Request.Builder()
                    .url("http://192.168.8.195/Food_Finder/insert_order.php")
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Order placed successfully", Toast.LENGTH_SHORT).show();
                    cartItemArrayList.clear();
                    cartAdapter.notifyDataSetChanged();
                    updateCartTotal();
                    startActivity(new Intent(getContext(), UserActivity.class));
                });
            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to save order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
            }
        }).start();
    }

    public static void updateCartTotal() {
        double total = 0;
        int cartqty = 0;
        for (CartItem item : cartItemArrayList) {
            total += item.getPrice() * item.getQty();
            cartqty += item.getQty();
        }
        String formattedTotal = String.format("%.2f", total);
        cartTotal.setText(formattedTotal);
        cartBadge.setText(String.valueOf(cartqty));
    }


    private void loadCartItems(String email) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://192.168.8.195/Food_Finder/get_cartItem.php?email=" + email)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String responseText = response.body().string();
                Log.i("foodfinderLog", responseText);

                JSONArray jsonArray = new JSONArray(responseText);
                cartItemArrayList.clear();
                double totalPrice = 0.0;

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    CartItem item = new CartItem(
                            obj.getString("item_name"),
                            obj.getString("cart_id"),
                            obj.getInt("item_id"),
                            obj.getInt("item_qty"),
                            obj.getDouble("item_normal_price"),
                            obj.getString("image_url")
                    );
                    cartItemArrayList.add(item);
                    totalPrice += item.getPrice() * item.getQty();
                }

                double finalTotalPrice = totalPrice;
                requireActivity().runOnUiThread(() -> {
                    cartAdapter.notifyDataSetChanged();
                    updateCartTotal();
                    cartTotal.setText(String.format("%.2f", finalTotalPrice));
                });

            } catch (IOException | JSONException e) {
                Log.e("foodfinderLog", "Error loading cart: " + e.getMessage());
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to load cart", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}