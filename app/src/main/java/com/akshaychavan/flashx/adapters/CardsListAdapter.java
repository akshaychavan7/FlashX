package com.akshaychavan.flashx.adapters;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.akshaychavan.flashx.R;
import com.akshaychavan.flashx.activities.MainActivity;
import com.akshaychavan.flashx.fragments.ViewCardsFragment;
import com.akshaychavan.flashx.pojo.CardPojo;
import com.akshaychavan.flashx.pojo.Definition;
import com.akshaychavan.flashx.pojo.WordDataPojo;
import com.akshaychavan.flashx.utility.ApiClient;
import com.akshaychavan.flashx.utility.ApiInterface;
import com.akshaychavan.flashx.utility.GlobalCode;
import com.akshaychavan.flashx.utility.MyDatabaseHelper;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Akshay Chavan on 15,February,2021
 */
public class CardsListAdapter extends RecyclerView.Adapter<CardsListAdapter.CardViewHolder> implements Filterable {

    final String TAG = "CardsListAdapter";
    TextToSpeech textToSpeech;
    Fragment currentFragment;

    GlobalCode globalCode = GlobalCode.getInstance();

    //////////////////////// Edit card popup vairables //////////////////////
    ImageView ivImageBrowser;
    EditText etImagePath, etWord, etWordDefinition, etWordSynonyms, etWordExample, etWordMnemonic;
    TextView tvSaveCard, tvCancelCard, tvAddNextCard;
    MaterialButton verbBtn, adjBtn, nounBtn, idiomBtn;
    ProgressBar progressBar;
    String selectedWordClass;
    AlertDialog editCardPopup;
    private ArrayList<CardPojo> mCardsList, mCardsListFull;
    /////////////////////////////////////////////////////////////////////////
    public Filter assetSearchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<CardPojo> cardsFilteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {              // If empty search text
                cardsFilteredList.addAll(mCardsListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                // filtering
                for (CardPojo item : mCardsListFull) {
                    if (item.getWord().toLowerCase().contains(filterPattern)) {
                        cardsFilteredList.add(item);
                    }
                }

            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = cardsFilteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mCardsList.clear();
            mCardsList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
    private Context mContext;


    public CardsListAdapter(ArrayList<CardPojo> cardsList, Context context, Fragment currentFragment) {
        mCardsList = cardsList;
        mCardsListFull = new ArrayList<>(mCardsList);
        this.mContext = context;
        this.currentFragment = currentFragment;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_card_layout, parent, false);
        CardViewHolder holdingsViewHolder = new CardViewHolder(v);
        return holdingsViewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardPojo currentItem = mCardsList.get(position);

        textToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.ENGLISH);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(mContext, "Language not supported for text to speech!", Toast.LENGTH_SHORT).show();
                    } else {
                        holder.ivTextToSpeechList.setEnabled(true);
                    }
                } else {
                    Toast.makeText(mContext, "Text to Speech initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.ivTextToSpeechList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak(holder);
            }
        });

//        Log.e("Holder", ">>>"+(holder==null)+" >>"+(holder.tvWord==null));
        holder.tvWord.setText(currentItem.getWord());
        holder.tvWordClass.setText(currentItem.getClass_() + ":");
        holder.tvWordDescription.setText(currentItem.getMeaning());
        holder.tvSynonyms.setText(currentItem.getSynonyms());
        holder.tvExample.setText(currentItem.getExample());
        holder.tvMnemonic.setText(currentItem.getMnemonic());

//        holder.ivWordImage.setImageBitmap();

        if (currentItem.getImageURL().contains("https") || currentItem.getImageURL().contains("http"))       // check if image is from web or from local storage
        {
            Glide.with(mContext)
                    .load(currentItem.getImageURL()) // image url
                    .placeholder(R.mipmap.loading_image) // any placeholder to load at start
                    .error(R.mipmap.image_not_found)  // any image in case of error
                    .override(200, 150) // resizing
                    .centerCrop()
                    .into(holder.ivWordImage);  // imageview object
        } else {
            Uri uri = Uri.parse(currentItem.getImageURL());
            holder.ivWordImage.setImageURI(uri);
        }


