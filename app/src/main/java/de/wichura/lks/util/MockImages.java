package de.wichura.lks.util;

import de.wichura.lks.R;

/**
 * Debug-only: maps mock ad IDs to bundled sport-themed drawables so the
 * MockInterceptor's ads can render without hitting a real picture endpoint.
 */
public final class MockImages {

    private MockImages() {
    }

    private static final int[] POOL = {
            R.drawable.mock_paragliding,
            R.drawable.mock_kitesurf,
            R.drawable.mock_sailplane,
            R.drawable.mock_paramotor,
    };

    public static int drawableFor(int adId) {
        int idx = Math.floorMod(adId - 1, POOL.length);
        return POOL[idx];
    }
}
