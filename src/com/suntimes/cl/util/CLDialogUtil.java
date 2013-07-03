package com.suntimes.cl.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Sean Zheng
 * @version 1.0
 * @CreateDate 2013-5-15
 */
public class CLDialogUtil {
	
	private static final String TAG = "CLDialogUtil";

	private static CLDialogUtil sGTWidgets;
	private static int sOKRes;
	private static int sConfirmRes;
	private static int sCancelRes;
	private static int sLoadingRes;
	
	private Context mContext;
	private Dialog mSingleAlertDialog;
	private Dialog mConfirmDialog;
	private ProgressDialog mProgressDialog;
	
	private int mCustomDialogMessageViewId;
	private int mCustomDialogOKButtonId;
	private int mCustomDialogPositiveButtonId;
	private int mCustomDialogNegativeButtonId;
	private int mCustomDialogStyle;
	private boolean mCancelable;
	private boolean mCanceledOnTouchOutside;
	
	private final boolean isCustomDialog;
	
	public static void init(Context context, int okRes, int confirmRes, int cancelRes, int loadingRes) {
		init(context, okRes, confirmRes, cancelRes, loadingRes, null);
	}
	
	public static void init(Context context, int okRes, int confirmRes, int cancelRes, int loadingRes, Class<? extends View> customViewClass) {
		if(sGTWidgets == null) {
			sOKRes = okRes;
			sConfirmRes = confirmRes;
			sCancelRes = cancelRes;
			sLoadingRes = loadingRes;
			sGTWidgets = new CLDialogUtil(context, customViewClass);
		}
	}
	
	public static void destroy() {
		if(sGTWidgets != null) {
			sGTWidgets.hideAllDialog();
			sGTWidgets.mContext = null;
			sGTWidgets = null;
		}
	}
	
	public static CLDialogUtil getInstance() {
		return sGTWidgets;
	}
	
