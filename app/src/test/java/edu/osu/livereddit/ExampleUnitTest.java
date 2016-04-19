package edu.osu.livereddit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginEspressoTest {

    @Test
    public void Login_Normal() {
        onView(withId(R.id.username)).perform(typeText("liveredditosu")).check(matches(withText("liveredditosu")));
        onView(withId(R.id.password)).perform(typeText("123123")).check(matches(withText("123123")));
        onView(withId(R.id.login)).perform(click());

    }
}