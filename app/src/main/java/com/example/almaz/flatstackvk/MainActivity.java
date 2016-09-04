package com.example.almaz.flatstackvk;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.almaz.flatstackvk.adapter.NewsRecyclerViewAdapter;
import com.example.almaz.flatstackvk.model.PostsResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MainActivity";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private final String[] mScope = new String[]{
            VKScope.FRIENDS,
            VKScope.WALL,
            VKScope.GROUPS,
            VKScope.DIRECT,
            VKScope.PHOTOS
    };

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mNewsRcView;
    private Gson mGson;
    private NewsRecyclerViewAdapter mNewsAdapter;
    private LinearLayoutManager mLayoutManager;
    private List<PostsResponse.Response.Item> mPosts = new ArrayList<>();
    private HashMap<Long, PostsResponse.Response.Group> mGroups = new HashMap<>();
    private HashMap<Long, PostsResponse.Response.Profile> mProfiles = new HashMap<>();

    private boolean isResumed = false;
    private boolean isLoading = false;
    private String lastPostInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout_main);
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.vk_share_blue_color);

        mNewsRcView = (RecyclerView) findViewById(R.id.rcv_news);
        mNewsRcView.addOnScrollListener(takeScrollListener());

        initializeVKSdk();
        GsonBuilder builder = new GsonBuilder();
        mGson = builder.create();;
    }

    private RecyclerView.OnScrollListener takeScrollListener(){
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy > 0 && !isLoading
                        && mLayoutManager.findFirstVisibleItemPosition()+15 > mPosts.size()){
                    isLoading = true;
                    loadMoreItems();
                }
            }
        };
    }

    private void loadMoreItems(){
        VKRequest request =
                new VKRequest("newsfeed.get", VKParameters.from(
                        VKApiConst.FILTERS, "post",
                        "start_from", lastPostInfo,
                        VKApiConst.COUNT, 20,
                        VKApiConst.FIELDS, "text"));

        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d(TAG, "loadMoreItems response- " + response.responseString);

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();

                PostsResponse postsResponse = gson
                        .fromJson(response.responseString, PostsResponse.class);
                PostsResponse.Response.Item[] posts = postsResponse.response.items;
                lastPostInfo = postsResponse.response.next_from;
                isLoading=false;
                for(int i=0;i<posts.length;i++){
                    mPosts.add(posts[i]);
                }
                takeAuthors(postsResponse);
                mNewsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initializeVKSdk(){
        VKSdk.wakeUpSession(this, new VKCallback<VKSdk.LoginState>() {
            @Override
            public void onResult(VKSdk.LoginState res) {
                Log.d(TAG, res.name() + isResumed);
                switch (res) {
                    case LoggedOut:
                        showLogin();
                        break;
                    case LoggedIn:
                        break;
                    case Pending:
                        showLogin();
                        break;
                    case Unknown:
                        showLogin();
                        break;
                }
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), "Wake up error", Toast.LENGTH_LONG).show();
                Log.d(TAG, error.toString());
            }
        });
    }

    private void showLogin(){
        VKSdk.login(this, mScope);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // User connected
                refreshNews();
            }

            @Override
            public void onError(VKError error) {
                // Auth error, user not connected
                Log.d(TAG, error.toString());
                finish();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void refreshNews(){
        VKRequest request =
                new VKRequest("newsfeed.get", VKParameters.from(
                        VKApiConst.FILTERS, "post",
                        VKApiConst.FIELDS, "text"));

        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d(TAG, "refreshNews response - " + response.responseString);

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();

                PostsResponse postsResponse = gson
                        .fromJson(response.responseString, PostsResponse.class);
                PostsResponse.Response.Item[] posts = postsResponse.response.items;
                mGroups.clear();
                mProfiles.clear();
                takeAuthors(postsResponse);
                mPosts.clear();
                for(int i=0;i<posts.length;i++){
                    mPosts.add(posts[i]);
                }
                lastPostInfo = postsResponse.response.next_from;
                updateAdapter();
            }
        });
    }

    private void updateAdapter(){
        if(mPosts!=null) {
            mNewsAdapter = new NewsRecyclerViewAdapter(getApplicationContext(),
                    mPosts, mGroups, mProfiles);
            mLayoutManager = new LinearLayoutManager(getApplicationContext(),
                    LinearLayoutManager.VERTICAL, false);
            RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
            mNewsRcView.setAdapter(mNewsAdapter);
            mNewsRcView.setLayoutManager(mLayoutManager);
            mNewsRcView.setItemAnimator(itemAnimator);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void takeAuthors(PostsResponse postsResponse){

        for(PostsResponse.Response.Group group : postsResponse.response.groups){
            mGroups.put(group.id, group);
        }

        for(PostsResponse.Response.Profile profile : postsResponse.response.profiles){
            mProfiles.put(profile.id, profile);
        }
    }

    public void onClickFabLogout(View view){
        VKSdk.logout();
        if (!VKSdk.isLoggedIn()) {
            showLogin();
        }
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        Log.d(TAG, "on refresh");
        refreshNews();
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("Response", "on refresh run");
                refreshNews();
            }
        }, 3000);
    }
}
