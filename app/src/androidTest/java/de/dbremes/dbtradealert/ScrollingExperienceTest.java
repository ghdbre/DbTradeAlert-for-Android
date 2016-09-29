package de.dbremes.dbtradealert;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ScrollingExperienceTest {

    @Rule
    public ActivityTestRule<WatchlistListActivity> mActivityTestRule = new ActivityTestRule<>(WatchlistListActivity.class);

    @Test
    public void scrollingExperienceTest() {
        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list),
                        withParent(allOf(withId(R.id.container),
                                withParent(withId(R.id.main_content)))),
                        isDisplayed()));

        recyclerView.perform(actionOnItemAtPosition(0, swipeUp()));
        recyclerView.perform(actionOnItemAtPosition(0, swipeLeft()));
    }
}
