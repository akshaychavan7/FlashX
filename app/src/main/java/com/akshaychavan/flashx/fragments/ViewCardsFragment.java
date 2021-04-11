package com.akshaychavan.flashx.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        cardsListAdapter = new CardsListAdapter(deckCardsList, getContext());     // by default shares should be loaded
        cardsListRecycler.setLayoutManager(cardsListLayoutManager);
        cardsListRecycler.setAdapter(cardsListAdapter);

    }       // end performFragmentWork()


    public void getDeckCards() {
        MyDatabaseHelper myDatabaseHelper = MyDatabaseHelper.getInstance(mContext);

        SQLiteDatabase db = myDatabaseHelper.getDatabase();

        // Query ==> SELECT * FROM Words_List where Deck_Name = "Barron's 1100";
        String query  = "SELECT * FROM Words_List where Deck_Name = \""+deckName+"\"";

        Cursor cursor = db.rawQuery(query, null);


        //                  0         1         2         3           4          5           6           7          8            9
        // DB Columns ==> "_id", "Deck_Name", "Word", "Definition", "Class", "Synonyms", "Examples", "Mnemonic", "Image_URL", "Is_Mastered"


        while (cursor.moveToNext()) {
            CardPojo cardPojo = new CardPojo();

            cardPojo.setWord(cursor.getString(2));
            cardPojo.setClass_(cursor.getString(4));
            cardPojo.setMeaning(cursor.getString(3));
            cardPojo.setExample(cursor.getString(6));
            cardPojo.setSynonyms(cursor.getString(5));
            cardPojo.setMnemonic(cursor.getString(7));
            cardPojo.setImageURL(cursor.getString(8));
            cardPojo.setIsMastered(cursor.getString(9));

            deckCardsList.add(cardPojo);
        }
    }


}