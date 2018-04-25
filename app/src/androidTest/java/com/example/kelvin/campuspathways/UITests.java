package com.example.kelvin.campuspathways;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Kelvin on 4/22/2018.
 * Used for Instrumented tests of UI
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class UITests {

    @Rule
    public ActivityTestRule<StartActivity> activityTestRule = new ActivityTestRule<>(StartActivity.class);

    //Test navigation to display screen from start
    @Test
    public void goToDisplayPaths() {
        onView(withId(R.id.btShowMap))
                .perform(click());
    }

    //Test navigation to discover screen from start
    @Test
    public void goToDiscoverPaths() {
        onView(withId(R.id.btTrackPath))
                .perform(click());
    }

    //Test navigation to node screen from start
    @Test
    public void goToNodes() {
        onView(withId(R.id.btNodesFromStart))
                .perform(click());
    }

    @Test
    public void testFullNavigation() {

        //Go to node screen and back to start
        onView(withId(R.id.btNodesFromStart))
                .perform(click());
        pressBack();

        //Go to discover and back
        onView(withId(R.id.btTrackPath))
                .perform(click());
        pressBack();

        //Go to display and back
        onView(withId(R.id.btShowMap))
                .perform(click());
        pressBack();

        //From start, go to discover and then display and then back
        onView(withId(R.id.btTrackPath))
                .perform(click());

        onView(withId(R.id.btDisplayPathFromDiscover))
                .perform(click());

        onView(withId(R.id.btDiscoverPathFromDisplay))
                .perform(click());

        //Go to nodes from discover and back
        onView(withId(R.id.btNodesFromDiscover))
                .perform(click());
        pressBack();

        //Go to display from discover
        onView(withId(R.id.btDisplayPathFromDiscover))
                .perform(click());

        //Go to nodes from display and back
        onView(withId(R.id.btNodesFromDisplay))
                .perform(click());
        pressBack();

    }

}
