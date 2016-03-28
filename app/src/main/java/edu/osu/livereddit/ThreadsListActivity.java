package edu.osu.livereddit;

import android.content.Context;
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

import java.util.Date;

public class ThreadsListActivity extends AppCompatActivity {
    private RedditClient redditClient = GlobalVars.getRedditClient();
    private String subredditName;
    private Submission[] submissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threads_list);

        subredditName = getIntent().getStringExtra("subreddit_name");

        setTitle(subredditName);

        ThreadsListTask threadsListTask = new ThreadsListTask();
        threadsListTask.execute((Void) null);
    }

    private void success() {
        ThreadsArrayAdapter adapter = new ThreadsArrayAdapter(this, submissions);
        ListView listView = (ListView) findViewById(R.id.threads_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(ThreadsListActivity.this, ThreadActivity.class);
//                intent.putExtra("thread_full_name", submissions[position].getFullName());
//                startActivity(intent);
            }
        });
    }

    public class ThreadsListTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            SubredditPaginator subredditPaginator = new SubredditPaginator(redditClient, subredditName);
            Listing<Submission> list = subredditPaginator.next();

            submissions = new Submission[list.size()];
            for (int i = 0; i < submissions.length; i++) {
                submissions[i] = list.get(i);
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
        private final Submission[] values;

        public ThreadsArrayAdapter(Context context, Submission[] values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.threads_listview, parent, false);

            Submission submission = values[position];

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
