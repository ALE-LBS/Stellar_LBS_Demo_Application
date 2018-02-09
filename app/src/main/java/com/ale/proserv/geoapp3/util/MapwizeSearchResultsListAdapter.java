package com.ale.proserv.geoapp3.util;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.ale.proserv.geoapp3.R;
import com.arlib.floatingsearchview.util.Util;

import java.util.ArrayList;
import java.util.List;

import io.mapwize.mapwizeformapbox.model.Venue;


public class MapwizeSearchResultsListAdapter extends RecyclerView.Adapter<MapwizeSearchResultsListAdapter.ViewHolder> {

    private List<Venue> mDataSet = new ArrayList<>();

    private int mLastAnimatedItemPosition = -1;

    public interface OnItemClickListener{
        void onClick(Venue venue);
    }

    private OnItemClickListener mItemsOnClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mColorName;
        public final TextView mColorValue;
        public final View mTextContainer;

        public ViewHolder(View view) {
            super(view);
            mColorName = (TextView) view.findViewById(R.id.color_name);
            mColorValue = (TextView) view.findViewById(R.id.color_value);
            mTextContainer = view.findViewById(R.id.text_container);
        }
    }

    public void swapData(List<Venue> mNewDataSet) {
        mDataSet = mNewDataSet;
        notifyDataSetChanged();
    }

    public void setItemsOnClickListener(OnItemClickListener onClickListener){
        this.mItemsOnClickListener = onClickListener;
    }

    @Override
    public MapwizeSearchResultsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result_list_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MapwizeSearchResultsListAdapter.ViewHolder holder, final int position) {

        Venue venueSuggestion = mDataSet.get(position);
        holder.mColorName.setText(venueSuggestion.getName());
        holder.mColorValue.setText(venueSuggestion.getAlias());

        if(mLastAnimatedItemPosition < position){
            animateItem(holder.itemView);
            mLastAnimatedItemPosition = position;
        }

        if(mItemsOnClickListener != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemsOnClickListener.onClick(mDataSet.get(position));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    private void animateItem(View view) {
        view.setTranslationY(Util.getScreenHeight((Activity) view.getContext()));
        view.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator(3.f))
                .setDuration(700)
                .start();
    }

}
