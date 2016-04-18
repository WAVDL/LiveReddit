package edu.osu.livereddit;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.managers.AccountManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ThreadsListActivity extends AppCompatActivity {

    public static final String POST_ID = "post_id";
    private RedditClient redditClient = GlobalVars.getRedditClient();
    private String subredditName;
    private List<Submission> submissions;
    SubredditPaginator subredditPaginator;
    private ThreadsArrayAdapter adapter;
    private boolean canFetchMore = true;
    private boolean isUserSubscribed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threads_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.threads_list_toolbar);
        setSupportActionBar(toolbar);

        subredditName = getIntent().getStringExtra(SubredditsList.SUBREDDIT_NAME);
        isUserSubscribed = getIntent().getBooleanExtra(SubredditsList.IS_SUBREDDIT_SUBSCRIBER, false);

        setTitle(subredditName);

        subredditPaginator = new SubredditPaginator(redditClient, subredditName);

        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
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
        }else{
           Toast.makeText(ThreadsListActivity.this,"Could not load content. :( Check your connection.",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.threads_list_menu, menu);
        if (isUserSubscribed) {
            menu.findItem(R.id.subreddit_subscribe).setTitle("unsubscribe");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.subreddit_subscribe:
                subscribeButtonClicked(item);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void subscribeButtonClicked(MenuItem item) {
        if (isUserSubscribed) {
            SubredditSubscribeTask subredditSubscribeTask = new SubredditSubscribeTask(false);
            subredditSubscribeTask.execute();

            item.setTitle("subscribe");
            isUserSubscribed = false;
        } else {
            SubredditSubscribeTask subredditSubscribeTask = new SubredditSubscribeTask(true);
            subredditSubscribeTask.execute();

            item.setTitle("unsubscribe");
            isUserSubscribed = true;
        }
    }

    private void subredditSubscribeSucess(boolean didSubscribe) {

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
            List<Submission> list;
            try {
                list = subredditPaginator.next().getChildren();
                canFetchMore = subredditPaginator.hasNext();

                if (params[0] == 1) {
                    submissions = list;
                } else {
                    submissions.addAll(list);
                }
            }catch(RuntimeException e){
                Toast.makeText(ThreadsListActivity.this, "Could not retrieve content. Check your connection.", Toast.LENGTH_LONG).show();
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
    }

    public class SubredditSubscribeTask extends AsyncTask<Void, Void, Boolean> {
        private boolean shouldSubscribe;

        public SubredditSubscribeTask(boolean shouldSubscribe) {
            this.shouldSubscribe = shouldSubscribe;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            AccountManager accountManager = new AccountManager(redditClient);
            Subreddit subreddit = redditClient.getSubreddit(subredditName);

            if (shouldSubscribe) {
                accountManager.subscribe(subreddit);
            } else {
                accountManager.unsubscribe(subreddit);
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                subredditSubscribeSucess(shouldSubscribe);
            }
        }
    }

    public static String formatTime(Date date) {
        Date now = new Date();
        long seconds = (now.getTime() - date.getTime()) / 1000;
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 60 * 60) {
            return seconds / 60 + "m";
        } else if (seconds < 60 * 60 * 24) {
            return seconds / 60 / 60 + "h";
        }
        return seconds / 60 / 60 / 24 + "d";
    }
}
