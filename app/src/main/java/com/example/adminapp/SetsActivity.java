package com.example.adminapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.UUID;


public class SetsActivity extends AppCompatActivity {

    private GridView gridview;
    private Dialog loadinDialog;
    private GridAdepter adepter;
    private String categoryName;
    private DatabaseReference myRef;
    private List<String> sets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadinDialog = new Dialog(this);
        loadinDialog.setContentView(R.layout.loading);
        loadinDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadinDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        loadinDialog.setCancelable(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        categoryName = getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(categoryName);

        gridview = findViewById(R.id.gridView);
        myRef =FirebaseDatabase.getInstance().getReference();

         sets =CategoryActivity.list.get(getIntent().getIntExtra("position", 0)).getSets();
         adepter = new GridAdepter(sets, getIntent().getStringExtra("title"), new GridAdepter.GridListener() {
            @Override
            public void addSet() {
                loadinDialog.show();
                final String id = UUID.randomUUID().toString();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference().child("categories").child(getIntent().getStringExtra("key"))
                        .child("sets").child(id).setValue("SET ID").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            sets.add(id);
                            adepter.notifyDataSetChanged();
                        }else {
                            Toast.makeText(SetsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                        loadinDialog.dismiss();
                    }
                });
            }

             @Override
             public void onLongClick(final String setId , int position) {

                 new AlertDialog.Builder(SetsActivity.this,R.style.Theme_AppCompat_Light_Dialog)
                         .setTitle("Delete SET "+position)
                         .setMessage("Are you Sure,You Want to delete this SET")
                         .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialogInterface, int i) {
                                 loadinDialog.show();
                                 myRef
                                         .child("SETS").child(setId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                         if (task.isSuccessful()) {
                                             myRef.child("categories").child(CategoryActivity.list.get(getIntent().getIntExtra("position", 0)).getKey())
                                                     .child("sets").child(setId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                 @Override
                                                 public void onComplete(@NonNull Task<Void> task) {
                                                     if (task.isSuccessful()){
                                                         sets.remove(setId);
                                                         adepter.notifyDataSetChanged();
                                                     }else {
                                                         Toast.makeText(SetsActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                                                         loadinDialog.dismiss();
                                                     }

                                                 }
                                             });


                                         } else {
                                             Toast.makeText(SetsActivity.this, "Something went wrong ", Toast.LENGTH_SHORT).show();
                                         }
                                         loadinDialog.dismiss();
                                     }
                                 });
            }
         })
                         .setNegativeButton("cancel",null)
                         .setIcon(android.R.drawable.ic_dialog_alert)
                         .show();

             }
         });
        gridview.setAdapter(adepter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}