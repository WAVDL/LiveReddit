package edu.osu.livereddit;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;

import java.util.List;

public class PostActivity extends AppCompatActivity {

    private static final String POST_IDENTIFIER = "edu.osu.LiveReddit.post_identifier";
    private static Submission post;
    private static TextView postText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        String identifier = getIntent().getStringExtra(POST_IDENTIFIER);
        post = GlobalVars.getRedditClient().getSubmission(identifier);
        postText = (TextView)findViewById(R.id.post_text);
        postText.setText(post.getSelftext());
        ListView commentListView = (ListView)findViewById(R.id.comment_list);
        CommentNode commentNode = post.getComments();
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.comment_listview);
        commentListView.setAdapter(adapter);
    }


    private static String[] populateCommentList(CommentNode commentNode){


        String[] list = new String[25];
        int i=0;
        List<CommentNode> comments = commentNode.getChildren();
        for(CommentNode comment: comments){
            list[i] = comment.getComment().getBody();
            i++;
        }
        return list;
    }


    public static Intent newIntent(Context packageContext, String postIdentifier) {
        Intent i = new Intent(packageContext, PostActivity.class);
        i.putExtra(POST_IDENTIFIER, postIdentifier);
        return i;
    }
}