	private CLDialogUtil(Context context, Class<? extends View> customViewClass) {
		mContext = context;
		isCustomDialog = customViewClass != null;
		if(customViewClass != null) {
			Class<?>[] interfaces = customViewClass.getInterfaces();
			CustomAlertDialogHandler handler = null;
			boolean hasImplementHandler = false;
			if(interfaces != null) {
				for(Class<?> aInterface : interfaces) {
					if(aInterface == CustomAlertDialogHandler.class) {
						hasImplementHandler = true;
						break;
					}
				}
				if(hasImplementHandler) {
					try {
						View alertView = customViewClass.getDeclaredConstructor(Context.class).newInstance(context);
						View confirmView = customViewClass.getDeclaredConstructor(Context.class).newInstance(context);
						handler = (CustomAlertDialogHandler) alertView;
						mCustomDialogMessageViewId = handler.getMessageViewId();
						mCustomDialogOKButtonId = handler.getOKButtonId();
						mCustomDialogPositiveButtonId = handler.getPositiveButtonId();
						mCustomDialogNegativeButtonId = handler.getNegativeButtonId();
						mCustomDialogStyle = handler.getDialogStyle();
						mCancelable = handler.cancelable();
						mCanceledOnTouchOutside = handler.canceledOnTouchOutside();
						mSingleAlertDialog = constructCustomDialog(alertView, 0);
						mConfirmDialog = constructCustomDialog(confirmView, 1);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		else {
			mSingleAlertDialog = getAlertDialog(context, 0, 0, 0, null);
			mConfirmDialog = getConfirmDialog(context, 0, 0, 0, null);
		}
		
		mProgressDialog = new ProgressDialog(context);
	}
	
	private Dialog constructCustomDialog(View view, int type) {
		Dialog dialog = new Dialog(mContext, mCustomDialogStyle);
		dialog.setCancelable(mCancelable);
		dialog.setCanceledOnTouchOutside(mCanceledOnTouchOutside);
		dialog.setContentView(view);
		dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		if(mCustomDialogOKButtonId > 0) {
			dialog.findViewById(mCustomDialogOKButtonId).setVisibility(type == 0 ? View.VISIBLE : View.GONE);
		}
		if(mCustomDialogPositiveButtonId > 0) {
			dialog.findViewById(mCustomDialogPositiveButtonId).setVisibility(type == 1 ? View.VISIBLE : View.GONE);
		}
		if(mCustomDialogNegativeButtonId > 0) {
			dialog.findViewById(mCustomDialogNegativeButtonId).setVisibility(type == 1 ? View.VISIBLE : View.GONE);
		}
		return dialog;
	}
	
	public void showSingleAlertDialog(int msgRes) {
		showSingleAlertDialog(mContext.getString(msgRes));
	}
	
	public void showSingleAlertDialog(String msg) {
		showSingleAlertDialog(msg, mContext.getString(sOKRes), null);
	}
	
	public void showSingleAlertDialog(int msgRes, int okButtonRes) {
		showSingleAlertDialog(mContext.getString(msgRes), mContext.getString(sOKRes));
	}
	
	public void showSingleAlertDialog(String msg, String okButtonText) {
		showSingleAlertDialog(msg, okButtonText, null);
	}
	
	public void showSingleAlertDialog(String msg, String okButtonText, OnClickListener listener) {
		hideAllDialog();
		if(mSingleAlertDialog != null) {
			if(mSingleAlertDialog instanceof AlertDialog) {
				mSingleAlertDialog = getAlertDialog(mContext, null, msg, okButtonText, listener);
			}
			else {
				TextView messageView = (TextView) mSingleAlertDialog.findViewById(mCustomDialogMessageViewId);
				Button okButton = (Button) mSingleAlertDialog.findViewById(mCustomDialogOKButtonId);
				messageView.setText(msg);
				okButton.setText(okButtonText);
				setupCustomButtonListener(mSingleAlertDialog, mCustomDialogOKButtonId, listener);
			}
			mSingleAlertDialog.show(); 
		}
	}
	
	public void showConfirmDialog(int msgRes) {
		showConfirmDialog(mContext.getString(msgRes));
	}
	
	public void showConfirmDialog(String msg) {
		showConfirmDialog(msg, null);
	}
	
	public void showConfirmDialog(int msgRes, OnClickListener positiveListener) {
		showConfirmDialog(mContext.getString(msgRes), positiveListener);
	}
	
	public void showConfirmDialog(String msg, OnClickListener positiveListener) {
		showConfirmDialog(msg, mContext.getString(sConfirmRes), positiveListener, mContext.getString(sCancelRes), null);
	}
	
	public void showConfirmDialog(int msgRes, int positiveRes, OnClickListener positiveListener, int negativeRes) {
		showConfirmDialog(mContext.getString(msgRes), mContext.getString(positiveRes), positiveListener, mContext.getString(negativeRes));
	}
	
	public void showConfirmDialog(String msg, String positiveButtonText, OnClickListener positiveListener, String negativeButtonText) {
		showConfirmDialog(msg, positiveButtonText, positiveListener, negativeButtonText, positiveListener);
	}
	
	public void showConfirmDialog(String msg, String positiveButtonText, OnClickListener positiveListener, 
			String negativeButtonText, OnClickListener negativeListener) {
		hideAllDialog();
		if(mConfirmDialog != null) {
			if(mConfirmDialog instanceof AlertDialog) {
				mConfirmDialog = getConfirmDialog(mContext, null, msg, positiveButtonText, positiveListener, negativeButtonText, negativeListener);
			}
			else {
				TextView messageView = (TextView) mConfirmDialog.findViewById(mCustomDialogMessageViewId);
				Button positiveButton = (Button) mConfirmDialog.findViewById(mCustomDialogPositiveButtonId);
				Button negativeButton = (Button) mConfirmDialog.findViewById(mCustomDialogNegativeButtonId);
				messageView.setText(msg);
				positiveButton.setText(positiveButtonText);
				negativeButton.setText(negativeButtonText);
				setupCustomButtonListener(mConfirmDialog, mCustomDialogPositiveButtonId, positiveListener);
				setupCustomButtonListener(mConfirmDialog, mCustomDialogNegativeButtonId, negativeListener);
			}
			mConfirmDialog.show();
		}
	}
	
	public void showProgressDialog() {
		showProgressDialog(sLoadingRes);
	}
	
	public void showProgressDialog(OnKeyListener listener) {
		showProgressDialog(sLoadingRes,false,listener);
	}
	
	public void showProgressDialog(int msgRes) {
		showProgressDialog(msgRes, false,null);
	}
	
	public void showProgressDialog(int msgRes, boolean cancelable,OnKeyListener listener) {
		hideAllDialog();
		if(mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(mContext);
		}
		mProgressDialog.setMessage(mContext.getString(msgRes));
		mProgressDialog.setCancelable(cancelable);
		mProgressDialog.setOnKeyListener(listener);
		mProgressDialog.show();
	}
	
	public void hideProgressDialog() {
		if(mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}
	
	private void setupCustomButtonListener(final Dialog dialog, final int buttonId, final OnClickListener listener) {
		if(dialog != null && buttonId > 0) {
			View button = dialog.findViewById(buttonId);
			button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(listener != null) {
						int which = buttonId == mCustomDialogNegativeButtonId ? DialogInterface.BUTTON_NEGATIVE : DialogInterface.BUTTON_POSITIVE;
						listener.onClick(dialog, which);
					}
					dialog.dismiss();
				}
			});
		}
	}
	
	private void hideAllDialog() {
		if(mSingleAlertDialog != null && !isCustomDialog) {
			mSingleAlertDialog.dismiss();
		}
		if(mConfirmDialog != null && !isCustomDialog) {
			mConfirmDialog.dismiss();
		}
		if(mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}
	
//Static Methods	
//--------------------------------------------------------	
	
	public static Dialog getAlertDialog(Context context, int title, int message, int okText, OnClickListener onClickOK) {
		return getAlertDialog(context, title > 0 ? context.getString(title) : null, message > 0 ? context.getString(message) : null, okText > 0 ? context.getString(okText) : null, onClickOK);
	}
	
	public static Dialog getAlertDialog(Context context, String title, String message, String okText, DialogInterface.OnClickListener onClickOK) {
		return getConfirmDialog(context, title, message, okText, onClickOK, null, null);
	}
	
	public static Dialog getConfirmDialog(Context context, int title, int message, 
			int confirmRes, OnClickListener onClickConfirm) {
		return getConfirmDialog(context, title, message, confirmRes, onClickConfirm, sCancelRes, null);
	}
	
	public static Dialog getConfirmDialog(Context context, int title, int message, 
			int confirmRes, OnClickListener onClickConfirm, int cancelRes, OnClickListener onClickCancel) {
		String titleText = title > 0 ? context.getString(title) : null;
		String messageText = message > 0 ? context.getString(message) : null;
		String confirmText = confirmRes > 0 ? context.getString(confirmRes) : null;
		String cancelText = cancelRes > 0 ? context.getString(cancelRes) : null;
		return getConfirmDialog(context, titleText, messageText, confirmText, onClickConfirm, cancelText, onClickCancel);
	}
	
	public static Dialog getConfirmDialog(Context context, String title, String message, 
			String confirmText, OnClickListener onClickConfirm, String cancelText, OnClickListener onClickCancel) {
		AlertDialog dialog = new AlertDialog.Builder(context)
									.setTitle(title)
									.setMessage(message)
									.create();
//		ItemTextView messageItemTextView = new ItemTextView(context);
//		int fontSize = (int) (Constants.isPad ? 23 : 13 * context.getResources().getDisplayMetrics().density);
//		messageItemTextView.setText(message);
//		messageItemTextView.setPadding(20, 20, 20, 20);
//		messageItemTextView.setTextSize(fontSize);
//		messageItemTextView.setGravity(Gravity.CENTER);
//		dialog.setView(messageItemTextView);
		
		if(confirmText != null) {
			dialog.setButton(DialogInterface.BUTTON_POSITIVE, confirmText, onClickConfirm);
		}
		if(cancelText != null) {
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancelText, onClickCancel);
		}
		dialog.setCancelable(false);
		return dialog;
	}
	
	public static ProgressDialog getProgressDialog(Context context, int message) {
		return getProgressDialog(context, context.getString(message));
	}
	
	public static ProgressDialog getProgressDialog(Context context, String message) {
		return getProgressDialog(context, message, false , true);
	}
	
	public static ProgressDialog getProgressDialog(Context context, String message, boolean CanceledOnTouchOutside, boolean cancelAble) {
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(message);
		progressDialog.setCanceledOnTouchOutside(CanceledOnTouchOutside);
		progressDialog.setCancelable(cancelAble);
		return progressDialog;
	}
	
	public static void changeViewState(View view, MotionEvent event) {
		if(view != null && event != null) {
			int action = event.getAction();
			if(action == MotionEvent.ACTION_DOWN) {
				view.setPressed(true);
			}
			else if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
				view.setPressed(false);
			}
		}
	}
	
	public static interface CustomAlertDialogHandler {
		int getMessageViewId();
		int getOKButtonId();
		int getPositiveButtonId();
		int getNegativeButtonId();
		int getDialogStyle();
		boolean cancelable();
		boolean canceledOnTouchOutside();
	}
}
