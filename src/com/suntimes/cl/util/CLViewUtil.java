package com.suntimes.cl.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class CLViewUtil {
	
	/**
	 * 此方法将一个View转换成一个Bitmap对象(自动适配Bitmap的宽高)。
	 * 
	 * @param view 需要转换的控件
	 * @return 位图
	 * @author Zhao Wang
	 * @CreateDate 2013-5-17
	 */
	public static Bitmap convertViewToBitmap(View view) {
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();

		return bitmap;
	}

	/**
	 * 此方法可将Bitmap翻转，
	 * 同时可添加渐变模糊效果，
	 * 与原Bitmap结合使用可达到倒影效果。
	 * 
	 * @param originalBitmap
	 * @return
	 * @author Zhao Wang
	 * @CreateDate 2013-5-17
	 */
	public static Bitmap reflectionFromBitmap(Bitmap originalBitmap, boolean isFuzzy){
		//获取原位图的宽高
		int width = originalBitmap.getWidth();
		int height = originalBitmap.getHeight();
		
		//设置矩阵并将位图翻转
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);
		Bitmap reflectionBitmap = Bitmap.createBitmap(originalBitmap, 0,
				0, width, height, matrix, false);
		
		//渐变模糊效果
		if(isFuzzy){
			Canvas canvasFuzzy = new Canvas(reflectionBitmap);
			// 创建一个渐变的模糊的效果放在被反转的图片上面
			Paint paint = new Paint();
			LinearGradient shader = new LinearGradient(0, 0, 0, height,
					0x80ffffff, 0x00ffffff, TileMode.CLAMP);
			// Set the paint to use this shader (linear gradient)
			paint.setShader(shader);
			// Set the Transfer mode to be porter duff and destination in
			paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
			// Draw a rectangle using the paint with our linear gradient
			canvasFuzzy.drawRect(0, 0, width, height, paint);
			
			return reflectionBitmap;
		}
		
		return reflectionBitmap;
	}
	
	
//	/**
//	 * 
//	 * @param context
//	 * @param originalView
//	 * @return
//	 * @author Zhao Wang
//	 * @CreateDate 2013-5-16
//	 */
//	public static View setReflection(Context context, View originalView) {
//		// 原始图片和反射图片中间的间距
//		final int reflectionGap = 4;
////		int width = originalView.getWidth();
////		int height = originalView.getHeight();
//		
//		// 反转
//		Matrix matrix = new Matrix();
//		matrix.preScale(1, -1);
//		// reflectionImage就是下面透明的那部分,可以设置它的高度为原始的3/4,这样效果会更好些
//		Bitmap originalWithBitmap = CLViewUtil.convertViewToBitmap(originalView);
//		int width = originalWithBitmap.getWidth();
//		int height = originalWithBitmap.getHeight();
//		Bitmap reflectionImage = Bitmap.createBitmap(originalWithBitmap, 0,
//				0, width, height, matrix, false);
//
//		// 创建一个新的bitmap
//		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, height, Config.ARGB_8888);
//		Canvas canvasRef = new Canvas(bitmapWithReflection);
//
//		// 画间距,间距为透明
//		Paint deafaultPaint = new Paint();
//		deafaultPaint.setColor(Color.TRANSPARENT);
//		canvasRef.drawRect(0, 0, width, reflectionGap, deafaultPaint);
//
//		// 画被反转以后的图片
//		canvasRef.drawBitmap(reflectionImage, 0, reflectionGap, null);
//
//		// 创建一个渐变的蒙版放在下面被反转的图片上面
//		Paint paint = new Paint();
//		LinearGradient shader = new LinearGradient(0,
//				0, 0, bitmapWithReflection.getHeight() + reflectionGap,
//				0x80ffffff, 0x00ffffff, TileMode.CLAMP);
//		// Set the paint to use this shader (linear gradient)
//		paint.setShader(shader);
//		// Set the Transfer mode to be porter duff and destination in
//		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
//
//		// Draw a rectangle using the paint with our linear gradient
//		canvasRef.drawRect(0, 0, width, bitmapWithReflection.getHeight()
//				+ reflectionGap, paint);
//
//		ImageView reflectionView = new ImageView(context);
//		reflectionView.setImageBitmap(bitmapWithReflection);
//
//		LinearLayout newViewLayout = new LinearLayout(context);
//		newViewLayout.setOrientation(LinearLayout.VERTICAL);
////		newViewLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
////		LinearLayout.LayoutParams params =
////				new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
////						android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
//		newViewLayout.addView(originalView);
//		newViewLayout.addView(reflectionView);
//
//		return newViewLayout;
//	}

}
