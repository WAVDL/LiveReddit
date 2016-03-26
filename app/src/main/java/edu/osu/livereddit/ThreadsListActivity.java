package edu.osu.livereddit;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.UserSubredditsPaginator;

public class ThreadsListActivity extends AppCompatActivity {
    private RedditClient redditClient = GlobalVars.getRedditClient();
    private String subredditName;
    private Listing<Submission> submissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threads_list);

        subredditName = getIntent().getStringExtra("subreddit_name");
        ThreadsListTask threadsListTask = new ThreadsListTask();
        threadsListTask.execute((Void) null);
    }

    private void success() {
        if (!submissions.isEmpty()) {
            String[] list = new String[25];
            for (int i = 0; i < submissions.size(); i++) {
                list[i] = submissions.get(i).getTitle();
            }

            ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.threads_listview, list);
            ListView listView = (ListView) findViewById(R.id.threads_list);
            listView.setAdapter(adapter);

//            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    Intent intent = new Intent(SubredditsList.this, ThreadsListActivity.class);
//                    intent.putExtra("subreddit_name", submissions.get(position).getDisplayName());
//                    startActivity(intent);
//                }
//            });
        }
    }

    public class ThreadsListTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            System.out.println(redditClient.isAuthenticated());
            SubredditPaginator subredditPaginator = new SubredditPaginator(redditClient, subredditName);
            submissions = subredditPaginator.next();
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
