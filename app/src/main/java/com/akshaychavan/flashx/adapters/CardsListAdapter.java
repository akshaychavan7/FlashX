package com.akshaychavan.flashx.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.akshaychavan.flashx.R;
import com.akshaychavan.flashx.pojo.CardPojo;
import com.akshaychavan.flashx.pojo.WordDataPojo;
import com.akshaychavan.flashx.utility.ApiClient;
import com.akshaychavan.flashx.utility.ApiInterface;
import com.akshaychavan.flashx.utility.GlobalCode;

import java.io.IOException;
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
public class CardsListAdapter extends RecyclerView.Adapter<CardsListAdapter.CardViewHolder> implements Filterable {

    final String TAG = "CardsListAdapter";
    private ArrayList<CardPojo> mCardsList, mCardsListFull;


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


    public CardsListAdapter(ArrayList<CardPojo> cardsList, Context context) {
        mCardsList = cardsList;
        mCardsListFull = new ArrayList<>(mCardsList);
        this.mContext = context;
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

        Log.e("Holder", ">>>"+(holder==null)+" >>"+(holder.tvWord==null));
        holder.tvWord.setText(currentItem.getWord());
        holder.tvWordClass.setText(currentItem.getClass_());
        holder.tvWordDescription.setText(currentItem.getMeaning());
        holder.tvSynonyms.setText(currentItem.getSynonyms());
        holder.tvExample.setText(currentItem.getExample());
        holder.tvMnemonic.setText(currentItem.getMnemonic());

//        holder.ivWordImage.setImageBitmap();

//        callGetWordData("Voracious");


        // Setting color of the cardside
        GradientDrawable backgroundGradient = (GradientDrawable) holder.cardClassColor.getBackground();
        backgroundGradient.setColor(GlobalCode.getInstance().getColor(position));

        bindEvents(holder, position, currentItem);
    }

    public void bindEvents(final CardViewHolder holder, int position, final CardPojo currentItem) {

    }       // end bindEvents

    @Override
    public int getItemCount() {
        return mCardsList.size();
    }

    @Override
    public Filter getFilter() {
        return assetSearchFilter;
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
        ImageView ivWordImage;
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

            cardClassColor = itemView.findViewById(R.id.v_card_class_color);

        }
    }
}
