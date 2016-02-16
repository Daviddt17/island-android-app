package com.island.island.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.island.island.Adapters.PostAdapter;
import com.island.island.Models.Post;
import com.island.island.Database.IslandDB;
import com.island.island.R;
import com.island.island.SimpleDividerItemDecoration;
import com.island.island.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Boilerplate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // TODO: Remove after login implemented
        // Set user hack
        String USER = "Rocky";
        Utils.setUser(this, USER);
        IslandDB.createIdentity(this, USER);
        IslandDB.postPublicKey(this);

        // Nav drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set user in nav drawer
        View header = navigationView.getHeaderView(0);
        ImageView navProfileImage = (ImageView) header.findViewById(R.id.nav_profile_image);
        TextView navUserName = (TextView) header.findViewById(R.id.nav_user_name);
        final String userName = Utils.getUser(FeedActivity.this);

        navProfileImage.setOnClickListener((View v) ->
        {
            Intent profileIntent = new Intent(FeedActivity.this, ProfileActivity.class);
            profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, userName);
            startActivity(profileIntent);
        });
        navUserName.setText(userName);

        // Feed posts setup
        List<Post> arrayOfPosts = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.feed_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PostAdapter(this, arrayOfPosts);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        // Populate feed
//        for(User user: userList)
//        {
//            //List<Post> userPosts = IslandDB.getPostsForUser(user);
//            arrayOfPosts.addAll(userPosts);
//        }
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile)
        {
            Intent profileIntent = new Intent(this, ProfileActivity.class);
            profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, Utils.getUser(this));
            startActivity(profileIntent);
        }
        else if (id == R.id.nav_friends)
        {
            startActivity(new Intent(this, ViewFriendsActivity.class));
        }
        else if (id == R.id.nav_settings)
        {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // On clicks
    public void startNewPostActivity(View view)
    {
        Intent newPostIntent = new Intent(FeedActivity.this, NewPostActivity.class);
        startActivity(newPostIntent);
    }
}
