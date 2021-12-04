package com.techland360.goldendays.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techland360.goldendays.Model.faqModel;
import com.techland360.goldendays.R;

import java.util.ArrayList;
import java.util.List;

public class FaqPage extends AppCompatActivity {
    RecyclerView faqPageRv;
    List<faqModel> faqListMain;
    FaqAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq_page);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Faq Page");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        faqPageRv = findViewById(R.id.faqPage);
        faqPageRv.setHasFixedSize(true);

        faqListMain =  new ArrayList<>();


        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Administrator").child("faqPage");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    faqListMain.clear();
                    for (DataSnapshot ds: snapshot.getChildren()){
                        faqModel faqs = ds.getValue(faqModel.class);
                        faqListMain.add(faqs);
                        mAdapter = new FaqAdapter(faqListMain);
                        faqPageRv.setAdapter(mAdapter);


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    private class  FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqHolder>{
        List<faqModel> faqList;

        public FaqAdapter(List<faqModel> faqList) {
            this.faqList = faqList;
        }

        @NonNull
        @Override
        public FaqHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(FaqPage.this).inflate(R.layout.faq_item, parent,false);
            return new FaqHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FaqHolder holder, int position) {
            faqModel faqs = faqList.get(position);

            holder.qText.setText(faqs.getQ());
            holder.ansText.setText(faqs.getAns());

            holder.itemView.setOnClickListener(v -> {
                if (holder.ansContainer.getVisibility() == View.GONE){
                    holder.ansContainer.setVisibility(View.VISIBLE);
                }else {
                    holder.ansContainer.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return faqList.size();
        }


        public class FaqHolder extends RecyclerView.ViewHolder {
            TextView qText,ansText;
            LinearLayout ansContainer;
            public FaqHolder(@NonNull View itemView) {
                super(itemView);

                qText = itemView.findViewById(R.id.qText);
                ansText = itemView.findViewById(R.id.ansText);
                ansContainer = itemView.findViewById(R.id.ansContainer);
            }
        }
    }
}