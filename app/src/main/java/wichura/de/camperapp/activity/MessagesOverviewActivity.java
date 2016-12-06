package wichura.de.camperapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.adapter.MsgOverviewAdapter;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.models.GroupedMsgItem;
import wichura.de.camperapp.presentation.MsgOverviewPresenter;

import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by ich on 20.06.2016.
 * Camper App
 */
public class MessagesOverviewActivity extends AppCompatActivity {

    private ListView listView;

    private ProgressBar mMessagesProgressBar;

    private MsgOverviewPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Service service = new Service();
        presenter = new MsgOverviewPresenter(this, service);

        setContentView(R.layout.message_overview_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.msg_overview_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

        mMessagesProgressBar = (ProgressBar) findViewById(R.id.msg_overview_ProgressBar);

        listView = (ListView) findViewById(R.id.message_overview_list);

        //getMessagesForUser(getUserId(), mMessagesProgressBar);
        presenter.loadAllMessages(getUserToken());
    }

    public void updateMsgList(List<GroupedMsgItem> messageList) {
        List<GroupedMsgItem> rowItems = new ArrayList<>();

        for (GroupedMsgItem e : messageList) {
            rowItems.add(e);
        }

        getSupportActionBar().setTitle("Messages: " + rowItems.size());
        getSupportActionBar().setSubtitle(getUserName());


        MsgOverviewAdapter adapter = new MsgOverviewAdapter(
                getApplicationContext(), R.layout.msg_overview_item, rowItems);
        listView.setAdapter(adapter);
        listView.setSelection(listView.getCount() - 1);
        adapter.notifyDataSetChanged();
        listView.setSelectionAfterHeaderView();
        listView.setOnItemClickListener((adapterView, view, position, l) -> {
            final GroupedMsgItem rowItem = (GroupedMsgItem) listView.getItemAtPosition(position);
            //open message threat
            final Intent intent = new Intent(getApplicationContext(), MessagesActivity.class);
            intent.putExtra(Constants.AD_ID, rowItem.getAdId());
            intent.putExtra(Constants.SENDER_ID, rowItem.getIdTo());
            intent.putExtra(Constants.ID_FROM, rowItem.getIdFrom());
            intent.putExtra(Constants.ID_TO, rowItem.getIdTo());
            intent.putExtra(Constants.SENDER_NAME, rowItem.getName());
            intent.putExtra(Constants.AD_URL, Urls.MAIN_SERVER_URL_V2 + "pictures/" + rowItem.getUrl() + "/thumbnail");
            startActivityForResult(intent, Constants.REQUEST_ID_FOR_MESSAGES);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ID_FOR_MESSAGES) {
            presenter.loadAllMessages(getUserToken());
        }
    }

    private String getUserToken() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }

    private String getUserName() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_NAME, "");
    }

    public void hideProgressBar() {
        mMessagesProgressBar.setVisibility(ProgressBar.GONE);
    }
}
