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
import com.akshaychavan.flashx.activities.MainActivity;
import com.akshaychavan.flashx.adapters.DecksListAdapter;
import com.akshaychavan.flashx.pojo.DeckPojo;
import com.akshaychavan.flashx.utility.GlobalCode;
import com.akshaychavan.flashx.utility.MyDatabaseHelper;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DecksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DecksFragment extends Fragment {

    private Context mContext;
    private final String TAG = "DecksFragment";

    // Adapter & Recycler
    RecyclerView decksListRecycler;
    RecyclerView.LayoutManager decksListLayoutManager;
    RecyclerView.Adapter decksListAdapter;

    // GlobalCode
    GlobalCode globalCode;

    //Decks List
    ArrayList<DeckPojo> decksList =  new ArrayList<>();

    public DecksFragment() {
        // Required empty public constructor
    }

    public DecksFragment(Context context) {
        mContext = context;
    }

    public static DecksFragment newInstance() {
        DecksFragment fragment = new DecksFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_decks, container, false);

        bindVariables(view);
        bindEvents();

        performFragmentWork();

        return view;

    }




    public void bindVariables(View view) {
         globalCode = GlobalCode.getInstance();

        // Setting up adapters
        decksListRecycler = view.findViewById(R.id.rv_decks);
        decksListRecycler.setHasFixedSize(true);
        decksListLayoutManager = new LinearLayoutManager(getContext());
    }       // end bindVariables()

    public void bindEvents() {

    }       // end bindEvents()

    public void performFragmentWork() {

        // Firstly, fetch all the decks from local storage
//        globalCode.fetchDecksFromLocalStorage();
//
//
//
//        for(String deckName: globalCode.getDecksNamesList()) {
//            decksList.add(new DeckPojo(deckName, globalCode.getMasteredWordsCount(deckName), globalCode.getDeckCards(deckName).size()));
//        }

        MyDatabaseHelper myDatabaseHelper = MyDatabaseHelper.getInstance(mContext);

        SQLiteDatabase db = myDatabaseHelper.getDatabase();

        // Query ==> SELECT Deck_Name, COUNT(Deck_Name) FROM Words_List GROUP BY Deck_Name;                                         ==> this will return deckname and it's total cards count
        // Query ==> SELECT Deck_Name, COUNT(Deck_Name) from "Words_List" WHERE Is_Mastered="Yes" GROUP BY Deck_Name;               ==> this will return deckname and it's mastered cards count

        String query = "SELECT T1.Deck_Name,T2.cnt Total_cnt, T1.cnt Mastered_cnt" +
                "   FROM (SELECT Deck_Name, COUNT(Deck_Name) cnt " +
                "FROM Words_List WHERE Is_Mastered=\"Yes\" GROUP BY Deck_Name " +
                "UNION " +
                "SELECT Deck_Name, 0 cnt FROM Words_List W1 " +
                "WHERE \"Yes\" NOT IN (SELECT Is_Mastered From Words_List WHERE W1.Deck_Name = Deck_Name ) GROUP BY Deck_Name) T1," +
                " (SELECT Deck_Name, COUNT(Deck_Name) cnt FROM Words_List GROUP BY Deck_Name)T2 " +
                "WHERE T1.Deck_Name = T2.Deck_Name;";

        Log.e(TAG, "Query>>"+query);
//        Log.e(TAG, "Query2>>"+query2);

        Cursor cursor = db.rawQuery(query, null);
//        Cursor cursor2 = db.rawQuery(query2, null);



        // DB Columns ==> "_id", "Deck_Name", "Word", "Definition", "Class", "Synonyms", "Examples", "Mnemonic", "Image_URL", "Is_Mastered"

        while (cursor.moveToNext()) {
//            cursor2.moveToNext();       // move cursor2 along with cursor1

            String deckname = cursor.getString(0);
            int totalCardsCount = cursor.getInt(1);
            int masteredWordsCount = cursor.getInt(2);
//            if(cursor2.getCount() ==0) {
//                masteredWordsCount = 0;
//            }
//            else {
//                masteredWordsCount = cursor2.getInt(1);
//            }
            decksList.add(new DeckPojo(deckname, masteredWordsCount, totalCardsCount));
        }

        // Passing data to Adapter
        decksListAdapter = new DecksListAdapter(decksList, getContext());     // by default shares should be loaded
        decksListRecycler.setLayoutManager(decksListLayoutManager);
        decksListRecycler.setAdapter(decksListAdapter);

    }       // end performFragmentWork()

}