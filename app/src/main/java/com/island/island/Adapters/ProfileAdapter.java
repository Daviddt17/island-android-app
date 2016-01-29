package com.island.island.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.island.island.Activities.ProfileActivity;
import com.island.island.Containers.Post;
import com.island.island.Containers.Profile;
import com.island.island.R;
import com.island.island.Utils.Utils;

import java.util.ArrayList;

/**
 * Created by poo on 1/18/2016.
 */
public class ProfileAdapter extends ArrayAdapter
{
    private Context mContext;

    private static final int PROFILE_HEADER = 0;
    private static final int POST = 1;

    ArrayList posts = new ArrayList<>();

    @Override
    public int getViewTypeCount()
    {
        return 2;
    }

    @Override
    public int getItemViewType(int position)
    {
        return position == 0 ? PROFILE_HEADER : POST;
    }

    public ProfileAdapter(Context context, ArrayList posts)
    {
        super(context, 0, posts);
        this.posts = posts;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            // Profile header
            if(position == 0)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.profile_header,
                        parent, false);
                buildProfileHeader(convertView);
            }
            // Post
            else
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.glance_post,
                        parent, false);
                buildPost(position, convertView);
            }
        }

        return convertView;
    }

    private void buildProfileHeader(View convertView)
    {
        Profile profile = (Profile) getItem(0);

        // Get layout views
        TextView userName = (TextView) convertView.findViewById(R.id.profile_name);
        TextView aboutMe = (TextView) convertView.findViewById(R.id.profile_about_me);

        // Set values
        userName.setText(profile.getUserName());
        aboutMe.setText(profile.getAboutMe());
    }

    private void buildPost(int position, View convertView)
    {
        final Post post = (Post) getItem(position);

        // Get layout views and set data
        ImageView postProfilePicture = (ImageView) convertView.findViewById(
                R.id.post_profile_image);
        TextView postName = (TextView) convertView.findViewById(R.id.post_user_name);
        TextView postTimestamp = (TextView) convertView.findViewById(R.id.post_timestamp);
        TextView postContent = (TextView) convertView.findViewById(R.id.post_content);

        postName.setText(post.getUserName());
        postTimestamp.setText(post.getTimestamp());
        postContent.setText(post.getContent());

        // Go to profile on picture click
        postProfilePicture.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                profileIntent.putExtra(ProfileActivity.USER_NAME_EXTRA, post.getUserName());
                mContext.startActivity(profileIntent);
            }
        });

        // Set number of comments
        TextView postCommentCount = (TextView) convertView.findViewById(R.id.post_comment_count);
        postCommentCount.setText(Utils.numberOfCommentsString(post.getComments().size()));
    }
}
