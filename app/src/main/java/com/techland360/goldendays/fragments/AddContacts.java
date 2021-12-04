package com.techland360.goldendays.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.techland360.goldendays.R;
import com.techland360.goldendays.helper.Sourov;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class AddContacts extends BottomSheetDialogFragment {

    Toast myToast;
    private FirebaseAuth mAuth;

    FirebaseStorage storage;
    StorageReference storageReference;

    EditText etNameOnAddFD, etNumberOnAddFD, etDOBOnAddFD, etAddressOnAddFD;
    String name, number, dateOfBirth, address, image_url, uniqueID;
    CircleImageView imageOnAddFD;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    DrawerLayout drawerLayout;
    NavigationView navigationView;

    private Uri photoURI = null;
    private Uri croppedPhotoUri;
    public static final int CAMERA_CODE = 200;
    private static final int PICK_IMAGE = 100;

    public AddContacts() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_contacts, container, false);


        mAuth = FirebaseAuth.getInstance(); //initialize firebase auth system
        myToast = Toast.makeText(getContext(), null, Toast.LENGTH_SHORT);

        uniqueID = UUID.randomUUID().toString();

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        etNameOnAddFD = view.findViewById(R.id.etUsernameOnAddFD);
        etNumberOnAddFD = view.findViewById(R.id.etNumberOnAddFD);
        etDOBOnAddFD = view.findViewById(R.id.etDOBOnAddFD);
        etAddressOnAddFD = view.findViewById(R.id.etAddressOnAddFD);
        imageOnAddFD = view.findViewById(R.id.imageOnAddFD);

        etDOBOnAddFD.setFocusable(false);
        etDOBOnAddFD.setOnClickListener(v -> {
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

        mDateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            String date = day + "/" + month + "/" + year;
            etDOBOnAddFD.setText(date);
        };

        view.findViewById(R.id.openCameraOnAddFD).setOnClickListener(v -> openCameraIntent());

        view.findViewById(R.id.openGalleryOnAddFd).setOnClickListener(v -> openGalleryIntent());

        view.findViewById(R.id.AddFDOnAddFD).setOnClickListener(v -> {
            name = etNameOnAddFD.getText().toString().trim();
            number = etNumberOnAddFD.getText().toString().trim();
            dateOfBirth = etDOBOnAddFD.getText().toString().trim();
            address = etAddressOnAddFD.getText().toString().trim();
            if (croppedPhotoUri != null) {
                try {
                    checkImageBeforeUpload();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                myToast.setText("Please select or capture a image");
                myToast.show();
            }


        });

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog dialogc = (BottomSheetDialog) dialog;
            // When using AndroidX the resource can be found at com.google.android.material.R.id.design_bottom_sheet
            FrameLayout bottomSheet = dialogc.findViewById(R.id.design_bottom_sheet);

            BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setPeekHeight(Resources.getSystem().getDisplayMetrics().heightPixels-50);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        return bottomSheetDialog;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_CODE) {
                if (photoURI == null) {
                    myToast.setText("The selected image appears to be blank");

                } else {
                    cropImage(photoURI);
                }

            } else if (requestCode == UCrop.REQUEST_CROP) {
                croppedPhotoUri = UCrop.getOutput(data);
                imageOnAddFD.setImageURI(croppedPhotoUri);
            } else if (requestCode == PICK_IMAGE) {
                photoURI = data.getData();
                cropImage(photoURI);
            }
        }
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
           e.printStackTrace();
           Sourov sourov = new Sourov(getContext());
           sourov.askPermission();
       }
    }

    private void cropImage(Uri photoURI) {
        UCrop.of(photoURI, Uri.fromFile(new File(requireActivity().getCacheDir(), UUID.randomUUID() + ".jpg")))
                .withAspectRatio(1, 1)
                .start(getContext(), this, UCrop.REQUEST_CROP);
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
                = storageReference
                .child("users/").child(mAuth.getCurrentUser().getUid()).child("friends").child(uniqueID + ".webp");
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("Friends").child(uniqueID);
        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", name);
        hashMap.put("number", number);
        hashMap.put("dateOfBirth", dateOfBirth);
        hashMap.put("address", address);
        hashMap.put("imageUrl", image_url);
        hashMap.put("uniqueID", uniqueID);
        hashMap.put("fd_added_date", date);


        reference.setValue(hashMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                myToast.setText("data sent to database");
                myToast.show();

                dismiss();
            }

        });
    }
}