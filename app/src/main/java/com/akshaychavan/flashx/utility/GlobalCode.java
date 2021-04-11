package com.akshaychavan.flashx.utility;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.akshaychavan.flashx.activities.MainActivity;
import com.akshaychavan.flashx.pojo.CardPojo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

import static android.content.Context.MODE_PRIVATE;

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
    final String FILE_NAME = "data.json";
    final String TAG ="GlobalCode";


    String colors[] = {"#2196F3", "#7E57C2", "#43A047","#F44336", "#4DD0E1", "#FF9800", "#7E57C2", "#AD1457", "#FFEA00"};

    public GlobalCode(Context context) {
        mContext = context;
    }

    public GlobalCode() {
    }

    public static synchronized GlobalCode getInstance() {
        if (null == mInstance) {
            mInstance = new GlobalCode();
        }
        return mInstance;
    }

    public void setInstance(GlobalCode obj) {
       mInstance = obj;
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
            InputStream is = MainActivity.mycontext.getAssets().open(FILE_NAME);
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


    public String loadJSONFromLocalStorage() {
        FileInputStream fis = null;

        try {
            fis = mContext.openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine())!=null) {
                sb.append(text).append("\n");
            }

//            Log.e(TAG,"Read text>>>"+sb.toString());

            return sb.toString();

        } catch (FileNotFoundException e) {

            writeFileOnInternalStorage();           //NOTE:  if file doesn't exist, then we first write the file using our assets file

            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fis!=null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public void writeFileOnInternalStorage() {
        FileOutputStream fos = null;

        try {

            fos = mContext.openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(loadJSONFromAsset().getBytes());       // load initial data from assets folder
            //Log.e(TAG, "File path>>"+getFilesDir());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public void fetchDecksFromLocalStorage() {
        Gson gson = new Gson();
        Type cardType = new TypeToken<ArrayList<CardPojo>>() {
        }.getType();

        decksNamesList.clear();

        try {
            JSONObject jsonObject = new JSONObject(loadJSONFromLocalStorage());

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

    public boolean addCardToDeck(CardPojo card, int deckPosition) {
        if(!checkIfCardExistsInDeck(card, deckPosition)) {
            decksList.get(deckPosition).add(card);
            updateDeckCards();
            return true;
//            Toast.makeText(mContext, "Card added to the deck!", Toast.LENGTH_SHORT).show();
        }
        else {
            return false;
        }
    }


    public boolean checkIfCardExistsInDeck(CardPojo card, int deckPosition) {
        for(CardPojo deckCard: decksList.get(deckPosition)) {
            if(card.getWord().equalsIgnoreCase(deckCard.getWord()))
                return true;
        }
        return false;
    }

    public void updateDeckCards() {         // IMP update the data file by rewriting it with newly added card
        FileOutputStream fos = null;
        Gson gson = new Gson();
        try {
            fos = mContext.openFileOutput(FILE_NAME, MODE_PRIVATE);
            Log.e(TAG, "JSON>>"+gson.toJson(decksList));
//            fos.write(gson.toJson(decksList).toString().getBytes());       // load initial data from assets folder
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


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

}

