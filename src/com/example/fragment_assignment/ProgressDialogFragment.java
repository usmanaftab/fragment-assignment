package com.example.fragment_assignment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class ProgressDialogFragment extends DialogFragment {
	public static final String TAG = "ProgressDialogFragment";
	private static final String PARENT_TAG_NAME = "parentTag";

	private ProgressDialog dialog;
	private String parentTag;
	
	public void setParentTag(String parentTag) {
		this.parentTag = parentTag;
	}

	@Override
	public ProgressDialog onCreateDialog(Bundle savedInstanceState) {
		dialog = new ProgressDialog(getActivity());
		dialog.setMessage(getResources().getString(R.string.downloading));
		dialog.setCancelable(false); // Disables back button
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setProgress(0);
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cancel),
				getCancelCallback(savedInstanceState));
		Log.v(TAG, "Adding cancel listener");
		return dialog;
	}

	public void setProgress(int p) {
		if (dialog != null)
			dialog.setProgress(p);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(PARENT_TAG_NAME, parentTag);
		super.onSaveInstanceState(outState);
	}
	
	private OnClickListener getCancelCallback(Bundle savedInstanceState) {
		if(savedInstanceState != null) {
			parentTag = savedInstanceState.getString(PARENT_TAG_NAME);
		}
		return ((ICancelDownloadHandler) getFragmentManager().findFragmentByTag(parentTag))
				.getCancelDownloadCallback();
	}
}