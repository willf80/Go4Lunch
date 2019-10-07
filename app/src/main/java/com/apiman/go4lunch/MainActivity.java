package com.apiman.go4lunch;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.apiman.go4lunch.services.FireStoreUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.activity_main_drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.activity_main_navigation_view)
    NavigationView mNavigationView;

//    @BindView(R.id.searchView)
//    SearchView mSearchView;

    @BindView(R.id.search_view_container)
    CardView searchViewContainer;

    Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mRealm = Realm.getDefaultInstance();

        // Delete all data
        mRealm.executeTransaction(realm -> realm.deleteAll());

        configureDrawerLayout();

        configureNavigationView();

        configureBottomNavigationView();

        navigationViewHeader();
    }

    private void navigationViewHeader(){
        View view = mNavigationView.getHeaderView(0);
        TextView userNameTextView = view.findViewById(R.id.userNameTextView);
        TextView emailTextView = view.findViewById(R.id.emailTextView);
        CircleImageView profileImage = view.findViewById(R.id.profile_image);

        FirebaseUser user = FireStoreUtils.getCurrentFirebaseUser();

        userNameTextView.setText(user.getDisplayName());
        emailTextView.setText(user.getEmail());

        Picasso.get()
                .load(user.getPhotoUrl())
                .resize(80, 80)
                .into(profileImage);
    }

    private void configureBottomNavigationView() {
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_maps, R.id.navigation_list_view, R.id.navigation_workmates)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    // Configure Drawer Layout
    private void configureDrawerLayout(){
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                mDrawerLayout.addDrawerListener(toggle);

        toggle.syncState();
    }

    // Configure NavigationView
    private void configureNavigationView(){
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed},
                new int[]{}
        };

        int[] colors = new int[]{
                getResources().getColor(R.color.colorGrey300),
                getResources().getColor(android.R.color.white)
        };

        ColorStateList colorStateList = new ColorStateList(states, colors);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setItemTextColor(colorStateList);
        mNavigationView.setItemIconTintList(colorStateList);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        return false;
    }

}
