package com.suntimes.cl.component.container;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ScrollView;

/**
 * 带阻尼效果(上下弹性效果)控件,可以对AbsListView,ScrollView,etc...加上阻尼效果<br>
 * <b>注意:</b>这个控制只可以加一个chilview,如果超过会出IllegalStateException
 * 
 * @author jianfeng.lao
 * @version 1.0
 * @CreateDate 2013-5-6
 */
public class CLBounceLayout extends FrameLayout {
	private static final String TAG = "CLItemBounceView";
	private MyTimer mMyTimer;
	private int mYDown = 0;
	private int mYOffset = 0;
	private Camera mCamera = new Camera();
	private boolean mIsDispatchTouchEvent = false;
	private long mTouchDownEventTime = 0;
	/**
	 * 无动作
	 */
	public static final int ACTION_NONE = 0x00000000;
	/**
	 * 下拉动作
	 */
	public static final int ACTION_PULL_DOWN = 0x00000001;
	/**
	 * 上拉动作
	 */
	public static final int ACTION_PULL_UP = 0x00000002;
	/**
	 * 回弹动作
	 */
	public static final int ACTION_BOUNCE = 0x00000003;

	private int mCurrentAction = ACTION_NONE;

	private CLItemBounceViewListener mCLItemBounceViewListener;

	public CLBounceLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public CLBounceLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CLBounceLayout(Context context) {
		super(context);
		init();
	}

