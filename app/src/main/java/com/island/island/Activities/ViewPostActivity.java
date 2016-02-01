package com.island.island.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import com.island.island.Adapters.ViewPostAdapter;
import com.island.island.Containers.Comment;
import com.island.island.Containers.Post;
import com.island.island.Database.IslandDB;
import com.island.island.R;

import java.util.ArrayList;
import java.util.List;


public class ViewPostActivity extends AppCompatActivity
{
    Post post = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Boilerplate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // List view stuff
        ArrayList viewPostList = new ArrayList<>();
        ViewPostAdapter viewPostAdapter = new ViewPostAdapter(this, viewPostList);
        ListView listView = (ListView) findViewById(R.id.view_post_list_view);
        listView.setAdapter(viewPostAdapter);

        // Get intent with post info
        Intent intent = getIntent();
        post = (Post)intent.getSerializableExtra(Post.POST_EXTRA);
        viewPostAdapter.add(post);

        // Add comments to list
        // TEST for now
        List<Comment> comments = post.getComments();
        for(int i = 0; i < comments.size(); ++i)
        {
            viewPostAdapter.add(comments.get(i));
        }
    }

    public void addCommentToPost(View view)
    {
        EditText addCommentEditText = (EditText) findViewById(R.id.post_comment_edit_text);
        String commentText = addCommentEditText.getText().toString();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        if(commentText.equals(""))
        {
            Snackbar.make(view, getString(R.string.empty_comment_post),
                    Snackbar.LENGTH_SHORT).setAction("Dismiss", null).show();
        }
        else
        {
            IslandDB.addCommentToPost(post, new Comment("", commentText));
            addCommentEditText.setText("");
            imm.hideSoftInputFromWindow(addCommentEditText.getWindowToken(), 0);
        }
    }
}
