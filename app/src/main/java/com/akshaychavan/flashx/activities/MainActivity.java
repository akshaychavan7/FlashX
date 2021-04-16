package com.akshaychavan.flashx.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.akshaychavan.flashx.R;
import com.akshaychavan.flashx.fragments.DecksFragment;
import com.akshaychavan.flashx.fragments.PracticeFragment;
import com.akshaychavan.flashx.fragments.ProgressFragment;
import com.akshaychavan.flashx.pojo.CardPojo;
import com.akshaychavan.flashx.pojo.Definition;
import com.akshaychavan.flashx.pojo.WordDataPojo;
import com.akshaychavan.flashx.utility.ApiClient;
import com.akshaychavan.flashx.utility.ApiInterface;
import com.akshaychavan.flashx.utility.GlobalCode;
import com.akshaychavan.flashx.utility.MyDatabaseHelper;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private static final int RC_SIGN_IN = 0;
    public static Context mycontext;
    private final String TAG = "MainActivity";
    Toolbar toolbar;
    NavigationView navigationView;
    BottomNavigationView bottomNavigationView;
    MenuItem mi_decks, mi_practice, mi_progress, mi_resetdecks, mi_exporttoexcel, mi_backup, mi_restore, mi_about;
    SignInButton signInButton;
    GoogleSignInClient mGoogleSignInClient;
    TextView tvUsername, tvEmail, tvMadeBy;
    ImageView ivProfileIcon;
    GlobalCode globalCode;
    //////////////////////////////////////////////////////////////////
    AlertDialog addNewDeckPopup, addNewCardPopup;
    String newDeckName = null;
    // Add new card to deck popup variables
    ImageView ivImageBrowser;
    EditText etImagePath, etWord, etWordDefinition, etWordSynonyms, etWordExample, etWordMnemonic;
    TextView tvSaveCard, tvCancelCard, tvAddNextCard;
    MaterialButton verbBtn, adjBtn, nounBtn, idiomBtn;
    ProgressBar progressBar;
    String selectedWordClass;
    private DrawerLayout drawerLayout;
    //////////////////////////////////////////////////////////////////

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


        //IMP: setup global code
        globalCode = new GlobalCode(MainActivity.this);
        globalCode.setInstance(globalCode);


        // check if database exists
        File database = getDatabasePath(MyDatabaseHelper.DATABASE_NAME);

        MyDatabaseHelper myDbHelper = MyDatabaseHelper.getInstance(MainActivity.this);
        try {
            myDbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        try {
            myDbHelper.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }
//        Toast.makeText(MainActivity.this, "Successfully Imported", Toast.LENGTH_SHORT).show();
//        copyDatabase();

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

//        globalCode.;
        globalCode.writeFileOnInternalStorage();

    }       // end onCreate()


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "Resumed");
        // on resume load Decks Fragment by default
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DecksFragment()).commit();
    }

    private void bindVariables() {
//        signInButton = findViewById(R.id.sign_in_button);

        mycontext = this;

        // side navigation tabs
        mi_resetdecks = navigationView.getMenu().getItem(0);
        mi_exporttoexcel = navigationView.getMenu().getItem(1);
        mi_backup = navigationView.getMenu().getItem(2);
        mi_restore = navigationView.getMenu().getItem(3);
        mi_about = navigationView.getMenu().getItem(4);

        tvMadeBy = navigationView.findViewById(R.id.tv_madeby);

        // Bottom navigation tabs
        mi_decks = bottomNavigationView.getMenu().getItem(0);
        mi_practice = bottomNavigationView.getMenu().getItem(1);
        mi_progress = bottomNavigationView.getMenu().getItem(2);

        // Side Navbar Item
        View sideNav = navigationView.getHeaderView(0);
        tvUsername = sideNav.findViewById(R.id.tv_username);
        tvEmail = sideNav.findViewById(R.id.tv_user_mail);
        ivProfileIcon = sideNav.findViewById(R.id.iv_profile_icon);

//        Toast.makeText(this, "Username textview set", Toast.LENGTH_SHORT).show();
    }

    private void bindEvents() {

//        signInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                signIn();
//            }
//        });

        tvMadeBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.linkedin.com/in/akshaychavan7/"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.nv_add_deck:
                        openAddNewDeckIntent();
                        break;
                    case R.id.nv_reset:
                        Toast.makeText(MainActivity.this, "Reset clicked!", Toast.LENGTH_SHORT).show();
                        MyDatabaseHelper myDatabaseHelper = MyDatabaseHelper.getInstance(MainActivity.this);

                        SQLiteDatabase db = myDatabaseHelper.getDatabase();

                        String query = "UPDATE " + myDatabaseHelper.getTableName() + " set Last_Five_Scores = \"0,0,0,1,1\", Score=\"2\";";

//                        db.rawQuery(query, null);
                        db.execSQL(query);


                        //refresh
                        finish();
                        startActivity(getIntent());
                        break;
                    case R.id.nv_export:
                        break;
                    case R.id.nv_backup:
                        break;
                    case R.id.nv_restore:
                        break;
                    case R.id.nv_about:
                        break;
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


    private boolean copyDatabase() {
        try {
            InputStream inputStream = getAssets().open(MyDatabaseHelper.DATABASE_NAME);
            String outFileName = MyDatabaseHelper.DB_LOCATION + MyDatabaseHelper.DATABASE_NAME;
            OutputStream outputStream = new FileOutputStream(outFileName);

            byte[] buff = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buff)) > 0) {
                outputStream.write(buff, 0, length);
                Log.e(TAG, buff.toString());
            }

            outputStream.flush();
            outputStream.close();

            Log.e(TAG, "DB copied successfully!");

            return true;


        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error while copying DB >>" + e.getMessage());
            return false;
        }
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nv_reset:
                Toast.makeText(MainActivity.this, "Reset clicked!", Toast.LENGTH_SHORT).show();
