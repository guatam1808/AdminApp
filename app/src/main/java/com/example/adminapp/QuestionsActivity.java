package com.example.adminapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.AsyncTaskLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class QuestionsActivity extends AppCompatActivity {
    private Button add,excle;
    private RecyclerView recyclerView;
    private QuestionsAdepter adepter;
    private Dialog loadinDialog;
    private TextView loadingText;
    public static List<QuestionModel> list;
    private DatabaseReference myRef;
    private String setId;
    private String categoryName;
    public static final int CELL_COUNT =6;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"RestrictedApi", "UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        Toolbar toolbar = findViewById(R.id.toolbar);
        myRef =FirebaseDatabase.getInstance().getReference();

        loadinDialog = new Dialog(this);
        loadinDialog.setContentView(R.layout.loading);
        loadinDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadinDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        loadinDialog.setCancelable(false);
        loadingText = loadinDialog.findViewById(R.id.textView);

        setSupportActionBar(toolbar);
        categoryName = getIntent().getStringExtra("category");
        setId =getIntent().getStringExtra("setId");
        getSupportActionBar().setTitle(categoryName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        add = findViewById(R.id.add_btn);
        excle = findViewById(R.id.excle_btn);
        recyclerView = findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        list = new ArrayList<>();
        adepter = new QuestionsAdepter(list, categoryName, new QuestionsAdepter.DeleteListener() {
            @Override
            public void onLongClick(final int position, final String id) {
                new AlertDialog.Builder(QuestionsActivity.this,R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete Question")
                        .setMessage("Are you Sure,You Want to delete this Question")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loadinDialog.show();
                                myRef.child("SETS").child(setId).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            list.remove(position);
                                            adepter.notifyItemRemoved(position);

                                        }else{
                                            Toast.makeText(QuestionsActivity.this,"Failed to delete",Toast.LENGTH_LONG).show();
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
        getData(categoryName,setId);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addquestion = new Intent(QuestionsActivity.this,AddQuestionActivity.class);
                addquestion.putExtra("categoryName",categoryName);
                addquestion.putExtra("setId",setId);
                startActivity(addquestion);

            }
        });
        excle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(QuestionsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    selectFile();
                }else {
                    ActivityCompat.requestPermissions(QuestionsActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectFile();
            }
        }else{
            Toast.makeText(this, "Please Grant Permission! ", Toast.LENGTH_SHORT).show();
        }
    }
    private void selectFile(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent,"Select File"),102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102){
            if (resultCode == RESULT_OK){
                String filePath =data.getData().getPath();
                if (filePath.endsWith(".xlsx")){
                    readFile(data.getData());
                }else {
                    Toast.makeText(this, "Please choose an Excle File ", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void getData(String categoryName, final String setId) {
        loadinDialog.show();
        myRef.child("SETS").child(setId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String id = dataSnapshot.getKey();
                    String question = dataSnapshot.child("question").getValue().toString();
                    String a = dataSnapshot.child("optionA").getValue().toString();
                    String b = dataSnapshot.child("optionB").getValue().toString();
                    String c = dataSnapshot.child("optionC").getValue().toString();
                    String d = dataSnapshot.child("optionD").getValue().toString();
                    String correctAns = dataSnapshot.child("correctAns").getValue().toString();

                    list.add(new QuestionModel(id,question,a,b,c,d,correctAns,setId));

                }
                loadinDialog.dismiss();
                adepter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuestionsActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                loadinDialog.dismiss();
                finish();
            }
        });
    }

    private void readFile(Uri fileUri) {
        loadingText.setText("Scanning Questions...");
        loadinDialog.show();


        HashMap<String,Object>  parentMap = new HashMap<>();
        final List<QuestionModel> tempList = new  ArrayList<>();
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet =workbook.getSheetAt(0);
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

            int rowsCount =sheet.getPhysicalNumberOfRows();
            if (rowsCount>0){
                for (int r=0;r<rowsCount;r++){
                    Row row = sheet.getRow(r);
                    if (row.getPhysicalNumberOfCells() == CELL_COUNT){
                        String question = getCallData(row,0,formulaEvaluator);
                        String a =getCallData(row,1,formulaEvaluator);
                        String b =getCallData(row,2,formulaEvaluator);
                        String c =getCallData(row,3,formulaEvaluator);
                        String d =getCallData(row,4,formulaEvaluator);
                        String correctAns =getCallData(row,5,formulaEvaluator);

                        if (correctAns.equals(a)||correctAns.equals(b)||correctAns.equals(c)||correctAns.equals(d)){
                            HashMap<String,Object> questionMap = new HashMap<>();
                            questionMap.put("question",question);
                            questionMap.put("optionA",a);
                            questionMap.put("optionB",b);
                            questionMap.put("optionC",c);
                            questionMap.put("optionD",d);
                            questionMap.put("correctAns",correctAns);
                            questionMap.put("setId",setId);

                            String id = UUID.randomUUID().toString();
                            parentMap.put(id,questionMap);
                            tempList.add(new QuestionModel(id,question,a,b,c,d,correctAns,setId));


                        }else {
                            loadingText.setText("Loading.....");
                            loadinDialog.dismiss();
                            Toast.makeText(this, "Row no ."+(r+1)+" has no correct option", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    else {
                        loadingText.setText("Loading.....");
                        loadinDialog.dismiss();
                        Toast.makeText(this, "Row no. " + (r + 1) + "has in correct data ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                loadingText.setText("Uploading....");
                FirebaseDatabase.getInstance().getReference().child("SETS").child(setId).updateChildren(parentMap).
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            list.addAll(tempList);
                            adepter.notifyDataSetChanged();
                        }else {
                            loadingText.setText("Loading.....");
                             Toast.makeText(QuestionsActivity.this, "Something Went wrong", Toast.LENGTH_SHORT).show();

                        }
                        loadinDialog.dismiss();
                    }
                });
            }else {
                loadingText.setText("Loading.....");
                loadinDialog.dismiss();
                Toast.makeText(this, "File is Empty !", Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            loadingText.setText("Loading.....");
            loadinDialog.dismiss();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            loadingText.setText("Loading.....");
            loadinDialog.dismiss();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        adepter.notifyDataSetChanged();
    }
    private String getCallData(Row row , int cellPosition , FormulaEvaluator formulaEvaluator){
        String value = " ";
        Cell cell = row.getCell(cellPosition);
        switch (cell.getCellType()){
            case Cell.CELL_TYPE_BOOLEAN:
                return value +cell.getBooleanCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                return value +cell.getNumericCellValue();

            case Cell.CELL_TYPE_STRING:
                return value +cell.getStringCellValue();
            default:
                return value;
        }
    }
}