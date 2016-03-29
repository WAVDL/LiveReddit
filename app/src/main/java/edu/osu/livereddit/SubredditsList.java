package edu.osu.livereddit;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.List;

public class SubredditsList extends AppCompatActivity {
    public static final String SUBREDDIT_NAME = "subreddit_name";
    public static final String IS_SUBREDDIT_SUBSCRIBER = "is_subreddit_subscriber";
    private RedditClient redditClient = GlobalVars.getRedditClient();
    private List<Subreddit> subreddits;
    private UserSubredditsPaginator userSubredditsPaginator = new UserSubredditsPaginator(redditClient, "subscriber");
    private ArrayAdapter adapter;
    private boolean canFetchMore = true;
    private Subreddit searchedSubreddit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddits_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.subreddits_toolbar);
        setSupportActionBar(toolbar);

        SubredditsListTask subredditsListTask = new SubredditsListTask();
        subredditsListTask.execute(1);

        ListView listView = (ListView) findViewById(R.id.subreddits_list);
        listView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                if (canFetchMore) {
                    SubredditsListTask subredditsListTask = new SubredditsListTask();
                    subredditsListTask.execute(page);
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.subreddits_menu, menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.subreddit_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SubredditsSearchTask subredditsSearchTask = new SubredditsSearchTask(query);
                subredditsSearchTask.execute();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    private void success() {
        if (!subreddits.isEmpty()) {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < subreddits.size(); i++) {
                list.add(subreddits.get(i).getDisplayName());
            }

            // if first fetch
            if (adapter == null) {
                adapter = new ArrayAdapter<String>(this, R.layout.subreddits_listview, list);
                ListView listView = (ListView) findViewById(R.id.subreddits_list);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        startThreadsListActivity(subreddits.get(position));
                    }
                });
            } else {
                adapter.clear();
                adapter.addAll(list);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void startThreadsListActivity(Subreddit subreddit) {
        Intent intent = new Intent(this, ThreadsListActivity.class);
        intent.putExtra(SUBREDDIT_NAME, subreddit.getDisplayName());
        intent.putExtra(IS_SUBREDDIT_SUBSCRIBER, subreddit.isUserSubscriber());
        startActivity(intent);
    }

    private void subredditSearchCompletion() {
        if (searchedSubreddit != null) {
            startThreadsListActivity(searchedSubreddit);
        }
    }

    public class SubredditsListTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            List<Subreddit> list = userSubredditsPaginator.next().getChildren();
            canFetchMore = userSubredditsPaginator.hasNext();

            if (params[0] == 1) {
                subreddits = list;
            } else {
                subreddits.addAll(list);
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

    public class SubredditsSearchTask extends AsyncTask<Void, Void, Boolean> {
        private String subredditName;

        public SubredditsSearchTask(String subredditName) {
            this.subredditName = subredditName;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                searchedSubreddit = redditClient.getSubreddit(subredditName);
            } catch (Exception e) { // subreddit doesn't exist
                searchedSubreddit = null;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            subredditSearchCompletion();
        }
    }
}