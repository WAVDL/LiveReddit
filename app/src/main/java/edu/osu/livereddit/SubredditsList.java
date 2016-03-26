package edu.osu.livereddit;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.List;

public class SubredditsList extends AppCompatActivity {
    private RedditClient redditClient = GlobalVars.getRedditClient();
    private Listing<Subreddit> subreddits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddits_list);

        SubredditsListTask subredditsListTask = new SubredditsListTask();
        subredditsListTask.execute((Void) null);
    }

    private void success() {
        if (!subreddits.isEmpty()) {
            String[] list = new String[25];
            for (int i = 0; i < subreddits.size(); i++) {
                list[i] = subreddits.get(i).getDisplayName();
            }

            ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.subreddits_listview, list);
            ListView listView = (ListView) findViewById(R.id.subreddits_list);
            listView.setAdapter(adapter);
        }
    }

    public class SubredditsListTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            UserSubredditsPaginator userSubredditsPaginator = new UserSubredditsPaginator(redditClient, "subscriber");
            subreddits = userSubredditsPaginator.next();
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