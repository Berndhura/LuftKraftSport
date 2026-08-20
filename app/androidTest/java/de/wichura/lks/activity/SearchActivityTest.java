package de.wichura.lks.activity;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

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
    public ActivityScenarioRule<SearchActivity> activityTestRule = new ActivityScenarioRule<>(SearchActivity.class);

    @Test
    public void getUserToken() {
        activityTestRule.getScenario().onActivity(activity -> {
            assertNotNull(activity.findViewById(R.id.bottomBar));
        });
    }
}