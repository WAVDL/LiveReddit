package edu.osu.livereddit;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;

import java.util.Date;

public class PostActivity extends AppCompatActivity {

    private static Submission post;
    private static TextView postContentView;
    private static TextView postScoreView;
    private static TextView postDetailsView;
    private static TextView postCommentsView;
    private static String identifier;
    private static String postTitle;
    private static String postAuthor;
    private static int postScore;
    private static String postSelfText;
    private static CommentNode comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        identifier = getIntent().getStringExtra(ThreadsListActivity.POST_ID);
        postContentView = (TextView)findViewById(R.id.post_content);
        postScoreView = (TextView)findViewById(R.id.post_score);
        postDetailsView = (TextView) findViewById(R.id.post_details);


        CommentListTask commentListTask = new CommentListTask();
        commentListTask.execute((Void) null);
    }

    private String formatTime(Date date) {
        Date now = new Date();
        long minutes = (now.getTime() - date.getTime()) / 1000 / 60;
        if (minutes < 60) {
            return minutes + "m";
        } else if (minutes < 60 * 24) {
            return minutes / 60 + "h";
        }
        return minutes / 60 / 24 + "d";
    }

    private void success() {

        postContentView.setText(postTitle + "\n---\n"+ postSelfText);
        postScoreView.setText(Integer.toString(postScore));
        String submissionTime = formatTime(post.getCreatedUtc());
        postDetailsView.setText(post.getCommentCount() + " Comments • " + submissionTime+"\n\n");

        TreeNode root = TreeNode.root();
        // the root comment is empty, only add children nodes
        for (CommentNode commentNode : comments.getChildren()) {
            addCommentsToTreeNode(root, commentNode);
        }

        // add comments tree to layout
        AndroidTreeView androidTreeView = new AndroidTreeView(this, root);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.post_comments);
        linearLayout.addView(androidTreeView.getView());
    }

    // recursively adds comments to tree
    private void addCommentsToTreeNode(TreeNode node, CommentNode commentNode) {
        TreeNode comment = new TreeNode(commentNode.getComment().getBody());
        node.addChild(comment);

        for (CommentNode commentNode1 : commentNode.getChildren()) {
            addCommentsToTreeNode(comment, commentNode1);
        }
    }

    public class CommentListTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            post = GlobalVars.getRedditClient().getSubmission(identifier);
            postSelfText = post.getSelftext();
            postTitle = post.getTitle();
            postAuthor = post.getAuthor();
            postScore = post.getScore();
            comments = post.getComments();

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                success();
            }
        }
    }
}
