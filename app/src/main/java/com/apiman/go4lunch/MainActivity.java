package com.apiman.go4lunch;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.navigation.NavAction;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.searchView)
    SearchView mSearchView;

    @BindView(R.id.search_view_container)
    CardView searchViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_maps, R.id.navigation_list_view, R.id.navigation_workmates)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navController.addOnDestinationChangedListener(mOnDestinationChangedListener);

        mSearchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus) {
                hideSearchView();
            }
        });
    }

    private NavController.OnDestinationChangedListener
            mOnDestinationChangedListener= new NavController.OnDestinationChangedListener() {
        @Override
        public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
            switch (destination.getId()){
                case R.id.navigation_maps:
                case R.id.navigation_list_view:
                    mSearchView.setQueryHint(getString(R.string.search_restaurants));
                    break;

                case R.id.navigation_workmates:
                    mSearchView.setQueryHint(getString(R.string.search_workmates));
                    break;
            }
        }
    };

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
        mSearchView.requestFocus();
    }

    private void hideSearchView(){
        mToolbar.setVisibility(View.VISIBLE);
        searchViewContainer.setVisibility(View.GONE);
    }
}
