package com.akshaychavan.flashx.pojo;

/**
 * Created by Akshay Chavan on 02,April,2021
 * akshay.chavan@finiq.com
 * FinIQ Consulting India
 */
public class DeckPojo {
    String deckTitle;
    int masteredWordsCount, deckCardsCount;

    public DeckPojo(String deckTitle, int masteredWordsCount, int deckCardsCount) {
        this.deckTitle = deckTitle;
        this.masteredWordsCount = masteredWordsCount;
        this.deckCardsCount = deckCardsCount;
    }

    public int getDeckCardsCount() {
        return deckCardsCount;
    }

    public void setDeckCardsCount(int deckCardsCount) {
        this.deckCardsCount = deckCardsCount;
    }



    public String getDeckTitle() {
        return deckTitle;
    }

    public void setDeckTitle(String deckTitle) {
        this.deckTitle = deckTitle;
    }

    public int getMasteredWordsCount() {
        return masteredWordsCount;
    }

    public void setMasteredWordsCount(int masteredWordsCount) {
        this.masteredWordsCount = masteredWordsCount;
    }


}
