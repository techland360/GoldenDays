package com.techland360.beloved.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;
import com.techland360.beloved.Model.Contacts;
import com.techland360.beloved.R;
import com.techland360.beloved.activity.FriendsProfile;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactHolder> {

    Context context;
    List<Contacts> contactsList;
    Activity activity ;


    public ContactAdapter(Context context, List<Contacts> contactsList) {
        this.context = context;
        this.contactsList = contactsList;

        activity = (Activity) context;


    }


    @NonNull
    @NotNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_item, parent,false);
        return new ContactHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ContactAdapter.ContactHolder holder, int position) {


        Contacts contacts = contactsList.get(position);
        holder.contactName.setText(contacts.getName());
        holder.addressOnContactItem.setText(contacts.getAddress());
        Glide.with(context).load(contacts.getImageUrl()).placeholder(R.drawable.profile).into(holder.contactImg);

        holder.editOnContactItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goTOFriendProfile = new Intent(context, FriendsProfile.class);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,holder.contactImg,holder.contactImg.getTransitionName());
                goTOFriendProfile.putExtra("image_url", contacts.getImageUrl());
                goTOFriendProfile.putExtra("unique_id", contacts.getUniqueID());
                context.startActivity(goTOFriendProfile,optionsCompat.toBundle());
            }
        });
        holder.callOnContactItem.setOnClickListener(v -> {

            String phone = contacts.getNumber();
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts(
                    "tel", phone, null));
            context.startActivity(phoneIntent);

        });
        holder.deleteOnContactItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth mAuth;
                DatabaseReference reference;
                mAuth = FirebaseAuth.getInstance();
                reference = FirebaseDatabase.getInstance().getReference("Users")
                        .child(mAuth.getCurrentUser().getUid()).child("Friends").child(contacts.getUniqueID());

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm deleting " + contacts.getName() + " ...");
                builder.setIcon(R.drawable.delete);
                builder.setMessage("Are you sure you want to delete " + contacts.getName() + " from your friend list?");
                builder.setPositiveButton("Yes", (dialog, which) -> reference.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "deleted", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));
                builder.setNegativeButton("No", (dialog, which) -> {

                });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goTOFriendProfile = new Intent(context, FriendsProfile.class);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,holder.contactImg,holder.contactImg.getTransitionName());
                goTOFriendProfile.putExtra("image_url", contacts.getImageUrl());
                goTOFriendProfile.putExtra("unique_id", contacts.getUniqueID());
                context.startActivity(goTOFriendProfile,optionsCompat.toBundle());
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                holder.moreOption.setVisibility(View.VISIBLE);
                return true;
            }
        });

        holder.favoriteBtn.setOnClickListener(v -> {
            notifyItemChanged(position);
            if (contacts.getFav()){
                fav(contacts.getUniqueID(), false);
            }else {
                fav(contacts.getUniqueID(), true);
            }
        });

        if (contacts.getFav()){
            holder.badge.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    class ContactHolder extends RecyclerView.ViewHolder{

        CircleImageView contactImg;
        TextView contactName,addressOnContactItem;
        ImageView callOnContactItem,deleteOnContactItem,editOnContactItem,favoriteBtn,badge;
     LinearLayout moreOption;

        public ContactHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contactNameOnContactItem);
            contactImg = itemView.findViewById(R.id.imageViewOnContactItem);
            moreOption = itemView.findViewById(R.id.moreOption);
            addressOnContactItem = itemView.findViewById(R.id.addressOnContactItem);

            badge = itemView.findViewById(R.id.badge);
            favoriteBtn = itemView.findViewById(R.id.favoriteBtn);
            callOnContactItem = itemView.findViewById(R.id.callOnContactItem);
            deleteOnContactItem = itemView.findViewById(R.id.deleteOnContactItem);
            editOnContactItem = itemView.findViewById(R.id.editOnContactItem);

        }
    }

    public void fav(String fdID , boolean value){


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Friends");
        reference.keepSynced(true);
        reference.child(fdID).child("fav").setValue(value);
    }
}
