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
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class NetworkFault1Test {
    private static final String CLASS_NAME = "NetworkFault1Test";

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
        Log.v(CLASS_NAME, METHOD_NAME + "(): Refresh -> HttpURLConnection.HTTP_INTERNAL_ERROR");
        actionMenuItemView.perform(click());
        // In theory one can verify user friendly text + error code in toast. In practice it's
        // nearly impossible to get the timing right - toasts show with a delay and fade away soon.
//        String toastText
//                = QuoteRefresherService.QUOTE_REFRESHER_BROADCAST_ERROR_EXTRA
//                + "download failed (response code " + HttpURLConnection.HTTP_INTERNAL_ERROR + ")!";
//        onView(withText(toastText))
//                .inRoot(
//                        withDecorView(
//                                not(mActivityTestRule.getActivity().getWindow().getDecorView())
//                        )
//                )
//                .check(matches(isDisplayed()));

        Log.v(CLASS_NAME, METHOD_NAME + "(): the End");
    } // networkFaultTest()

    @Before
    public void setupWireMock() {
        Log.v(CLASS_NAME, "@Before - setupWireMock(): start");
        wireMockRule.stubFor(get(urlPathMatching(".*"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)
                )
        );
    } // setupWireMock()

} // class NetworkFault1Test
