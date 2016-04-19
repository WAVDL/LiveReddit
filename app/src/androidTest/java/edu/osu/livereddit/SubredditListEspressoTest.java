package edu.osu.livereddit;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matcher.*;
import static org.hamcrest.Matchers.anything;

/**
 * Created by WAVDL on 4/18/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SubredditListEspressoTest {

    @Rule
    public ActivityTestRule<LoginActivity> activityTestRule =
            new ActivityTestRule<>(LoginActivity.class);

    @Before
    public void login(){
        onView(withId(R.id.username)).perform(typeText("liveredditosu")).check(matches(withText("liveredditosu")));
        onView(withId(R.id.password)).perform(typeText("123123")).check(matches(withText("123123")));
        onView(withId(R.id.login)).perform(click());
    }

    @Test
    public void clickOnFirstSubreddit() {
        onData(anything()).inAdapterView(withId(R.id.subreddits_list)).atPosition(1).perform(click());
    }

    @Test
    public void clickOnSecondSubreddit() {
        onData(anything()).inAdapterView(withId(R.id.subreddits_list)).atPosition(2).perform(click());
    }

    @Test
    public void clickOnTenthSubreddit() {
        onData(anything()).inAdapterView(withId(R.id.subreddits_list)).atPosition(10).perform(click());
    }




}
