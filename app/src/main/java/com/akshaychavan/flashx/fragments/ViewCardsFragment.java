package com.akshaychavan.flashx.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akshaychavan.flashx.R;
import com.akshaychavan.flashx.adapters.CardsListAdapter;
import com.akshaychavan.flashx.adapters.DecksListAdapter;
import com.akshaychavan.flashx.pojo.CardPojo;
import com.akshaychavan.flashx.pojo.DeckPojo;
import com.akshaychavan.flashx.utility.GlobalCode;
import com.akshaychavan.flashx.utility.MyDatabaseHelper;

import java.util.ArrayList;

public class ViewCardsFragment extends Fragment {

    private Context mContext;
    GlobalCode globalCode = GlobalCode.getInstance();
    String deckName;
    ArrayList<CardPojo> deckCardsList = new ArrayList<>();

    // Adapter & Recycler
    RecyclerView cardsListRecycler;
    RecyclerView.LayoutManager cardsListLayoutManager;
    RecyclerView.Adapter cardsListAdapter;

    private final String TAG = "ViewCardsFragment";


    public ViewCardsFragment() {
        // Required empty public constructor
    }

    public ViewCardsFragment(Context context, String deckName) {
        mContext = context;
        this.deckName = deckName;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_view_cards, container, false);

        bindVariables(view);
        bindEvents();

        performFragmentWork();

        return view;
    }



    public void bindVariables(View view) {

        // Setting up adapters
        cardsListRecycler = view.findViewById(R.id.rv_deck_cards);
        cardsListRecycler.setHasFixedSize(true);
        cardsListLayoutManager = new LinearLayoutManager(getContext());

    }       // end bindVariables()

    public void bindEvents() {

    }       // end bindEvents()


    public void performFragmentWork() {

//        deckCardsList = globalCode.getDeckCards(deckName);


        getDeckCards();


        // Passing data to Adapter
        cardsListAdapter = new CardsListAdapter(deckCardsList, getContext(), ViewCardsFragment.this);     // by default shares should be loaded
        cardsListRecycler.setLayoutManager(cardsListLayoutManager);
        cardsListRecycler.setAdapter(cardsListAdapter);

    }       // end performFragmentWork()


    public void getDeckCards() {
        MyDatabaseHelper myDatabaseHelper = MyDatabaseHelper.getInstance(mContext);

        SQLiteDatabase db = myDatabaseHelper.getDatabase();

        // Query ==> SELECT * FROM Words_List where Deck_Name = "Barron's 1100";
        String query  = "SELECT * FROM Words_List where Deck_Name = \""+deckName+"\"";

        Log.e(TAG, "Query>>"+query);

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
    }       // end getDeckCards()


}