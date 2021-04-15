package com.akshaychavan.flashx.activities;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.akshaychavan.flashx.R;
import com.akshaychavan.flashx.pojo.CardPojo;
import com.akshaychavan.flashx.utility.GlobalCode;
import com.akshaychavan.flashx.utility.MyDatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

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
    TextView tvFrontWord, tvWord, tvWordClass, tvWordDescription, tvSynonyms, tvExample, tvMnemonic;
    TextView tvKnownBtn, tvNotKnownBtn;
    MaterialButton btnFrontLevel;
    ImageView ivWordImage;
    View cardClassColor;
    //////////////////////////////////////////////////////////
    MyDatabaseHelper myDatabaseHelper = MyDatabaseHelper.getInstance(PracticeActivity.this);
    //////////////////////////////////////////////////////////
    SQLiteDatabase db = myDatabaseHelper.getDatabase();
    //Decks List
    ArrayList<CardPojo> deckCardsList = new ArrayList<>();
    String currentScore = null;

    //////////////////////////////////////////////////////////

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
        practiceCard = findViewById(R.id.practice_card);
        flCard = findViewById(R.id.fl_card);
        flCardDetails = findViewById(R.id.fl_card_details);
        ibBack = findViewById(R.id.ib_back);


        // card info variables
        btnFrontLevel = findViewById(R.id.btn_front_level);
        tvFrontWord = findViewById(R.id.tv_front_word);
        tvWord = findViewById(R.id.tv_word);
        tvWordClass = findViewById(R.id.tv_word_class);
        tvWordDescription = findViewById(R.id.tv_word_description);
        tvSynonyms = findViewById(R.id.tv_synonyms);
        tvExample = findViewById(R.id.tv_example);
        tvMnemonic = findViewById(R.id.tv_mnemonic);
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
            }
        });

    }       // end bindEvents()


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
            Log.e(TAG, c.getWord() + " " + c.getLastFiveScores());

            // update in database
            String query = "UPDATE Words_List SET _id = " + tempID + ", Last_Five_Scores = \"" + c.getLastFiveScores() + "\", Score = " + quality + " WHERE Word = \"" + c.getWord() + "\" AND Deck_Name = \""+ deckName +"\";";
            Log.e(TAG, "Query >>" + query);

            db.execSQL(query);

            tempID += 1;
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

        // Setting color of the cardside
        GradientDrawable backgroundGradient = (GradientDrawable) cardClassColor.getBackground();
        backgroundGradient.setColor(GlobalCode.getInstance().getWordClassColor(card.getClass_()));
    }


}