package com.example.foodfinder.ui.slideshow;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodfinder.databinding.FragmentSlideshowBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import adapter.OrderAdapter;
import model.OrderItem;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private String loginEmail;
    private ArrayList<OrderItem> orderItemArrayList; // Declare here
    private OrderAdapter orderAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the ArrayList before using it
        orderItemArrayList = new ArrayList<>();

        RecyclerView recyclerViewOrder = binding.recyclerViewOrder;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewOrder.setLayoutManager(layoutManager);

        // Pass the initialized ArrayList to the adapter
        orderAdapter = new OrderAdapter(orderItemArrayList);
        recyclerViewOrder.setAdapter(orderAdapter);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("com.example.foodfinder.data", Context.MODE_PRIVATE);
        loginEmail = sharedPreferences.getString("key_Email", null);

        if (loginEmail != null) {
            loadOrderItems(loginEmail);
        } else {
            Toast.makeText(getContext(), "Please login to view orders", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadOrderItems(String email) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://192.168.8.195/Food_Finder/get_orderItem.php?email=" + email)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                String responseText = response.body() != null ? response.body().string() : "";
                Log.i("foodfinderLog", "Response: " + responseText);

                JSONArray jsonArray = new JSONArray(responseText);

                // Clear the list on the UI thread before adding new items
                requireActivity().runOnUiThread(() -> orderItemArrayList.clear());

                ArrayList<OrderItem> tempList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    OrderItem item = new OrderItem(
                            obj.getString("order_id"),
                            obj.getString("order_status"),
                            obj.getString("order_date"),
                            obj.getString("order_total"),
                            obj.getString("order_name")
                    );
                    tempList.add(item);
                }

                // Update the UI on the main thread
                requireActivity().runOnUiThread(() -> {
                    orderItemArrayList.addAll(tempList);
                    orderAdapter.notifyDataSetChanged();
                });

            } catch (IOException e) {
                Log.e("foodfinderLog", "Network Error: " + e.getMessage());
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } catch (JSONException e) {
                Log.e("foodfinderLog", "JSON Error: " + e.getMessage());
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Data parsing error", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                Log.e("foodfinderLog", "Unexpected Error: " + e.getMessage());
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error loading orders", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}