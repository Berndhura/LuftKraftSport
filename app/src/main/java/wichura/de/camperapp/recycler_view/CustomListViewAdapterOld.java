package wichura.de.camperapp.recycler_view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.models.RowItem;

/**
 * Created by ich on 21.11.2016.
 */

public class CustomListViewAdapterOld extends RecyclerView.Adapter<CustomListViewAdapterOld.ViewHolder> {
    //extends ArrayAdapter<RowItem> {

    /* private view holder class */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtTitle;
        TextView txtPrice;
        TextView txtDate;
        ImageView bookmarkStar;
        LinearLayout myAdsView;
        LinearLayout mainLl;
        ImageView thumbNail;

        public ViewHolder(View view) {
            super(view);
            txtTitle = (TextView) view.findViewById(R.id.title);
            txtPrice = (TextView) view.findViewById(R.id.price);
            txtDate = (TextView) view.findViewById(R.id.creation_date);
            bookmarkStar = (ImageView) view.findViewById(R.id.bookmark_star);
            myAdsView = (LinearLayout) view.findViewById(R.id.my_ads_view);
            mainLl = (LinearLayout) view.findViewById(R.id.main_linear_layout);
            thumbNail = (ImageView) view.findViewById(R.id.icon);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == bookmarkStar.getId()) {
                Toast.makeText(v.getContext(), "ITEM PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public CustomListViewAdapterOld(List<RowItem> list, Context context, String bookmarks) {
        this.adList = list;
        this.context = context;
        if (bookmarks != null) {
            this.bookmarks = bookmarks.split(",");
        } else {
            this.bookmarks = null;
        }
    }

    private Context context;
    private String[] bookmarks;
    private List<RowItem> adList;


    @Override
    public CustomListViewAdapterOld.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        return new CustomListViewAdapterOld.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CustomListViewAdapterOld.ViewHolder holder, int position) {
        RowItem rowItem = adList.get(position);
        holder.txtTitle.setText(rowItem.getTitle());
        holder.txtPrice.setText(rowItem.getPrice());

        Picasso.with(context)
                .load(Urls.MAIN_SERVER_URL + Urls.GET_PICTURE_THUMB + rowItem.getUrl())
                .resize(100, 100)
                .centerCrop()
                .into(holder.thumbNail);

        if (bookmarks != null) {
            Log.d("CONAN", "bookmarks: " + Arrays.asList(bookmarks));
        }
        //bookmark star full for bookmarked ad
        if (bookmarks == null) {
            holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_empty);
        } else {
            if (Arrays.asList(bookmarks).contains(rowItem.getAdId())) {
                holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_full);
            } else {
                holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_empty);
            }
        }

        holder.mainLl.removeView(holder.myAdsView);

        //click to bookmark/debookmark an ad
        holder.bookmarkStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bookmarks != null && Arrays.asList(bookmarks).contains(rowItem.getAdId())) {
                    deleteBookmark(rowItem.getAdId(), getUserId());
                    holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_empty);
                    notifyDataSetChanged();
                    Log.d("CONAN  ", position + "");
                    Log.d("CONAN", "bookmark weg");
                } else {
                    bookmarkAd(rowItem.getAdId(), getUserId());
                    holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_full);
                    notifyDataSetChanged();
                    Log.d("CONAN", "bookmark dazu");
                    Log.d("CONAN  ", position + "");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return adList.size();
    }

    private void deleteBookmark(String adId, String userId) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = Urls.MAIN_SERVER_URL + Urls.BOOKMARK_DELETE + "?adId=" + adId + "&userId=" + userId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context, "Bookmark deleted!", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Something went wrong...", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(stringRequest);

    }

    private void bookmarkAd(String adId, String userId) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = Urls.MAIN_SERVER_URL + Urls.BOOKMARK_AD + "?adId=" + adId + "&userId=" + userId;
        Log.d("CONAN", url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context, "Bookmarked!", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Something went wrong...\n" + error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(stringRequest);
    }

    private String getUserId() {
        return context.getSharedPreferences("UserInfo", 0).getString(Constants.USER_ID, "");
    }
}

//aus MainActivity:
/*

    private RecyclerView.OnScrollListener
            mRecyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView,
                                         int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

            //if (!mIsLoading && !mIsLastPage) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                    && totalItemCount >= PAGE_SIZE) {
                // loadMoreItems();
                //    }
            }
        }
    };


    public void updateAds(AdsAndBookmarks elements) {

        List<RowItem> rowItems = new ArrayList<>();
        for (RowItem e : elements.getAds()) {
            rowItems.add(e);
        }

        showNumberOfAds(elements.getAds().size());

        adapter = new CustomListViewAdapter(rowItems, getApplicationContext(), elements.getBookmarks());

        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView = (RecyclerView) findViewById(R.id.main_list_recyle);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(mRecyclerViewOnScrollListener);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(30));
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        final RowItem rowItem = rowItems.get(position);
                        final Intent intent = new Intent(getApplicationContext(), OpenAdActivity.class);
                        intent.putExtra(Constants.URI, Urls.MAIN_SERVER_URL + "getBild?id=" + rowItem.getUrl());
                        intent.putExtra(Constants.AD_ID, rowItem.getAdId());
                        intent.putExtra(Constants.TITLE, rowItem.getTitle());
                        intent.putExtra(Constants.DESCRIPTION, rowItem.getDescription());
                        intent.putExtra(Constants.LOCATION, rowItem.getLocation());
                        intent.putExtra(Constants.PHONE, rowItem.getPhone());
                        intent.putExtra(Constants.PRICE, rowItem.getPrice());
                        intent.putExtra(Constants.DATE, rowItem.getDate());
                        intent.putExtra(Constants.VIEWS, rowItem.getViews());
                        intent.putExtra(Constants.USER_ID_FROM_AD, rowItem.getUserId());
                        intent.putExtra(Constants.USER_ID, getUserId());
                        startActivityForResult(intent, Constants.REQUEST_ID_FOR_OPEN_AD);
                    }
                })
        );
        adapter.notifyDataSetChanged();
    }



 private PresenterLayer presenterLayer;
    private Service service;
    public RecyclerView recyclerView;
    private CustomListViewAdapter adapter;
    private LinearLayoutManager mLayoutManager;


     <android.support.v7.widget.RecyclerView
        android:id="@+id/main_list_recyle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"

 */