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

/**
 * Created by bwichura on 04.04.2017.
 * Luftkraftsport
 */

public class CustomSwipeadapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private String pictureUri;

    private List<String> IMAGES = new ArrayList<>();

    public CustomSwipeadapter(Context context, String pictureUri) {

        this.context = context;
        this.pictureUri = pictureUri;
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
        return (view == (LinearLayout)object );
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item_view = layoutInflater.inflate(R.layout.swipe_layout, container, false);
        ImageView image_view = (ImageView) item_view.findViewById(R.id.imageView);

        int displayHeight=1600;
        int displayWidth=1000;

        //image_view.setImageResource(R.drawable.applogo);
        int ratio = Math.round((float) displayWidth / (float) displayWidth);
        Picasso.with(context)
                .load(IMAGES.get(position))
                .placeholder(R.drawable.empty_photo)
                .resize((int) Math.round((float) displayWidth * 0.6), (int) Math.round((float) displayHeight * 0.6) * ratio)
                .centerInside()
                .into(image_view, new Callback() {
                    @Override
                    public void onSuccess() {

                        //mOpenAdProgressBar.setVisibility(ProgressBar.GONE);
                    }

                    @Override
                    public void onError() {
                        //mOpenAdProgressBar.setVisibility(ProgressBar.GONE);
                        Toast.makeText(context, "No network connection while loading picture!", Toast.LENGTH_SHORT).show();
                        //showDefaultPic();
                    }
                });





        container.addView(item_view);

        return item_view;
    }



    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout)container);
    }
}