//                MyDatabaseHelper myDatabaseHelper = MyDatabaseHelper.getInstance(MainActivity.this);
//
//                SQLiteDatabase db = myDatabaseHelper.getDatabase();
//
//                String query  = "DELETE FROM "+myDatabaseHelper.getTableName();
//
//                db.rawQuery(query, null);
//
//
//                //refresh
//                finish();
//                startActivity(getIntent());
                break;
        }

        return super.onOptionsItemSelected(item);
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

        // for image picker
        if (resultCode == RESULT_OK && data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES) != null) {
            ArrayList<MediaFile> mediaFiles = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);

            String path = mediaFiles.get(0).getPath();
            globalCode.setImagePath(path);
//            Toast.makeText(MainActivity.this, "Path >>"+path, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
//            updateUI(account);
//            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
            getUserAccountInfo();
        } catch (ApiException e) {
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
            if (e.getStatusCode() == 12501) {
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
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DecksFragment(MainActivity.this)).commit();
                break;
            case R.id.nv_practice:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PracticeFragment()).commit();
                break;
            case R.id.nv_progress:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProgressFragment()).commit();
                break;

            case R.id.nv_reset:
                Toast.makeText(mycontext, "Reset clicked!", Toast.LENGTH_SHORT).show();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    public void openAddNewDeckIntent() {
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View view = li.inflate(R.layout.create_new_deck_intent_layout, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.CustomDialog);
        alertDialogBuilder.setView(view);
//        alertDialogBuilder.create();
//        alertDialogBuilder.show();

        addNewDeckPopup = alertDialogBuilder.create();
        addNewDeckPopup.show();


        // binding popup variables and events

        EditText etNewDeckName = view.findViewById(R.id.et_new_deck_name);
        Button addNewDeck = view.findViewById(R.id.add_cards);
        Button cancel = view.findViewById(R.id.cancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewDeckPopup.dismiss();
            }
        });

        addNewDeck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newDeckName = etNewDeckName.getText().toString().trim();
                openAddNewCardIntent();
            }
        });

    }           // end openAddNewDeckIntent()

    public void openAddNewCardIntent() {
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View view = li.inflate(R.layout.add_card_to_deck_layout, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.CustomDialog);
        alertDialogBuilder.setView(view);
//        alertDialogBuilder.create();
//        alertDialogBuilder.show();

        addNewCardPopup = alertDialogBuilder.create();
        addNewCardPopup.show();

        bindNewCardVariables(view);
        bindNewCardEvents(view);
    }       // openAddNewCardIntent()


    public void bindNewCardVariables(View v) {
        ivImageBrowser = v.findViewById(R.id.iv_image_browser);
        etImagePath = v.findViewById(R.id.et_image_path);
        //Making this image path edittext global
        globalCode.setEtcardImagePath(etImagePath);

        verbBtn = v.findViewById(R.id.verb_btn);
        adjBtn = v.findViewById(R.id.adj_btn);
        nounBtn = v.findViewById(R.id.noun_btn);
        idiomBtn = v.findViewById(R.id.idiom_btn);

        etWord = v.findViewById(R.id.et_word);
        etWordDefinition = v.findViewById(R.id.et_word_definition);
        etWordSynonyms = v.findViewById(R.id.et_word_synonyms);
        etWordExample = v.findViewById(R.id.et_word_examples);
        etWordMnemonic = v.findViewById(R.id.et_word_mnemonic);

        progressBar = v.findViewById(R.id.progressBar);

        tvSaveCard = v.findViewById(R.id.tv_save_card);
        tvCancelCard = v.findViewById(R.id.tv_cancel_card);
        tvAddNextCard = v.findViewById(R.id.tv_add_next_card);


    }

    public void bindNewCardEvents(View v) {
        ivImageBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check for camera permissions, if not granted then ask for permissions
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                } else {          // when permission is granted
                    imagePicker();

                }
            }
        });

        verbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWordClassButton("verb");
            }
        });

        adjBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWordClassButton("adjective");
            }
        });

        nounBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWordClassButton("noun");
            }
        });

        idiomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWordClassButton("idiom");

            }
        });

        etWord.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String word = etWord.getText().toString();
                if ((!hasFocus) && word.length() > 0) {
//                    Toast.makeText(mContext, "Got the focus", Toast.LENGTH_LONG).show();
                    getWordDetails(word);
                }
            }
        });

        tvSaveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCard();
