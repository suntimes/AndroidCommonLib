package com.suntimes.cl.component.gearview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

/**
 * 
 * 
 * @author Zhao Wang
 * @version 1.0
 * @CreateDate 2013-5-3
 */
public class CLGearView extends AdapterView<Adapter> implements GestureDetector.OnGestureListener {

	public static final String TAG = "ItemGearView";

	/**
	 * Number for childs, default is 8
	 * 子控件的数量
	 */
	// public static final int COUNT_CHILD_VIEW = 8;
	// change by jiu at 20130322
	public static final int COUNT_CHILD_VIEW = 16;

	private float mBetweenAngle = 360.0f / COUNT_CHILD_VIEW;

	/**
	 * Store child data for item view
	 * 保存齿轮子控件数据的数组
	 */
	private ItemGearChildData[] mChildViewsData = new ItemGearChildData[COUNT_CHILD_VIEW];

	/**
	 * Helper for detecting touch gestures.
	 * 手势识别
	 */
	private GestureDetector mGestureDetector;

	/**
	 * Executes the delta rotations from a fling or scroll movement.
	 * 创建执行旋转动作线程任务
	 */
	private FlingRotateRunnable mFlingRunnable = new FlingRotateRunnable();

	/**
	 * How long the transition animation should run when a child view changes
	 * position, measured in milliseconds.
	 * 动画时间
	 */
	private int mAnimationDuration = 300;

	/**
	 * The position of the item that received the user's down touch.
	 * 接受到action_down的子控件编号
	 */
	private int mDownTouchPosition;

	/**
	 * When fling runnable runs, it resets this to false. Any method along the
	 * path until the end of its run() can set this to true to abort any
	 * remaining fling. For example, if we've reached either the leftmost or
	 * rightmost item, we will set this to true.
	 */
	private boolean mShouldStopFling;

	/**
	 * If true, this onScroll is the first for this user's drag (remember, a
	 * drag sends many onScrolls).
	 */
	private boolean mIsFirstScroll;

	/**
	 * Whether to continuously callback on the item selected listener during a
	 * fling.
	 */
	private boolean mShouldCallbackDuringFling = true;

	/**
	 * If true, do not callback to item selected listener.
	 * 完成选择判断
	 */
	private boolean mSuppressSelectionChanged;

	/**
	 * 点击子控件监听器
	 */
	private OnItemClickListener mItemClickListener;

	private float mCurrentDegree;

	/**
	 * 顶部Item控件
	 */
	private View mTopItemView;

	/**
	 * 数据适配器
	 */
	private Adapter mAdapter;

	/**
	 * 存放子控件视图的集合
	 */
	private ArrayList<View> mSortChildList;

	/**
	 * 中心Y坐标
	 */
	private float centerY;

	private float radius = 0;

	private boolean isCircleStyle = false;

	private int invalidateDelay = 100;

	private Camera camera = new Camera();

	// 自动滚动时使用的角度参数
	private int dergeeIncreate = 1;

	// private boolean isAutoRun = false;
	// add by jiu at 20130325
	private boolean isRunning = false;

