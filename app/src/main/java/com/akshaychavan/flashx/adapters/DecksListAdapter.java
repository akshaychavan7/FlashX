package com.akshaychavan.flashx.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.akshaychavan.flashx.R;
import com.akshaychavan.flashx.activities.MainActivity;
import com.akshaychavan.flashx.fragments.ViewCardsFragment;
import com.akshaychavan.flashx.pojo.DeckPojo;
import com.akshaychavan.flashx.utility.GlobalCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class DecksListAdapter extends RecyclerView.Adapter<DecksListAdapter.DecksViewHolder> implements Filterable {

    final String TAG = "DecksListAdapter";
    private ArrayList<DeckPojo> mDecksList, mDecksListFull;


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


    public static class DecksViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeckTitle, tvMasteredCount, tvPracticeBtn, tvResetBtn, tvViewCardsBtn;
        View deckSideColor;
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
        }
    }
}
