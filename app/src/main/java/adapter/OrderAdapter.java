package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodfinder.R;

import java.util.ArrayList;

import model.OrderItem;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderItemViewHolder> {

    ArrayList<OrderItem> orderItemArrayList;

    public OrderAdapter(ArrayList<OrderItem> orderItemArrayList) {
        this.orderItemArrayList = orderItemArrayList;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflatedView = layoutInflater.inflate(R.layout.order_item,parent,false);

        OrderItemViewHolder orderItemViewHolder = new OrderItemViewHolder(inflatedView);

        return orderItemViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {

        OrderItem orderItem = orderItemArrayList.get(position);

        holder.orderId.setText(orderItem.getName());
        holder.orderStatus.setText(orderItem.getStatus());
        holder.orderDate.setText(orderItem.getDate());
        holder.orderTotal.setText(orderItem.getTotal());
    }

    public int getItemCount() {
        return orderItemArrayList.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder{



        TextView orderId;
        TextView orderStatus;

        TextView orderDate;

        TextView orderTotal;



        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);

            orderId = itemView.findViewById(R.id.order_id);
            orderStatus = itemView.findViewById(R.id.status);
            orderDate = itemView.findViewById(R.id.date);
            orderTotal = itemView.findViewById(R.id.amount);
        }
    }

}
