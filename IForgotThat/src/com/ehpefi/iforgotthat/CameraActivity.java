package com.ehpefi.iforgotthat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";
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
				// Compress the data
				data = compressImage(data);

				File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
				if (pictureFile == null) {
					Log.e(TAG, "Error creating media file, check storage permissions");
					return;
				}

				try {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					Log.d(TAG, "Writing file to system!");
					fos.write(data);
					fos.close();
				} catch (FileNotFoundException e) {
					Log.w(TAG, "File not found", e);
				} catch (IOException e) {
					Log.e(TAG, "Error accessing file", e);
				}
			}

			private File getOutputMediaFile(String mediaTypeImage) {
				// TODO Auto-generated method stub
				return null;

			}
		};

		// capture image button listener
		ImageButton captureButton = (ImageButton) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get an image from the camera
				Log.d(TAG, "Photo in the taking!");
				mCamera.takePicture(null, null, mPicture);
			}
		});
	}

	/**
	 * Compresses a raw image from the camera to JPEG with 70% quality
	 * 
	 * @param input
	 * @return A byte array containing the compressed image
	 */
	byte[] compressImage(byte[] input) {
		Bitmap original = BitmapFactory.decodeByteArray(input, 0, input.length);

		ByteArrayOutputStream blob = new ByteArrayOutputStream();
		original.compress(Bitmap.CompressFormat.JPEG, 70, blob);

		return blob.toByteArray();
	}

	// get an instance of the camera
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Log.e(TAG, "Camera is not available or doesn't exist", e);

		}
		return c; // returns null if camera is unavailable
	}
	
}//end of class
