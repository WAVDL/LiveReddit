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
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.List;

public class SubredditsList extends AppCompatActivity {
    private RedditClient redditClient = GlobalVars.getRedditClient();
    private List<Subreddit> subreddits;
    private UserSubredditsPaginator userSubredditsPaginator = new UserSubredditsPaginator(redditClient, "subscriber");
    private ArrayAdapter adapter;
    private boolean canFetchMore = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddits_list);

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
                        Intent intent = new Intent(SubredditsList.this, ThreadsListActivity.class);
                        intent.putExtra("subreddit_name", subreddits.get(position).getDisplayName());
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
}