package de.dbremes.dbtradealert;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.NearMiss;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.List;

import de.dbremes.dbtradealert.DbAccess.DbHelper;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.findNearMissesForAllUnmatched;
import static com.github.tomakehurst.wiremock.client.WireMock.findUnmatchedRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getAllServeEvents;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ScrollingExperienceTest {
    private static final String CLASS_NAME = "ScrollingExperienceTest";
    private String symbolParameterValue;

    @Rule
    public ActivityTestRule<WatchlistListActivity> mActivityTestRule
            = new ActivityTestRule<>(WatchlistListActivity.class);

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(Integer.valueOf(BuildConfig.PORT));

    private void createTestData() {
        Log.v(CLASS_NAME, "createTestData(): start");
        String testDataString = readFileFromTestAssets("ch_securities.csv");
        Context targetContext = InstrumentationRegistry.getTargetContext();
        DbHelper dbHelper = new DbHelper(targetContext);
        // SQLite Ids start with 1; 1 == CH watchlist
        long watchlistId = 1;
        dbHelper.importTestSecurities(testDataString, watchlistId);
    } // createTestData()

    @After
    public void deleteTestData() {
        Log.v(CLASS_NAME, "@After - deleteTestData(): start");
        Context targetContext = InstrumentationRegistry.getTargetContext();
        DbHelper dbHelper = new DbHelper(targetContext);
        dbHelper.deleteTestSecurities();
    } // deleteTestData()

    private void logRequests() {
        final String METHOD_NAME = "logRequests";
        // allServeEvents
        List<ServeEvent> allServeEvents = getAllServeEvents();
        Log.v(CLASS_NAME, METHOD_NAME + "(): allServeEvents.size() = " + allServeEvents.size());
        for (int i = 0; i < allServeEvents.size(); i++) {
            Log.v(CLASS_NAME, String.format("%s(): allServeEvents[%d].Url = %s",
                    METHOD_NAME, i, allServeEvents.get(i).getRequest().getUrl()));
        }
        // unmatchedRequests
        List<LoggedRequest> unmatchedRequests = findUnmatchedRequests();
        Log.v(CLASS_NAME, METHOD_NAME + "(): unmatchedRequests.size() = " + unmatchedRequests.size());
        for (int i = 0; i < unmatchedRequests.size(); i++) {
            Log.v(CLASS_NAME, String.format("%s(): unmatchedRequests[%d] = %s",
                    METHOD_NAME, i, unmatchedRequests.get(i).toString()));
        }
        // nearMisses
        // Currently WireMock reports expected requests as received, too
        // See https://github.com/tomakehurst/wiremock/issues/484
        List<NearMiss> nearMisses = findNearMissesForAllUnmatched();
        Log.v(CLASS_NAME, METHOD_NAME + "(): nearMisses.size() = " + nearMisses.size());
        for (int i = 0; i < nearMisses.size(); i++) {
            Log.v(CLASS_NAME, String.format("%s(): nearMisses[%d].Diff = %s",
                    METHOD_NAME, i, nearMisses.get(i).getDiff()));
        }
    } // logRequests()

    @Before
    public void prepareTest() {
        Log.v(CLASS_NAME, "@Before - prepareTest(): start");
        try {
            // Must match imported symbols from ch_securities.csv + sample symbols from install:
            symbolParameterValue = URLEncoder.encode(
                    "ABBN.VX+BAYN.DE+CFR.VX+NESN.VX+NOVN.VX+ROG.VX+SIE.DE+SYNN.VX+UBSG.VX+ZURN.VX",
                    "utf-8");
        } catch (UnsupportedEncodingException e) {
            PlayStoreHelper.logError(e);
        }
        createTestData();
        setupWireMock();
    } // prepareTest()

    private String readFileFromTestAssets(String fileName) {
        String fileContent = "";
        Context testContext = InstrumentationRegistry.getContext();
        try {
            InputStream testDataStream
                    = testContext.getResources().getAssets().open(fileName);
            byte[] testData = new byte[testDataStream.available()];
            int bytesRead = testDataStream.read(testData);
            fileContent = new String(testData);
        } catch (IOException e) {
            PlayStoreHelper.logError(e);
        }
        return fileContent;
    } // readFileFromTestAssets()

    private void setupWireMock() {
        Log.v(CLASS_NAME, "setupWireMock(): start");
        String quotesCsv = readFileFromTestAssets("quotes.csv");
        wireMockRule.stubFor(get(urlPathMatching(".*"))
                .withQueryParam("f", equalTo(DbHelper.QUOTE_DOWNLOAD_FORMAT_PARAMETER))
                // Currently WireMock fails to match URL encoded parameters
                // See https://github.com/tomakehurst/wiremock/issues/515
                //.withQueryParam("s", equalTo(symbolParameterValue))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withBody(quotesCsv)));
    } // setupWireMock()

    @Test
    public void scrollingExperienceTest() throws VerificationException {
        final String METHOD_NAME = "scrollingExperienceTest";
        Log.v(CLASS_NAME, METHOD_NAME + "(): start");
        // Make added securities show up in watchlist:
        Log.v(CLASS_NAME, METHOD_NAME + "(): tapping Refresh");
        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.action_refresh), withContentDescription("Refresh"), isDisplayed()));
        actionMenuItemView.perform(click());

        // Checking request was correct helps to track down reasons for failed tests
        verify(1, getRequestedFor(urlPathMatching(".*"))
                .withQueryParam("f", equalTo(DbHelper.QUOTE_DOWNLOAD_FORMAT_PARAMETER))
                // Currently WireMock fails to match URL encoded parameters
                // See https://github.com/tomakehurst/wiremock/issues/515
                //.withQueryParam("s", equalTo(symbolParameterValue))
        );
        Log.v(CLASS_NAME, METHOD_NAME + "(): verified correctness of request");

        // Scroll items to trigger creation of new items
        // Visually check if scrolling stutters -> test failed
        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list),
                        withParent(allOf(withId(R.id.container),
                                withParent(withId(R.id.main_content)))),
                        isDisplayed()));
        for (int i = 0; i < 8; i++) {
            Log.v(CLASS_NAME, METHOD_NAME + "(): swipeUp" + i);
            recyclerView.perform(actionOnItemAtPosition(i, swipeUp()));
        }
        logRequests();
        Log.v(CLASS_NAME, METHOD_NAME + "(): the End");
    } // scrollingExperienceTest()
} // class ScrollingExperienceTest
