package com.akshaychavan.flashx.activities;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.akshaychavan.flashx.R;
import com.akshaychavan.flashx.pojo.CardPojo;
import com.akshaychavan.flashx.pojo.Definition;
import com.akshaychavan.flashx.pojo.WordDataPojo;
import com.akshaychavan.flashx.utility.ApiClient;
import com.akshaychavan.flashx.utility.ApiInterface;
import com.akshaychavan.flashx.utility.GlobalCode;
import com.akshaychavan.flashx.utility.MyDatabaseHelper;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PracticeActivity extends AppCompatActivity {

    final String SELECTED_DECK_KEY = "Selected_Deck";
    final int HARD_SHIFT = 5, MEDIUM_SHIFT = 10, EASY_SHIFT = 15;
    private final String TAG = "PracticeActivity";
    String deckName = null;
    MaterialCardView practiceCard;
    FrameLayout flCard, flCardDetails;
    Animation animation;
    MediaPlayer mp;
    ImageButton ibBack;
    TextView tvFrontWord, tvWord, tvWordClass, tvWordDescription, tvSynonyms, tvExample, tvMnemonic, tvMasteredCount, tvReviewingCount, tvLearningCount, tvTotalCount;
    ProgressBar masteredProgressBar, reviewingProgressBar, learningProgressBar, totalProgressBar;
    TextView tvKnownBtn, tvNotKnownBtn;
    ImageView ivTextToSpeech, ivPracticeOptions;
    TextToSpeech textToSpeech;
    MaterialButton btnFrontLevel;
    ImageView ivWordImage;
    View cardClassColor;
    Animation slideInAnimation;
    //////////////////////////////////////////////////////////
    MyDatabaseHelper myDatabaseHelper = MyDatabaseHelper.getInstance(PracticeActivity.this);
    //////////////////////////////////////////////////////////
    SQLiteDatabase db = myDatabaseHelper.getDatabase();
    //Decks List
    ArrayList<CardPojo> deckCardsList = new ArrayList<>();
    String currentScore = null;

    //////////////////////////////////////////////////////////

    GlobalCode globalCode = GlobalCode.getInstance();

    //////////////////////// Edit card popup vairables //////////////////////
    ImageView ivImageBrowser;
    EditText etImagePath, etWord, etWordDefinition, etWordSynonyms, etWordExample, etWordMnemonic;
    TextView tvSaveCard, tvCancelCard, tvAddNextCard;
    MaterialButton verbBtn, adjBtn, nounBtn, idiomBtn;
    ProgressBar progressBar;
    String selectedWordClass;
    AlertDialog editCardPopup;
    //////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        Intent intent = getIntent();
        deckName = intent.getStringExtra(SELECTED_DECK_KEY);

        Log.e(TAG, "Received deckname>>" + deckName);

        bindVariables();
        bindEvents();

        fetchDeckCards();
        setValues();
    }       // end onCreate()


    public void bindVariables() {
        slideInAnimation = AnimationUtils.loadAnimation(PracticeActivity.this, R.anim.slide_in_right);

        practiceCard = findViewById(R.id.practice_card);
        flCard = findViewById(R.id.fl_card);
        flCardDetails = findViewById(R.id.fl_card_details);
        ibBack = findViewById(R.id.ib_back);
        ivPracticeOptions = findViewById(R.id.iv_practice_options);

        // card info variables
        btnFrontLevel = findViewById(R.id.btn_front_level);
        tvFrontWord = findViewById(R.id.tv_front_word);
        tvWord = findViewById(R.id.tv_word);
        tvWordClass = findViewById(R.id.tv_word_class);
        tvWordDescription = findViewById(R.id.tv_word_description);
        tvSynonyms = findViewById(R.id.tv_synonyms);
        tvExample = findViewById(R.id.tv_example);
        tvMnemonic = findViewById(R.id.tv_mnemonic);
        tvMasteredCount = findViewById(R.id.tv_mastered_count);
        tvReviewingCount = findViewById(R.id.tv_reviewing_count);
        tvLearningCount = findViewById(R.id.tv_learning_count);
        tvTotalCount = findViewById(R.id.tv_total_count);

        ivTextToSpeech = findViewById(R.id.iv_text_to_speech);

        masteredProgressBar = findViewById(R.id.progressBar);
        reviewingProgressBar = findViewById(R.id.progressBar2);
        learningProgressBar = findViewById(R.id.progressBar3);
        totalProgressBar = findViewById(R.id.progressBar4);

        ivWordImage = findViewById(R.id.iv_word_image);
        cardClassColor = findViewById(R.id.v_card_class_color);

        tvKnownBtn = findViewById(R.id.tv_known);
        tvNotKnownBtn = findViewById(R.id.tv_not_known);


    }       // end bindVariables()

    public void bindEvents() {

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        practiceCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // run card flip animation
                AnimatorSet anim = (AnimatorSet) AnimatorInflater.loadAnimator(PracticeActivity.this, R.animator.flipping);
                anim.setTarget(practiceCard);
                anim.setDuration(300);
                anim.start();

                // run card flip sound
                mp = MediaPlayer.create(PracticeActivity.this, R.raw.card_flip_sound2);
                try {
                    if (mp.isPlaying()) {
                        mp.stop();
                        mp.release();
                        mp = MediaPlayer.create(PracticeActivity.this, R.raw.card_flip_sound2);
                    }
                    mp.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                // after flip, switch between details page
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (flCard.getVisibility() == View.VISIBLE) {
                            flCard.setVisibility(View.GONE);
                            flCardDetails.setVisibility(View.VISIBLE);
                        } else {
                            flCard.setVisibility(View.VISIBLE);
                            flCardDetails.setVisibility(View.GONE);
                        }
                    }
                }, 150);            // when card is flipped half, then switch between non-details card with the details card

            }
        });

        tvKnownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                currentScore = "1";         // quality score should not increase if it's correct
                CardPojo card = deckCardsList.get(0);
                // update the value for last five scores
                String lastFiveScores = card.getLastFiveScores();
                lastFiveScores = lastFiveScores.substring(2, lastFiveScores.length()) + ",1";
                card.setLastFiveScores(lastFiveScores);


                int quality = 0;

                // calculating quality as a sum of past five scores
                for (String score : card.getLastFiveScores().split(",")) {
                    quality += Integer.parseInt(score);
                }

                Log.e(TAG, "Quality>>" + quality + " Last Five Scores>>" + card.getLastFiveScores());
                performSpacedRepetition(quality);
                setValues();

                flCard.setVisibility(View.VISIBLE);
                flCardDetails.setVisibility(View.GONE);

                practiceCard.startAnimation(slideInAnimation);
            }
        });

        tvNotKnownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardPojo card = deckCardsList.get(0);

                // update the value for last five scores
                String lastFiveScores = card.getLastFiveScores();
