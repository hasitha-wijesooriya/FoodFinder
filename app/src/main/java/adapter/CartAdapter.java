package adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodfinder.R;
import com.example.foodfinder.ui.cart.GalleryFragment;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import model.CartItem;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartItemViewHolder> {

    private ArrayList<CartItem> cartItemArrayList;
    private final Context context;

    public CartAdapter(Context context, ArrayList<CartItem> cartItemArrayList) {
        this.context = context;
        this.cartItemArrayList = cartItemArrayList;
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflatedView = layoutInflater.inflate(R.layout.cart_item, parent, false);
        return new CartItemViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        CartItem cartItem = cartItemArrayList.get(position);

        Picasso.get().load(cartItem.getImg_url()).into(holder.imageView1);
        holder.textViewName.setText(cartItem.getName());
        holder.textViewPrice.setText(String.format("Rs. %.2f", cartItem.getPrice()));
        holder.textViewQty.setText(String.valueOf(cartItem.getQty()));

        holder.buttonDeleteCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeCartItem(position, cartItem.getPid());
            }
        });

        holder.buttonQtyAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newQty = cartItem.getQty() + 1;
                cartItem.setQty(newQty);
                holder.textViewQty.setText(String.valueOf(newQty));
                updateCartQty(cartItem.getPid(), newQty);
                GalleryFragment.updateCartTotal();
            }
        });

        holder.buttonQtyReduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cartItem.getQty() > 1) {
                    int newQty = cartItem.getQty() - 1;
                    cartItem.setQty(newQty);
                    holder.textViewQty.setText(String.valueOf(newQty));
                    updateCartQty(cartItem.getPid(), newQty);
                    GalleryFragment.updateCartTotal();
                }
            }
        });
    }

    private void removeCartItem(int position, int pid) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://192.168.8.195/Food_Finder/remove_cartItem.php?pid=" + pid)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                Log.i("foodfinderLog", responseData);

                if (response.isSuccessful()) {
                    cartItemArrayList.remove(position);

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
                        GalleryFragment.updateCartTotal();
                    });
                } else {
                    showError("Failed to remove item");
                }
            } catch (IOException e) {
                showError("Network error: " + e.getMessage());
            }
        }).start();
    }

    private void updateCartQty(int pid, int qty) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://192.168.8.195/Food_Finder/set_cartQty.php?pid=" + pid + "&qty=" + qty)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                Log.i("foodfinderLog", responseData);

                if (response.isSuccessful()) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "Quantity updated", Toast.LENGTH_SHORT).show());
                } else {
                    showError("Failed to update quantity");
                }
            } catch (IOException e) {
                showError("Network error: " + e.getMessage());
            }
        }).start();
    }

    private void showError(String message) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show());
    }

    @Override
    public int getItemCount() {
        return cartItemArrayList.size();
    }

    static class CartItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView1;
        TextView textViewName;
        TextView textViewPrice;
        TextView textViewQty;
        ImageButton buttonDeleteCart;
        ImageButton buttonQtyAdd;
        ImageButton buttonQtyReduce;

        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView1 = itemView.findViewById(R.id.cartItemImage);
            textViewName = itemView.findViewById(R.id.cartItemName);
            textViewPrice = itemView.findViewById(R.id.cartItemPrice);
            textViewQty = itemView.findViewById(R.id.cartItemQty);
            buttonDeleteCart = itemView.findViewById(R.id.deleteCartItem);
            buttonQtyAdd = itemView.findViewById(R.id.cartViewQtyAdd);
            buttonQtyReduce = itemView.findViewById(R.id.cartViewQtyReduce);
        }
    }
}