package edu.osu.livereddit;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ThreadsListActivity extends AppCompatActivity {

    public static final String POST_ID = "post_id";
    private RedditClient redditClient = GlobalVars.getRedditClient();
    private String subredditName;
    private List<Submission> submissions;
    SubredditPaginator subredditPaginator = new SubredditPaginator(redditClient, subredditName);
    private ThreadsArrayAdapter adapter;
    private boolean canFetchMore = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threads_list);

        subredditName = getIntent().getStringExtra(SubredditsList.SUBREDDIT_NAME);

        setTitle(subredditName);

        ThreadsListTask subredditsListTask = new ThreadsListTask();
        subredditsListTask.execute(1);

        ListView listView = (ListView) findViewById(R.id.threads_list);
        listView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                if (canFetchMore) {
                    ThreadsListTask subredditsListTask = new ThreadsListTask();
                    subredditsListTask.execute(page);
                }
                return true;
            }
        });
    }

    private void success() {
        if (!submissions.isEmpty()) {
            List<Submission> list = new ArrayList<>();
            for (int i = 0; i < submissions.size(); i++) {
                list.add(submissions.get(i));
            }

            // if first fetch
            if (adapter == null) {
                adapter = new ThreadsArrayAdapter(this, submissions);
                ListView listView = (ListView) findViewById(R.id.threads_list);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(ThreadsListActivity.this, PostActivity.class);
                        intent.putExtra(POST_ID, submissions.get(position).getId());
                        startActivity(intent);
                    }
                });
            } else {
                adapter.clear();
                adapter.addAll(list);
                adapter.notifyDataSetChanged();
            }
        }
    }

    public class ThreadsListTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            List<Submission> list = subredditPaginator.next().getChildren();
            canFetchMore = subredditPaginator.hasNext();

            if (params[0] == 1) {
                submissions = list;
            } else {
                submissions.addAll(list);
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                success();
            }
        }
    }

    public class ThreadsArrayAdapter extends ArrayAdapter {
        private final Context context;
        private final List<Submission> values;

        public ThreadsArrayAdapter(Context context, List<Submission> values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.threads_listview, parent, false);

            Submission submission = values.get(position);

            TextView scoreTextView = (TextView) rowView.findViewById(R.id.score);
            scoreTextView.setText(submission.getScore() + "");

            TextView titleTextView = (TextView) rowView.findViewById(R.id.title);
            titleTextView.setText(submission.getTitle());

            String submissionTime = formatTime(submission.getCreatedUtc());
            TextView detailsTextView = (TextView) rowView.findViewById(R.id.details);
            detailsTextView.setText(submission.getCommentCount() + " Comments • " + submissionTime);

            return rowView;
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
    }
}
