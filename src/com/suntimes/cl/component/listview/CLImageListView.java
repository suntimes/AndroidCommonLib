package com.suntimes.cl.component.listview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.suntimes.cl.component.imageview.bitmapview.CLBitmapView.CLItemBitmapViewInterface;

public class CLImageListView extends ListView {
	private static final String TAG = "ItemListView";

	private static final int MSG_CHECK_SCROLL_STATE = 0x00000001;
	private static final int CHECK_SCROLL_STATE_DELAY = 350;// 滚动时每隔50毫秒设置一次
	private int mFirstVisibleItem = -1;// 滚动时每隔50毫秒设置一次
	private int mVisibleItemCount = -1;
	private int mLastFirstVisibleItem = -1;
	private int mLastVisibleItemCount = -1;
	private boolean mIsScrolling = false;
	private OnListViewScrollListener mScrollListener;
	/**
	 * true 如果childView是实现CLItemBitmapViewInterface会自动loadImage
	 */
	private boolean mIsAutoLoadImage=false;

	public CLImageListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setupListener();
	}

	public CLImageListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupListener();
	}

	public CLImageListView(Context context) {
		super(context);
		setupListener();
	}

	
	private void setupListener() {
		super.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (mScrollListener != null) {
					mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

					int ptp = view.pointToPosition(0, 0);
//					 Log.i(TAG, "firstVisibleItem:" + firstVisibleItem + " visibleItemCount:" + visibleItemCount + " totalItemCount:" + totalItemCount);
					if (ptp != AdapterView.INVALID_POSITION) {
						// if (obtainDataListener != null && obtainDataThread.isInitServerContext()) {
						sendScrollStatusMsg(firstVisibleItem, visibleItemCount);

						// }
					}
				}

			}
		});
	}
	
	

	private void sendScrollStatusMsg(int firstVisibleIndex, int visibleCount) {
		mLastFirstVisibleItem = firstVisibleIndex;
		mLastVisibleItemCount = visibleCount;
		if (mLastFirstVisibleItem == mFirstVisibleItem && mLastVisibleItemCount == mVisibleItemCount) {
			return;
		}
		if (!mIsScrolling&&!handler.hasMessages(MSG_CHECK_SCROLL_STATE)) {
			Log.i(TAG,"sendScrollStatusMsg>>"+firstVisibleIndex+","+visibleCount);
			handler.sendEmptyMessage(MSG_CHECK_SCROLL_STATE);
		}

	}

	private Handler handler = new Handler() {

		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case MSG_CHECK_SCROLL_STATE:
//				Log.i(TAG,lastFirstVisibleItem+","+lastVisibleItemCount+"  "+firstVisibleItem+","+visibleItemCount);
				if (mLastFirstVisibleItem != mFirstVisibleItem || mLastVisibleItemCount != mVisibleItemCount) {
					mFirstVisibleItem = mLastFirstVisibleItem;
					mVisibleItemCount = mLastVisibleItemCount;
					mIsScrolling = true;
					sendEmptyMessageDelayed(MSG_CHECK_SCROLL_STATE, CHECK_SCROLL_STATE_DELAY);
				} else {
					if(mIsAutoLoadImage){
						int i=0,size=getChildCount();
						for(;i<size;i++){
							View child=getChildAt(i);
							if(child instanceof CLItemBitmapViewInterface){
								CLItemBitmapViewInterface clbvi=(CLItemBitmapViewInterface)child;
								if(clbvi.getItemBitmapView()!=null){
									clbvi.getItemBitmapView().loadImageOneByOne();
								}
							}
						}
					}
					mScrollListener.onScrollWidthDelay(mFirstVisibleItem, mVisibleItemCount);
					mIsScrolling = false;
				}
				Log.i(TAG, "isScrolling =="+mIsScrolling);
				break;
			}
			
			super.dispatchMessage(msg);
		}
	};

	public interface OnListViewScrollListener {
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount);

		public void onScrollWidthDelay(int firstVisibleItem, int visibleItemCount);
	}

	public void setOnListViewScrollListener(OnListViewScrollListener scrollListener) {
		this.mScrollListener = scrollListener;
	}

	public void resetScrollAttribute() {
		mFirstVisibleItem = -1;
		mVisibleItemCount = -1;
		mLastFirstVisibleItem = -1;
		mLastVisibleItemCount = -1;
		mIsScrolling = false;
	}
	/**
	 * 
	 * @param mIsAutoLoadImage true 如果childView是实现CLItemBitmapViewInterface会自动loadImage
	 * @author jianfeng.lao
	 * @CreateDate 2013-5-2
	 */
	public void setIsAutoLoadImage(boolean mIsAutoLoadImage) {
		this.mIsAutoLoadImage = mIsAutoLoadImage;
	}

	public boolean isIsAutoLoadImage() {
		return mIsAutoLoadImage;
	}
	
	
	
}
