package com.akshaychavan.FlashX.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.akshaychavan.FlashX.R;
import com.akshaychavan.FlashX.fragments.DecksFragment;
import com.akshaychavan.FlashX.fragments.PracticeFragment;
import com.akshaychavan.FlashX.fragments.ProgressFragment;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "MainActivity";
    Toolbar toolbar;
    private DrawerLayout drawerLayout;
    NavigationView navigationView;
    BottomNavigationView bottomNavigationView;
    MenuItem mi_decks, mi_practice, mi_progress,mi_resetdecks, mi_exporttoexcel, mi_backup, mi_restore, mi_about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        drawerLayout = findViewById(R.id.drawer_layout);


        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);


//        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
//
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        bindVariables();
        bindEvents();

        // on app open load Decks Fragment by default
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DecksFragment()).commit();


        try {
            getSheetResponse();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

    }

    public void getSheetResponse() throws IOException, GeneralSecurityException {
        Log.e(TAG, "Setting up sheets");

        // The ID of the spreadsheet to retrieve data from.
        String spreadsheetId = "1LOYACk3WgoaQjQPLqfxUwUtyMCE2rau7idJBreUb5Hc"; // TODO: Update placeholder value.

        // The A1 notation of the values to retrieve.
        String range = "A2:C2"; // TODO: Update placeholder value.

        // How values should be represented in the output.
        // The default render option is ValueRenderOption.FORMATTED_VALUE.
        String valueRenderOption = ""; // TODO: Update placeholder value.

        // How dates, times, and durations should be represented in the output.
        // This is ignored if value_render_option is
        // FORMATTED_VALUE.
        // The default dateTime render option is [DateTimeRenderOption.SERIAL_NUMBER].
        String dateTimeRenderOption = ""; // TODO: Update placeholder value.

        Sheets sheetsService = createSheetsService();
        Sheets.Spreadsheets.Values.Get request =
                sheetsService.spreadsheets().values().get(spreadsheetId, range);
        request.setValueRenderOption(valueRenderOption);
        request.setDateTimeRenderOption(dateTimeRenderOption);

        ValueRange response = request.execute();

        // TODO: Change code below to process the `response` object:
        Log.e(TAG, ">>>"+(response==null));
    }

    public static Sheets createSheetsService() throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        // TODO: Change placeholder below to generate authentication credentials. See
        // https://developers.google.com/sheets/quickstart/java#step_3_set_up_the_sample
        //
        // Authorize using one of the following scopes:
        //   "https://www.googleapis.com/auth/drive"
        //   "https://www.googleapis.com/auth/drive.file"
        //   "https://www.googleapis.com/auth/drive.readonly"
        //   "https://www.googleapis.com/auth/spreadsheets"
        //   "https://www.googleapis.com/auth/spreadsheets.readonly"
        GoogleCredential credential = null;

        return new Sheets.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("Google-SheetsSample/0.1")
                .build();
    }



    private void bindVariables() {
        // side navigation tabs
        mi_resetdecks = navigationView.getMenu().getItem(0);
        mi_exporttoexcel = navigationView.getMenu().getItem(1);
        mi_backup = navigationView.getMenu().getItem(2);
        mi_restore = navigationView.getMenu().getItem(3);
        mi_about = navigationView.getMenu().getItem(4);

        // Bottom navigation tabs
        mi_decks = bottomNavigationView.getMenu().getItem(0);
        mi_practice = bottomNavigationView.getMenu().getItem(1);
        mi_progress = bottomNavigationView.getMenu().getItem(2);
    }

    private void bindEvents() {
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nv_decks:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DecksFragment()).commit();
                break;
            case R.id.nv_practice:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PracticeFragment()).commit();
                break;
            case R.id.nv_progress:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProgressFragment()).commit();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}