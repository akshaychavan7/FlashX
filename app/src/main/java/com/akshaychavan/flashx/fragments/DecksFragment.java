package com.akshaychavan.flashx.fragments;

import android.content.Context;
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
        globalCode.fetchDecksFromLocalStorage();



        for(String deckName: globalCode.getDecksNamesList()) {
            decksList.add(new DeckPojo(deckName, globalCode.getMasteredWordsCount(deckName), globalCode.getDeckCards(deckName).size()));
        }


        // Passing data to Adapter
        decksListAdapter = new DecksListAdapter(decksList, getContext());     // by default shares should be loaded
        decksListRecycler.setLayoutManager(decksListLayoutManager);
        decksListRecycler.setAdapter(decksListAdapter);

    }       // end performFragmentWork()

}