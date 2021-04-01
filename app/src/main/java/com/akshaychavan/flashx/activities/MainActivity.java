package com.akshaychavan.flashx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akshaychavan.flashx.R;
import com.akshaychavan.flashx.fragments.DecksFragment;
import com.akshaychavan.flashx.fragments.PracticeFragment;
import com.akshaychavan.flashx.fragments.ProgressFragment;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private static final int RC_SIGN_IN = 0;
    private final String TAG = "MainActivity";
    Toolbar toolbar;
    private DrawerLayout drawerLayout;
    NavigationView navigationView;
    BottomNavigationView bottomNavigationView;
    MenuItem mi_decks, mi_practice, mi_progress,mi_resetdecks, mi_exporttoexcel, mi_backup, mi_restore, mi_about;
    SignInButton signInButton;
    GoogleSignInClient mGoogleSignInClient;
    TextView tvUsername, tvEmail;
    ImageView ivProfileIcon;

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        // Check for existing Google Sign In account, if the user is already signed in
//// the GoogleSignInAccount will be non-null.
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
////        updateUI(account);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        // Setting navigation bar
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



        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        // on app open load Decks Fragment by default
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DecksFragment()).commit();


        signIn();
//        try {
//            getSheetResponse();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (GeneralSecurityException e) {
//            e.printStackTrace();
//        }

    }

//    public void getSheetResponse() throws IOException, GeneralSecurityException {
//        Log.e(TAG, "Setting up sheets");
//
//        // The ID of the spreadsheet to retrieve data from.
//        String spreadsheetId = "1LOYACk3WgoaQjQPLqfxUwUtyMCE2rau7idJBreUb5Hc"; // TODO: Update placeholder value.
//
//        // The A1 notation of the values to retrieve.
//        String range = "A2:C2"; // TODO: Update placeholder value.
//
//        // How values should be represented in the output.
//        // The default render option is ValueRenderOption.FORMATTED_VALUE.
//        String valueRenderOption = ""; // TODO: Update placeholder value.
//
//        // How dates, times, and durations should be represented in the output.
//        // This is ignored if value_render_option is
//        // FORMATTED_VALUE.
//        // The default dateTime render option is [DateTimeRenderOption.SERIAL_NUMBER].
//        String dateTimeRenderOption = ""; // TODO: Update placeholder value.
//
//        Sheets sheetsService = createSheetsService();
//        Sheets.Spreadsheets.Values.Get request =
//                sheetsService.spreadsheets().values().get(spreadsheetId, range);
//        request.setValueRenderOption(valueRenderOption);
//        request.setDateTimeRenderOption(dateTimeRenderOption);
//
//        ValueRange response = request.execute();
//
//        // TODO: Change code below to process the `response` object:
//        Log.e(TAG, ">>>"+(response==null));
//    }

//    public static Sheets createSheetsService() throws IOException, GeneralSecurityException {
//        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//
//        // TODO: Change placeholder below to generate authentication credentials. See
//        // https://developers.google.com/sheets/quickstart/java#step_3_set_up_the_sample
//        //
//        // Authorize using one of the following scopes:
//        //   "https://www.googleapis.com/auth/drive"
//        //   "https://www.googleapis.com/auth/drive.file"
//        //   "https://www.googleapis.com/auth/drive.readonly"
//        //   "https://www.googleapis.com/auth/spreadsheets"
//        //   "https://www.googleapis.com/auth/spreadsheets.readonly"
//        GoogleCredential credential = null;
//
//        return new Sheets.Builder(httpTransport, jsonFactory, credential)
//                .setApplicationName("Google-SheetsSample/0.1")
//                .build();
//    }



    private void bindVariables() {
//        signInButton = findViewById(R.id.sign_in_button);

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

        // Side Navbar Item
        View sideNav =  navigationView.getHeaderView(0);
        tvUsername = sideNav.findViewById(R.id.tv_username);
        tvEmail = sideNav.findViewById(R.id.tv_user_mail);
        ivProfileIcon = sideNav.findViewById(R.id.iv_profile_icon);

        Toast.makeText(this, "Username textview set", Toast.LENGTH_SHORT).show();
    }

    private void bindEvents() {

//        signInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                signIn();
//            }
//        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.nv_reset: break;
                    case R.id.nv_export: break;
                    case R.id.nv_backup: break;
                    case R.id.nv_restore: break;
                    case R.id.nv_about: break;
                    case R.id.nv_logout:
                        mGoogleSignInClient.signOut().addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(MainActivity.this, "You have successfully logged out!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
//            updateUI(account);
            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
            getUserAccountInfo();
        } catch (ApiException e) {
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
            if(e.getStatusCode()==12501) {
                signIn();
            }
//            updateUI(null);
        }
    }


    public void getUserAccountInfo() {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();

            tvUsername.setText(personName.toString());
            tvEmail.setText(personEmail.toString());

            Glide.with(this)
                    .load(personPhoto)
                    .into(ivProfileIcon);

        }
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
            case R.id.nv_reset:
                Toast.makeText(this, "Reset clicked!", Toast.LENGTH_SHORT).show();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}