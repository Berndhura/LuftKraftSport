package wichura.de.camperapp.ad;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import wichura.de.camperapp.R;


/**
 * Created by ich on 03.11.2015.
 */
public class CustomSwipeAdapter extends PagerAdapter {

    //TODO: gets a list of URLs -> Later input for Volley Networkview
    private int[] imageResources = {R.drawable.image_bg, R.drawable.abc_btn_borderless_material};

    private Context ctx;
    private LayoutInflater layoutInflater;

    public CustomSwipeAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return imageResources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==(LinearLayout)object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item_view = layoutInflater.inflate(R.layout.swipe_layout, container,false);
        ImageView imageView = (ImageView)item_view.findViewById(R.id.image_view);
        imageView.setImageResource(imageResources[position]);
        container.addView(item_view);

        return item_view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
