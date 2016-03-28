package edu.osu.livereddit;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
    private static ListView commentListView;
    private static String identifier;
    private static String postContent;
    private static String[] comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        identifier = getIntent().getStringExtra(POST_IDENTIFIER);
        postText = (TextView)findViewById(R.id.post_text);
        commentListView = (ListView)findViewById(R.id.comment_list);

        CommentListTask commentListTask = new CommentListTask();
        commentListTask.execute((Void) null);


    }

    private void success(){
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.comment_listview, comments);
        commentListView.setAdapter(adapter);
    }

    public class CommentListTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            post = GlobalVars.getRedditClient().getSubmission(identifier);
            postContent = post.getSelftext();
            comments = populateCommentList();

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                success();
            }
        }
    }

    private static String[] populateCommentList(){
        CommentNode commentNode = post.getComments();
        String[] list = new String[30];
        int i=0;
        List<CommentNode> commentNodeList = commentNode.getChildren();
        for(CommentNode comment: commentNodeList){
            if(i>24)
            {
                break;
            }
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

