package com.example.adminapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryAdepter extends RecyclerView.Adapter<CategoryAdepter.Viewholder> {

    private List<CategoryModel>  categoryModelsList;
    private DeleteListener deleteListener;

    public CategoryAdepter(List<CategoryModel> categoryModelsList,DeleteListener deleteListener) {
        this.categoryModelsList = categoryModelsList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item,parent ,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        holder.setData(categoryModelsList.get(position).getUrl(),
                categoryModelsList.get(position).getName(),
                categoryModelsList.get(position).getKey(),position);
    }

    @Override
    public int getItemCount() {
        return categoryModelsList.size();
    }

     class Viewholder extends RecyclerView.ViewHolder{
        private CircleImageView imageView;
        private TextView title;
        private ImageButton delete;
        public Viewholder(@NonNull View itemView) {
            super(itemView);

            imageView =itemView.findViewById(R.id.image_view);
            title = itemView.findViewById(R.id.title);
            delete = itemView.findViewById(R.id.delete);
        }

        private void setData(String url, final String title,final String key,final int position){
            Glide.with(itemView.getContext()).load(url).into(imageView);
            this.title.setText(title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent setIntent = new Intent(itemView.getContext(),SetsActivity.class);
                    setIntent.putExtra("title",title);
                    setIntent.putExtra("position",position);
                    setIntent.putExtra("key", key);
                    itemView.getContext().startActivity(setIntent);
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteListener.onDelete(key,position);
                }
            });
        }
    }
    public interface DeleteListener{
        public void onDelete(String key, int position);
    }

}
