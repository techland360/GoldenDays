package com.techland360.goldendays.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.techland360.goldendays.R;
import com.techland360.goldendays.adapter.HomepagePagerAdapter;
import com.techland360.goldendays.auth.AuthActivity;
import com.techland360.goldendays.fragments.AddContacts;
import com.techland360.goldendays.fragments.Dashboard;
import com.techland360.goldendays.fragments.FavouriteList;
import com.techland360.goldendays.fragments.FriendListFragment;
import com.techland360.goldendays.helper.AndroidBug5497Workaround;
import com.techland360.goldendays.helper.SaveState;
import com.techland360.goldendays.helper.Sourov;

public class Homepage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    ViewPager2 viewPager2;
    HomepagePagerAdapter homepagePagerAdapter;
    BottomNavigationView bottom_navigation;
    Sourov sourov;
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
//        AndroidBug5497Workaround.assistActivity(this);

        sourov = new Sourov(Homepage.this);

        floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddContacts fragment = new AddContacts();
                fragment.show(getSupportFragmentManager(), "TAG");
            }
        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        drawerLayout = findViewById(R.id.drawer_layoutOnHomepage);
        NavigationView navigationView = findViewById(R.id.navigationViewOnHomepage);
        navigationView.setItemIconTintList(null);

        //navigation toggle
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        toolbar.setNavigationIcon(R.drawable. ic_custom_nav_icon);

        viewPager2 = findViewById(R.id.pager2);
        homepagePagerAdapter = new HomepagePagerAdapter(getSupportFragmentManager(), getLifecycle());
        homepagePagerAdapter.add(new Dashboard());
        homepagePagerAdapter.add(new FriendListFragment());
        homepagePagerAdapter.add(new FavouriteList());


        viewPager2.setAdapter(homepagePagerAdapter);


        bottom_navigation = findViewById(R.id.bottom_navigation);
        bottom_navigation.setOnItemSelectedListener(this::onNavigationItemSelected);


        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bottom_navigation.getMenu().getItem(position).setChecked(true);
                if (position == 0) {
                    getSupportActionBar().setTitle(getString(R.string.home));
                    floatingActionButton.setVisibility(View.GONE);
                } else if (position == 1) {
                    getSupportActionBar().setTitle(getString(R.string.friends));
                    floatingActionButton.setVisibility(View.VISIBLE);
                } else if (position == 2) {
                    getSupportActionBar().setTitle(getString(R.string.favourite));
                    floatingActionButton.setVisibility(View.VISIBLE);
                }
            }
        });


        createNotificationChanel();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.homepage_menu, menu);
        menu.findItem(R.id.app_bar_setting).setOnMenuItemClickListener(item -> {
            startActivity(new Intent(Homepage.this, Settings.class));
            return true;
        });
        menu.findItem(R.id.app_bar_logout).setOnMenuItemClickListener(item -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(Homepage.this, AuthActivity.class));
            finish();
            return true;
        });
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_home) {
            viewPager2.setCurrentItem(0);
        } else if (itemId == R.id.action_friends) {
            viewPager2.setCurrentItem(1);

        } else if (itemId == R.id.action_fav) {
            viewPager2.setCurrentItem(2);
        } else if (itemId == R.id.action_settings) {
            sourov.openActivity(Settings.class, false);
        } else if (itemId == R.id.action_about_dev) {
            sourov.invokeNativeApp(getResources().getString(R.string.about_us_url));
        } else if (itemId == R.id.action_contact_dev) {
            sourov.invokeNativeApp("https://goldendays.techland360.com/contact");



        } else if (itemId == R.id.action_facebook) {
            sourov.invokeNativeApp(getResources().getString(R.string.facebook_url));
        } else if (itemId == R.id.action_youtube) {
            sourov.invokeNativeApp(getResources().getString(R.string.youtube_url));
        } else if (itemId == R.id.privacy_policy) {
            sourov.invokeNativeApp(getResources().getString(R.string.privacy_policy_url));

        } else if (itemId == R.id.action_share) {
            shareApp();
        } else if (itemId == R.id.action_rate_app) {
            rateThisApp();
        } else if (itemId == R.id.action_more_app) {
            moreApps();
        } else if (itemId == R.id.action_faq) {
            sourov.openActivity(FaqPage.class, false);
        } else if (itemId == R.id.action_about_report) {
            sourov.invokeNativeApp(getResources().getString(R.string.report_page_link));
        } else if (itemId == R.id.action_exit) {
            exitConfirmation();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void shareApp() {
        try {
            final String appPackageName = getPackageName();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_text) + " https://play.google.com/store/apps/details?id=" + appPackageName);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rateThisApp() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void moreApps() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            intent.setData(Uri.parse(getString(R.string.developer_page)));
            startActivity(intent);
        } catch (Exception e) {
            intent.setData(Uri.parse(getString(R.string.alternate_dev_page)));
            startActivity(intent);
        }
        startActivity(intent);
    }

    private void exitConfirmation() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    System.exit(0);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };
        builder.setTitle("Exit the app?");
        builder.setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();
    }

    @Override
    public void onBackPressed() {
        exitConfirmation();
    }

    private void createNotificationChanel() {
        SaveState saveState = new SaveState(this);
        if (saveState.isNotificationOn()) {
            FirebaseMessaging.getInstance().subscribeToTopic(FirebaseAuth.getInstance().getCurrentUser().getUid());
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }

    }
}