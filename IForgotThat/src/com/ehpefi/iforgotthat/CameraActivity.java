package com.ehpefi.iforgotthat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.ehpefi.iforgotthat.R.id;


public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivty";
	protected static final String MEDIA_TYPE_IMAGE = null;
	private Camera mCamera;
	private CameraPreview mPreview;
	private PictureCallback mPicture;


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


		mPicture = new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {

				File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
				if (pictureFile == null) {
					Log.d(TAG, "Error creating media file, check storage permissions: ");
					return;
				}

				try {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					System.out.println("Writing file to system!"); // for debugging only
					fos.write(data);
					fos.close();
				} catch (FileNotFoundException e) {
					Log.d(TAG, "File not found: " + e.getMessage());
				} catch (IOException e) {
					Log.d(TAG, "Error accessing file: " + e.getMessage());
				}
			}

			private File getOutputMediaFile(String mediaTypeImage) {
				// TODO Auto-generated method stub
				return null;

			}
		};

		// capture image button listener
		ImageButton captureButton = (ImageButton) findViewById(id.button_capture);
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get an image from the camera
				System.out.println("Photo in the taking!");//for debugging only
				mCamera.takePicture(null, null, mPicture);
			}
		});
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
