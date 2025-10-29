package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodfinder.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import model.Category;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    ArrayList<Category> categoryArrayList;
    public CategoryAdapter(ArrayList<Category> categoryArrayList) {
        this.categoryArrayList = categoryArrayList;
    }


    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflatedView = layoutInflater.inflate(R.layout.category_item,parent,false);

        CategoryViewHolder categoryViewHolder = new CategoryViewHolder(inflatedView);

        return categoryViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {

        Category category = categoryArrayList.get(position);
        String categoryName = category.getName();

        holder.categoryName.setText(categoryName);

        Picasso.get().load(category.getImg_url()).into(holder.categoryImage);

    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder{

        ImageView categoryImage;

        TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.categoryImage);
            categoryName = itemView.findViewById(R.id.categoryName);
        }
    }

}
