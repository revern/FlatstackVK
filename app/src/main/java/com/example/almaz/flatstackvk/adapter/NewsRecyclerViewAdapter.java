package com.example.almaz.flatstackvk.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.almaz.flatstackvk.PostActivity;
import com.example.almaz.flatstackvk.R;
import com.example.almaz.flatstackvk.model.PostsResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by almaz on 01.09.2016.
 */
public class NewsRecyclerViewAdapter extends
        RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolder>{

    private List<PostsResponse.Response.Item> mRecords;
    private HashMap<Long, PostsResponse.Response.Group> mGroups = new HashMap<>();
    private HashMap<Long, PostsResponse.Response.Profile> mProfiles = new HashMap<>();
    Context mContext;

    public NewsRecyclerViewAdapter(Context context,
                                   List<PostsResponse.Response.Item> records,
                                   HashMap<Long, PostsResponse.Response.Group> groups,
                                   HashMap<Long, PostsResponse.Response.Profile> profiles){

        mContext = context;
        mRecords = records;
        mGroups = groups;
        mProfiles = profiles;
    }

    @Override
    public NewsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext()).inflate(R.layout.news_card_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NewsRecyclerViewAdapter.ViewHolder holder, final int position) {
        final PostsResponse.Response.Item post = mRecords.get(position);
        CardView postContainer = holder.mPostContainer;
        ImageView postAuthorPhotoView = holder.mAuthorPhotoView;
        TextView postTextView = holder.mPostTextView;
        TextView postDateView = holder.mPostDateView;
        TextView postAuthorNameView = holder.mAuthorNameView;
        ImageView postImageView = holder.mPostImageView;

        setAuthorInfo(post, postAuthorNameView, postAuthorPhotoView);
        postDateView.setText(takeFormattedDate(position));
        postTextView.setText(takeCutText(position));

        final String imageUrlExtra = setPostImageAndGetUrl(post, postImageView);;
        postContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PostActivity.class);
                intent.putExtra(PostActivity.POST_TEXT_EXTRA, post.text);
                intent.putExtra(PostActivity.POST_IMAGE_URL_EXTRA, imageUrlExtra);
                intent.putExtra(PostActivity.POST_LIKES_COUNT_EXTRA, post.likes.count);
                intent.putExtra(PostActivity.POST_REPOSTS_COUNT_EXTRA, post.reposts.count);
                mContext.startActivity(intent);
            }
        });
    }

    private void setAuthorInfo(PostsResponse.Response.Item post,
                                TextView authorNameView, ImageView authorPhotoView){
        if (post.source_id < 0) {
            PostsResponse.Response.Group group = mGroups.get(post.source_id * (-1));
            authorNameView.setText(group.name);
            Glide.with(mContext)
                    .load(group.photo_50)
                    .into(authorPhotoView);
        } else {
            PostsResponse.Response.Profile profile = mProfiles.get(post.source_id);
            authorNameView.setText(profile.first_name + " " + profile.last_name);
            Glide.with(mContext)
                    .load(profile.photo_50)
                    .into(authorPhotoView);
        }
    }

    private String setPostImageAndGetUrl(PostsResponse.Response.Item post, ImageView imageView){
        String imageUrl = null;

        try {
            imageUrl = post.attachments[0].photo.photo_604;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        try {
            imageUrl = post.attachments[0].doc.preview.photo.sizes[0].src;
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        Glide.clear(imageView);
        if(imageUrl!=null) {
            Glide.with(mContext)
                    .load(imageUrl)
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }
        return imageUrl;
    }

    private String takeFormattedDate(int position){
        return new SimpleDateFormat("HH:mm:ss   dd MMM yyyy")
                .format(new Date(mRecords.get(position).date*1000L));
    }
    private String takeCutText(int position){
        String text = mRecords.get(position).text;
        if(text.length()>500){
            text = text.substring(0, 400) + "..." + "\n\n" + "read more...";
        }
        return text;
    }

    @Override
    public int getItemCount() {
        return mRecords.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CardView mPostContainer;
        private ImageView mAuthorPhotoView;
        private TextView mAuthorNameView;
        private TextView mPostDateView;
        private TextView mPostTextView;
        private ImageView mPostImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            mPostContainer = (CardView) itemView.findViewById(R.id.news_cv);
            mAuthorPhotoView = (ImageView) itemView.findViewById(R.id.item_post_author_photo);
            mAuthorNameView = (TextView) itemView.findViewById(R.id.item_post_author_name);
            mPostDateView = (TextView) itemView.findViewById(R.id.item_post_date);
            mPostTextView = (TextView) itemView.findViewById(R.id.item_post_text);
            mPostImageView = (ImageView) itemView.findViewById(R.id.item_post_image);
        }
    }
}
