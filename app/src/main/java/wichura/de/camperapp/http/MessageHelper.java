package wichura.de.camperapp.http;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by ich on 04.06.2016.
 */
public class MessageHelper {

    private Context ctx;

    public MessageHelper(Context ctx) {
        this.ctx = ctx;
    }

    public void sendMessageRequest(String message, String adId, String ownerId, String sender) {
        //adId, userId, sender
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        String url = Urls.MAIN_SERVER_URL + Urls.SEND_MESSAGE +
                "?message=" + message.replaceAll(" ", "%20")
                + "&adId=" + adId
                + "&idFrom=" + sender + "&idTo=" + ownerId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //save message done
                        if (response.equals("ok"))
                            Toast.makeText(ctx, "Message sent...", Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        requestQueue.add(stringRequest);
    }
}
