package com.ehpefi.iforgotthat;
import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/**
 * Handles the camera preview
 * 
 * @author Per Erik Finstad
 * @since 1.0
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "CameraPreview";

	private SurfaceHolder mHolder;
	private Camera mCamera;
	private boolean isPreviewActive = false;

	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;

		// Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the preview.
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
			isPreviewActive = true;
		} catch (IOException e) {
			Log.e(TAG, "Error setting camera preview!", e);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Release the camera on orientation change
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.

		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		if (isPreviewActive) {
			try {
				mCamera.stopPreview();
			} catch (Exception e) {
				Log.e(TAG, "Couldn't stop camera preview", e);
			}
		}

		// From http://stackoverflow.com/questions/3841122/android-camera-preview-is-sideways
		Parameters parameters = mCamera.getParameters();
		Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		if (display.getRotation() == Surface.ROTATION_0) {
			parameters.setPreviewSize(h, w);
			mCamera.setDisplayOrientation(90);
		}

		if (display.getRotation() == Surface.ROTATION_90) {
			parameters.setPreviewSize(w, h);
		}

		if (display.getRotation() == Surface.ROTATION_180) {
			parameters.setPreviewSize(h, w);
		}

		if (display.getRotation() == Surface.ROTATION_270) {
			parameters.setPreviewSize(w, h);
			mCamera.setDisplayOrientation(180);
		}

		// start preview with new settings
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
		} catch (Exception e) {
			Log.e(TAG, "Error starting camera preview", e);
		}
	}
}