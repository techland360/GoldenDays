package com.techland360.goldendays.activity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.techland360.goldendays.R;

import java.util.Objects;

public class ImageViewer extends AppCompatActivity {
    PhotoView imageViewOnImageViewer;
    String image_url;
    Toolbar toolbarOnImageViewer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);


        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        image_url = getIntent().getStringExtra("image_url");
        imageViewOnImageViewer = findViewById(R.id.imageViewOnImageViewer);
        Glide
                .with(ImageViewer.this)
                .load(image_url)
                .placeholder(R.drawable.logo)
                .into(imageViewOnImageViewer);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_preview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.download_image_menu) {
            downloadImage(image_url);
        }
        return super.onOptionsItemSelected(item);
    }

    private void downloadImage(String thumbUrl) {


        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(thumbUrl));
        request.setDescription("Downloading file. please wait...");
        String cookie = CookieManager.getInstance().getCookie(thumbUrl);
        request.addRequestHeader("cookie", cookie);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS ,"image.jpg");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
        //set BroadcastReceiver
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(ImageViewer.this, "Download success", Toast.LENGTH_SHORT).show();


            }
        };
        //register receiver for when .apk download is compete
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}