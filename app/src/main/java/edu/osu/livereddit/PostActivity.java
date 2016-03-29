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

public class PostActivity extends AppCompatActivity {

    private static Submission post;
    private static TextView postText;
    private static String identifier;
    private static String postContent;
    private static CommentNode comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        identifier = getIntent().getStringExtra(ThreadsListActivity.POST_ID);
        postText = (TextView)findViewById(R.id.post_text);

        CommentListTask commentListTask = new CommentListTask();
        commentListTask.execute((Void) null);
    }

    private void success() {
        TreeNode root = TreeNode.root();

        // the root comment is empty, only add children nodes
        for (CommentNode commentNode : comments.getChildren()) {
            addCommentsToTreeNode(root, commentNode);
        }

        // add comments tree to layout
        AndroidTreeView androidTreeView = new AndroidTreeView(this, root);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.post_container);
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
            postContent = post.getSelftext();
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
