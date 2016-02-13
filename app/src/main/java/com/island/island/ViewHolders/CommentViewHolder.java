package com.island.island.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.island.island.R;

/**
 * Created by poo on 2/3/2016.
 */
public class CommentViewHolder extends RecyclerView.ViewHolder
{
    public ImageView profileImage;
    public TextView userName;
    public TextView comment;

    public CommentViewHolder(View itemView)
    {
        super(itemView);
        profileImage = (ImageView) itemView.findViewById(R.id.comment_profile_image);
        userName = (TextView) itemView.findViewById(R.id.comment_user_name);
        comment = (TextView) itemView.findViewById(R.id.comment);
    }
}