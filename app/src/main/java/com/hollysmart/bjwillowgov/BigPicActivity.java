package com.hollysmart.bjwillowgov;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.hollysmart.beans.JDPicInfo;
import com.hollysmart.imageview.GestureImageView;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.style.StyleAnimActivity;
import com.hollysmart.value.Values;

import java.io.File;
import java.util.List;

public class BigPicActivity extends StyleAnimActivity {

	private RelativeLayout rl_title;
	private TextView tv_page;
	private Context context;
	private boolean isLoction;
	private List<JDPicInfo> infos;
	private static JDPicInfo picBeannull = new JDPicInfo(0, null, null, null, 1, "false");

	@Override
	public int layoutResID() {
		return R.layout.activity_big_pic;
	}

	@Override
	public void findView() {
		rl_title =  findViewById(R.id.rl_title);
		tv_page =  findViewById(R.id.tv_page);
		findViewById(R.id.iv_back).setOnClickListener(this);
	}

	@Override
	public void init() {
		context = this;
		isLoction = getIntent().getBooleanExtra("isLoction", false);
		infos = (List<JDPicInfo>)getIntent().getSerializableExtra("infos");
		for (int i = 0; i < infos.size(); i++) {

			if (infos.get(i).getIsAddFlag() == 1) {
				infos.remove(i);
			}
		}
		int index = getIntent().getIntExtra("index", 0);
		tv_page.setText("(" + (index + 1) + "/" + infos.size() + ")");

		ViewPager vp_pic = (ViewPager) findViewById(R.id.vp_pic_detail);
		vp_pic.setOnPageChangeListener(new MyOnPageChangeListener());

		ImageAdapter adapter = new ImageAdapter();
		vp_pic.setAdapter(adapter);
		vp_pic.setCurrentItem(index);

	}

	public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

		public void onPageScrollStateChanged(int arg0) {
		}
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		public void onPageSelected(int arg0) {
			tv_page.setText("("+ (arg0+1) +"/" + infos.size() +")");
		}
	}
	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		if (id == R.id.iv_back) {
			finish();
			overridePendingTransition(R.anim.activity_yuandian, R.anim.activity_exit_right);
		} else if (id == R.id.image) {
			if (tag)
				gongTitle();
			else
				visibileTitle();
			tag = !tag;
		}

	}


	private boolean tag = true;
	private void gongTitle(){
		rl_title.setVisibility(View.GONE);
	}
	private void visibileTitle(){
		rl_title.setVisibility(View.VISIBLE);
	}


	private class ImageAdapter extends PagerAdapter {
		private LayoutInflater inflater;
		ImageAdapter() {
			inflater = LayoutInflater.from(context);
		}
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return infos.size();
		}

		private Bitmap getBitMap(Bitmap bitmap){
			WindowManager wm = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			float rw = (float)bitmap.getWidth() / (float)wm.getDefaultDisplay().getWidth();
			float rh = (float)bitmap.getHeight() / (float)wm.getDefaultDisplay().getHeight();
			if (rw > rh) {
				return Bitmap.createScaledBitmap(bitmap, wm.getDefaultDisplay().getWidth(),(int) (bitmap.getHeight() / rw), false);
			}else {
				return Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() / rh), wm.getDefaultDisplay().getHeight(), false);
			}
		}
		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
			assert imageLayout != null;

			final GestureImageView imageView = (GestureImageView) imageLayout.findViewById(R.id.image);
			if (infos.get(position).getFilePath()!= null) {
				File file = new File(infos.get(position).getFilePath());

				if (file.exists()) {
					Glide.with(BigPicActivity.this)
							.load(new File(infos.get(position).getFilePath()))
							.asBitmap()
							.placeholder(R.mipmap.a_v)
							.error(R.mipmap.a_v)
							.into(new SimpleTarget<Bitmap>() {
								@Override
								public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
									imageView.setImageBitmap(getBitMap(resource));
								}
							});

				} else {
					Glide.with(BigPicActivity.this)
							.load(Values.SERVICE_URL_ADMIN_FORM + infos.get(position).getImageUrl())
							.asBitmap()
							.placeholder(R.mipmap.a_v)
							.error(R.mipmap.a_v)
							.into(new SimpleTarget<Bitmap>() {
								@Override
								public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
									imageView.setImageBitmap(getBitMap(resource));
								}
							});
				}
			} else {

				if (infos.get(position).getImageUrl() != null) {
					Glide.with(BigPicActivity.this)
							.load(Values.SERVICE_URL_ADMIN_FORM + infos.get(position).getImageUrl())
							.asBitmap()
							.placeholder(R.mipmap.a_v)
							.error(R.mipmap.a_v)
							.into(new SimpleTarget<Bitmap>() {
								@Override
								public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
									imageView.setImageBitmap(getBitMap(resource));
								}
							});
				} else {

					imageView.setImageResource(R.mipmap.a_v);
				}
			}
			imageView.setOnClickListener(BigPicActivity.this);
			view.addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}
}
