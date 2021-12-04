package com.techland360.goldendays.helper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.app.ActivityOptionsCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.techland360.goldendays.R;
import com.techland360.goldendays.activity.FriendsProfile;
import com.techland360.goldendays.activity.ImageViewer;

import java.util.List;

public class Sourov {
    private Context context;
    AlertDialog progressDialog;
    Activity activity;

    public Sourov(Context context) {
        this.context = context;
        this.activity = (Activity) context;

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View progressView = layoutInflater.inflate(R.layout.progress_bar, null);
        progressDialog = new AlertDialog.Builder(context).create();
        progressDialog.setView(progressView);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

    }

    public AlertDialog spinner() {
        return progressDialog;
    }

    public void openActivity(Class destination, boolean finish) {
        Intent intent = new Intent(context, destination);
        context.startActivity(intent);
        if (finish) {

            activity.finish();
        }
    }


    public void invokeNativeApp(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }

    public void askPermission() {

        Dexter.withContext(context)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {/* ... */}

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    showSettingsDialog();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void showSettingsDialog() {
        // we are displaying an alert dialog for permissions
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // below line is the title
        // for our alert dialog.
        builder.setTitle("Need Permissions");

        // below line is our message for our dialog
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // this method is called on click on positive
                // button and on clicking shit button we
                // are redirecting our user from our app to the
                // settings page of our app.
                dialog.cancel();
                // below is the intent from which we
                // are redirecting our user.
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                activity.startActivityForResult(intent, 101);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // this method is called when
                // user click on negative button.
                dialog.cancel();

            }
        });
        // below line is used
        // to display our dialog
        builder.show();
    }

    public void openImageViewer(String imageLink,View view){
        Intent intent = new Intent(context, ImageViewer.class);
        intent.putExtra("image_url", imageLink);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, view.getTransitionName());
        context.startActivity(intent,optionsCompat.toBundle());
    }

}
