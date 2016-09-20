package de.dbremes.dbtradealert;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.core.AllOf.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddSecurityTest {

    @Rule
    public ActivityTestRule<WatchlistListActivity> mActivityTestRule = new ActivityTestRule<>(WatchlistListActivity.class);

    @Test
    public void addSecurityTest() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.title), withText("Manage Securities"), isDisplayed()));
        appCompatTextView.perform(click());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.newButton), withText("New"), isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.symbolEditText), isDisplayed()));
        appCompatEditText.perform(replaceText("CSGN.VX"), closeSoftKeyboard());

        pressBack();

        ViewInteraction appCompatCheckedTextView = onView(
                allOf(withId(android.R.id.text1), withText("CH Stocks"),
                        childAtPosition(
                                withId(R.id.watchlistsListView),
                                0),
                        isDisplayed()));
        appCompatCheckedTextView.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.okButton), withText("OK"), isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.okButton), withText("OK"), isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.action_refresh), withContentDescription("Refresh"), isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.securityNameTextView), withText("CS GROUP N"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.list),
                                        0),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("CS GROUP N")));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
