package com.akshaychavan.flashx.utility;

import android.content.Context;
import android.graphics.Color;
import android.widget.EditText;

import com.akshaychavan.flashx.activities.MainActivity;
import com.akshaychavan.flashx.pojo.CardPojo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Akshay Chavan on 02,April,2021
 */

public class GlobalCode {

    private static GlobalCode mInstance = null;
    ArrayList<ArrayList<CardPojo>> decksList = new ArrayList<>();
    JSONObject deckIndex = new JSONObject();
    ArrayList<String> decksNamesList = new ArrayList<>();
    Context mContext;
    String imagePath = null;
    EditText etcardImagePath;


    String colors[] = {"#2196F3", "#7E57C2", "#43A047","#F44336", "#4DD0E1", "#FF9800", "#7E57C2", "#AD1457", "#FFEA00"};

    public GlobalCode(Context context) {
        mContext = context;
    }

    public EditText getEtcardImagePath() {
        return etcardImagePath;
    }

    public void setEtcardImagePath(EditText etcardImagePath) {
        this.etcardImagePath = etcardImagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
        this.etcardImagePath.setText(this.imagePath);
    }

    public GlobalCode() {
    }

    public static synchronized GlobalCode getInstance() {
        if (null == mInstance) {
            mInstance = new GlobalCode();
        }
        return mInstance;
    }

    public int getColor(int i) {
        return Color.parseColor(colors[i % colors.length]);
    }


    public ArrayList<String> getDecksNamesList() {
        return decksNamesList;
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = MainActivity.mycontext.getAssets().open("data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void fetchDecksFromLocalStorage() {
        Gson gson = new Gson();
        Type cardType = new TypeToken<ArrayList<CardPojo>>() {
        }.getType();

        decksNamesList.clear();

        try {
            JSONObject jsonObject = new JSONObject(loadJSONFromAsset());

            int i = 0;
            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                String key = it.next();

                JSONArray jsonArray = jsonObject.getJSONArray(key);

                decksNamesList.add(key);
                decksList.add((ArrayList<CardPojo>) gson.fromJson(jsonArray.toString(), cardType));         // this will store decks
                deckIndex.put(key, i++);                                                                    // this will store key's index ie. decks name and it's index
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<CardPojo> getDeckCards(String deckName) {
        try {
            return decksList.get(deckIndex.getInt(deckName));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getMasteredWordsCount(String deckName) {
        int count = 0;

        for (CardPojo card : getDeckCards(deckName)) {
            if (card.getIsMastered().equalsIgnoreCase("Yes")) {
                count++;
            }
        }
        return count;
    }

}

