package de.dbremes.dbtradealert;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

import de.dbremes.dbtradealert.DbAccess.DbHelper;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ScrollingExperienceTest {
    private static final String CLASS_NAME = "ScrollingExperienceTest";
    private SQLiteDatabase db;

    @Rule
    public ActivityTestRule<WatchlistListActivity> mActivityTestRule
            = new ActivityTestRule<>(WatchlistListActivity.class);

    @Before
    public void createTestData() {
        String testDataString = "";
        Context testContext = InstrumentationRegistry.getContext();
        try {
            InputStream testDataStream
                    = testContext.getResources().getAssets().open("ch_securities.csv");
            byte[] testData = new byte[testDataStream.available()];
            testDataStream.read(testData);
            testDataString = new String(testData);
        } catch (IOException e) {
            PlayStoreHelper.logError(e);
        }
        Context targetContext = InstrumentationRegistry.getTargetContext();
        DbHelper dbHelper = new DbHelper(targetContext);
        // SQLite Ids start with 1
        long watchlistId = 1;
        Log.d(CLASS_NAME, "createTestData(): calling importTestSecurities()");
        dbHelper.importTestSecurities(testDataString, watchlistId);
    } // createTestData()

    @After
    public void deleteTestData() {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        DbHelper dbHelper = new DbHelper(targetContext);
        dbHelper.deleteTestSecurities();
    } // deleteTestData()

    @Test
    public void scrollingExperienceTest() {
        Log.d(CLASS_NAME, "scrollingExperienceTest(): start");
        // Make added securities show up in watchlist:
        Log.d(CLASS_NAME, "scrollingExperienceTest(): tapping Refresh");
        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.action_refresh), withContentDescription("Refresh"), isDisplayed()));
        actionMenuItemView.perform(click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list),
                        withParent(allOf(withId(R.id.container),
                                withParent(withId(R.id.main_content)))),
                        isDisplayed()));
        for (int i = 0; i < 8; i++) {
            Log.d(CLASS_NAME, "scrollingExperienceTest(): swipeUp" + i);
            recyclerView.perform(actionOnItemAtPosition(i, swipeUp()));
        }
        Log.d(CLASS_NAME, "scrollingExperienceTest(): the End");
    } // scrollingExperienceTest()
} // class ScrollingExperienceTest
