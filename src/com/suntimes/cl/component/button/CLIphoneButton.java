package com.suntimes.cl.component.button;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

/**
 * 类似于IPHONE的按钮，当按钮被按下时，会把按钮的背景的透明度降低
 * 控件缩写：iBtn
 * @author Sean Zheng
 */
public class CLIphoneButton extends Button {

	public CLIphoneButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CLIphoneButton(Context context) {
		super(context);
	}
	
	@Override
	public void setBackgroundDrawable(Drawable drawable) {
		StateListDrawable bg = new StateListDrawable();
		if(drawable instanceof StateListDrawable) {
			drawable = ((StateListDrawable)drawable).getCurrent();
		}
		Bitmap bitmap = null;
		if(drawable instanceof NinePatchDrawable) {
			bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), 
						drawable.getIntrinsicHeight(), 
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			drawable.draw(canvas);
		} else {
			bitmap = ((BitmapDrawable)drawable).getBitmap();
		}
		Bitmap pressed = bitmap.copy(Config.ARGB_8888, false);
		pressed = reduceAlpha(pressed);
		Drawable drawable2 = new BitmapDrawable(pressed);
		bg.addState(View.PRESSED_ENABLED_STATE_SET, drawable2);
		bg.addState(View.ENABLED_STATE_SET, drawable);
		super.setBackgroundDrawable(bg);
	}
	
	@Override
	public void setBackgroundResource(int resid) {
		Drawable drawable = getResources().getDrawable(resid);
		setBackgroundDrawable(drawable);
	}

	/**
	 * 图片透明度处理
	 * @param sourceImg 原始图片
	 */
	public static Bitmap reduceAlpha(Bitmap sourceImg) {
		int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];
		int width = sourceImg.getWidth();
		int height = sourceImg.getHeight();
		
		//从图像中提取一个INT的数组，并将每个像素的alpha值降低100个单位（alpha的值最大为255）
		sourceImg.getPixels(argb, 0, width, 0, 0, width, height);// 获得图片的ARGB值
		int reduce = 100;
		int alpha = 0x00000000;
		for (int i = 0; i < argb.length; i++) {
			alpha = (argb[i] >> 24) & 0x000000FF;
			alpha -= reduce;
			if(alpha < 0)
				alpha = 0;
			argb[i] = ((alpha << 24) | (argb[i] & 0x00FFFFFF));
		}
		sourceImg = Bitmap.createBitmap(argb, width,
				height, Config.ARGB_8888);
		return sourceImg;
	}
}
