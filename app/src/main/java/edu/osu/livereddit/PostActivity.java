package edu.osu.livereddit;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.dean.jraw.models.LiveThread;
import net.dean.jraw.models.RedditObject;
import net.dean.jraw.paginators.RedditIterable;

public class PostActivity extends AppCompatActivity {

    private static final String POST_IDENTIFIER = "edu.osu.LiveReddit.post_identifier";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        String identifier = getIntent().getStringExtra(POST_IDENTIFIER);
        
    }


    public static Intent newIntent(Context packageContext, String postIdentifier) {
        Intent i = new Intent(packageContext, PostActivity.class);
        i.putExtra(POST_IDENTIFIER, postIdentifier);
        return i;
    }
}
