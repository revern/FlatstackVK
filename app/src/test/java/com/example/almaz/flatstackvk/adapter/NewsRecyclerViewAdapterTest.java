package com.example.almaz.flatstackvk.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.almaz.flatstackvk.model.PostsResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by almaz on 07.09.2016.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({Glide.class})
public class NewsRecyclerViewAdapterTest {

    @Mock
    Context mContext;

    @Mock
    NewsRecyclerViewAdapter mAdapter;

    @Mock
    List<PostsResponse.Response.Item> mItems;

    @Mock
    HashMap<Long, PostsResponse.Response.Group> mGroups;

    @Mock
    HashMap<Long, PostsResponse.Response.Profile> mProfiles;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mAdapter = new NewsRecyclerViewAdapter(mContext, mItems, mGroups, mProfiles);
    }

    @Test
    public void takeFormattedDateTest(){
        assertNotNull(mAdapter.takeFormattedDate(1410548435));
        assertEquals("23:00   12 сен 2014", mAdapter.takeFormattedDate(1410548435));
    }

    @Test
    public void takeCutTextTest(){
        String text0 = makeText(0);
        String text100 = makeText(100);
        String text450 = makeText(450);
        String text600 = makeText(600);
        String cutText600 = makeText(400) + "..." + "\n\n" + "read more...";

        assertNotNull(text0);
        assertEquals(text100, mAdapter.takeCutText(text100));
        assertEquals(text450, mAdapter.takeCutText(text450));
        assertEquals(cutText600, mAdapter.takeCutText(text600));
    }

    private String makeText(int charCount){
        String text = "";
        for(int i=0; i<charCount; i++){
            text+="a";
        }
        return text;
    }

    @Test
    public void getItemCountTest() throws Exception {
        when(mItems.size()).thenReturn(10);
        assertNotNull(mAdapter.getItemCount());
        assertEquals(10, mAdapter.getItemCount());
    }

}