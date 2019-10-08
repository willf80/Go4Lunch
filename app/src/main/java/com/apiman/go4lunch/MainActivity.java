package com.apiman.go4lunch;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.apiman.go4lunch.models.Booking;
import com.apiman.go4lunch.services.FireStoreUtils;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
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
    Disposable mDisposable;

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
        switch (menuItem.getItemId()) {
            case R.id.menu_action_your_lunch:
                fetchSelectedRestaurant();
                return true;

            case R.id.menu_action_logout:
                confirmSignOut();
                return true;
        }
        return false;
    }

    private void confirmSignOut() {
        new AlertDialog.Builder(this)
                .setTitle("Sign out")
                .setMessage("Do you want to sign out ?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes, Sign out",
                (dialog, which) -> signOut())
                .create()
                .show();
    }

    private void signOut() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();

        GoogleSignInClient googleSignInClient = FireStoreUtils.getGoogleSignInClient(this);

        googleSignInClient.signOut()
                .addOnSuccessListener(aVoid -> showLoginActivity())
                .addOnFailureListener(command ->
                        Toast.makeText(
                                MainActivity.this,
                                getString(R.string.operation_failed),
                                Toast.LENGTH_SHORT).show());
    }

    private void showLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void fetchSelectedRestaurant() {
        FirebaseUser user = FireStoreUtils.getCurrentFirebaseUser();

        mDisposable = Observable.just(user.getUid())
                .map(uuid -> Tasks.await(FireStoreUtils.getWorkmateBookOfDay(uuid)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(documentSnapshot -> {
                    Booking booking = documentSnapshot.toObject(Booking.class);
                    if(booking != null) {
                        showRestaurantDetails(booking.placeId);
                    }
                });
    }

    private void showRestaurantDetails(String placeId){
        Intent intent = new Intent(this, RestaurantDetailsActivity.class);
        intent.putExtra(FireStoreUtils.FIELD_PLACE_ID, placeId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }
}
