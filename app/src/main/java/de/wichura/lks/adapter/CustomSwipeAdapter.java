package de.wichura.lks.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.wichura.lks.R;
import de.wichura.lks.activity.OpenAdActivity;

/**
 * Created by bwichura on 04.04.2017.
 * Luftkraftsport
 */

public class CustomSwipeAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private OpenAdActivity activity;
    private String pictureUri;
    private int displayWidth;
    private int displayHeight;

    private List<String> IMAGES = new ArrayList<>();

    public CustomSwipeAdapter(OpenAdActivity activity, String pictureUri, int displayHeight, int displayWidth) {

        this.context = activity.getContext();
        this.activity = activity;
        this.pictureUri = pictureUri;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.IMAGES.add(0, pictureUri);
        this.IMAGES.add(1, pictureUri);
        this.IMAGES.add(2, pictureUri);
    }

    @Override
    public int getCount() {
        return IMAGES.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object );
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item_view = layoutInflater.inflate(R.layout.swipe_layout, container, false);
        ImageView image_view = (ImageView) item_view.findViewById(R.id.imageView);


        int ratio = Math.round((float) displayWidth / (float) displayWidth);
        Picasso.with(context)
                .load(IMAGES.get(position))
                .placeholder(R.drawable.empty_photo)
                .resize((int) Math.round((float) displayWidth * 0.6), (int) Math.round((float) displayHeight * 0.6) * ratio)
                .centerInside()
                .into(image_view, new Callback() {
                    @Override
                    public void onSuccess() {
                        activity.mOpenAdProgressBar.setVisibility(ProgressBar.GONE);
                    }

                    @Override
                    public void onError() {
                        activity.mOpenAdProgressBar.setVisibility(ProgressBar.GONE);
                        Toast.makeText(context, "No network connection while loading picture!", Toast.LENGTH_SHORT).show();
                        //activity.showDefaultPic();
                    }
                });

        container.addView(item_view);

        return item_view;
    }



    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(container);
    }
}
