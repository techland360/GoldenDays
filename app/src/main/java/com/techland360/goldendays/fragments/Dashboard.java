package com.techland360.goldendays.fragments;

import static android.view.View.TEXT_ALIGNMENT_VIEW_END;
import static android.view.View.TEXT_ALIGNMENT_VIEW_START;

import android.Manifest;
import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.techland360.goldendays.Model.Users;
import com.techland360.goldendays.R;
import com.techland360.goldendays.helper.Sourov;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class Dashboard extends Fragment {

    CircleImageView ImageOnDashboard;
    EditText nameOnDashboard, emailOnDashboard, numberOnDashboard, dateOnDashboard;
    TextView nameTextOnDashboard, numberOfFriendsOnDashboard;


    Toast myToast;
    private FirebaseAuth mAuth;

    StorageReference storageReference;
    String name, number, dateOfBirth, email, image_url;

    private Uri photoURI = null;
    private Uri croppedPhotoUri;
    public static final int CAMERA_CODE = 200;
    private static final int PICK_IMAGE = 100;

    LinearLayout saveCancel, imagePicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mAuth  = FirebaseAuth.getInstance(); //initialize firebase auth system
        storageReference = FirebaseStorage.getInstance().getReference();
        myToast = Toast.makeText(getContext(), null, Toast.LENGTH_SHORT);

        saveCancel = view.findViewById(R.id.saveCancel);
        imagePicker = view.findViewById(R.id.imagePicker);

        ImageOnDashboard = view.findViewById(R.id.ImageOnDashboard);
        nameTextOnDashboard = view.findViewById(R.id.nameTextOnDashboard);
        nameOnDashboard = view.findViewById(R.id.nameOnDashboard);
        emailOnDashboard = view.findViewById(R.id.emailOnDashboard);
        numberOnDashboard = view.findViewById(R.id.numberOnDashboard);
        dateOnDashboard = view.findViewById(R.id.dateOnDashboard);
        numberOfFriendsOnDashboard = view.findViewById(R.id.numberOfFriendsOnDashboard);

        dateFunctionality();


        getDetails();
        getTheNumberOfFriends();

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.favorites, new FavouriteList()).commit();


        view.findViewById(R.id.editBtnOnDashboard).setOnClickListener(v -> makeEditable());

        view.findViewById(R.id.openGallery).setOnClickListener(v -> openGalleryIntent());
        view.findViewById(R.id.openCameraOn).setOnClickListener(v -> openCameraIntent());

        view.findViewById(R.id.saveBtn).setOnClickListener(v -> {
            name = nameOnDashboard.getText().toString().trim();
            number = numberOnDashboard.getText().toString().trim();
            dateOfBirth = dateOnDashboard.getText().toString().trim();
            email = emailOnDashboard.getText().toString().trim();

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

        view.findViewById(R.id.cancelBtn).setOnClickListener(v -> {
            makeNonEditable();
            getDetails();
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
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

    String dateString, monthString;
    private void dateFunctionality() {
        DatePickerDialog.OnDateSetListener mDateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;

            if (day < 10) {
                dateString = "0" + day;
            } else {
                dateString = String.valueOf(day);
            }
            if (month < 10) {
                monthString = "0" + month;
            } else {
                monthString = String.valueOf(month);
            }
            String date = dateString + "/" + monthString + "/" + year;
            dateOnDashboard.setText(date);
        };
        dateOnDashboard.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    getContext(),
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
        nameOnDashboard.setBackground(getActivity().getDrawable(R.drawable.round_border));

        emailOnDashboard.setEnabled(true);
        emailOnDashboard.setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        emailOnDashboard.setBackground(getActivity().getDrawable(R.drawable.round_border));

        numberOnDashboard.setEnabled(true);
        numberOnDashboard.setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        numberOnDashboard.setBackground(getActivity().getDrawable(R.drawable.round_border));

        dateOnDashboard.setEnabled(true);
        dateOnDashboard.setFocusable(false);
        dateOnDashboard.setBackground(getActivity().getDrawable(R.drawable.round_border));

        saveCancel.setVisibility(View.VISIBLE);
        imagePicker.setVisibility(View.VISIBLE);

    }

    private void makeNonEditable() {
        nameOnDashboard.setEnabled(false);
        nameOnDashboard.setTextAlignment(TEXT_ALIGNMENT_VIEW_END);
        nameOnDashboard.setBackground(null);

        emailOnDashboard.setEnabled(false);
        emailOnDashboard.setTextAlignment(TEXT_ALIGNMENT_VIEW_END);
        emailOnDashboard.setBackground(null);

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

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).child("selfInfo");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                Users users = snapshot.getValue(Users.class);
                if (users != null) {

                    image_url = users.getImageUrl();
                    name = users.getName();
                    email = users.getEmail();
                    number = users.getNumber();
                    dateOfBirth = users.getDateOfBirth();

                    setDetails();

                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getTheNumberOfFriends() {

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .child("Friends");
        reference2.keepSynced(true);
        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                String friendsCount = String.valueOf(snapshot.getChildrenCount());


                numberOfFriendsOnDashboard.setText(friendsCount);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }


    private void setDetails() {
        try {
            Glide.with(getContext()).load(image_url).placeholder(R.drawable.profile).into(ImageOnDashboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
        nameOnDashboard.setText(name);
        nameTextOnDashboard.setText(name);

        dateOnDashboard.setText(dateOfBirth);
        numberOnDashboard.setText(number);
        emailOnDashboard.setText(email);
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
            Sourov sourov = new Sourov(getContext());
            sourov.askPermission();
        }
    }

    private void openCameraIntent() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "temp_title");
        contentValues.put(MediaStore.Images.Media.TITLE, "temp_desc");
        photoURI = requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        try {
            startActivityForResult(intent, CAMERA_CODE);
        } catch (Exception e) {
            Sourov sourov = new Sourov(getContext());
            sourov.askPermission();
        }

    }

    private void cropImage(Uri photoURI) {
        UCrop.of(photoURI, Uri.fromFile(new File(requireContext().getCacheDir(), UUID.randomUUID() + ".jpg")))
                .withAspectRatio(1, 1)
                .start(requireContext(), this, UCrop.REQUEST_CROP);
    }


    private void checkImageBeforeUpload() throws IOException {
        File f = new File(croppedPhotoUri.getPath());
        long sizeInByte = f.length();
        if (sizeInByte < 51200) {
            uploadImage(f);
        } else {
            File compressedImageFile = new Compressor(requireContext())
                    .setMaxWidth(600)
                    .setMaxHeight(600)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .compressToFile(f);
            uploadImage(compressedImageFile);
        }

    }

    private void uploadImage(File imageFile) {
        ProgressDialog progressDialog
                = new ProgressDialog(getContext());
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser()
                .getUid()).child("selfInfo");
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("name", name);
        hashMap.put("email", email);
        hashMap.put("number", number);
        hashMap.put("imageUrl", image_url);
        hashMap.put("dateOfBirth", dateOfBirth);


        reference.updateChildren(hashMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                myToast.setText("Data Sent Successfully");
                myToast.show();
                makeNonEditable();
            }

        });
    }

}