package edu.cornell.yh742.cs5450lab4;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by seanhsu on 11/23/17.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    public class GalleryViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mDescValue;
        public TextView mTypeValue;
        public GalleryViewHolder(View view){
            super(view);
            mImageView = (ImageView)itemView.findViewById(R.id.iv_photo);
            mDescValue = (TextView)itemView.findViewById(R.id.desc_value);
            mTypeValue = (TextView)itemView.findViewById(R.id.type_value);
        }
    }

    private Context mContext;
    private LinkedHashMap<ImageData, String> mImageDataList;

    public GalleryAdapter(Context context, Map<ImageData, String> picList){
        mContext = context;
        mImageDataList = (LinkedHashMap)picList;
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int ViewType){
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View photoView = inflater.inflate(R.layout.item, parent, false);
        GalleryViewHolder viewHolder = new GalleryViewHolder(photoView);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position){
        ImageData data = new ArrayList<ImageData>(mImageDataList.keySet()).get(position);
        String security = new ArrayList<String>(mImageDataList.values()).get(position);
        ImageView view = holder.mImageView;

        holder.mDescValue.setText(data.description);
        holder.mTypeValue.setText(security);
        Log.d("Adapter", "Image URL: " + data.getUrl().toString());
        Glide.with(mContext)
                .load(data.getUrl())
                .placeholder(R.drawable.ic_action_name)
                .into(view);
    }

    @Override
    public int getItemCount(){
        return mImageDataList.size();
    }
}
