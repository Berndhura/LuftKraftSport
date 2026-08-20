package de.wichura.lks.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.viewpager.widget.PagerAdapter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import de.wichura.lks.BuildConfig;
import de.wichura.lks.R;
import de.wichura.lks.activity.OpenAdActivity;
import de.wichura.lks.http.Urls;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.util.MockImages;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by bwichura on 04.04.2017.
 * Luftkraftsport
 */

public class CustomSwipeAdapter extends PagerAdapter {

    private Context context;
    private OpenAdActivity activity;
    private int displayWidth;
    private int displayHeight;

    private List<String> IMAGES = new ArrayList<>();

    public CustomSwipeAdapter(OpenAdActivity activity, String pictureUri, int displayHeight, int displayWidth) {

        this.context = activity.getContext();
        this.activity = activity;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;

        if (pictureUri != null) {
            String[] uris = pictureUri.split(",");
            int size = uris.length;
            for (int i = 0; i < size; i++) {
                IMAGES.add(i, uris[i]);
            }
        } else {
            IMAGES.add(0, "");
        }

        Log.d("CONAN", "all pictures from article: " + pictureUri);
    }

    @Override
    public int getCount() {
        return IMAGES.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item_view = layoutInflater.inflate(R.layout.swipe_layout, container, false);
        ImageView image_view = (ImageView) item_view.findViewById(R.id.imageView);

        final int adId = activity.getIntent().getIntExtra(Constants.ID, 0);
        Glide.with(context)
                .load(Urls.MAIN_SERVER_URL_V3 + "pictures/" + IMAGES.get(position))
                .placeholder(R.drawable.empty_photo)
                .error(MockImages.drawableFor(adId))
                .override((int) Math.round((float) displayWidth * 0.6), (int) Math.round((float) displayHeight * 0.6))
                .centerInside()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        activity.mOpenAdProgressBar.setVisibility(ProgressBar.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        activity.mOpenAdProgressBar.setVisibility(ProgressBar.GONE);
                        return false;
                    }
                })
                .into(image_view);

        container.addView(item_view);

        image_view.setOnClickListener(v -> {

            final Dialog nagDialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            nagDialog.setCancelable(false);
            nagDialog.setContentView(R.layout.full_screen_image);

            activity.mOpenFullScreenImgProgressBar = (CircularProgressIndicator) nagDialog.findViewById(R.id.progress_loading_full_screen_pic);
            activity.mOpenFullScreenImgProgressBar.setVisibility(View.VISIBLE);

            ImageView ivPreview = (ImageView) nagDialog.findViewById(R.id.iv_preview_image);
            PhotoViewAttacher photoView = new PhotoViewAttacher(ivPreview);
            photoView.update();
            final int fullAdId = activity.getIntent().getIntExtra(Constants.ID, 0);
            Glide.with(context)
                    .load(Urls.MAIN_SERVER_URL_V3 + "pictures/" + IMAGES.get(position))
                    .error(MockImages.drawableFor(fullAdId))
                    .override(displayWidth, displayHeight)
                    .centerInside()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            activity.mOpenFullScreenImgProgressBar.setVisibility(ProgressBar.GONE);
                            ImageView closeImage = (ImageView) nagDialog.findViewById(R.id.close_full_screen_image);
                            closeImage.setVisibility(View.VISIBLE);
                            closeImage.setOnClickListener(dialog -> nagDialog.dismiss());
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            activity.mOpenFullScreenImgProgressBar.setVisibility(ProgressBar.GONE);
                            ImageView closeImage = (ImageView) nagDialog.findViewById(R.id.close_full_screen_image);
                            closeImage.setVisibility(View.VISIBLE);
                            closeImage.setOnClickListener(dialog -> nagDialog.dismiss());
                            return false;
                        }
                    })
                    .into(ivPreview);

            nagDialog.setOnKeyListener((arg0, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    nagDialog.dismiss();
                }
                return true;
            });

            nagDialog.show();
        });

        return item_view;
    }

    private void showDefaultPic(ImageView image_view) {
        Glide.with(context)
                .load(R.drawable.lks_app_logo)
                .placeholder(R.drawable.empty_photo)
                .centerCrop()
                .into(image_view);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(container);
    }
}
