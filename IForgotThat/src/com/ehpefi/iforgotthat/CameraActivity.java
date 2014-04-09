package com.ehpefi.iforgotthat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Build;
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

				// Hack for the Google Nexus S
				if (Build.MODEL.equals("Nexus S")
						&& mCamera.getParameters().getFlashMode().equals(Parameters.FLASH_MODE_ON)) {
					Log.d(TAG, "Google Nexus S detected, applying flash hack!");
					Camera.Parameters tempParam = mCamera.getParameters();
					tempParam.setFlashMode(Parameters.FLASH_MODE_TORCH);
					mCamera.setParameters(tempParam);
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mCamera.takePicture(null, null, mPicture);

					try {
						Thread.sleep(500);
						tempParam.setFlashMode(Parameters.FLASH_MODE_OFF);
						mCamera.setParameters(tempParam);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					} catch (Exception e) {
						Log.e(TAG, "Exceptionfest", e);
					}

				} else {
					mCamera.takePicture(null, null, mPicture);
				}
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
			// find supported image sizes
			try {
				Parameters params = c.getParameters();
				List<Camera.Size> sizes = params.getSupportedPictureSizes();
				Camera.Size selected = sizes.get(sizes.size() - 1);// selects the smallest resolution (last item in array)
				Log.i(TAG, "Sizes available for this camera:");
				for (Camera.Size size : sizes) {
					Log.i(TAG, size.width + "x" + size.height);
				}
				Log.i(TAG, "Size selected for this application: " + selected.width + "x" + selected.height);
				params.setPictureSize(selected.width, selected.height);
				params.setPreviewSize(selected.width, selected.height);
				c.setParameters(params);
			} catch (Exception e) {
				Log.d(TAG, "Failed to set parameters: " + e);
			}

		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Log.e(TAG, "Camera is not available or doesn't exist" + e);

		}
		return c; // returns null if camera is unavailable
	}

	/**
	 * Method for turning camera flash on/off. Default is off
	 * 
	 * @param none
	 * @return boolean
	 * 
	 */
	public void flashOnOff(View v) {
		Camera.Parameters param = mCamera.getParameters();
		List<String> flashModes = param.getSupportedFlashModes();

		if (flashModes != null) {
			String currentFlashMode = param.getFlashMode();

			if (currentFlashMode.equals(Parameters.FLASH_MODE_OFF)) {
				currentFlashMode = Parameters.FLASH_MODE_ON;
			} else {
				currentFlashMode = Parameters.FLASH_MODE_OFF;
			}

			param.setFlashMode(currentFlashMode);
			mCamera.setParameters(param);
		}
	}

}// end of class
