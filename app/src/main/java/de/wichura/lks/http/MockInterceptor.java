package de.wichura.lks.http;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.wichura.lks.mainactivity.MainApp;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Debug-only interceptor that serves canned JSON from assets/mock/ instead of
 * hitting the backend. The lookup strategy tries progressively looser matches:
 *   1. exact path:          POST /articles/1/bookmark  -> post_articles_1_bookmark.json
 *   2. numeric-id wildcard: same request               -> post_articles_id_bookmark.json
 *   3. last-segment wildcard (GET only, for /users/{anyId})
 *
 * If nothing matches, returns HTTP 200 with an empty string body so the app
 * doesn't crash — check logcat (tag "MOCK") to see which file to add.
 */
public class MockInterceptor implements Interceptor {

    private static final String TAG = "MOCK";
    private static final String MOCK_DIR = "mock";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String method = request.method();
        String fullPath = request.url().encodedPath();
        String apiPath = fullPath.replaceFirst("^/api/V[0-9]+/?", "");

        String body = loadMock(method, apiPath);
        String message;
        if (body == null) {
            Log.w(TAG, "no mock for " + method + " " + apiPath + " — returning empty 200");
            body = "\"\"";
            message = "OK (mock empty)";
        } else {
            message = "OK (mock)";
        }

        return new Response.Builder()
                .code(200)
                .protocol(Protocol.HTTP_1_1)
                .message(message)
                .request(request)
                .body(ResponseBody.create(body, JSON))
                .build();
    }

    private String loadMock(String method, String apiPath) {
        String[] segments = apiPath.replaceAll("^/", "").split("/");
        String methodLower = method.toLowerCase();

        String exact = filename(methodLower, segments);
        String cached = tryRead(exact);
        if (cached != null) {
            Log.d(TAG, method + " " + apiPath + " -> " + exact);
            return cached;
        }

        String[] numericWildcard = new String[segments.length];
        boolean changed = false;
        for (int i = 0; i < segments.length; i++) {
            if (segments[i].matches("\\d+")) {
                numericWildcard[i] = "id";
                changed = true;
            } else {
                numericWildcard[i] = segments[i];
            }
        }
        if (changed) {
            String wc = filename(methodLower, numericWildcard);
            cached = tryRead(wc);
            if (cached != null) {
                Log.d(TAG, method + " " + apiPath + " -> " + wc);
                return cached;
            }
        }

        if ("get".equals(methodLower) && segments.length >= 2) {
            String[] lastAsId = segments.clone();
            lastAsId[lastAsId.length - 1] = "id";
            String wc = filename(methodLower, lastAsId);
            cached = tryRead(wc);
            if (cached != null) {
                Log.d(TAG, method + " " + apiPath + " -> " + wc);
                return cached;
            }
        }

        return null;
    }

    private String filename(String methodLower, String[] segments) {
        StringBuilder sb = new StringBuilder(MOCK_DIR).append('/').append(methodLower);
        for (String s : segments) {
            sb.append('_').append(s.toLowerCase());
        }
        return sb.append(".json").toString();
    }

    private String tryRead(String assetPath) {
        AssetManager assets = MainApp.getInstance().getAssets();
        try (InputStream in = assets.open(assetPath)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            return out.toString("UTF-8");
        } catch (IOException e) {
            return null;
        }
    }
}
