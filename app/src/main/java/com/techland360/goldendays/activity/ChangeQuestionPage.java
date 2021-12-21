package com.techland360.goldendays.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.techland360.goldendays.R;
import com.techland360.goldendays.SplashScreen;

import java.util.HashMap;

public class ChangeQuestionPage extends AppCompatActivity {


    AutoCompleteTextView questionSE;
    EditText ansET;
    ArrayAdapter <String> adapterItems;
    DatabaseReference reference;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_question_page);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Update Security Question");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        ansET = findViewById(R.id.anserofQs);
        questionSE = findViewById(R.id.questionSent);
        mAuth  = FirebaseAuth.getInstance(); //initialize firebase auth system

        //functions of dropdown starts
        String [] items = new String[]{"Your Primary School Name?", "Your Place of Birth?", "Your Childhood Friend's Father's Name?",  "Your Pet's Name?",  "Your Favourite Person's Name?", "Your Favourite Snack's Name?" };
        adapterItems = new ArrayAdapter<String> (this, R.layout.list_item, items);
        questionSE.setAdapter(adapterItems);

        questionSE.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Toast.makeText(getApplicationContext(), "Question : "+item, Toast.LENGTH_SHORT).show();
            }
        });
        //functions of dropdown ends



        //=====================================================

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("selfInfo");

        findViewById(R.id.submitBtn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String que = questionSE.getText().toString().trim();
                String ans = ansET.getText().toString().trim();

                if(!que.isEmpty()&&!ans.isEmpty()){
                    HashMap<String, Object> dataHolder = new HashMap<>();
                    dataHolder.put("question", que);
                    dataHolder.put("answer", ans);

                    reference.updateChildren(dataHolder).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Question Updated", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ChangeQuestionPage.this, Homepage.class));
                            finish();
                        }

                    });
                }else{
                    Toast.makeText(getApplicationContext(), "Fields can't be empty", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }
}