//                if (saveCard(position))
//                    addNewCardPopup.dismiss();


                // refresh fragment to see the changes    NOTE: here we are opening the MainActivity again to see the changes
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        tvCancelCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewCardPopup.dismiss();
            }
        });

        tvAddNextCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save info first
                saveCard();

                // then clear the input fields for new input
                etWord.setText("");
                etWordDefinition.setText("");
                etWordSynonyms.setText("");
                etWordExample.setText("");
                etWordMnemonic.setText("");
                etImagePath.setText("");

                selectedWordClass = null;

            }
        });
    }           // end bindNewCardVariables()


    // to save card info into DB
    public void saveCard() {

        if (!validateFields())
            return;

        CardPojo card = new CardPojo();

        card.setWord(etWord.getText().toString());
        card.setClass_(selectedWordClass);
        card.setMeaning(etWordDefinition.getText().toString());
        card.setSynonyms(etWordSynonyms.getText().toString());
        card.setExample(etWordExample.getText().toString());
        card.setMnemonic(etWordMnemonic.getText().toString());
        card.setImageURL(etImagePath.getText().toString());

        MyDatabaseHelper myDB = MyDatabaseHelper.getInstance(MainActivity.this);


        myDB.addCardToDeck(newDeckName, card);

    }

    // to check if required fields are blank or not
    public boolean validateFields() {
        if (etWord.getText().length() == 0 || etWordDefinition.getText().length() == 0 || selectedWordClass == null) {
            Toast.makeText(MainActivity.this, "Word, Part of Speech Class and Definition are required fields!\nMake sure you input all the required fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    public void setWordClassButton(String wordClass) {
        switch (wordClass) {
            case "verb":
                verbBtn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#F44336")));
                verbBtn.setStrokeWidth(2);

                adjBtn.setStrokeWidth(0);
                nounBtn.setStrokeWidth(0);
                idiomBtn.setStrokeWidth(0);

                selectedWordClass = "verb";
                break;
            case "adjective":
                adjBtn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#F44336")));
                adjBtn.setStrokeWidth(2);

                verbBtn.setStrokeWidth(0);
                nounBtn.setStrokeWidth(0);
                idiomBtn.setStrokeWidth(0);

                selectedWordClass = "adjective";
                break;
            case "noun":
                nounBtn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#F44336")));
                nounBtn.setStrokeWidth(2);

                verbBtn.setStrokeWidth(0);
                adjBtn.setStrokeWidth(0);
                idiomBtn.setStrokeWidth(0);

                selectedWordClass = "noun";
                break;
            case "idiom":
                idiomBtn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#F44336")));
                idiomBtn.setStrokeWidth(2);

                verbBtn.setStrokeWidth(0);
                adjBtn.setStrokeWidth(0);
                nounBtn.setStrokeWidth(0);

                selectedWordClass = "idiom";
                break;
        }
    }

    public void imagePicker() {
        Intent intent = new Intent(MainActivity.this, FilePickerActivity.class);

        intent.putExtra(FilePickerActivity.CONFIGS,
                new Configurations.Builder()
                        .setCheckPermission(true)
                        .setShowImages(true)
                        .enableImageCapture(true)
                        .setMaxSelection(1)
                        .setSkipZeroSizeFiles(true)
                        .build());

        startActivityForResult(intent, 101);
    }

    public void getWordDetails(String word) {
        progressBar.setVisibility(View.VISIBLE);


        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);


        Call<List<WordDataPojo>> call = apiInterface.getWordData(word);

        call.enqueue(new Callback<List<WordDataPojo>>() {
            @Override
            public void onResponse(Call<List<WordDataPojo>> call, Response<List<WordDataPojo>> response) {
                Log.e(TAG, "Response Code -> " + response.code());
                if (response.isSuccessful()) {
                    Definition wordDetails = response.body().get(0).getMeanings().get(0).getDefinitions().get(0);
                    Log.e(TAG, wordDetails.getDefinition());

                    etWordDefinition.setText(wordDetails.getDefinition());

                    //Toast.makeText(mContext, "Class>>"+response.body().get(0).getMeanings().get(0).getPartOfSpeech().toLowerCase(), Toast.LENGTH_SHORT).show();
                    String wordClass = response.body().get(0).getMeanings().get(0).getPartOfSpeech().toLowerCase();
                    if (wordClass.contains("verb")) {
                        wordClass = "verb";
                    }

                    setWordClassButton(wordClass);

                    String synonyms = "";
                    if (wordDetails.getSynonyms() != null) {
                        for (String synonym : wordDetails.getSynonyms()) {
                            synonyms = synonyms + synonym + ", ";
                        }
                        synonyms = synonyms.substring(0, synonyms.length() - 2);
                    }
                    etWordSynonyms.setText(synonyms);

                    etWordExample.setText(wordDetails.getExample());


                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(MainActivity.this, "Word not found!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    // Set details empty if word not found
                    etWordDefinition.setText("");
                    etWordSynonyms.setText("");
                    etWordExample.setText("");
                    verbBtn.setStrokeWidth(0);
                    adjBtn.setStrokeWidth(0);
                    nounBtn.setStrokeWidth(0);
                    idiomBtn.setStrokeWidth(0);
                    Log.e(TAG, response.message());
                }
            }

            @Override
            public void onFailure(Call<List<WordDataPojo>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Something went wrong!\n>>" + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Something went wrong >>" + t.getMessage());
            }
        });

    }

}