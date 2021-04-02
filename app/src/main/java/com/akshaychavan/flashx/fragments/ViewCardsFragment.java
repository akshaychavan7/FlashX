package com.akshaychavan.flashx.fragments;

import android.content.Context;
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
import com.akshaychavan.flashx.utility.GlobalCode;

import java.util.ArrayList;

public class ViewCardsFragment extends Fragment {

    private Context mContext;
    GlobalCode globalCode = GlobalCode.getInstance();
    String deckName;
    ArrayList<CardPojo> deckCardsList;

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

        deckCardsList = globalCode.getDeckCards(deckName);

        // Passing data to Adapter
        cardsListAdapter = new CardsListAdapter(deckCardsList, getContext());     // by default shares should be loaded
        cardsListRecycler.setLayoutManager(cardsListLayoutManager);
        cardsListRecycler.setAdapter(cardsListAdapter);

    }       // end performFragmentWork()

}