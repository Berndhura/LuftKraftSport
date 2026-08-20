# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build (debug + release)
./gradlew build

# Build debug APK only
./gradlew assembleDebug

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a single test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=de.wichura.lks.mainactivity.MainActivityTest

# Lint check
./gradlew lint
```

CI runs `./gradlew build` on every push (`.github/workflows/android.yml`).

## Architecture

The app is a classified-ads marketplace for air sports (Luftkraftsport), written in Java with MVP architecture.

**Layer overview:**
- `activity/` + `mainactivity/` — Views (Activities and Fragments)
- `presentation/` — Presenters; one per major screen, hold references to their Activity
- `http/Service.java` — Single Retrofit service wrapping all REST API calls as RxJava3 Observables
- `models/` — POJOs serialized/deserialized by Gson
- `adapter/` — ListView adapters
- `dialogs/` — DialogFragment subclasses
- `gcm/MyGcmListenerService` — Firebase Cloud Messaging receiver
- `util/` — Helpers (SharedPrefs, Bitmap, Google API, etc.)

**Data flow:** Activity creates its Presenter with `new Presenter(this, new Service(), context)`. The Presenter calls `Service` methods which return `Observable<T>`. Results are subscribed on `Schedulers.io()` / `Schedulers.newThread()` and observed on `AndroidSchedulers.mainThread()`, then callbacks reach the Activity via direct method calls (the Presenter holds a reference to the Activity).

**Parallel API requests:** When the user is logged in, ads and bookmarks are always fetched in parallel using `Observable.zip(bookmarksObservable, adsObservable, ...)` and combined into `AdsAndBookmarks` before updating the UI.

## Key Technical Details

**Backend:** `http://52.29.200.187:80/api/V3/` (plain HTTP — `android:usesCleartextTraffic="true"` is set in the manifest). Auth is passed as a `?token=` query parameter on every authenticated request. The token is the raw Facebook/Google/email token stored in SharedPreferences under `Constants.USER_TOKEN`.

**Authentication types:** Facebook SDK, Google Sign-In (silent refresh on 401), and email/password. User type is stored in SharedPrefs as `Constants.USER_TYPE` (`"facebook_user"`, `"google_user"`, `"email_user"`).

**Location storage:** Latitude/longitude are stored in SharedPreferences as `Long` bits (`Double.doubleToRawLongBits`) under the `"usersLocation"` SharedPreferences file. Read back with `Double.longBitsToDouble`.

**FCM push notifications:** `MyGcmListenerService` handles two types — `"message"` (chat) and `"article"` (search alert). Unread message state is stored per-conversation in the `"unreadMessages"` SharedPreferences file, keyed as `"articleId,senderId"`.

**Inter-component communication:** `LocalBroadcastManager` broadcasts are used for login completion (`Constants.LOGIN_COMPLETE`), new messages (`"messageReceived"`), and live chat updates (`"appendChatScreenMsg"`).

**Navigation:** Drawer navigation in `MainActivity`. All sub-screens are launched with `startActivityForResult`. Back-navigation in MainActivity handles returning to all-ads view from bookmarks/my-ads/search states.

**Pagination:** `EndlessScrollListener` triggers `loadNextDataFromApi` when scrolling. Page/size (default 10) is tracked in `MainActivity` and incremented on scroll.

**SharedPreferences files used:** `"UserInfo"` (user name/id/token/picture/type), `"usersLocation"` (lat/lng/service status), `"unreadMessages"`, `"messageActivity"` (active chat session tracking), `"showMyAds"`, `"showBookmarks"`, `"welcomeDialog"`.

**Debug logging:** Tag `"CONAN"` is used throughout for `Log.d` calls.

**LeakCanary** is included as `debugImplementation` only.

## Required Config Files (not in repo)

`google-services.json` must be present at `app/google-services.json` for Firebase to initialize. Google Maps API key is referenced as `@string/google_maps_key` (in `strings.xml`, not committed). Facebook App ID and Client Token are in `strings.xml` as `facebook_app_id` / `facebook_client_token`.
