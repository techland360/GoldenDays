package com.techland360.beloved.activity;

import static android.view.View.TEXT_ALIGNMENT_VIEW_END;
import static android.view.View.TEXT_ALIGNMENT_VIEW_START;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.techland360.beloved.Model.Contacts;
import com.techland360.beloved.Model.Users;
import com.techland360.beloved.R;
import com.techland360.beloved.fragments.FavouriteList;
import com.techland360.beloved.helper.Sourov;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class FriendsProfile extends AppCompatActivity {
    Sourov sourov;
    CircleImageView ImageOnDashboard;
    EditText nameOnDashboard, addressOnDashboard, numberOnDashboard, dateOnDashboard;
    TextView nameTextOnDashboard, numberOfFriendsOnDashboard;


    Toast myToast;
    private FirebaseAuth mAuth;

    StorageReference storageReference;
    String name, number, dateOfBirth, address, image_url, unique_id;

    private Uri photoURI = null;
    private Uri croppedPhotoUri;
    public static final int CAMERA_CODE = 200;
    private static final int PICK_IMAGE = 100;

    LinearLayout saveCancel, imagePicker;
    private boolean fav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_profile);
        sourov = new Sourov(FriendsProfile.this);

        unique_id = getIntent().getStringExtra("unique_id");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Details");
        toolbar.setNavigationOnClickListener(v -> finish());

        mAuth = FirebaseAuth.getInstance(); //initialize firebase auth system
        storageReference = FirebaseStorage.getInstance().getReference();
        myToast = Toast.makeText(FriendsProfile.this, null, Toast.LENGTH_SHORT);

        saveCancel = findViewById(R.id.saveCancel);
        imagePicker = findViewById(R.id.imagePicker);

        ImageOnDashboard = findViewById(R.id.ImageOnDashboard);
        nameTextOnDashboard = findViewById(R.id.nameTextOnDashboard);
        nameOnDashboard = findViewById(R.id.nameOnDashboard);
        addressOnDashboard = findViewById(R.id.addressOnDashboard);
        numberOnDashboard = findViewById(R.id.numberOnDashboard);
        dateOnDashboard = findViewById(R.id.dateOnDashboard);
        numberOfFriendsOnDashboard = findViewById(R.id.numberOfFriendsOnDashboard);

        dateFunctionality();
        BottomExtraBtnInit();


        getDetails();


        findViewById(R.id.editBtnOnDashboard).setOnClickListener(v -> makeEditable());

        findViewById(R.id.openGallery).setOnClickListener(v -> openGalleryIntent());
        findViewById(R.id.openCameraOn).setOnClickListener(v -> openCameraIntent());

        findViewById(R.id.saveBtn).setOnClickListener(v -> {
            name = nameOnDashboard.getText().toString().trim();
            number = numberOnDashboard.getText().toString().trim();
            dateOfBirth = dateOnDashboard.getText().toString().trim();
            address = addressOnDashboard.getText().toString().trim();

            if (croppedPhotoUri == null) {
                sendData();
            } else {
                try {
                    checkImageBeforeUpload();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.cancelBtn).setOnClickListener(v -> {
            makeNonEditable();
            getDetails();
        });

        ImageOnDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sourov.openImageViewer(image_url,ImageOnDashboard);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_CODE) {
                if (photoURI == null) {
                    myToast.setText("The selected image appears to be blank");

                } else {
                    cropImage(photoURI);
                }

            } else if (requestCode == UCrop.REQUEST_CROP) {
                croppedPhotoUri = UCrop.getOutput(data);
                ImageOnDashboard.setImageURI(croppedPhotoUri);
            } else if (requestCode == PICK_IMAGE) {
                photoURI = data.getData();
                cropImage(photoURI);
            }
        }
    }

    private void dateFunctionality() {
        DatePickerDialog.OnDateSetListener mDateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            String date = day + "/" + month + "/" + year;
            dateOnDashboard.setText(date);
        };
        dateOnDashboard.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    mDateSetListener,
                    year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });


    }

    private void makeEditable() {
        nameOnDashboard.setEnabled(true);
        nameOnDashboard.setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        nameOnDashboard.setBackground(getResources().getDrawable(R.drawable.round_border));

        addressOnDashboard.setEnabled(true);
        addressOnDashboard.setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        addressOnDashboard.setBackground(getResources().getDrawable(R.drawable.round_border));

        numberOnDashboard.setEnabled(true);
        numberOnDashboard.setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        numberOnDashboard.setBackground(getResources().getDrawable(R.drawable.round_border));

        dateOnDashboard.setEnabled(true);
        dateOnDashboard.setFocusable(false);
        dateOnDashboard.setBackground(getResources().getDrawable(R.drawable.round_border));

        saveCancel.setVisibility(View.VISIBLE);
        imagePicker.setVisibility(View.VISIBLE);

    }

    private void makeNonEditable() {
        nameOnDashboard.setEnabled(false);
        nameOnDashboard.setTextAlignment(TEXT_ALIGNMENT_VIEW_END);
        nameOnDashboard.setBackground(null);

        addressOnDashboard.setEnabled(false);
        addressOnDashboard.setTextAlignment(TEXT_ALIGNMENT_VIEW_END);
        addressOnDashboard.setBackground(null);

        numberOnDashboard.setEnabled(false);
        numberOnDashboard.setTextAlignment(TEXT_ALIGNMENT_VIEW_END);
        numberOnDashboard.setBackground(null);

        dateOnDashboard.setEnabled(false);
        dateOnDashboard.setFocusable(false);
        dateOnDashboard.setBackground(null);

        saveCancel.setVisibility(View.GONE);
        imagePicker.setVisibility(View.GONE);
    }

    private void getDetails() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("Friends").child(unique_id);


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                Contacts users = snapshot.getValue(Contacts.class);
                if (users != null) {

                    image_url = users.getImageUrl();
                    name = users.getName();
                    address = users.getAddress();
                    number = users.getNumber();
                    dateOfBirth = users.getDateOfBirth();


                    ImageView badge = findViewById(R.id.badge);
                    if (users.getFav()) {
                        fav = true;
                        badge.setVisibility(View.VISIBLE);
                    } else {
                        fav = false;
                        badge.setVisibility(View.GONE);
                    }

                    setDetails();

                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    private void setDetails() {
        try {
            Glide.with(this).load(image_url).placeholder(R.drawable.profile).into(ImageOnDashboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
        nameOnDashboard.setText(name);
        nameTextOnDashboard.setText(name);

        dateOnDashboard.setText(dateOfBirth);
        numberOnDashboard.setText(number);
        addressOnDashboard.setText(address);
        photoURI = null;
    }

    private void openGalleryIntent() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");


        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});


        try {
            startActivityForResult(pickIntent, PICK_IMAGE);

        } catch (Exception e) {
            e.printStackTrace();
            startActivityForResult(chooserIntent, PICK_IMAGE);
            sourov.askPermission();
        }
    }

    private void openCameraIntent() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "temp_title");
        contentValues.put(MediaStore.Images.Media.TITLE, "temp_desc");
        photoURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        try {
            startActivityForResult(intent, CAMERA_CODE);
        } catch (Exception e) {
            sourov.askPermission();
        }

    }

    private void cropImage(Uri photoURI) {
        UCrop.of(photoURI, Uri.fromFile(new File(getCacheDir(), UUID.randomUUID() + ".jpg")))
                .withAspectRatio(1, 1)
                .start(this);
    }


    private void checkImageBeforeUpload() throws IOException {
        File f = new File(croppedPhotoUri.getPath());
        long sizeInByte = f.length();
        if (sizeInByte < 51200) {
            uploadImage(f);
        } else {
            File compressedImageFile = new Compressor(this)
                    .setMaxWidth(600)
                    .setMaxHeight(600)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .compressToFile(f);
            uploadImage(compressedImageFile);
        }

    }

    private void uploadImage(File imageFile) {
        ProgressDialog progressDialog
                = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        StorageReference ref
                = FirebaseStorage.getInstance().getReference()
                .child("users/" + mAuth.getCurrentUser().getUid()).child("profilePic/").child(UUID.randomUUID().toString() + ".webp");
        ref.putFile(Uri.fromFile(imageFile)).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> task1 = taskSnapshot.getStorage().getDownloadUrl();
            task1.addOnSuccessListener(uri -> {
                image_url = uri.toString();
                sendData();
            });

            progressDialog.dismiss();
            myToast.setText("Image Uploaded!!");
            myToast.show();

        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            myToast.setText("Failed " + e.getMessage());
            myToast.show();
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            progressDialog.setMessage("Uploaded " + (int) progress + "%");
        });

    }

    private void sendData() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("Friends").child(unique_id);

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("name", name);
        hashMap.put("number", number);
        hashMap.put("dateOfBirth", dateOfBirth);
        hashMap.put("address", address);
        hashMap.put("imageUrl", image_url);


        reference.updateChildren(hashMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                myToast.setText("data sent to database");
                myToast.show();
                makeNonEditable();
            }

        });
    }

    public void BottomExtraBtnInit() {
        findViewById(R.id.callOnContactItem).setOnClickListener(v -> {
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts(
                    "tel", number, null));
            startActivity(phoneIntent);
        });
        findViewById(R.id.deleteOnContactItem).setOnClickListener(v -> {
            DatabaseReference reference;
            mAuth = FirebaseAuth.getInstance();
            reference = FirebaseDatabase.getInstance().getReference("Users")
                    .child(mAuth.getCurrentUser().getUid()).child("Friends").child(unique_id);

            AlertDialog.Builder builder = new AlertDialog.Builder(FriendsProfile.this);
            builder.setTitle("Confirm deleting " + name + " ...");
            builder.setIcon(R.drawable.delete);
            builder.setMessage("Are you sure you want to delete " + name + " from your friend list?");
            builder.setPositiveButton("Yes", (dialog, which) -> reference.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    myToast.setText("deleted");
                    myToast.show();
                    finish();

                } else {
                    myToast.setText(task.getException().getMessage());
                    myToast.show();
                }
            }));
            builder.setNegativeButton("No", (dialog, which) -> {

            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });
        findViewById(R.id.editOnContactItem).setOnClickListener(v -> makeEditable());
        findViewById(R.id.favoriteBtn).setOnClickListener(v -> {
            if (fav) {
                favManager(unique_id, false);
            } else {
                favManager(unique_id, true);
            }
        });

    }

    public void favManager(String fdID, boolean value) {


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Friends");
        reference.keepSynced(true);
        reference.child(fdID).child("fav").setValue(value);
    }
}