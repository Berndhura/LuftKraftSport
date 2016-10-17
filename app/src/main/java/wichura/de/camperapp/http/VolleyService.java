package wichura.de.camperapp.http;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

/**
 * Created by ich on 17.10.2016.
 * CamperApp
 */

public class VolleyService {

    private Context context;

    public VolleyService(Context ctx) {
        this.context = ctx;
    }

    public String sendStringGetRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, listener, errorListener);
        MyVolley.getRequestQueue().add(stringRequest);
        return "";
    }

    public  void deleteBookmark() throws TimeoutException {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(URL, null, future, future);
        MyVolley.getRequestQueue().add(request);

        try {
            JSONObject response = future.get(); // Blocks for at most 10 seconds.
        } catch (InterruptedException e) {
            // Exception handling
        } catch (ExecutionException e) {
            // Exception handling
        }
    }
}