	// //add by jiu at 20130415
	// private boolean isSingleTapUp = false;

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CLGearView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGearView();
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public CLGearView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGearView();
	}

	/**
	 * @param context
	 */
	public CLGearView(Context context) {
		super(context);
		initGearView();
	}

	// Close by jiu at 20130325
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mItemClickListener = listener;
	}

	private void initGearView() {
		setWillNotDraw(false);
		setClipChildren(true);
		setChildrenDrawnWithCacheEnabled(true);
		setChildrenDrawingCacheEnabled(true);
		setChildrenDrawingOrderEnabled(true);
		setStaticTransformationsEnabled(true);

		mGestureDetector = new GestureDetector(this.getContext(), this);

	}

	// 初始话问题在这个方法解决
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// Log.i(TAG, "onSizeChanged>>" + w + "," + h + ",getW>>" + getWidth()
		// + ",getH>>" + getHeight());

		radius = (getHeight() / 1.5f);// 1.6
		centerY = getHeight() / 2;
		int childHeight = (int) (getHeight() / 3.4);
		// Log.i(TAG, TAG+"高度为================"+getHeight()/3.4);
		if (childHeight < 188) {
			childHeight = 188;
		}
		// int gap =(int)((getWidth()*0.05)/2);
		// int childWiddth = getWidth()-2*gap;
		int mode = MeasureSpec.EXACTLY;
		int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth(), mode);
		int heightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, mode);// 3.5
		// Log.i(TAG, TAG+"获取最少宽度"+(int)(getHeight()/3.5));
		//开始初始化角度，与0度相隔6个item
		float lastAngle = 225;
		mCurrentDegree = 360;

		if (mAdapter != null) {
			View child;
			int n;
			ItemGearChildData childData;
			for (int i = 0; i < COUNT_CHILD_VIEW; i++) {
				n = i;
				// 有待改进
				if (mAdapter.getCount() > 0) {
					//由于0度必须显示第一条加载的信息，所有需要对加载数据向后顺移6位
					n -=6; 
					while(n < 0){
						n = mAdapter.getCount() + n;
					}
					while(n > (mAdapter.getCount() - 1)) {
						n = n - mAdapter.getCount();
					}
					child = mAdapter.getView(n, null, this);

				} else {
					child = new View(getContext());
				}
				// 创建数据对象
				childData = new ItemGearChildData();
				// 数据源编号
				childData.setIndex(n);
				childData.setPosition(i);
				childData.setDegress(lastAngle);
				lastAngle = lastAngle + 360f / COUNT_CHILD_VIEW;
				mChildViewsData[i] = childData;

				if (lastAngle > 360) {
					lastAngle = lastAngle - 360;
				}
				child.setTag(childData);

				// Log.i(TAG, TAG+ "这是第 " + i + " 个item，" +"初始化角度为："+ lastAngle);
				// Close by jiu at 20130322 not used
				// if (Build.VERSION.SDK_INT >= 11) {
				// childData.setMatrix(new Matrix());// api 11要设置
				// }
				child.measure(widthMeasureSpec, heightMeasureSpec);
				// 将控件放在正中
				child.layout(0,
						getHeight() / 2 - child.getMeasuredHeight() / 2,
						child.getMeasuredWidth(),
						getHeight() / 2 + child.getMeasuredHeight() / 2);
				addAndMeasureChild(child, 0);
			}
			// 测试使用确认初始角度
			// for(int i =0; i <mChildViewsData.length; i++){
			// Log.i(TAG, TAG+"当前控件item位置编号="+mChildViewsData[i].getPosition());
			// Log.i(TAG, TAG+"当前控件资源编号="+mChildViewsData[i].getIndex());
			// Log.i(TAG, TAG+"当前控件item的角度="+mChildViewsData[i].getDegress());
			// }
		}

		// setupChildViews();
		requestLayout();

	}

	// 将所items转换成一个齿轮
	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		ItemGearChildData childData = (ItemGearChildData) child.getTag();
		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);
		Matrix childMatrix = t.getMatrix();
		float degress = childData.getDegress();

		// 使用画圆的方式实现
		float[] pointYZ = getCircleZY(degress);
		camera.save();
		camera.translate(0, pointYZ[0], pointYZ[1]);
		// camera.dotWithNormal(0, pointYZ[0], pointYZ[1]);
		camera.getMatrix(childMatrix);
		int centerY = child.getMeasuredHeight() / 2;
		int centerX = child.getMeasuredWidth() / 2;
		childMatrix.preTranslate(-centerX, -centerY);
		childMatrix.postTranslate(centerX, centerY);
		camera.restore();

		child.invalidate();
		return true;
	}

	private void addAndMeasureChild(final View child, int viewPos) {
		LayoutParams params = (LayoutParams) child.getLayoutParams();
		if (params == null) {
			params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
		addViewInLayout(child, viewPos, params, true);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		mSortChildList = new ArrayList<View>();
		for (int i = 0; i < COUNT_CHILD_VIEW; i++) {
			mSortChildList.add(this.getChildAt(i));
		}
		// 根据角度排序比较排序
		Collections.sort(mSortChildList, new Comparator<View>() {
			@Override
			public int compare(View child1, View child2) {
				ItemGearChildData childData1 = (ItemGearChildData) child1.getTag();
				ItemGearChildData childData2 = (ItemGearChildData) child2.getTag();
				float aDegress = childData1.getDegress();
				if (aDegress > 180) {
					aDegress = 360 - aDegress;
				}

				float bDegress = childData2.getDegress();
				if (bDegress > 180) {
					bDegress = 360 - bDegress;
				}
				return (int) (bDegress - aDegress);
			}

		});

		// 测试使用
		// for(int i = 0; i < getChildCount(); i++){
		// ItemGearChildData data = (ItemGearChildData) getChildAt(i).getTag();
		// Log.i(TAG, TAG+"内部getChildAt()数据排列：item 位置"+ data.getPosition()+"，推广编号"+data.getIndex()+",这是第i="+i);
		// }

		final long drawingTime = getDrawingTime();
		float degress;
		float alphaLevel;
		View child;
		mTopItemView = mSortChildList.get(mSortChildList.size() - 1);
		for (int i = 0; i < COUNT_CHILD_VIEW; i++) {
			// drawChild(canvas, mSortChildList.get(i), drawingTime);

			child = mSortChildList.get(i);
			ItemGearChildData childData = (ItemGearChildData) child.getTag();

			// 设置透明效果和需要显示的子item
			alphaLevel = childData.getDegress();
			if (alphaLevel > 180) {
				alphaLevel = Math.abs((360f - alphaLevel));
			}
			// alphaLevel < 65 时显示5个item
			if (alphaLevel < 85) {
				if (alphaLevel < 1 && !isRunning) {
					// child.setBackgroundResource(R.drawable.bg_gear_box_on);
					child.setBackgroundColor(Color.YELLOW);
				} else {
					// child.setBackgroundResource(R.drawable.bg_gear_box);
					child.setBackgroundColor(Color.WHITE);
				}
				// 有需要的item才画出来
				drawChild(canvas, child, drawingTime);
				/*
				 * 以下是通过角度改变透明度的代码，在 api 11 之后才能支持
				 */
				// 在45度的位置透明度提高20%
				// alphaLevel = (float) (1 - (alphaLevel * (2 / 450f)));
				// if (Build.VERSION.SDK_INT >= 11) {
				// child.setAlpha(alphaLevel);// api 11要设置
				// }
			}
		}

		// Log.i(TAG, "dispatchDraw");
		// if (android.os.Build.VERSION.SDK_INT >= 16 /*JellyBean*/) {
		// for (int i = 0; i < getChildCount(); i ++) {
		// Log.i(TAG, "dispatchDraw child");
		// // Item must be invalidate with JellyBean and superior
		// if ( android.os.Build.VERSION.SDK_INT >= 16 ) {
		// getChildAt(i).invalidate();
		// }
		// }
		//
		// invalidate();
		// }
	}

	@Override
	public Adapter getAdapter() {
		return mAdapter;
	}

	@Override
	public View getSelectedView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAdapter(Adapter adapter) {
		if (mAdapter != null) {
			mAdapter = null;
		}
		this.mAdapter = adapter;
	}

	@Override
	public void setSelection(int position) {

	}

	/**
	 * 计算圆中每个点的YZ座标
	 * 
	 * @param degress
	 * @return
	 */
	public float[] getCircleZY(float degress) {
		// 半径有问题
		float centerZ = (getHeight() / 1.5f);// 1.6
		float centerY = 0;
		float[] pointYZ = new float[2];
		float z = centerZ - radius * android.util.FloatMath.cos((float) (degress * Math.PI / 180f));// 1(degrees)=(2pi *
																									// radian)/360
		float y = centerY - radius * android.util.FloatMath.sin((float) (degress * Math.PI / 180f));
		pointYZ[0] = y;
		pointYZ[1] = z;
		return pointYZ;
	}

	// ******************************* 手势相关代码 ***********************************

	// private static final int MSG_CYCLE_SCROLL = 0x0000001;
	// Handler handler = new Handler() {
	//
	// @Override
	// public void handleMessage(Message msg) {
	//
	// switch (msg.what) {
	// case MSG_CYCLE_SCROLL:
	// trackMotionScroll(dergeeIncreate);自动滚动调用的方法
	// sendEmptyMessageDelayed(MSG_CYCLE_SCROLL, invalidateDelay);
	// break;
	// }
	//
	// super.handleMessage(msg);
	// }
	//
	// };

	public int getInvaldateDelay() {
		return invalidateDelay;
	}

	public void setInvaldateDelay(int invaldateDelay) {
		this.invalidateDelay = invaldateDelay;
	}

	// public void setCircleStyle(boolean isCircleStyle) {
	// this.isCircleStyle = isCircleStyle;
	// }

	// public boolean isAutoRun() {
	// return isAutoRun;
	// }

	// 自动滚动
	// public void setAutoRun(boolean isAutoRun) {
	// this.isAutoRun = isAutoRun;
	// if (isAutoRun) {
	// handler.sendEmptyMessage(MSG_CYCLE_SCROLL);
	// } else {
	// handler.removeMessages(MSG_CYCLE_SCROLL);
	// }
	// }

	/**
	 * 获取前一项数据id
	 * 
	 * @param offset
	 * @return
	 */
	private int getPreviousIndex(int offset) {
		if (mAdapter != null) {
			return offset < mAdapter.getCount() && offset - 1 >= 0 ? offset - 1 : mAdapter.getCount() - 1;
		} else {
			return -1;
		}
	}

	/**
	 * 获取下一项数据id
	 * 
	 * @param offset
	 * @return
	 */
	private int getNextIndex(int offset) {
		if (mAdapter != null) {
			return offset >= 0 && offset + 1 < mAdapter.getCount() ? offset + 1 : 0;
		} else {
			return -1;
		}
	}

	/**
	 * Brings an item with nearest to 0 degrees angle
	 * to this angle and sets it selected
	 * 将Item移动到0角度位置并设置为选中~~~~~~~~~~~~~~~~~~~~~~~~~~~~~重点~~~~~~~~~~~~~
	 * 根据最接近0度的item将其定位到0度位置，
	 * 使得有校准位置的效果
	 */
	private void scrollIntoSlots() {
		// Nothing to do
		if (getChildCount() == 0) {
			return;
		}

		ItemGearChildData childData1;
		float angle = 0f;
		if (mSortChildList != null && mSortChildList.size() > 0) {
			childData1 = (ItemGearChildData) mSortChildList.get(mSortChildList.size() - 1).getTag();
			angle = childData1.getDegress();
		}

		// Make it minimum to rotate
		if (angle > 180.0f) {
			angle = -(360.0f - angle);
		}

		// Start rotation if needed
		if (angle != 0.0f) {
			mFlingRunnable.startUsingDistance(angle, 300);
		} else {
			onFinishedMovement();
		}
	}

	/**
	 * Called when rotation is finished
	 * 完成旋转时执行这个方法
	 */
	private void onFinishedMovement() {
		if (mSuppressSelectionChanged) {
			mSuppressSelectionChanged = false;

			// We haven't been callbacking during the fling, so do it now
			// super.selectionChanged();
		}
		// checkSelectionChanged();
	}

	/**
	 * Tracks a motion scroll. In reality, this is used to do just about any
	 * movement to items (touch scroll, arrow-key scroll, set an item as selected).
	 * 
	 * @param deltaAngle Change in X from the previous event.
	 *            修改齿轮中item角度的方法
	 */
	// set to position
	void trackMotionScroll(float deltaAngle) {
		if (deltaAngle == 0) {
			isRunning = false;
		}

		mCurrentDegree = mCurrentDegree + deltaAngle;

		// 方案四 齿轮数据动态加载并头尾相接实现代码
		// 由于初始化时 由225角度为数据开始加载点，所以 202.5 为更新起点。
		// 当deltaAngle < 0 为 向下拉的情况
		// 此时 需要以比213.75度大同时离213.75度最近的角度作为参照对象，(225f)
		// 对小于213.75度的对象进行更新
		// 通过deltaAngle 与 betweenAngle 的倍数关系加一得出需要更新的个数
		if (deltaAngle < 0 && mAdapter != null && mAdapter.getCount() > 0) {
			View referenceItem = getChildByDegress(225f);
			ItemGearChildData referenceData = (ItemGearChildData) referenceItem.getTag();
			int referenceItemPosition = referenceData.getPosition();
			int referenceDataPosition = referenceData.getIndex();
			// 计算需要更新的item个数
			int changeCount = (int) (-deltaAngle / mBetweenAngle) + 1;
			View changView;
			int changeItemPosition;
			int changeDataPosition;
			int dataCount = 0;
			for (int i = 1; i <= changeCount; i++) {
				dataCount = mAdapter.getCount();
				// 找出需要更新数据的item
				changeItemPosition = referenceItemPosition - i;
				while (changeItemPosition < 0) {
					changeItemPosition = COUNT_CHILD_VIEW + changeItemPosition;
				}
				changView = getChildByItemPosition(changeItemPosition);
				// 找出需要替换的新数据并更新
				changeDataPosition = referenceDataPosition - i;
				while (changeDataPosition < 0) {
					changeDataPosition = dataCount + changeDataPosition;
				}
				changView = changeViewByDataPosition(changeDataPosition, changView);
			}
			// 由于初始化时 由225角度为数据开始加载点，所以 202.5 为更新起点。
			// 当deltaAngle > 0 为 向上推的情况
			// 此时 需要以比213.75度小，同时离213.75度最近的角度作为参照对象，(180f)
			// 对大于213.75度 后的对象进行更新
			// 通过deltaAngle 与 betweenAngle 的倍数关系加一得出需要更新的个数
		} else if (deltaAngle > 0 && mAdapter != null && mAdapter.getCount() > 0) {
			View referenceItem = getChildByDegress(180f);
			ItemGearChildData referenceData = (ItemGearChildData) referenceItem.getTag();
			int referenceItemPosition = referenceData.getPosition();
			int referenceDataPosition = referenceData.getIndex();
			// 计算需要更新的item个数
			int changeCount = (int) (deltaAngle / mBetweenAngle) + 1;
			View changView;
			int changeItemPosition;
			int changeDataPosition;
			int dataCount = 0;
			for (int i = 1; i <= changeCount; i++) {
				dataCount = mAdapter.getCount();
				// 找出需要更新数据的item
				changeItemPosition = referenceItemPosition + i;
				while (changeItemPosition > COUNT_CHILD_VIEW - 1) {
					changeItemPosition = changeItemPosition - COUNT_CHILD_VIEW;
				}
				changView = getChildByItemPosition(changeItemPosition);
				// 找出需要替换的新数据并更新
				changeDataPosition = referenceDataPosition + i;
				while (changeDataPosition > dataCount - 1) {
					changeDataPosition = changeDataPosition - dataCount;
				}
				changView = changeViewByDataPosition(changeDataPosition, changView);
			}
		}

		// Log.i(TAG, "trackMotionScroll >> deltaAngle = " + deltaAngle);

		// for(int i = 0; i < mSortChildList.size(); i ++) {
		// View child = mSortChildList.get(i);
		// ItemGearChildData data = (ItemGearChildData)child.getTag();
		// if(data != null) {
		// boolean isItemChanged = false;
		// if(data.getDegress() <= 240 && (data.getDegress() + deltaAngle) >= 240) {
		// isItemChanged = true;
		// } else if(data.getDegress() >= 240 && (data.getDegress() + deltaAngle) <= 240) {
		// isItemChanged = true;
		// }
		//
		// if(isItemChanged) {
		// int position = 0;
		// if(mCurrentDegree > 0) {
		// position = mCurrentDegree / 40;
		// }
		// mAdapter.getView(position, child, this);
		// break;
		// }
		// }
		// }

		// Log.i(TAG, TAG+"查看刷新角度的大小"+deltaAngle);
		// int i = 0;
		int childCount = getChildCount();
		View child;
		ItemGearChildData childData;
		float angle;
		for (int i = 0; i < childCount; i++) {
			child = getChildAt(i);
			childData = (ItemGearChildData) child.getTag();
			angle = childData.getDegress();
			angle -= deltaAngle;
			while (angle > 360) {
				angle -= 360;
			}
			while (angle < 0) {
				angle += 360;
			}
			// if (angle >= 270 && angle <= 90) {
			// ItemGearView.this.bringChildToFront(item);
			// }

			childData.setDegress(angle);
			child.invalidate();
		}
		invalidate();
	}

	private View getChildByItemPosition(int itemPosition) {
		// 常规代码，优化代码出错的情况下使用(请勿删除)
		// View child;
		// int childItemPosition;
		// for(int i = 0; i < getChildCount(); i++){
		// child = getChildAt(i);
		// childItemPosition = ((ItemGearChildData)child.getTag()).getPosition();
		// if(childItemPosition == itemPosition){
		// return child;
		// }
		// }
		// return child;
		// 根据(onSizeChange()初始化时)添加item的顺序得出此结论，(这是优化代码)失效时请使用常规代码
		return getChildAt(COUNT_CHILD_VIEW - 1 - itemPosition);
	}

	// 有待改进，此方法对 大于360度 的item不支持 ，请传入正确的度数，只供内部使用
	private View getChildByDegress(float degress) {
		View child;
		float childDegress;
		float lowMatchAngle = degress - (float) ((int) mBetweenAngle / 2);
		float highMatchAngle = degress + mBetweenAngle - (float) ((int) mBetweenAngle / 2);
		for (int i = 0; i < getChildCount(); i++) {
			child = getChildAt(i);
			childDegress = ((ItemGearChildData) child.getTag()).getDegress();
			// 当degress 为 大于360度时 此方法不正确
			if (lowMatchAngle < childDegress && highMatchAngle >= childDegress) {
				return child;
			}
		}
		// 当没有找到数据时，返回一个空内容的View
		return new View(getContext());
	}

	private View changeViewByDataPosition(int newChildDataPosition, View child) {
		ItemGearChildData data = (ItemGearChildData) child.getTag();
		data.setIndex(newChildDataPosition);
		child = mAdapter.getView(newChildDataPosition, child, this);
		// 为空的情况
		if (child == null) {
			child = new View(getContext());
			child.setTag(data);
			return child;
		}
		// 调试使用
		// ((ItemGearCell)child).setText("这是第 " + data.getPosition() +" 个item, 第 " + data.getIndex() + "条推广");
		return child;
	}

	/**
	 * Maps a point to a position in the list.
	 * 
	 * @param x X in local coordinate
	 * @param y Y in local coordinate
	 * @return The position of the item which contains the specified point, or {@link #INVALID_POSITION} if the point
	 *         does not intersect an item.
	 */
	public int pointToPosition(int x, int y) {
		return 0;
	}

	void onUp() {
		if (mFlingRunnable.mRotator.isFinished()) {
			scrollIntoSlots();
		}

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// Log.d(TAG, "dispatchTouchEvent");
		boolean retValue = mGestureDetector.onTouchEvent(event);
		int action = event.getAction();
		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
			// Helper method for lifted finger
			onUp();
		}
		return retValue;
	}

	// @Override
	// public boolean onInterceptTouchEvent(MotionEvent ev) {
	// Log.d(TAG, "onInterceptTouchEvent");
	// if(ev.getAction() == MotionEvent.ACTION_DOWN) {
	// return true;
	// }
	// return true;
	// }

	/**
	 * 当接收到到onDown事件时,调用旋转任务中的 stop 方法，
	 * 停止旋转，并执行矫正方法
	 */
	@Override
	public boolean onDown(MotionEvent e) {
		// Kill any existing fling/scroll
		mFlingRunnable.stop(false);
		scrollIntoSlots();

		// /// Don't know yet what for it is
		// Get the item's view that was touched
		mDownTouchPosition = pointToPosition((int) e.getX(), (int) e.getY());

		if (mDownTouchPosition >= 0) {
			// mDownTouchView = getChildAt(mDownTouchPosition - mFirstPosition);
		}

		// Reset the multiple-scroll tracking state
		mIsFirstScroll = true;

		// Must return true to get matching events for this down event.
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// if(isSingleTapUp){
		// return true;
		// }
		// isSingleTapUp = true;
		// if(mFlingRunnable.mRotator.isFinished()) {
		if (this.mItemClickListener != null) {
			ItemGearChildData childData = (ItemGearChildData) mTopItemView.getTag();
			float y = e.getY();
			// 测试使用
			// Log.i(TAG, "onSingleTapUp y >> " + y);
			if ((y >= (this.getMeasuredHeight() / 2 - mTopItemView.getMeasuredHeight() / 2))
					&& (y <= (this.getMeasuredHeight() / 2 + mTopItemView.getMeasuredHeight() / 2))) {
				if (mAdapter.getCount() > 0) {
					// 如果点中中间控件就调用响应事件
					this.mItemClickListener.onItemClick(this, mTopItemView, childData.getPosition(),
							childData.getIndex());
					return true;
				}
			} else if ((y < (this.getMeasuredHeight() / 2 - mTopItemView.getMeasuredHeight() / 2))
					&& (y > (this.getMeasuredHeight() / 2 - 2 * (mTopItemView.getMeasuredHeight() / 2)))) {
				mFlingRunnable.startUsingDistance(-22f, 190);
				return true;
			} else if ((y < (this.getMeasuredHeight() / 2 - 2 * (mTopItemView.getMeasuredHeight() / 2)))
					&& (y > 0)) {
				// (this.getMeasuredHeight() / 2 - 3*(mTopItemView.getMeasuredHeight() / 2))
				mFlingRunnable.startUsingDistance(-49f, 200);
				return true;
			} else if ((y > (this.getMeasuredHeight() / 2 + mTopItemView.getMeasuredHeight() / 2))
					&& (y < (this.getMeasuredHeight() / 2 + 2 * (mTopItemView.getMeasuredHeight() / 2)))) {
				mFlingRunnable.startUsingDistance(22f, 190);
				return true;
			} else if ((y > (this.getMeasuredHeight() / 2 + 2 * (mTopItemView.getMeasuredHeight() / 2)))
					&& (y < this.getMeasuredHeight())) {
				// (this.getMeasuredHeight() / 2 + 3 *(mTopItemView.getMeasuredHeight() / 2))
				mFlingRunnable.startUsingDistance(49f, 200);
				return true;
			}
		}
		// }
		// else {
		// mFlingRunnable.mRotator.forceFinished(true);
		// }
		// Log.e(TAG, "onSingleTapUp >> mDownTouchPosition = " + mDownTouchPosition + ", mSelectedPosition = " +
		// mSelectedPosition + ", isAnimateFinished: " + mFlingRunnable.mRotator.isFinished());
		// if (mDownTouchPosition >= 0) {
		//
		// // Pass the click so the client knows, if it wants to.
		// if (mFlingRunnable.mRotator.isFinished() && mShouldCallbackOnUnselectedItemClick || mDownTouchPosition ==
		// mSelectedPosition) {
		// performItemClick(mDownTouchView, mDownTouchPosition, mAdapter
		// .getItemId(mDownTouchPosition));
		// }
		//
		// return true;
		// }
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		/*
		 * Now's a good time to tell our parent to stop intercepting our events!
		 * The user has moved more than the slop amount, since GestureDetector
		 * ensures this before calling this method. Also, if a parent is more
		 * interested in this touch's events than we are, it would have
		 * intercepted them by now (for example, we can assume when a Gallery is
		 * in the ListView, a vertical scroll would not end up in this method
		 * since a ListView would have intercepted it by now).
		 */

		getParent().requestDisallowInterceptTouchEvent(true);

		isRunning = true;
		// As the user scrolls, we want to callback selection changes so related-
		// info on the screen is up-to-date with the gallery's selection
		if (!mShouldCallbackDuringFling) {
			if (mIsFirstScroll) {
				/*
				 * We're not notifying the client of selection changes during
				 * the fling, and this scroll could possibly be a fling. Don't
				 * do selection changes until we're sure it is not a fling.
				 */
				if (!mSuppressSelectionChanged) {
					mSuppressSelectionChanged = true;
				}
				// postDelayed(mDisableSuppressSelectionChangedRunnable, SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT);
			}
		} else {
			if (mSuppressSelectionChanged) {
				mSuppressSelectionChanged = false;
			}
		}

		// Track the motion
		trackMotionScroll(/* -1 * */(int) distanceY / 3);

		mIsFirstScroll = false;
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (!mShouldCallbackDuringFling) {
			if (!mSuppressSelectionChanged) {
				mSuppressSelectionChanged = true;
			}
		}

		// Fling the gallery!
		// final int distance = (int) (velocityY / 5);
		// mFlingRunnable.startUsingDistance(-distance, mAnimationDuration + Math.abs(distance));
		// float distance = velocityY / 80f;
		float distance = velocityY / 60f;
		mFlingRunnable.startUsingDistance(-distance, mAnimationDuration + Math.abs((int) distance));

		return true;
	}

	/**
	 * 由于控件动画有待完善，所以在使用本控件时，
	 * 在外部调用此方法才能安全退出
	 * 
	 * @author Zhao Wang
	 * @CreateDate 2013-4-28
	 */
	public void stopGear() {
		boolean complete = false;
		while (!complete) {
			// Log.i(TAG, TAG + "检测removeCallbacks方法的返回" + removeCallbacks(mFlingRunnable));
			complete = removeCallbacks(mFlingRunnable);
		}
	}

	private static class ItemGearChildData {

		private float xOffset, yOffset, zOffset;
		private Matrix matrix;
		private int position;
		private float degress;
		private int index;

		public float getxOffset() {
			return xOffset;
		}

		public void setxOffset(float xOffset) {
			this.xOffset = xOffset;
		}

		public float getyOffset() {
			return yOffset;
		}

		public void setyOffset(float yOffset) {
			this.yOffset = yOffset;
		}

		public float getzOffset() {
			return zOffset;
		}

		public void setzOffset(float zOffset) {
			this.zOffset = zOffset;
		}

		public Matrix getMatrix() {
			return matrix;
		}

		public void setMatrix(Matrix matrix) {
			this.matrix = matrix;
		}

		public int getPosition() {
			return position;
		}

		public void setPosition(int position) {
			this.position = position;
		}

		public float getDegress() {
			return degress;
		}

		public void setDegress(float degress) {
			this.degress = degress;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}
	}

	// Rotation class for the Carousel

	/**
	 * 执行旋转动作线程任务 类
	 * 
	 * @author Administrator
	 * 
	 */
	private class FlingRotateRunnable implements Runnable {

		/**
		 * Tracks the decay of a fling rotation
		 */
		private Rotator mRotator;

		/**
		 * Angle value reported by mRotator on the previous fling
		 */
		private float mLastFlingAngle;

		/**
		 * Constructor
		 */
		public FlingRotateRunnable() {
			mRotator = new Rotator(getContext());
		}

		// 移除上一次的旋转执行任务
		private void startCommon() {
			// Remove any pending flings
			removeCallbacks(this);
		}

		// 通过速率执行旋转的方法，暂时没有被调用
		public void startUsingVelocity(float initialVelocity) {
			if (initialVelocity == 0)
				return;

			startCommon();

			mLastFlingAngle = 0.0f;

			mRotator.fling(initialVelocity);
			post(this);
		}

		// 通过角度执行旋转的方法，暂时没有被调用
		public void startUsingDistance(float deltaAngle) {
			startUsingDistance(deltaAngle, mAnimationDuration);
		}

		// 通过角度执行旋转的方法，正在使用的旋转核心方法
		public void startUsingDistance(float deltaAngle, int duration) {
			if (deltaAngle == 0)
				return;

			startCommon();

			// mLastFlingAngle = 0;
			// 初始化需要进行运算的数据
			synchronized (this) {
				mRotator.startRotate(0.0f, -deltaAngle, duration);
			}
			// 这里用到了mFlingRunnable 的run()方法
			post(this);
		}

		// 停止旋转方法,出入参数为 是否进行 旋转结束 纠正
		public void stop(boolean scrollIntoSlots) {
			removeCallbacks(this);
			endFling(scrollIntoSlots);
		}

		// 结束旋转方法
		private void endFling(boolean scrollIntoSlots) {
			/*
			 * Force the scroller's status to finished (without setting its
			 * position to the end)
			 */
			synchronized (this) {
				mRotator.forceFinished(true);
			}

			if (scrollIntoSlots)
				scrollIntoSlots();
		}

		/**
		 * 执行旋转工作
		 */
		public void run() {
			if (CLGearView.this.getChildCount() == 0) {
				endFling(true);
				return;
			}

			mShouldStopFling = false;

			final Rotator rotator;
			final float angle;
			boolean more;
			synchronized (this) {
				rotator = mRotator;
				more = rotator.computeAngleOffset();
				angle = rotator.getCurrAngle();
			}

			// Flip sign to convert finger direction to list items direction
			// (e.g. finger moving down means list is moving towards the top)
			float delta = mLastFlingAngle - angle;

			// ////// Shoud be reworked
			trackMotionScroll(delta);
			// 判读是否继续 执行旋转
			if (more && !mShouldStopFling) {
				mLastFlingAngle = angle;
				post(this);
			} else {
				mLastFlingAngle = 0.0f;
				endFling(true);
			}

		}
	}

	/**
	 * This class encapsulates rotation. The duration of the rotation
	 * can be passed in the constructor and specifies the maximum time that
	 * the rotation animation should take. Past this time, the rotation is
	 * automatically moved to its final stage and computeRotationOffset()
	 * will always return false to indicate that scrolling is over.
	 */
	/**
	 * 旋转体，只在FlingRotateRunnable中使用
	 * 
	 * @author Administrator
	 * 
	 */
	public class Rotator {
		private int mMode;
		private float mStartAngle;
		private float mCurrAngle;

		private long mStartTime;
		private long mDuration;

		private float mDeltaAngle;

		private boolean mFinished;

		private float mCoeffVelocity = 0.05f;
		private float mVelocity;

		private static final int DEFAULT_DURATION = 250;
		private static final int SCROLL_MODE = 0;
		private static final int FLING_MODE = 1;

		private final float mDeceleration = 240.0f;

		/**
		 * Create a Scroller with the specified interpolator. If the interpolator is
		 * null, the default (viscous) interpolator will be used.
		 */
		public Rotator(Context context) {
			mFinished = true;
		}

		/**
		 * 
		 * Returns whether the scroller has finished scrolling.
		 * 
		 * @return True if the scroller has finished scrolling, false otherwise.
		 */
		public final boolean isFinished() {
			return mFinished;
		}

		/**
		 * Force the finished field to a particular value.
		 * 
		 * @param finished The new finished value.
		 */
		public final void forceFinished(boolean finished) {
			mFinished = finished;
		}

		/**
		 * Returns how long the scroll event will take, in milliseconds.
		 * 
		 * @return The duration of the scroll in milliseconds.
		 */
		public final long getDuration() {
			return mDuration;
		}

		/**
		 * Returns the current X offset in the scroll.
		 * 
		 * @return The new X offset as an absolute distance from the origin.
		 */
		public final float getCurrAngle() {
			return mCurrAngle;
		}

		/**
		 * @hide
		 *       Returns the current velocity.
		 * 
		 * @return The original velocity less the deceleration. Result may be
		 *         negative.
		 */
		// Close by jiu at 20130322
		// 速率有关的方法
		// public float getCurrVelocity() {
		// return mCoeffVelocity * mVelocity - mDeceleration * timePassed() /* / 2000.0f*/;
		// }

		/**
		 * Returns the start X offset in the scroll.
		 * 
		 * @return The start X offset as an absolute distance from the origin.
		 */
		// Close by jiu at 20130322
		// public final float getStartAngle() {
		// return mStartAngle;
		// }

		/**
		 * Returns the time elapsed since the beginning of the scrolling.
		 * 
		 * @return The elapsed time in milliseconds.
		 */
		public int timePassed() {
			return (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);
		}

		/**
		 * Extend the scroll animation. This allows a running animation to scroll
		 * further and longer, when used with {@link #setFinalX(int)} or {@link #setFinalY(int)}.
		 * 
		 * @param extend Additional time to scroll in milliseconds.
		 * @see #setFinalX(int)
		 * @see #setFinalY(int)
		 */
		// Close by jiu at 20130322
		// public void extendDuration(int extend) {
		// int passed = timePassed();
		// mDuration = passed + extend;
		// mFinished = false;
		// }

		/**
		 * Stops the animation. Contrary to {@link #forceFinished(boolean)},
		 * aborting the animating cause the scroller to move to the final x and y
		 * position
		 * 
		 * @see #forceFinished(boolean)
		 */
		// Close by jiu at 20130322
		// public void abortAnimation() {
		// mFinished = true;
		// }

		/**
		 * Call this when you want to know the new location. If it returns true,
		 * the animation is not yet finished. loc will be altered to provide the
		 * new location.
		 */
		public boolean computeAngleOffset()
		{
			if (mFinished) {
				return false;
			}
			// 时间校对
			long systemClock = AnimationUtils.currentAnimationTimeMillis();
			long timePassed = systemClock - mStartTime;

			if (timePassed < mDuration) {
				switch (mMode) {
				case SCROLL_MODE:

					float sc = (float) timePassed / mDuration;
					mCurrAngle = mStartAngle + Math.round(mDeltaAngle * sc);
					break;

				case FLING_MODE:

					float timePassedSeconds = timePassed / 1000.0f;
					float distance;

					if (mVelocity < 0)
					{
						distance = mCoeffVelocity * mVelocity * timePassedSeconds -
								(mDeceleration * timePassedSeconds * timePassedSeconds / 2.0f);
					}
					else {
						distance = -mCoeffVelocity * mVelocity * timePassedSeconds -
								(mDeceleration * timePassedSeconds * timePassedSeconds / 2.0f);
					}

					mCurrAngle = mStartAngle - Math.signum(mVelocity) * Math.round(distance);

					break;
				}
				return true;
			} else {
				mFinished = true;
				return false;
			}
		}

		/**
		 * Start scrolling by providing a starting point and the distance to travel.
		 * 
		 * @param startX Starting horizontal scroll offset in pixels. Positive
		 *            numbers will scroll the content to the left.
		 * @param startY Starting vertical scroll offset in pixels. Positive numbers
		 *            will scroll the content up.
		 * @param dx Horizontal distance to travel. Positive numbers will scroll the
		 *            content to the left.
		 * @param dy Vertical distance to travel. Positive numbers will scroll the
		 *            content up.
		 * @param duration Duration of the scroll in milliseconds.
		 */
		public void startRotate(float startAngle, float dAngle, int duration) {
			mMode = SCROLL_MODE;
			mFinished = false;
			mDuration = duration;
			mStartTime = AnimationUtils.currentAnimationTimeMillis();
			mStartAngle = startAngle;
			mDeltaAngle = dAngle;
		}

		/**
		 * Start scrolling by providing a starting point and the distance to travel.
		 * The scroll will use the default value of 250 milliseconds for the
		 * duration.
		 * 
		 * @param startX Starting horizontal scroll offset in pixels. Positive
		 *            numbers will scroll the content to the left.
		 * @param startY Starting vertical scroll offset in pixels. Positive numbers
		 *            will scroll the content up.
		 * @param dx Horizontal distance to travel. Positive numbers will scroll the
		 *            content to the left.
		 * @param dy Vertical distance to travel. Positive numbers will scroll the
		 *            content up.
		 */
		public void startRotate(float startAngle, float dAngle) {
			startRotate(startAngle, dAngle, DEFAULT_DURATION);
		}

		/**
		 * Start scrolling based on a fling gesture. The distance travelled will
		 * depend on the initial velocity of the fling.
		 * 
		 * @param velocityAngle Initial velocity of the fling (X) measured in pixels per second.
		 */
		public void fling(float velocityAngle) {

			mMode = FLING_MODE;
			mFinished = false;

			float velocity = velocityAngle;

			mVelocity = velocity;
			mDuration = (int) (1000.0f * Math.sqrt(2.0f * mCoeffVelocity *
					Math.abs(velocity) / mDeceleration));

			mStartTime = AnimationUtils.currentAnimationTimeMillis();

		}

	}

	/**
	 * Interface definition for a callback to be invoked when an item in this
	 * CarouselAdapter has been clicked.
	 */
	// Close by jiu at 20130325
	public interface OnItemClickListener {

		/**
		 * Callback method to be invoked when an item in this CarouselAdapter has
		 * been clicked.
		 * <p>
		 * Implementers can call getItemAtPosition(position) if they need to access the data associated with the
		 * selected item.
		 * 
		 * @param parent The CarouselAdapter where the click happened.
		 * @param view The view within the CarouselAdapter that was clicked (this
		 *            will be a view provided by the adapter)
		 * @param position The position of the view in the adapter.
		 * @param id The row id of the item that was clicked.
		 */

		void onItemClick(CLGearView parent, View view, int position, long id);
	}

}
