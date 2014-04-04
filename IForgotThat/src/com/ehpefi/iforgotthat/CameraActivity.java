package com.ehpefi.iforgotthat;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;


public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivty";
	private Camera mCamera;
	private CameraPreview mPreview;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		
		// Create an instance of Camera
		mCamera = getCameraInstance();

		// Create preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
	}

	// get an instance of the camera
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Log.d(TAG, "Camera is not available or doesn't exist " + e.getMessage());

		}
		return c; // returns null if camera is unavailable
	}
	


}//end of class
