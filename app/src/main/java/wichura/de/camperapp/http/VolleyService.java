package wichura.de.camperapp.http;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

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
}
