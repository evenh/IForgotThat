package com.ehpefi.iforgotthat;

import java.io.ByteArrayOutputStream;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

/**
 * Responsible for capturing images for new list elements
 * 
 * @author Per Erik Finstad
 * @author Even Holthe
 * @since 1.0
 */
public class CameraActivity extends Activity {
	private static final String TAG = "CameraActivity";
	protected static final String MEDIA_TYPE_IMAGE = null;
	private Camera mCamera;
	private CameraPreview mPreview;
	private PictureCallback mPicture;

	private ImageButton captureButton;
	private Button flashButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		// Get the UI elements
		captureButton = (ImageButton) findViewById(R.id.button_capture);
		flashButton = (Button) findViewById(R.id.button_flash);

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

				// Call the NewReminderActivity
				createNewReminder(data);
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
				if (Build.MODEL.equals("Nexus S") && mCamera.getParameters().getFlashMode().equals(Parameters.FLASH_MODE_ON)) {
					Log.i(TAG, "Google Nexus S detected, applying flash hack!");
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
						tempParam.setFlashMode(Parameters.FLASH_MODE_ON);
						mCamera.setParameters(tempParam);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					} catch (Exception e) {
						Log.e(TAG, "Exception while setting the parameters", e);
					}

				} else {
					mCamera.takePicture(null, null, mPicture);
				}
			}
		});

	}

	/**
	 * Fires off the new intent to NewReminderActivity
	 * 
	 * @param A compressed image as a byte array
	 * @since 1.0
	 */
	private void createNewReminder(byte[] image) {
		Intent intent = new Intent(this, NewReminderActivity.class);
		// Put all the necessary extras in
		intent.putExtra("image", image);

		// Check for incoming data from the previous activity
		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.getInt("listID") != 0) {
			intent.putExtra("listID", bundle.getInt("listID"));
		} else {
			intent.putExtra("listID", 0);
		}

		startActivity(intent);

		// Transition smoothly
		overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}

	/**
	 * Compresses a raw image from the camera to JPEG with 70% quality
	 * 
	 * @param A raw image in a byte array
	 * @return A byte array containing the compressed image
	 * @since 1.0
	 */
	private byte[] compressImage(byte[] input) {
		Log.d(TAG, "Size of image before compression: " + input.length / 1024 + " kB");

		Bitmap original = BitmapFactory.decodeByteArray(input, 0, input.length);
		ByteArrayOutputStream blob = new ByteArrayOutputStream();

		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		if (Build.MODEL.equals("Nexus S") && display.getRotation() == Surface.ROTATION_0) {
			Log.i(TAG, "Nexus S detected, applying rotation hack!");
			Matrix matrix = new Matrix();
			matrix.postRotate(90);
			Bitmap rotated = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
			rotated.compress(Bitmap.CompressFormat.JPEG, 70, blob);
		} else {
			original.compress(Bitmap.CompressFormat.JPEG, 70, blob);
		}

		byte[] compressed = blob.toByteArray();
		Log.d(TAG, "Size of image after compression: " + compressed.length / 1024 + " kB");

		return compressed;
	}

	/**
	 * Gets an instance of the camera
	 * 
	 * @return An instance of camera if successful, null otherwise
	 * @since 1.0
	 */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
			// find supported image sizes
			try {
				Parameters params = c.getParameters();

				List<Camera.Size> cameraSizes = params.getSupportedPictureSizes();
				// selects the smallest resolution (last item in array)
				Camera.Size selectedPictureSize = cameraSizes.get(cameraSizes.size() - 1);
				Log.i(TAG, "Camera resolution selected: " + selectedPictureSize.width + "x" + selectedPictureSize.height);
				params.setPictureSize(selectedPictureSize.width, selectedPictureSize.height);
				
				List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
				Camera.Size selectedPreviewSize = previewSizes.get(previewSizes.size() - 1);
				Log.i(TAG, "Preview resolution selected: " + selectedPreviewSize.width + "x" + selectedPreviewSize.height);
				params.setPreviewSize(selectedPreviewSize.width, selectedPreviewSize.height);
				
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
	 * @param The view
	 * @since 1.0
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

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, CameraActivity.class);
		startActivityForResult(intent, 0);
		// Transition animation
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}
}
