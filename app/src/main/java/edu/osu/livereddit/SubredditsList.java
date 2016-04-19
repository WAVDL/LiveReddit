package edu.osu.livereddit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubredditsList extends AppCompatActivity {
    public static final String SUBREDDIT_NAME = "subreddit_name";
    public static final String IS_SUBREDDIT_SUBSCRIBER = "is_subreddit_subscriber";
    private RedditClient redditClient = GlobalVars.getRedditClient();
    private List<Subreddit> subreddits;
    private Set<String> pinnedSubreddits;
    private UserSubredditsPaginator userSubredditsPaginator = new UserSubredditsPaginator(redditClient, "subscriber");
    private SubredditsArrayAdapter adapter;
    private boolean canFetchMore = true;
    private Subreddit searchedSubreddit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddits_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.subreddits_toolbar);
        setSupportActionBar(toolbar);


        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            Button voiceButton = (Button) findViewById(R.id.search_voice_btn);
            voiceButton.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v){
                        Intent voiceIntent = new Intent(SubredditsList.this,SpeechRecognitionActivity.class);
                        startActivityForResult(voiceIntent, 0);

                        //getResult and send to SubredditsSearchTask in onActivityResult
                    }


            });
            SubredditsListTask subredditsListTask = new SubredditsListTask();
            subredditsListTask.execute(1);

            ListView listView = (ListView) findViewById(R.id.subreddits_list);
            listView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
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
        }else{
            Toast.makeText(SubredditsList.this, "Could not load content. :( Check your connection.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (0) : {
                if (resultCode == Activity.RESULT_OK) {
                    String query = data.getStringExtra("SUBREDDITNAME");
                    SubredditsSearchTask subredditsSearchTask = new SubredditsSearchTask(query);
                    subredditsSearchTask.execute();
                }
                break;
            }
        }
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
            // make custom subreddit object list
            final List<LRSubreddit> list = new ArrayList<>();
            // add fetched subscribed subreddits
            for (Subreddit subreddit : subreddits) {
                boolean isPinned = pinnedSubreddits.contains(subreddit.getDisplayName());
                LRSubreddit lrSubreddit = new LRSubreddit(subreddit.getDisplayName(), true, isPinned);
                list.add(lrSubreddit);
            }

            // add pinned subbreddits if not already added
            for (String pinnedSubreddit : pinnedSubreddits) {
                if (!subredditsListContains(pinnedSubreddit)) {
                    list.add(new LRSubreddit(pinnedSubreddit, false, true));
                }
            }

            // add pinned subreddits to the beginning of the list
            for (int i = 0; i < list.size(); i++) {
                LRSubreddit lrSubreddit = list.get(i);
                if (lrSubreddit.isPinned) {
                    list.remove(i);
                    list.add(0, lrSubreddit);
                }
            }

            // if first fetch
            if (adapter == null) {
                adapter = new SubredditsArrayAdapter(this, list);
                ListView listView = (ListView) findViewById(R.id.subreddits_list);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        startThreadsListActivity(list.get(position));
                    }
                });
            } else {
                adapter.clear();
                adapter.addAll(list);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private boolean subredditsListContains(String subredditName) {
        for (String s : pinnedSubreddits) {
            if (s.equals(subredditName)) {
                return true;
            }
        }
        return false;
    }

    private void startThreadsListActivity(LRSubreddit lrSubreddit) {
        Intent intent = new Intent(this, ThreadsListActivity.class);
        intent.putExtra(SUBREDDIT_NAME, lrSubreddit.subredditDisplayName);
        intent.putExtra(IS_SUBREDDIT_SUBSCRIBER, lrSubreddit.isSubscriber);
        startActivity(intent);
    }

    private void subredditSearchCompletion() {
        if (searchedSubreddit != null) {
            startThreadsListActivity(new LRSubreddit(searchedSubreddit.getDisplayName(), searchedSubreddit.isUserSubscriber(), false));
        }
    }

    public class SubredditsListTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            // get stored pinned subbreddits
            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.user_preferences_key), MODE_PRIVATE);
            Set<String> stringSet = sharedPreferences.getStringSet(getString(R.string.pinned_subbreddits_key), null);
            pinnedSubreddits = new HashSet<>();
            if (stringSet != null) {
                pinnedSubreddits.addAll(stringSet);
            }

            // get subscribed subreddits
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

    public class SubredditsArrayAdapter extends ArrayAdapter {
        private final Context context;
        private final List<LRSubreddit> values;

        public SubredditsArrayAdapter(Context context, List<LRSubreddit> values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, final View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.subreddits_listview, parent, false);

            final LRSubreddit lrSubreddit = values.get(position);

            TextView nameTextView = (TextView) rowView.findViewById(R.id.subreddit_name);
            nameTextView.setText(lrSubreddit.subredditDisplayName);

            ImageButton subredditPinButton = (ImageButton) rowView.findViewById(R.id.pin_subreddit);
            if (lrSubreddit.isPinned) {
                subredditPinButton.setBackgroundResource(R.drawable.subreddit_pinned);
            }

            subredditPinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPreferences = context.getSharedPreferences(getString(R.string.user_preferences_key), context.MODE_PRIVATE);
                    // create a new set since you should never modify the return value of getStringSet
                    Set<String> stringSet = new HashSet<>(sharedPreferences.getStringSet(getString(R.string.pinned_subbreddits_key), new HashSet<String>()));

                    if (lrSubreddit.isPinned) {
                        stringSet.remove(lrSubreddit.subredditDisplayName);
                        v.setBackgroundResource(R.drawable.subreddit_unpinned);
                    } else {
                        stringSet.add(lrSubreddit.subredditDisplayName);
                        v.setBackgroundResource(R.drawable.subreddit_pinned);
                    }
                    lrSubreddit.isPinned = !lrSubreddit.isPinned;

                    sharedPreferences.edit()
                            .putStringSet(getString(R.string.pinned_subbreddits_key), stringSet)
                            .apply();
                }
            });

            return rowView;
        }
    }
}

class LRSubreddit {
    public String subredditDisplayName;
    public boolean isSubscriber;
    public boolean isPinned;

    public LRSubreddit(String subredditDisplayName, boolean isSubscriber, boolean isPinned) {
        this.subredditDisplayName = subredditDisplayName;
        this.isSubscriber = isSubscriber;
        this.isPinned = isPinned;
    }
}






