package com.apiman.go4lunch;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.apiman.go4lunch.fragments.ProgressDialogFragment;
import com.apiman.go4lunch.helpers.FireStoreUtils;
import com.apiman.go4lunch.helpers.NotificationHelper;
import com.apiman.go4lunch.helpers.SettingsHelper;
import com.apiman.go4lunch.models.Booking;
import com.apiman.go4lunch.models.SearchMode;
import com.apiman.go4lunch.viewmodels.BaseViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.activity_main_drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.activity_main_navigation_view)
    NavigationView mNavigationView;

    @BindView(R.id.searchView)
    SearchView mSearchView;

    @BindView(R.id.search_view_container)
    CardView searchViewContainer;

    Disposable mDisposable;
    BaseViewModel mBaseViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mBaseViewModel = ViewModelProviders.of(this).get(BaseViewModel.class);

        Places.initialize(this, getString(R.string.place_api_key));
        PlacesClient placesClient = Places.createClient(this);

        configureDrawerLayout();

        configureNavigationView();

        configureBottomNavigationView();

        navigationViewHeader();

        startNotification();

        onQueryTextFocusListener();
        onQueryTextListener(placesClient);
    }

    private void onQueryTextFocusListener() {
        mSearchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus) {
                hideSearchView();
            }
            mBaseViewModel.closeSearchMode();
        });
    }

    private void onQueryTextListener(final PlacesClient placesClient) {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mBaseViewModel.searchAutoComplete(placesClient, newText);
                return false;
            }
        });
    }

    private void startNotification() {
        if(SettingsHelper.isNotificationEnabled(this) &&
                !SettingsHelper.isAlarmStarted(this)) {
            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.startAlarm();
        }
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
                .error(R.drawable.user_profil)
                .placeholder(R.drawable.user_profil)
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
        navController.addOnDestinationChangedListener(mOnDestinationChangedListener);

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

    private NavController.OnDestinationChangedListener
        mOnDestinationChangedListener= new NavController.OnDestinationChangedListener() {
        @Override
        public void onDestinationChanged(@NonNull NavController controller,
                                         @NonNull NavDestination destination,
                                         @Nullable Bundle arguments) {
            switch (destination.getId()){
                case R.id.navigation_maps:
                case R.id.navigation_list_view:
                    mSearchView.setQueryHint(getString(R.string.search_restaurants));
                    mBaseViewModel.setSearchMode(SearchMode.RESTAURANTS);
                    break;

                case R.id.navigation_workmates:
                    mSearchView.setQueryHint(getString(R.string.search_workmates));
                    mBaseViewModel.setSearchMode(SearchMode.WORKMATES);
                    break;
            }
        }
    };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_action_your_lunch:
                fetchSelectedRestaurant();
                return true;

            case R.id.menu_action_settings:
                startSettingsActivity();
                return true;

            case R.id.menu_action_logout:
                confirmSignOut();
                return true;
        }
        return false;
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void confirmSignOut() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.sign_out)
                .setMessage(R.string.confirm_sign_out)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes_sign_out,
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
            .addOnFailureListener(command -> Toast.makeText(MainActivity.this,
                    getString(R.string.operation_failed), Toast.LENGTH_SHORT).show());
    }

    private void showLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void fetchSelectedRestaurant() {
        FirebaseUser user = FireStoreUtils.getCurrentFirebaseUser();

        ProgressDialogFragment dialogFragment = ProgressDialogFragment.newInstance();
        dialogFragment.show(getSupportFragmentManager());

        mDisposable = Observable.just(user.getUid())
                .map(uuid -> Tasks.await(FireStoreUtils.getWorkmateBookOfDay(uuid)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(documentSnapshot -> {
                    dialogFragment.dismiss();
                    Booking booking = documentSnapshot.toObject(Booking.class);
                    if(booking != null) {
                        showRestaurantDetails(booking.placeId, booking.restaurantPhoto);
                        mDrawerLayout.closeDrawers();
                        return;
                    }

                    showNoBookingFound();
                }, throwable -> dialogFragment.dismiss());
    }

    private void showNoBookingFound() {
        Toast.makeText(this, getString(R.string.no_booking_found), Toast.LENGTH_LONG).show();
    }

    private void showRestaurantDetails(String placeId, String restaurantPhoto){
        Intent intent = new Intent(this, RestaurantDetailsActivity.class);
        intent.putExtra(FireStoreUtils.FIELD_PLACE_ID, placeId);
        intent.putExtra(FireStoreUtils.FIELD_PHOTO, restaurantPhoto);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.app_bar_search){
            showSearchView();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSearchView(){
        mToolbar.setVisibility(View.GONE);
        searchViewContainer.setVisibility(View.VISIBLE);
        mSearchView.setQuery("", false);
        mSearchView.requestFocus();
    }

    private void hideSearchView(){
        mToolbar.setVisibility(View.VISIBLE);
        searchViewContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }
}
