package adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodfinder.R;
import com.example.foodfinder.SingleViewActivity;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import model.FoodItem;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodItemViewHolder> {

    ArrayList<FoodItem> foodItemArrayList;

    public FoodAdapter(ArrayList<FoodItem> foodItemArrayList) {

        this.foodItemArrayList = foodItemArrayList;
    }


    @NonNull
    @Override
    public FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflatedView = layoutInflater.inflate(R.layout.food_item,parent,false);

        FoodItemViewHolder foodItemViewHolder = new FoodItemViewHolder(inflatedView);

        return foodItemViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FoodItemViewHolder holder, int position) {

        FoodItem foodItem = foodItemArrayList.get(position);

        Picasso.get().load(foodItem.getItemImgUrl()).into(holder.imageView1);
        holder.textViewName.setText(foodItem.getName());
        holder.textViewPrice.setText(String.valueOf(foodItem.getNormalPrice()));
        holder.ratingBar.setRating(3.5f);
        holder.textViewQty.setText(foodItem.getQty());

        holder.buttonAddCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(), "Add to Cart",Toast.LENGTH_LONG).show();

                SharedPreferences sharedPreferences = view.getContext().getSharedPreferences("com.example.foodfinder.data",Context.MODE_PRIVATE);
                String loginEmail = sharedPreferences.getString("key_Email",null);

                AddToCart(view.getContext(), foodItem.getId(),loginEmail);

            }
        });

        holder.imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SingleViewActivity.class);
                intent.putExtra("item_id",foodItem.getId());
                view.getContext().startActivity(intent);
            }
        });

    }

    public static void AddToCart(Context context,String id,String email){

        int pid = Integer.parseInt(id);

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();


                Request request = new Request.Builder()
                        .url("http://192.168.8.195/Food_Finder/addToCart.php?id="+pid+"&email="+email)
                        .build();

                try {

                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();

                    Log.i("foodfinderLog",responseData);

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, responseData, Toast.LENGTH_LONG).show()
                    );

                } catch (RuntimeException | IOException e) {
                    throw new RuntimeException(e);
                }

            }

        }).start();
    }

    @Override
    public int getItemCount() {
        return foodItemArrayList.size();
    }

    static class FoodItemViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView1;

        TextView textViewName;
        TextView textViewPrice;

        TextView textViewQty;

        Button buttonAddCart;

        RatingBar ratingBar;

        public FoodItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView1 = itemView.findViewById(R.id.imageViewFoodItem);
            textViewName = itemView.findViewById(R.id.foodItemName);
            textViewPrice = itemView.findViewById(R.id.foodItemPrice);
            buttonAddCart = itemView.findViewById(R.id.foodItemAddCartButton);
            ratingBar = itemView.findViewById(R.id.ratingBarFoodItem);
            textViewQty = itemView.findViewById(R.id.foodItemQty);
        }
    }

}
