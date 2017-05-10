package de.wichura.lks.activity;

import android.support.test.rule.ActivityTestRule;
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.wichura.lks.R;

import static org.junit.Assert.assertNotNull;

/**
 * Created by bwichura on 10.05.2017.
 * Luftkraftsport
 */

public class SearchActivityTest {

    @Rule
    public ActivityTestRule<SearchActivity> activityTestRule = new ActivityTestRule<>(SearchActivity.class);

    private SearchActivity activity;

    @Before
    public void setup() throws Exception {
        activity = activityTestRule.getActivity();
    }

    @Test
    public void getUserToken() throws Exception {

        View view = activity.findViewById(R.id.bottomBar);
        assertNotNull(view);

        /*SearchActivity ac = new SearchActivity();
        String token = ac.getUserToken();
        assertTrue(!token.isEmpty());*/
    }

    @After
    public void tearDown() throws Exception {
        activity = null;
    }
}