	/**
	 * 初始化
	 * 
	 * @author jianfeng.lao
	 * @CreateDate 2013-5-6
	 */
	private void init() {
		mMyTimer = new MyTimer();
		setStaticTransformationsEnabled(true);
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);
		Matrix matrix = t.getMatrix();
		mCamera.save();
		mCamera.translate(0, mYOffset, 0);
		mCamera.getMatrix(matrix);
		mCamera.restore();
		child.invalidate();
		return true;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// Log.v(TAG, "dispatchTouchEvent>>" + ev.getAction());
		if (getChildCount() > 0) {
			View child = getChildAt(0);

			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mYDown = (int) ev.getY() + mYOffset;
				removeCallbacks(mMyTimer);
				mTouchDownEventTime = ev.getEventTime();
				mIsDispatchTouchEvent = isScrollerView(child) ? false : true;

				break;
			case MotionEvent.ACTION_MOVE:
				int tmpY = (int) (mYDown - ev.getY());
				boolean isdropDown = tmpY < 0 ? true : false;
				mCurrentAction = isOverScroll(child, isdropDown);
				mIsDispatchTouchEvent = ev.getEventTime() - mTouchDownEventTime > 100 ? mCurrentAction != ACTION_NONE
						: false;
				if (mIsDispatchTouchEvent) {
					mYOffset = tmpY;
				} else {
					mYOffset = 0;
					mYDown = (int) ev.getY();
				}
				if (mCLItemBounceViewListener != null) {
					mCLItemBounceViewListener.onStateChange(mCurrentAction, mYOffset);
				}
				Log.v(TAG, "tmpY>>" + tmpY + ",yOffset>>" + mYOffset + ",yDown>>" + mYDown + ",currY>>" + ev.getY()
						+ ",isDispatchTouchEvent>>" + mIsDispatchTouchEvent);
				/*
				 * 注意:旧机或者使用旧theme会出现花屏问题,以下解决办法
				 */
				child.postInvalidate();
				postInvalidate();
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				if (mYOffset != 0) {
					mMyTimer.setStartYoffset(mYOffset);
				}
				break;
			}

		}
		return !mIsDispatchTouchEvent ? super.dispatchTouchEvent(ev) : true;

	}

	@Override
	public void addView(View child) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("BounceView can host only one direct child");
		}

		super.addView(child);
	}

	@Override
	public void addView(View child, int index) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("BounceView can host only one direct child");
		} else {
			mYOffset = 0;
		}

		super.addView(child, index);
	}

	@Override
	public void addView(View child, ViewGroup.LayoutParams params) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("BounceView can host only one direct child");
		}

		super.addView(child, params);
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("BounceView can host only one direct child");
		}
		super.addView(child, index, params);
	}

	/**
	 * 计算Y轴位置
	 * 
	 * @author jianfeng.lao
	 * @version 1.0
	 * @CreateDate 2013-5-6
	 */
	class MyTimer implements Runnable {

		private static final int DURATION = 300;
		private long mStartTime = 0;
		private int mStartYoffset = 0;

		public void setStartYoffset(int startYoffset) {
			this.mStartYoffset = startYoffset;
			mStartTime = AnimationUtils.currentAnimationTimeMillis();
			post(this);
		}

		@Override
		public void run() {
			// Log.v(TAG, "yOffset>>" + mYOffset);
			int timePassed = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);
			if (timePassed <= DURATION) {
				double rate = ((double) timePassed / DURATION);
				mCurrentAction = ACTION_BOUNCE;
				mYOffset = mStartYoffset - (int) (mStartYoffset * rate);
				if (mCLItemBounceViewListener != null) {
					mCLItemBounceViewListener.onStateChange(mCurrentAction, mYOffset);
				}
				post(this);
			} else {
				mCurrentAction = ACTION_NONE;
				mYOffset = 0;
				if (mCLItemBounceViewListener != null) {
					mCLItemBounceViewListener.onStateChange(mCurrentAction, mYOffset);
				}
				removeCallbacks(this);
			}
			postInvalidate();
		}
	}

	/**
	 * 判断超出滚动范围
	 * 
	 * @param child
	 * @param dropDown
	 * @return
	 * @author jianfeng.lao
	 * @CreateDate 2013-5-6
	 */
	private int isOverScroll(View child, boolean dropDown) {
		// Log.d(TAG, "child height>>" + child.getHeight());
		if (child.getHeight() < getHeight()) {
			return dropDown ? ACTION_PULL_DOWN : ACTION_PULL_UP;
		}
		if (child instanceof ScrollView) {
			ScrollView view = (ScrollView) child;
			int scrollViewHeight = 0;
			if (view.getChildCount() > 0) {
				scrollViewHeight = view.getChildAt(0).getHeight();
				// Log.v(TAG,"scrollViewHeight>>"+scrollViewHeight+",scroll y>"+view.getScrollY()+",scroll view height>>"+view.getHeight());
				if (dropDown && view.getScrollY() == 0) {
					// 判断Scrollview 滚动到底部
					return ACTION_PULL_DOWN;
				} else if (!dropDown && (view.getHeight() + view.getScrollY()) >= scrollViewHeight) {
					// 判断Scrollview 滚动到底部
					return ACTION_PULL_UP;
				}
			}
		} else if (child instanceof AbsListView) {
			AbsListView view = (AbsListView) child;
			if (view.getChildCount() > 0) {
				if (!dropDown && view.getLastVisiblePosition() == (view.getCount() - 1)) {
					Rect r = new Rect();
					View topChildview = view.getChildAt(view.getChildCount() - 1);
					topChildview.getHitRect(r);
					// Log.d(TAG, "rect>>" + r);
					// 判断list view 滚动到底部
					if (r.bottom <= getHeight()) {
						return ACTION_PULL_UP;
					}
					return ACTION_NONE;
				} else if (dropDown && view.getFirstVisiblePosition() == 0) {
					Rect r = new Rect();
					View topChildview = view.getChildAt(0);
					topChildview.getHitRect(r);
					// Log.d(TAG, "rect>>" + r);
					// 判断list view 滚动到顶部
					return r.top == 0 ? ACTION_PULL_DOWN : ACTION_NONE;
				}
			}
		} else {
			// Log.i(TAG, "other view");
			return dropDown ? ACTION_PULL_DOWN : ACTION_PULL_UP;
		}
		return ACTION_NONE;
	}

	private boolean isScrollerView(View child) {
		if (child instanceof AbsListView || child instanceof ScrollView) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 监听CLItemBounceView返回状态
	 * 
	 * @author jianfeng.lao
	 * @version 1.0
	 * @CreateDate 2013-5-6
	 */
	public static interface CLItemBounceViewListener {
		/**
		 * 
		 * @param action 当前动作
		 * @param distance 动作距离
		 * @author jianfeng.lao
		 * @CreateDate 2013-5-6
		 */
		public void onStateChange(int action, int distance);
	}

	public void setCLItemBounceViewListener(CLItemBounceViewListener mCLItemBounceViewListener) {
		this.mCLItemBounceViewListener = mCLItemBounceViewListener;
	}

}
