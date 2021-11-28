package com.techland360.beloved.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.techland360.beloved.Model.Contacts;
import com.techland360.beloved.R;
import com.techland360.beloved.adapter.ContactAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FriendListFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;


    RecyclerView recyclerView;
    List<Contacts> contactsList;
    ContactAdapter mAdapter;



    public FriendListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_friend_list, container, false);


        mAuth = FirebaseAuth.getInstance();



        recyclerView = view.findViewById(R.id.fdListRV);
        recyclerView.setHasFixedSize(true);

        displayContacts();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();




        return view;
    }


    private void displayContacts() {
        contactsList =  new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid())
                .child("Friends");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                contactsList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Contacts contacts = ds.getValue(Contacts.class);
                    contactsList.add(contacts);
                    mAdapter = new ContactAdapter(getContext(),contactsList);
                    recyclerView.setAdapter(mAdapter);

                    syncBirthdayTable(firebaseUser.getUid(),contacts.getUniqueID(),contacts.getDateOfBirth(),contacts.getImageUrl(),contacts.getName());

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void syncBirthdayTable(String userID, String contactID, String dateOfBirth,String imageLink,String contactname) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("birthdays").child(contactID);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", userID);
        hashMap.put("contactid", contactID);
        hashMap.put("contactname", contactname);
        hashMap.put("imageLink", imageLink);
        hashMap.put("dateOfBirth", dateOfBirth.substring(0, 5));

        reference.updateChildren(hashMap);
    }


}