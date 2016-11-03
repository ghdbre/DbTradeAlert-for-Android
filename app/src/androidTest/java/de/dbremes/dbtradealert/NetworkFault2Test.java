package de.dbremes.dbtradealert;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class NetworkFault2Test {
    private static final String CLASS_NAME = "NetworkFault2Test";

    @Rule
    public ActivityTestRule<WatchlistListActivity> mActivityTestRule
            = new ActivityTestRule<>(WatchlistListActivity.class);

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(Integer.valueOf(BuildConfig.PORT));

    @Test
    public void networkFaultTest() throws VerificationException {
        final String METHOD_NAME = "networkFaultTest";
        Log.v(CLASS_NAME, METHOD_NAME + "(): start");
        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.action_refresh), withContentDescription("Refresh"), isDisplayed()));

        Log.v(CLASS_NAME, METHOD_NAME + "(): Refresh -> Timeout");
        actionMenuItemView.perform(click());
        // Should result in a toast with "Error: connection timed out!"
        // See NetworkFault1Test about how to test for toasts

        Log.v(CLASS_NAME, METHOD_NAME + "(): the End");
    } // networkFaultTest()

    @Before
    public void setupWireMock() {
        Log.v(CLASS_NAME, "@Before - setupWireMock(): start");
        wireMockRule.stubFor(get(urlPathMatching(".*"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        // Must be longer than timeouts in QuoteRefresherService.downloadQuotes()
                        .withFixedDelay(20000)
                )
        );
    } // setupWireMock()

} // class NetworkFault2Test
