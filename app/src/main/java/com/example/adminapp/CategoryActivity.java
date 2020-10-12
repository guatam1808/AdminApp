package com.example.adminapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    private RecyclerView recyclerView;
    private Dialog loadinDialog,categorydialog;
    private CircleImageView addImage;
    private EditText categoryname;
    private Button addBtn;
    public static List<CategoryModel> list;
    private CategoryAdepter adepter;
    private Uri image;
    private String downloadUrl;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Toolbar toolbar = findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.rv);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("categories");

        loadinDialog = new Dialog(this);
        loadinDialog.setContentView(R.layout.loading);
        loadinDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadinDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        loadinDialog.setCancelable(false);

        setCategorydialog();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);

        list = new ArrayList<>();

        adepter = new CategoryAdepter(list, new CategoryAdepter.DeleteListener() {
            @Override
            public void onDelete(final String key, final int position) {
                new AlertDialog.Builder(CategoryActivity.this,R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete category")
                        .setMessage("Are you Sure,You Want to delete this Category")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loadinDialog.show();
                                myRef.child("categories").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            for (String setIds :list.get(position).getSets()){
                                                myRef.child("SETS").child(setIds).removeValue();
                                            }
                                            list.remove(position);
                                            adepter.notifyDataSetChanged();
                                            loadinDialog.dismiss();
                                        }else{
                                            Toast.makeText(CategoryActivity.this,"Failed to delete",Toast.LENGTH_LONG).show();
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
        recyclerView.setAdapter(adepter);
        loadinDialog.show();
        myRef.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    List<String> sets = new ArrayList<>();

                    for (DataSnapshot dataSnapshot1:dataSnapshot.child("sets").getChildren()){
                        sets.add(dataSnapshot1.getKey());
                    }
                    list.add(new CategoryModel(dataSnapshot.child("name").getValue().toString(),
                            sets,dataSnapshot.child("url").getValue().toString(),dataSnapshot.getKey()));
                }
                adepter.notifyDataSetChanged();
                loadinDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategoryActivity.this, error.getMessage(),Toast.LENGTH_LONG).show();
                loadinDialog.dismiss();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add){
            categorydialog.show();
        }
        if (item.getItemId()==R.id.logout){

            new AlertDialog.Builder(CategoryActivity.this,R.style.Theme_AppCompat_Light_Dialog)
                    .setTitle("LOGOUT")
                    .setMessage("Are you Sure,You Want to logout?")
                    .setPositiveButton("LogOut", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            loadinDialog.show();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(CategoryActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();

                        }
                    })
                    .setNegativeButton("cancel",null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }

        return super.onOptionsItemSelected(item);
    }

    private void setCategorydialog(){

        categorydialog = new Dialog(this);
        categorydialog.setContentView(R.layout.add_category_dialog);
        categorydialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        categorydialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_box));
        categorydialog.setCancelable(true);

        addImage = categorydialog.findViewById(R.id.image);
        categoryname = categorydialog.findViewById(R.id.categoryname);
        addBtn = categorydialog.findViewById(R.id.add);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent,101);
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (categoryname.getText() == null ||categoryname.getText().toString().isEmpty()){
                    categoryname.setError("Required!");
                    return;
                }
                for (CategoryModel model :list){
                    if (categoryname.getText().toString().equals(model.getName())){
                        categoryname.setError("category name already present");
                        return;
                    }
                }
                if (image == null){
                    Toast.makeText(CategoryActivity.this,"Please Upload Your Image.",Toast.LENGTH_SHORT).show();
                    return;
                }
                categorydialog.dismiss();
                uploadData();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==101){
            if (resultCode == RESULT_OK){
                image = data.getData();
                addImage.setImageURI(image);
            }
        }
    }
    private void uploadData(){
        loadinDialog.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference imageReference = storageReference.child("categories").child(image.getLastPathSegment());

       UploadTask uploadTask = imageReference.putFile(image);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            downloadUrl = task.getResult().toString();
                            uplaodCategoryName();
                        }else{
                            loadinDialog.dismiss();
                            Toast.makeText(CategoryActivity.this,"Something Went Wrong",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                } else {
                    Toast.makeText(CategoryActivity.this,"Something Went Wrong",Toast.LENGTH_SHORT).show();
                    loadinDialog.dismiss();
                }
            }
        });
    }
    private void uplaodCategoryName(){
        Map<String,Object> map = new HashMap<>();
        map.put("name",categoryname.getText().toString());
        map.put("sets",0);
        map.put("url",downloadUrl);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String id = UUID.randomUUID().toString();
        database.getReference().child("categories").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    list.add(new CategoryModel(categoryname.getText().toString(),new ArrayList<String>(),downloadUrl,id));
                    adepter.notifyDataSetChanged();
                }else{
                    Toast.makeText(CategoryActivity.this,"Something Went Wrong",Toast.LENGTH_SHORT).show();
                }
                loadinDialog.dismiss();
            }
        });
    }
}