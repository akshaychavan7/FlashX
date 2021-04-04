package com.akshaychavan.flashx.adapters;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
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
import androidx.recyclerview.widget.RecyclerView;

import com.akshaychavan.flashx.R;
import com.akshaychavan.flashx.activities.MainActivity;
import com.akshaychavan.flashx.fragments.ViewCardsFragment;
import com.akshaychavan.flashx.pojo.DeckPojo;
import com.akshaychavan.flashx.pojo.Definition;
import com.akshaychavan.flashx.pojo.WordDataPojo;
import com.akshaychavan.flashx.utility.ApiClient;
import com.akshaychavan.flashx.utility.ApiInterface;
import com.akshaychavan.flashx.utility.GlobalCode;
import com.google.android.material.button.MaterialButton;
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Akshay Chavan on 15,February,2021
 * akshay.chavan@finiq.com
 * FinIQ Consulting India
 */
public class DecksListAdapter extends RecyclerView.Adapter<DecksListAdapter.DecksViewHolder> implements Filterable, ActivityCompat.OnRequestPermissionsResultCallback {

    final String TAG = "DecksListAdapter";
    GlobalCode globalCode = GlobalCode.getInstance();
    // New Card Popup Variables
    ImageView ivImageBrowser;
    EditText etImagePath, etWord, etWordDefinition, etWordSynonyms, etWordExample, etWordMnemonic;
    MaterialButton verbBtn, adjBtn, nounBtn, idiomBtn;
    private ArrayList<DeckPojo> mDecksList, mDecksListFull;
    ProgressBar progressBar;
    ///////////////////////////


    public Filter assetSearchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<DeckPojo> decksFilteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {              // If empty search text
                decksFilteredList.addAll(mDecksListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                // filtering
                for (DeckPojo item : mDecksListFull) {
                    if (item.getDeckTitle().toLowerCase().contains(filterPattern)) {
                        decksFilteredList.add(item);
                    }
                }

            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = decksFilteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mDecksList.clear();
            mDecksList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
    private Context mContext;


    public DecksListAdapter(ArrayList<DeckPojo> decksList, Context context) {
        mDecksList = decksList;
        Log.e(TAG, "Decks size>>" + decksList.size());
        mDecksListFull = new ArrayList<>(mDecksList);
        this.mContext = context;
    }

    @NonNull
    @Override
    public DecksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.deck_layout, parent, false);
        DecksViewHolder decksViewHolder = new DecksViewHolder(v);
        return decksViewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull DecksViewHolder holder, int position) {
        DeckPojo currentItem = mDecksList.get(position);

        int masteredCount = currentItem.getMasteredWordsCount();
        int cardsCount = currentItem.getDeckCardsCount();
        int progress = Math.round((float) masteredCount / cardsCount * 100);


        holder.tvDeckTitle.setText(currentItem.getDeckTitle());
        holder.tvMasteredCount.setText("You have mastered " + masteredCount + " of " + cardsCount + " words");
//        holder.progressBar.setProgress(progress,true);
        setProgressAnimate(holder.progressBar, progress);

        // Setting color of the cardside
        GradientDrawable backgroundGradient = (GradientDrawable) holder.deckSideColor.getBackground();
        backgroundGradient.setColor(GlobalCode.getInstance().getColor(position));

        bindEvents(holder, position, currentItem);
    }

    public void bindEvents(final DecksViewHolder holder, int position, final DeckPojo currentItem) {
        holder.tvViewCardsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ViewCardsFragment(mContext, currentItem.getDeckTitle())).commit();
            }
        });


        // open deck options menu
        holder.ivDeckOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsMenuPopup(v);
            }
        });

    }       // end bindEvents

    @Override
    public int getItemCount() {
        return mDecksList.size();
    }

    @Override
    public Filter getFilter() {
        return assetSearchFilter;
    }

    private void setProgressAnimate(ProgressBar pb, int progressTo) {
//        Log.e(TAG, "progress>>"+progressTo);
        ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), progressTo);
        animation.setDuration(1000);
        animation.setAutoCancel(true);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    public void showOptionsMenuPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(mContext, v);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.deck_option_add_cards:
                        openAddNewCardIntent();
                        break;
                    case R.id.deck_option_edit:
                        break;
                    case R.id.deck_option_delete:
                        break;
                }
                return true;
            }
        });

        popupMenu.inflate(R.menu.deck_options_menu);
        popupMenu.show();
    }

    public void openAddNewCardIntent() {
        LayoutInflater li = LayoutInflater.from(mContext);
        View view = li.inflate(R.layout.create_new_deck_layout, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, R.style.CustomDialog);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.create();
        alertDialogBuilder.show();

        bindNewCardVariables(view);
        bindNewCardEvents(view);
    }

    public void bindNewCardVariables(View v) {
        ivImageBrowser = v.findViewById(R.id.iv_image_browser);
        etImagePath = v.findViewById(R.id.et_image_path);
        //Making this image path edittext global
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
    }

    public void bindNewCardEvents(View v) {
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


//        etWord.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                getWordDetails(s.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

        etWord.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String word = etWord.getText().toString();
                if ( (!hasFocus) && word.length()>0 ) {
//                    Toast.makeText(mContext, "Got the focus", Toast.LENGTH_LONG).show();
                    getWordDetails(word);
                }
            }
        });

    }


    public void setWordClassButton(String wordClass) {
        switch (wordClass) {
            case "verb":
                verbBtn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#F44336")));
                verbBtn.setStrokeWidth(2);

                adjBtn.setStrokeWidth(0);
                nounBtn.setStrokeWidth(0);
                idiomBtn.setStrokeWidth(0);
                break;
            case "adjective":
                adjBtn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#F44336")));
                adjBtn.setStrokeWidth(2);

                verbBtn.setStrokeWidth(0);
                nounBtn.setStrokeWidth(0);
                idiomBtn.setStrokeWidth(0);
                break;
            case "noun":
                nounBtn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#F44336")));
                nounBtn.setStrokeWidth(2);

                verbBtn.setStrokeWidth(0);
                adjBtn.setStrokeWidth(0);
                idiomBtn.setStrokeWidth(0);
                break;
            case "idiom":
                idiomBtn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#F44336")));
                idiomBtn.setStrokeWidth(2);

                verbBtn.setStrokeWidth(0);
                adjBtn.setStrokeWidth(0);
                nounBtn.setStrokeWidth(0);
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

        ((MainActivity) mContext).startActivityForResult(intent, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            if (requestCode == 1) {
                imagePicker();
            } else {
                Toast.makeText(mContext, "Permission denied!", Toast.LENGTH_SHORT);
            }
        }
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
                    if(wordClass.contains("verb")) {
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


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static class DecksViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeckTitle, tvMasteredCount, tvPracticeBtn, tvResetBtn, tvViewCardsBtn;
        View deckSideColor;
        ImageView ivDeckOptions;
        ProgressBar progressBar;

        public DecksViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDeckTitle = itemView.findViewById(R.id.tv_deck_title);
            tvMasteredCount = itemView.findViewById(R.id.tv_mastered_count);
            deckSideColor = itemView.findViewById(R.id.v_deck_side_color);
            progressBar = itemView.findViewById(R.id.progressBar);

            tvPracticeBtn = itemView.findViewById(R.id.tv_practice_btn);
            tvResetBtn = itemView.findViewById(R.id.tv_reset_btn);
            tvViewCardsBtn = itemView.findViewById(R.id.tv_view_cards_btn);

            ivDeckOptions = itemView.findViewById(R.id.iv_deck_options);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