//        callGetWordData("Voracious");

        Log.e(TAG, "Word class>>" + currentItem.getClass_());

        // Setting color of the cardside
        GradientDrawable backgroundGradient = (GradientDrawable) holder.cardClassColor.getBackground();
        backgroundGradient.setColor(GlobalCode.getInstance().getWordClassColor(currentItem.getClass_()));

        bindEvents(holder, position, currentItem);
    }

    public void bindEvents(final CardViewHolder holder, int position, final CardPojo currentItem) {

        holder.ivCardsListOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsMenuPopup(v, position);
            }
        });

    }       // end bindEvents

    @Override
    public int getItemCount() {
        return mCardsList.size();
    }

    @Override
    public Filter getFilter() {
        return assetSearchFilter;
    }

    public void speak(CardViewHolder holder) {
        String word = holder.tvWord.getText().toString();
        float pitch = 1f;
        float speed = 0.5f;

        textToSpeech.setPitch(pitch);
        textToSpeech.setSpeechRate(speed);

        textToSpeech.speak(word, TextToSpeech.QUEUE_ADD, null);

        String meaning = holder.tvWordClass.getText() + ",,,," + holder.tvWordDescription.getText();
        textToSpeech.speak(meaning, TextToSpeech.QUEUE_ADD, null);
    }

    public void showOptionsMenuPopup(View v, int position) {
        PopupMenu popupMenu = new PopupMenu(mContext, v);
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
        LayoutInflater li = LayoutInflater.from(mContext);
        View view = li.inflate(R.layout.edit_card_layout, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.CustomDialog);
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
        CardPojo card = mCardsList.get(cardPosition);
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
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((MainActivity) mContext, new String[]{Manifest.permission.CAMERA}, 1);
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
                Intent intent = new Intent(mContext, MainActivity.class);
                mContext.startActivity(intent);

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

        CardPojo card = mCardsList.get(cardPosition);

        String word = etWord.getText().toString();
        String wordClass = selectedWordClass;
        String wordDefinition = etWordDefinition.getText().toString();
        String synonyms = etWordSynonyms.getText().toString();
        String example = etWordExample.getText().toString();
        String mnemonic = etWordMnemonic.getText().toString();
        String url = etImagePath.getText().toString();

        MyDatabaseHelper myDatabaseHelper = MyDatabaseHelper.getInstance(mContext);
        SQLiteDatabase db = myDatabaseHelper.getDatabase();

        String query = "UPDATE Words_List SET Word = \"" + word + "\", Class = \"" + wordClass + "\", Definition = \"" + wordDefinition + "\", Synonyms = \"" + synonyms + "\", Examples = \"" + example + "\", Mnemonic = \"" + mnemonic + "\", Image_URL = \"" + url + "\"  WHERE _id = "+ card.get_id() +"  AND Word = \""+ card.getWord() +"\";";

        Log.e(TAG, "Query>>"+query);

        db.execSQL(query);

        editCardPopup.dismiss();
    }

    // to check if required fields are blank or not
    public boolean validateFields() {
        if (etWord.getText().length() == 0 || etWordDefinition.getText().length() == 0 || selectedWordClass == null) {
            Toast.makeText(mContext, "Word, Part of Speech Class and Definition are required fields!\nMake sure you input all the required fields", Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(mContext, FilePickerActivity.class);

        intent.putExtra(FilePickerActivity.CONFIGS,
                new Configurations.Builder()
                        .setCheckPermission(true)
                        .setShowImages(true)
                        .enableImageCapture(true)
                        .setMaxSelection(1)
                        .setSkipZeroSizeFiles(true)
                        .build());

        ((MainActivity)mContext).startActivityForResult(intent, 101);
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
                    Toast.makeText(mContext, "Word not found!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(mContext, "Something went wrong!\n>>" + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Something went wrong >>" + t.getMessage());
            }
        });

    }


    public void callGetWordData(String word) throws IOException {


//        ApiClient.setWord(word);
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);


        Call<List<WordDataPojo>> call = apiInterface.getWordData(word);

//        Log.e(TAG, "Call>>"+call.request().url());
//        Log.e(TAG, ""+call.execute().body().getWord());
        call.enqueue(new Callback<List<WordDataPojo>>() {
            @Override
            public void onResponse(Call<List<WordDataPojo>> call, Response<List<WordDataPojo>> response) {
//                    Log.e(TAG, "Response Code -> " + response.code());
                if (response.isSuccessful()) {
                    Log.e(TAG, response.body().get(0).getMeanings().get(0).getDefinitions().get(0).getDefinition());
                } else {
                    Toast.makeText(mContext, "Response Error >> " + response.errorBody(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, response.message());
                }
            }

            @Override
            public void onFailure(Call<List<WordDataPojo>> call, Throwable t) {
                Toast.makeText(mContext, "Something went wrong!\n>>" + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Something went wrong >>" + t.getMessage());
            }
        });

    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvWordClass, tvWordDescription, tvSynonyms, tvExample, tvMnemonic;
        ImageView ivWordImage, ivTextToSpeechList, ivCardsListOptions;
        View cardClassColor;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);

            tvWord = itemView.findViewById(R.id.tv_word);
//            Log.e("Holder", "tvword set");
            tvWordClass = itemView.findViewById(R.id.tv_word_class);
            tvWordDescription = itemView.findViewById(R.id.tv_word_description);
            tvSynonyms = itemView.findViewById(R.id.tv_synonyms);
            tvExample = itemView.findViewById(R.id.tv_example);
            tvMnemonic = itemView.findViewById(R.id.tv_mnemonic);

            ivWordImage = itemView.findViewById(R.id.iv_word_image);
            ivTextToSpeechList = itemView.findViewById(R.id.iv_text_to_speech_list);
            ivCardsListOptions = itemView.findViewById(R.id.iv_cards_list_options);

            cardClassColor = itemView.findViewById(R.id.v_card_class_color);

        }
    }
}
