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
 * Created by Bernd Wichura on 09.05.2016.
 * Camper App
 */
public class HttpHelper {

    private Context context;

    public HttpHelper(Context ctx) {
        this.context = ctx;
    }

    public void createNewUser(final String name, final String id) {

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = Urls.MAIN_SERVER_URL + Urls.CREATE_USER + "?name=" + name.replaceAll(" ", "%20") + "&id=" + id;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        progressDialog.dismiss();
                        //TODO toasts weg , logs dafuer
                        if (!response.equals("wrong")) {
                            Toast.makeText(context, "new user created", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(context, "Wrong irgendwas!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // progressDialog.dismiss();
                Toast.makeText(context, "Network problems...Try again!", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(stringRequest);
    }
}
