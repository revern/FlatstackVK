package com.example.almaz.flatstackvk.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.almaz.flatstackvk.PostActivity;
import com.example.almaz.flatstackvk.R;
import com.example.almaz.flatstackvk.model.GroupsResponse;
import com.example.almaz.flatstackvk.model.PostsResponse;
import com.example.almaz.flatstackvk.model.UsersResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiGroups;
import com.vk.sdk.api.model.VKApiPost;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKPostArray;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by almaz on 01.09.2016.
 */
public class NewsRecyclerViewAdapter extends
        RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolder>{

    private PostsResponse.Response.Item[] mRecords;
    private Context mContext;
    private Gson mGson;

    public NewsRecyclerViewAdapter(Context context, PostsResponse.Response.Item[] records){
        mRecords = records;
        mContext = context;

        GsonBuilder builder = new GsonBuilder();
        mGson = builder.create();
    }

    @Override
    public NewsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext()).inflate(R.layout.news_card_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NewsRecyclerViewAdapter.ViewHolder holder, final int position) {
        final PostsResponse.Response.Item post = mRecords[position];
        final CardView postContainer = holder.mPostContainer;
        final ImageView postAuthorPhoto = holder.mAuthorPhoto;
        final TextView postText = holder.mPostText;
        final TextView postDate = holder.mPostDate;
        final TextView postAuthorName = holder.mAuthorName;
        final ImageView postImage = holder.mPostImage;

        VKRequest request;
        if(post.source_id>=0) {
             request = VKApi.users()
                     .get(VKParameters.from(VKApiConst.USER_ID, post.source_id,
                             VKApiConst.FIELDS, "photo_50"));
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    Log.d("USER", response.responseString);
                    UsersResponse usersResponse = mGson
                            .fromJson(response.responseString, UsersResponse.class);
                    postAuthorName.setText(usersResponse.responses[0].first_name + " "
                            + usersResponse.responses[0].last_name);
                    Glide.with(mContext)
                            .load(usersResponse.responses[0].photo_50)
                            .into(postAuthorPhoto);

                }
            });

        } else {
            request = new VKApiGroups()
                    .getById(VKParameters.from(VKApiConst.GROUP_ID, post.source_id*(-1)));
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    Log.d("USER", response.responseString);
                    GroupsResponse groupsResponse = mGson
                            .fromJson(response.responseString, GroupsResponse.class);
                    postAuthorName.setText(groupsResponse.responses[0].name);
                    Glide.with(mContext)
                            .load(groupsResponse.responses[0].photo_50)
                            .into(postAuthorPhoto);

                }
            });
        }

        postDate.setText(takeFormattedDate(position));

        postText.setText(takeCutText(position));

        String imageUrl = null;
        try {
            imageUrl = post.attachments[0].photo.photo_604;
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        if(imageUrl!=null) {
            Glide.with(mContext).load(imageUrl).into(postImage);
        }
        final String imageUrlExtra = imageUrl;
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

    private String takeFormattedDate(int position){
        return new SimpleDateFormat("HH:mm:ss   dd MMM yyyy")
                .format(new Date(mRecords[position].date*1000L));
    }
    private String takeCutText(int position){
        String text = mRecords[position].text;
        if(text.length()>500){
            text = text.substring(0, 400) + "..." + "\n\n" + "read more...";
        }
        return text;
    }

    @Override
    public int getItemCount() {
        return mRecords.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CardView mPostContainer;
        private ImageView mAuthorPhoto;
        private TextView mAuthorName;
        private TextView mPostDate;
        private TextView mPostText;
        private ImageView mPostImage;
        public ViewHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            mPostContainer = (CardView) itemView.findViewById(R.id.news_cv);
            mAuthorPhoto = (ImageView) itemView.findViewById(R.id.item_post_author_photo);
            mAuthorName = (TextView) itemView.findViewById(R.id.item_post_author_name);
            mPostDate = (TextView) itemView.findViewById(R.id.item_post_date);
            mPostText = (TextView) itemView.findViewById(R.id.item_post_text);
            mPostImage = (ImageView) itemView.findViewById(R.id.item_post_image);
        }
    }
}
