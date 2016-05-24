package wichura.de.camperapp.mainactivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.HttpHelper;
import wichura.de.camperapp.http.Urls;

/**
 * Created by ich on 22.05.2016.
 */
public class MessagesActivity extends Activity {

    private TextView msg;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_layout);

        msg = (TextView) findViewById(R.id.messages);

        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        String userId = settings.getString(Constants.USER_ID, "");
        getMessages(userId);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    private void getMessages(String userId) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = Urls.MAIN_SERVER_URL + Urls.GET_ALL_MESSAGES_FOR_USER + "?userId=" + userId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        msg.setText(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Network problems...Try again!", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(stringRequest);
    }
}