//                lastFiveScores = lastFiveScores.substring(2, lastFiveScores.length()) + ",0";
                lastFiveScores = "0,0,0,0,0";
                card.setLastFiveScores(lastFiveScores);


                int quality = 0;

                // if answer is incorrect then directly pass quality as 0
                performSpacedRepetition(quality);
                setValues();

                flCard.setVisibility(View.VISIBLE);
                flCardDetails.setVisibility(View.GONE);

                practiceCard.startAnimation(slideInAnimation);
            }
        });


        // setup text to speech event
        textToSpeech = new TextToSpeech(PracticeActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.ENGLISH);

                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                        Toast.makeText(PracticeActivity.this, "Language not supported for text to speech!", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Language not supported for text to speech!");
                    } else{
                        ivTextToSpeech.setEnabled(true);
                    }
                } else {
                    Toast.makeText(PracticeActivity.this, "Text to Speech initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        ivTextToSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });


        ivPracticeOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsMenuPopup(v, 0);
            }
        });

    }       // end bindEvents()

    public void speak() {
        String word = tvWord.getText().toString();
        float pitch = 1f;
        float speed = 0.5f;

        textToSpeech.setPitch(pitch);
        textToSpeech.setSpeechRate(speed);

        textToSpeech.speak(word, TextToSpeech.QUEUE_ADD, null);

        String meaning= tvWordClass.getText()+",,,,"+tvWordDescription.getText();
        textToSpeech.speak(meaning, TextToSpeech.QUEUE_ADD, null);
    }


    @Override
    protected void onDestroy() {
        if(textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        super.onDestroy();
    }

    // NOTE: Initial default quality as 1,1,0,0,0 -> i.e. Medium
    // NOTE: HARD_SHIFT = 5, MEDIUM_SHIFT = 10, EASY_SHIFT = 15
    private void performSpacedRepetition(int quality) {


        int newPosition = 0;
        switch (quality) {
            case 0:                 // hard
                newPosition = Math.min(HARD_SHIFT, (int) Math.floor(deckCardsList.size() * 0.2));         // Min(5 OR 20% shift)
                break;
            case 1:                 // hard
                newPosition = Math.min(HARD_SHIFT + 2, (int) Math.floor(deckCardsList.size() * 0.35));         // Min(7 OR 35% shift)
                break;
            case 2:                 // Medium
                newPosition = Math.min(MEDIUM_SHIFT, (int) Math.floor(deckCardsList.size() * 0.5));         // Min(10 OR 50% shift)
                break;
            case 3:                 // Medium
                newPosition = Math.min(MEDIUM_SHIFT + 3, (int) Math.floor(deckCardsList.size() * 0.7));         // Min(13 OR 70% shift)
                break;
            case 4:                 // Easy
                newPosition = Math.min(EASY_SHIFT, (int) Math.floor(deckCardsList.size() * 0.8));         // Min(15 OR 80% shift)

                break;
            case 5:                 // Easy
                newPosition = Math.min(EASY_SHIFT + 5, (int) Math.floor(deckCardsList.size() * 0.95));         // Min(20 OR 95% shift)
                break;
        }

        // shifting card
        CardPojo card = deckCardsList.get(0);
        deckCardsList.remove(0);
        deckCardsList.add(newPosition, card);

        Log.e(TAG, "**************************************\nPrinting Card After Rearrangement");

        db = myDatabaseHelper.getDatabase();

        int tempID = 1;
        for (CardPojo c : deckCardsList) {


            int score = 0;
            // calculate score as sum of past five scores
            for(String s: c.getLastFiveScores().split(",")) {
                score+= Integer.parseInt(s);
            }

            // update in database
            String query = "UPDATE Words_List SET _id = " + tempID + ", Last_Five_Scores = \"" + c.getLastFiveScores() + "\", Score = " + score + " WHERE Word = \"" + c.getWord() + "\" AND Deck_Name = \""+ deckName +"\";";
//            Log.e(TAG, "Query >>" + query);

            db.execSQL(query);

            tempID += 1;

            Log.e(TAG, "Word: "+c.getWord() + " Last Five Score: " + c.getLastFiveScores()+" Score:"+score);
        }
    }

    public void fetchDeckCards() {


        // Query ==> SELECT * FROM Words_List where Deck_Name = "Barron's 1100" ORDER BY _id;
        String query = "SELECT * FROM Words_List where Deck_Name = \"" + deckName + "\"";

        Log.e(TAG, "Query>>" + query);

//        db = myDatabaseHelper.getDatabase();
        Cursor cursor = db.rawQuery(query, null);

//                 "_id"                                    0
//                "Deck_Name"                               1
//                "Word"                                    2
//                "Definition"                              3
//                "Class"                                   4
//                "Synonyms"                                5
//                "Examples"                                6
//                "Mnemonic"                                7
//                "Image_URL"                               8
//                "Last_Five_Scores"                        9
//                "Score"                                   10

        while (cursor.moveToNext()) {
            CardPojo cardPojo = new CardPojo();

            cardPojo.set_id(cursor.getInt(0));
            cardPojo.setWord(cursor.getString(2));
            cardPojo.setClass_(cursor.getString(4));
            cardPojo.setMeaning(cursor.getString(3));
            cardPojo.setExample(cursor.getString(6));
            cardPojo.setSynonyms(cursor.getString(5));
            cardPojo.setMnemonic(cursor.getString(7));
            cardPojo.setImageURL(cursor.getString(8));
            cardPojo.setLastFiveScores(cursor.getString(9));
            cardPojo.setScore(cursor.getInt(10));

            deckCardsList.add(cardPojo);
        }

//        db.close();
//        Log.e(TAG, "Word: "+deckCardsList.get(0).getWord());
//        return deckCardsList.get(0);        // always get the first card since the already practiced will be moved towards end
    }       // end getDeckCards()


    public void setValues() {
        //get first card
        CardPojo card = deckCardsList.get(0);

        switch (card.getScore()) {
            case 0:
                btnFrontLevel.setText("New Word");
                break;
            case 1:
            case 2:
                btnFrontLevel.setText("Learning");
                break;
            case 3:
            case 4:
                btnFrontLevel.setText("Reviewing");
                break;
            case 5:
                btnFrontLevel.setText("Mastered");
                break;
        }

        tvFrontWord.setText(card.getWord());
        tvWord.setText(card.getWord());
        tvWordClass.setText(card.getClass_() + ":");
        tvWordDescription.setText(card.getMeaning());
        tvSynonyms.setText(card.getSynonyms());
        tvExample.setText(card.getExample());
        tvMnemonic.setText(card.getMnemonic());

        if (card.getImageURL().contains("https") || card.getImageURL().contains("http"))       // check if image is from web or from local storage
        {
            Glide.with(PracticeActivity.this)
                    .load(card.getImageURL()) // image url
                    .placeholder(R.mipmap.loading_image) // any placeholder to load at start
                    .error(R.mipmap.image_not_found)  // any image in case of error
                    .override(200, 150) // resizing
                    .centerCrop()
                    .into(ivWordImage);  // imageview object
        } else {
            Uri uri = Uri.parse(card.getImageURL());
            ivWordImage.setImageURI(uri);
        }

        // Setting color of the cardside
        GradientDrawable backgroundGradient = (GradientDrawable) cardClassColor.getBackground();
        backgroundGradient.setColor(GlobalCode.getInstance().getWordClassColor(card.getClass_()));

        int masteredCount = 0, reviewingCount = 0, learningCount = 0;
        for(CardPojo c: deckCardsList) {
            switch (c.getScore()) {
                case 5:
                    masteredCount++;
                break;
                case 4:
                case 3:
                case 2:
                    reviewingCount++;
                    break;
                case 1:
                    learningCount++;
                    break;
            }
        }

        tvMasteredCount.setText(""+masteredCount);
        tvReviewingCount.setText(""+reviewingCount);
        tvLearningCount.setText(""+learningCount);
        tvTotalCount.setText(""+deckCardsList.size());

        masteredProgressBar.setProgress(masteredCount);
        reviewingProgressBar.setProgress(reviewingCount);
        learningProgressBar.setProgress(learningCount);
        totalProgressBar.setProgress(deckCardsList.size());


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    public void showOptionsMenuPopup(View v, int position) {
        PopupMenu popupMenu = new PopupMenu(PracticeActivity.this, v);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.card_list_option_edit:
                        openEditCardIntent(position);
                        break;
                    case R.id.card_list_option_delete:
                        // TODO: write delete card code here
                        break;
                }
                return true;
            }
        });

        popupMenu.inflate(R.menu.cards_list_options_menu);
        popupMenu.show();
    }


    public void openEditCardIntent(int cardPosition) {
        LayoutInflater li = LayoutInflater.from(PracticeActivity.this);
        View view = li.inflate(R.layout.edit_card_layout, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PracticeActivity.this, R.style.CustomDialog);
        alertDialogBuilder.setView(view);
//        alertDialogBuilder.create();
//        alertDialogBuilder.show();

        editCardPopup = alertDialogBuilder.create();
        editCardPopup.show();

        bindPopupVariables(view, cardPosition);
        bindPopupEvents(view, cardPosition);
    }


    public void bindPopupVariables(View v, int cardPosition) {
        ivImageBrowser = v.findViewById(R.id.iv_image_browser);
        etImagePath = v.findViewById(R.id.et_image_path);

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


        // IMP: setting previous values
        CardPojo card = deckCardsList.get(cardPosition);
        etWord.setText(card.getWord());
        etWordDefinition.setText(card.getMeaning());
        etWordSynonyms.setText(card.getSynonyms());
        etWordExample.setText(card.getExample());
        etWordMnemonic.setText(card.getMnemonic());
        etImagePath.setText(card.getImageURL());

        switch (card.getClass_()) {
            case "verb":
                setWordClassButton("verb");
                break;
            case "adjective":
                setWordClassButton("adjective");
                break;
            case "noun":
                setWordClassButton("noun");
                break;
            case "idiom":
                setWordClassButton("idiom");
                break;
        }
    }

    public void bindPopupEvents(View view, int cardPosition) {

        ivImageBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check for camera permissions, if not granted then ask for permissions
                if (ContextCompat.checkSelfPermission(PracticeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions( PracticeActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
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
                saveCard(cardPosition);
//                if (saveCard(position))
//                    addNewCardPopup.dismiss();


                // refresh fragment to see the changes    NOTE: here we are opening the MainActivity again to see the changes
                Intent intent = new Intent(PracticeActivity.this, MainActivity.class);
                PracticeActivity.this.startActivity(intent);

//                ((MainActivity)mContext).getSupportFragmentManager().beginTransaction().detach(currentFragment).attach(currentFragment).commit();
            }
        });

        tvCancelCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCardPopup.dismiss();
            }
        });

    }

    // to save card info into DB
    public void saveCard(int cardPosition) {

        if (!validateFields())
            return;

        CardPojo card = deckCardsList.get(cardPosition);

        String word = etWord.getText().toString();
        String wordClass = selectedWordClass;
        String wordDefinition = etWordDefinition.getText().toString();
        String synonyms = etWordSynonyms.getText().toString();
        String example = etWordExample.getText().toString();
        String mnemonic = etWordMnemonic.getText().toString();
        String url = etImagePath.getText().toString();

        MyDatabaseHelper myDatabaseHelper = MyDatabaseHelper.getInstance(PracticeActivity.this);
        SQLiteDatabase db = myDatabaseHelper.getDatabase();

        String query = "UPDATE Words_List SET Word = \"" + word + "\", Class = \"" + wordClass + "\", Definition = \"" + wordDefinition + "\", Synonyms = \"" + synonyms + "\", Examples = \"" + example + "\", Mnemonic = \"" + mnemonic + "\", Image_URL = \"" + url + "\"  WHERE _id = "+ card.get_id() +"  AND Word = \""+ card.getWord() +"\";";

        Log.e(TAG, "Query>>"+query);

        db.execSQL(query);

        editCardPopup.dismiss();
    }

    // to check if required fields are blank or not
    public boolean validateFields() {
        if (etWord.getText().length() == 0 || etWordDefinition.getText().length() == 0 || selectedWordClass == null) {
            Toast.makeText(PracticeActivity.this, "Word, Part of Speech Class and Definition are required fields!\nMake sure you input all the required fields", Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(PracticeActivity.this, FilePickerActivity.class);

        intent.putExtra(FilePickerActivity.CONFIGS,
                new Configurations.Builder()
                        .setCheckPermission(true)
                        .setShowImages(true)
                        .enableImageCapture(true)
                        .setMaxSelection(1)
                        .setSkipZeroSizeFiles(true)
                        .build());

        this.startActivityForResult(intent, 101);
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
                    Toast.makeText(PracticeActivity.this, "Word not found!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PracticeActivity.this, "Something went wrong!\n>>" + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Something went wrong >>" + t.getMessage());
            }
        });

    }


}