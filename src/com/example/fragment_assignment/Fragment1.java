package com.example.fragment_assignment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class Fragment1 extends Fragment implements LoaderCallbacks<Void>, ICancelDownloadHandler {

	public static final String TAG = "Fragment1";

	private Button startBtn;
	private boolean firstTime = true;
	private Handler progressHandler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.images_list, container, false);
		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// this is really important in order to save the state across screen
		// configuration changes for example
		setRetainInstance(true);

		startBtn = (Button) getView().findViewById(R.id.startButton);
		startBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// first time
				if (firstTime) {
					firstTime = false;
					startImageDownloading();
				}
				// already started once
				else {
					restartImageDownloading();
				}
			}
		});
	}

	@Override
	public Loader<Void> onCreateLoader(int arg0, Bundle arg1) {
		ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(
				Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			try {
				AsyncDownloader loader = new AsyncDownloader(getActivity());
				loader.setResultReceiver(new DownloadReceiver(progressHandler));
				// HACK: somehow the AsyncTaskLoader doesn't want to start its
				// job without calling this method
				loader.forceLoad();
				return loader;
			} catch (Exception ex) {
				ex.printStackTrace();
				Toast.makeText(getActivity(), getResources().getString(R.string.exception_message),
						Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(getActivity(), getResources().getString(R.string.no_network_message),
					Toast.LENGTH_SHORT).show();
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Void> arg0, Void arg1) {
		Log.v(TAG, "Loader Finished");
	}

	@Override
	public void onLoaderReset(Loader<Void> arg0) {
		Log.v(TAG, "Reseting Loader");
	}

	public void startImageDownloading() {
		showProgressDialog();
		Log.d(TAG, "startImageDownloading(): starting loader");
		getLoaderManager().initLoader(0, null, this);
	}

	public void restartImageDownloading() {
		showProgressDialog();
		Log.d(TAG, "restartImageDownloading(): re-starting loader");
		getLoaderManager().restartLoader(0, null, this);
	}

	private void showProgressDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag(Constants.PROGRESS_FRAGMENT_NAME);
		ProgressDialogFragment newFragment = null;
		if (prev != null) {
			newFragment = (ProgressDialogFragment) prev;
		} else {
			newFragment = new ProgressDialogFragment();
			newFragment.setParentTag(Constants.FRAGMENT1_NAME);
		}
		newFragment.show(ft, Constants.PROGRESS_FRAGMENT_NAME);
	}

	private void hideProgressDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag(Constants.PROGRESS_FRAGMENT_NAME);
		if (prev != null) {
			ft.remove(prev).commitAllowingStateLoss();
		}
	}

	public class DownloadReceiver extends ResultReceiver {
		public DownloadReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			if (resultCode == AsyncDownloader.UPDATE_PROGRESS) {
				int progress = resultData.getInt("progress");
				ProgressDialogFragment mProgressDialog = (ProgressDialogFragment) getFragmentManager()
						.findFragmentByTag(Constants.PROGRESS_FRAGMENT_NAME);

				if (mProgressDialog != null) {
					mProgressDialog.setProgress(progress);
				}

				if (progress == 100) {
					hideProgressDialog();
					runMediaScanner();
				}
			}
		}
	}

	private void runMediaScanner() {
		getActivity().sendBroadcast(
				new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
						+ Environment.getExternalStorageDirectory())));
	}

	@Override
	public OnClickListener getCancelDownloadCallback() {
		return new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				AsyncDownloader.getInstance().cancelLoading();
				runMediaScanner();
			}
		};
	}
}
