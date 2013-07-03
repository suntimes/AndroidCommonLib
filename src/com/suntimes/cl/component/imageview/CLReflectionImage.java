package com.suntimes.cl.component.imageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 带倒影的ImageView
 * 设置倒影方法: setReflection(Bitmap originalImage)
 * 所有原ImageView的setImageXXX(XXX xxx)方法都需要重写，
 * 并将传入参数对象转换成Bitmap对象调用倒影方法，才能实现倒影效果。
 * 构造函数已经调用了此方法。
 * 
 * @author Zhao Wang
 * @version 1.0
 * @CreateDate 2013-5-6
 */
public class CLReflectionImage extends ImageView {

	public CLReflectionImage(Context context) {
		super(context);
	}

	public CLReflectionImage(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CLReflectionImage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// 取得原始图片的bitmap并重画
		Bitmap originalImage = ((BitmapDrawable) this.getDrawable()).getBitmap();
		setReflection(originalImage);
	}

	/*
	 *  和构造函数里面干了同样的事情(non-Javadoc)
	 * @see android.widget.ImageView#setImageResource(int)
	 */
	@Override
	public void setImageResource(int resId) {
		Bitmap originalImage = BitmapFactory.decodeResource(getResources(), resId);
		if (originalImage != null) {
			setReflection(originalImage);
		}
	}
	
	/*
	 * 和构造函数里面干了同样的事情(non-Javadoc)
	 * @see android.widget.ImageView#setImageBitmap(android.graphics.Bitmap)
	 */
	@Override
	public void setImageBitmap(Bitmap bm) {
		if(bm != null){
			setReflection(bm);
		}
	}
	
	/*
	 * 设置倒影方法
	 */
	private void setReflection(Bitmap originalImage) {
		// 原始图片和反射图片中间的间距
		final int reflectionGap = 4;            
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		// 反转
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);
		// reflectionImage就是下面透明的那部分,可以设置它的高度为原始的3/4,这样效果会更好些
		Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
				0, width, height, matrix, false);
		// 创建一个新的bitmap,高度为原来的两倍
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height), Config.ARGB_8888);
		Canvas canvasRef = new Canvas(bitmapWithReflection);

		// 先画原始的图片
		canvasRef.drawBitmap(originalImage, 0, 0, null);
		// 画间距,间距为透明
		Paint deafaultPaint = new Paint();
		deafaultPaint.setColor(Color.TRANSPARENT);
		canvasRef.drawRect(0, height, width, height + reflectionGap, deafaultPaint);

		// 画被反转以后的图片
		canvasRef.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
		
		// 创建一个渐变的蒙版放在下面被反转的图片上面
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0,
				originalImage.getHeight(), 0, bitmapWithReflection.getHeight()
						+ reflectionGap, 0x80ffffff, 0x00ffffff, TileMode.CLAMP);
		// Set the paint to use this shader (linear gradient)
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

		// Draw a rectangle using the paint with our linear gradient
		canvasRef.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);

		// 调用ImageView中的setImageBitmap
		super.setImageBitmap(bitmapWithReflection);
	}

}
