package com.example.adminapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class GridAdepter extends BaseAdapter {
    public List<String> sets;
    private String category;
    private GridListener listener;


    public GridAdepter(List<String> sets, String category,GridListener listener) {
        this.category = category;
        this.sets = sets;
        this.listener = listener;

    }

    @Override
    public int getCount() {
        return sets.size()+1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertview, final ViewGroup viewGroup) {
        View view;
        if(convertview == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.set_item, viewGroup , false);

        }else{
            view = convertview;
        }
        if (position ==0){
            ((TextView)view.findViewById(R.id.textView)).setText("+");
        }else{
            ((TextView)view.findViewById(R.id.textView)).setText(String.valueOf(position));
        }
        view.setOnClickListener(new View.OnClickListener() {
            private View v;

            @Override
            public void onClick(View v) {
                if (position == 0) {
                    listener.addSet();
                } else{
                    Intent questionIntent = new Intent(viewGroup.getContext(), QuestionsActivity.class);
                    questionIntent.putExtra("category",category);
                    questionIntent.putExtra("setId", sets.get(position - 1));
                    viewGroup.getContext().startActivity(questionIntent);
                }
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (position!=0) {
                    listener.onLongClick(sets.get(position-1),position);
                }
                return false;
            }
        });

        return view;
    }
    public interface GridListener{
        public void addSet();

        void onLongClick(String setId , int position);
    }
}
