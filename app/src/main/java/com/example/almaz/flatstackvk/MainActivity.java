package com.example.almaz.flatstackvk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.almaz.flatstackvk.adapter.NewsRecyclerViewAdapter;
import com.example.almaz.flatstackvk.model.GroupsResponse;
import com.example.almaz.flatstackvk.model.PostsResponse;
import com.example.almaz.flatstackvk.model.UsersResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiGroups;
import com.vk.sdk.api.model.VKApiApplicationContent;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKPostArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean isResumed = false;

    private static final String TAG = "MainActivity";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private final String[] mScope = new String[]{
            VKScope.FRIENDS,
            VKScope.WALL,
            VKScope.GROUPS,
            VKScope.DIRECT,
            VKScope.PHOTOS
    };

    private RecyclerView mNewsRcView;
    private Gson mGson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GsonBuilder builder = new GsonBuilder();
        mGson = builder.create();;

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
                mNewsRcView = (RecyclerView) findViewById(R.id.rcv_news);

                VKRequest request =
                        new VKRequest("newsfeed.get", VKParameters
                                .from(VKApiConst.FILTERS, "post", VKApiConst.FIELDS, "text"));
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        Log.d("RESPONSE", response.responseString);

                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();

                        PostsResponse postsResponse = gson
                                .fromJson(response.responseString, PostsResponse.class);
                        PostsResponse.Response.Item[] posts = postsResponse.response.items;
                        updateAdapter(posts);
                    }
                });
            }

            @Override
            public void onError(VKError error) {
                // Auth error, user not connected
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void updateAdapter(PostsResponse.Response.Item[] posts){
        if(posts!=null) {
            NewsRecyclerViewAdapter newsAdapter =
                    new NewsRecyclerViewAdapter(
                            getApplicationContext(),
                            posts);
            LinearLayoutManager layoutManager =
                    new LinearLayoutManager(
                            getApplicationContext(),
                            LinearLayoutManager.VERTICAL,
                            false);
            RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
            mNewsRcView.setAdapter(newsAdapter);
            mNewsRcView.setLayoutManager(layoutManager);
            mNewsRcView.setItemAnimator(itemAnimator);
        }
    }

    private List<String> takeAuthors(PostsResponse.Response.Item[] posts){
        final HashMap<Integer,Long> users = new HashMap();
        final HashMap<Integer,Long> groups = new HashMap();

        final List<String> list = new ArrayList<String>();
        for(int i = 0; i < posts.length; i++){
            list.add(" ");
        }
        for(int i = 0; i < posts.length; i++){
            if(posts[i].source_id>=0){
                users.put(i, posts[i].source_id);
            } else {
                groups.put(i, posts[i].source_id * (-1));
            }
        }

        String usersIDs = users.values().toString()
                .substring(1, users.values().toString().length()-1);

        String groupsIDs = groups.values().toString()
                .substring(1, groups.values().toString().length()-1);

        VKRequest usersRequest = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, usersIDs));
        usersRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                UsersResponse usersResponse = mGson
                        .fromJson(response.responseString, UsersResponse.class);
                List<Integer> keys = new ArrayList();
                keys.addAll(users.keySet());
                for(int i = 0; i < users.size(); i++){
                    list.set(keys.get(i), usersResponse.responses[i].first_name + " " + usersResponse.responses[i].last_name);
                }
            }
        });

        VKRequest groupsRequest = VKApi.users().get(VKParameters.from("group_ids", groupsIDs));
        groupsRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                GroupsResponse groupsResponse = mGson
                        .fromJson(response.responseString, GroupsResponse.class);
                List<Integer> keys = new ArrayList();
                keys.addAll(groups.keySet());
                for(int i = 0; i < groups.size(); i++){
                    list.set(keys.get(i), groupsResponse.responses[i].name);
                }
            }
        });
        return list;
    }

    public void onClickFabLogout(View view){
        VKSdk.logout();
        if (!VKSdk.isLoggedIn()) {
            showLogin();
        }
    }
}
