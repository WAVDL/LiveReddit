package edu.osu.livereddit;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;

public class PostActivity extends AppCompatActivity {
    private static Submission post;
    private static String identifier;
    private static CommentNode comments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        identifier = getIntent().getStringExtra(ThreadsListActivity.POST_ID);

        CommentListTask commentListTask = new CommentListTask();
        commentListTask.execute((Void) null);
    }

    private void success() {
        // add post
        TextView postText = (TextView) findViewById(R.id.post_text);
        postText.setText(post.isSelfPost() ? post.getSelftext() : "");

        // add num comments
        TextView numCommentsTextView = (TextView) findViewById(R.id.num_comments);
        numCommentsTextView.setText(post.getCommentCount() + " Comments");

        // add comments
        TreeNode root = TreeNode.root();

        // the root comment is empty, only add children nodes
        for (CommentNode commentNode : comments.getChildren()) {
            addCommentsToTreeNode(root, commentNode);
        }

        // add comments tree to layout
        AndroidTreeView androidTreeView = new AndroidTreeView(this, root);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.comments_container);
        layout.addView(androidTreeView.getView());
        androidTreeView.expandAll();
    }

    // recursively adds comments to tree
    private void addCommentsToTreeNode(TreeNode node, CommentNode commentNode) {
        TreeNode comment = new TreeNode(commentNode).setViewHolder(new CommentsHolder(this));
        node.addChild(comment);

        for (CommentNode commentNode1 : commentNode.getChildren()) {
            addCommentsToTreeNode(comment, commentNode1);
        }
    }

    public class CommentListTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            post = GlobalVars.getRedditClient().getSubmission(identifier);
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

    public class CommentsHolder extends TreeNode.BaseNodeViewHolder<CommentNode> {
        public CommentsHolder(Context context) {
            super(context);
        }

        @Override
        public View createNodeView(TreeNode node, CommentNode value) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.comment_listview, null, false);

            // indent replies
            view.setPadding(((value.getDepth() - 1) * 25) + 5, 10, 0, 10);

            TextView bodyTextView = (TextView) view.findViewById(R.id.body);
            bodyTextView.setText(value.getComment().getBody());

            TextView authorTextView = (TextView) view.findViewById(R.id.author);
            authorTextView.setText(value.getComment().getAuthor());

            String date =  " • " + ThreadsListActivity.formatTime(value.getComment().getCreatedUtc());
            TextView scoreTextView = (TextView) view.findViewById(R.id.score);
            int score = value.getComment().getScore();
            if (score > 0) {
                scoreTextView.setText("+" + score + date);
            } else {
                scoreTextView.setText(score + date);
            }

            return view;
        }
    }
}
