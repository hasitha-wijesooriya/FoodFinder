package com.example.foodfinder.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodfinder.R;
import com.example.foodfinder.UserActivity;
import com.example.foodfinder.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import adapter.CategoryAdapter;
import adapter.FoodAdapter;
import model.Category;
import model.FoodItem;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private HomeViewModel homeViewModel;
    private CategoryAdapter categoryAdapter;
    private FoodAdapter foodAdapter;

    ArrayList<Category> categoryArrayList = new ArrayList<>();

    ArrayList<FoodItem> foodItemArrayList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        View root = binding.getRoot();

        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();



        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Get RecyclerView
        RecyclerView recyclerViewCategory = binding.recyclerViewCategory;
        RecyclerView recyclerViewFoodItem = binding.recyclerViewFoodItems;

        //Set Layout Managers
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getContext());
        layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewCategory.setLayoutManager(layoutManager1);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getContext());
        layoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewFoodItem.setLayoutManager(layoutManager2);

        // Set Adapters
        categoryAdapter = new CategoryAdapter(categoryArrayList);
        foodAdapter = new FoodAdapter(foodItemArrayList);

        recyclerViewCategory.setAdapter(categoryAdapter);
        recyclerViewFoodItem.setAdapter(foodAdapter);

        loadCategories();

        loadFoodItems();


    }

    private void loadCategories(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("http://192.168.8.195/Food_Finder/get_category.php")
                        .build();

                try {

                    Response response = client.newCall(request).execute();
                    String responseText = response.body().string();

                    //Log.i("foodfinderLog",responseText);

                    JSONArray jsonArray = new JSONArray(responseText);
                    categoryArrayList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        categoryArrayList.add(new Category(
                                obj.getString("category_name"),
                                obj.getString("image_url")
                        ));
                    }
                    requireActivity().runOnUiThread(new Runnable() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void run() {
                            categoryAdapter.notifyDataSetChanged();
                        }
                    });


                } catch (RuntimeException | IOException | JSONException e) {
                    throw new RuntimeException(e);
                }

            }

        }).start();
    }

    private void loadFoodItems(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("http://192.168.8.195/Food_Finder/get_products.php")
                        .build();

                try {

                    Response response = client.newCall(request).execute();
                    String responseText = response.body().string();

                    //Log.i("foodfinderLog",responseText);

                    JSONArray jsonArray = new JSONArray(responseText);
                    foodItemArrayList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        foodItemArrayList.add(new FoodItem(
                                obj.getString("item_id"),
                                obj.getString("item_name"),
                                obj.getDouble("item_normal_price"),
                                obj.getDouble("item_full_price"),
                                obj.getString("item_qty"),
                                obj.getString("item_description"),
                                obj.getString("image_url")
                        ));
                    }

                    requireActivity().runOnUiThread(new Runnable() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void run() {
                            foodAdapter.notifyDataSetChanged();
                        }
                    });

                } catch (RuntimeException | IOException | JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();

        //Load HomeFragment Category


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}