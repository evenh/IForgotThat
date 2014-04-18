package com.ehpefi.iforgotthat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Handles the camera
 *
 * @author Per Erik Finstad
 * @since 1.0
 */
public class CamTest extends Activity implements SurfaceHolder.Callback {
	// UI elements
	private TextView title;
	private ImageButton captureButton;
	private ImageButton flashButton;
	private String listTitle;

	// Camera
	private Camera camera;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	boolean isPreviewActive = false;
	private PictureCallback mPicture;

	// Helper class
	ListHelper listHelper = new ListHelper(this);
	private int listID;

	// Used for logging
	private static final String TAG = "CAMERA";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cam_test);

		// getWindow().setFormat(PixelFormat.UNKNOWN);
		surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);

		camera = getCameraInstance();

		// Check for incoming data and set list title
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			// Check for list id
			if (bundle.getInt("listID") > 0) {
				listID = bundle.getInt("listID");
				Log.d(TAG, "List id recieved: " + listID);

				ListObject list = listHelper.getList(listID);
				listTitle = list.getTitle();
				Log.d(TAG, "List title resolved: " + listTitle);

				// Get TextView for the list title and set its name
				title = (TextView) findViewById(R.id.listName);
				title.setText(listTitle);
			}
		}

		// Get the UI elements
		captureButton = (ImageButton) findViewById(R.id.button_capture);
		flashButton = (ImageButton) findViewById(R.id.button_flash);

		/**
		 * Callback function to get the capture image
		 * 
		 * @param byte[] data, Camere camera
		 * @since 1.0
		 * 
		 */
		mPicture = new PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {

				Log.d(TAG, "Picture taken! Compressing image and passing it on to NewReminderActivity");
				// Compress the data
				data = compressImage(data);

				// Call the NewReminderActivity
				createNewReminder(data);
			}
		};
		
		
		
		/**
		 * Method to take the picture
		 * @param View v
		 * @since 1.0
		 */
		// Handle clicks for the captureButton
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get an image from the camera
				Log.d(TAG, "Photo in the taking!");
				
				camera.takePicture(null,null, mPicture);
				Log.d(TAG, "Picture captured");
			}
		});

	}

	/**
	 * Fires off the new intent to NewReminderActivity
	 * 
	 * @param image A compressed image as a byte array
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

		// Flip transition
		overridePendingTransition(R.anim.flip_from_middle, R.anim.flip_to_middle);

	}

	/**
	 * Method for check if the orientation of the surface changes
	 * 
	 * @param SurfaceHolder
	 *              holder, int format, int width, int height
	 * @since 1.0
	 * 
	 */

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// If preview is active, stop it
		if (isPreviewActive) {
			camera.stopPreview();
			isPreviewActive = false;
		}

		// If camera is not null
		if (camera != null) {
			// Try to start the preview
			try {
				camera.setPreviewDisplay(surfaceHolder);
				camera.startPreview();
				isPreviewActive = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// FIX/hack for rotation/stretch issue on NEXUS S
		if (Build.MODEL.equals("Nexus S") && holder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// Stop preview before making changes
		try {
			camera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// make any resize, rotate or reformatting changes here
		if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
			camera.setDisplayOrientation(90);
		} else {
			camera.setDisplayOrientation(0);
		}
		// start preview with new settings
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();

		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}
	}

    /**
     * Gets an instance of the Camera
     *
     * @return An instance of Camera, may be null
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
				// params.setPreviewSize(selectedPreviewSize.width, selectedPreviewSize.height);

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
	 * Compresses a raw image from the camera to JPEG with 70% quality
	 * 
	 * @param input A raw image in a byte array
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
	 * Method for release the camera and stop it from using the surface
	 * 
	 * @param Surfaceholder
	 *              holder
	 * @since 1.0
	 */

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
		camera = null;
		isPreviewActive = false;
	}


	/**
	 * Method for turning camera flash on/off. Default is off
	 * 
	 * @param v The view
	 * @since 1.0
	 */
	public void flashOnOff(View v) {
		Camera.Parameters param = camera.getParameters();
		List<String> flashModes = param.getSupportedFlashModes();

		if (flashModes != null) {
			String currentFlashMode = param.getFlashMode();

			if (currentFlashMode.equals(Parameters.FLASH_MODE_OFF)) {
				currentFlashMode = Parameters.FLASH_MODE_ON;
				flashButton.setImageResource(R.drawable.ic_action_flash_on);
				Log.d(TAG, "Flash on!");

			} else {
				currentFlashMode = Parameters.FLASH_MODE_OFF;
				flashButton.setImageResource(R.drawable.ic_action_flash_off);
				Log.d(TAG, "Flash off!");
			}

			param.setFlashMode(currentFlashMode);
			camera.setParameters(param);
		}
	}

	/**
	 * Goes back to the MainActivity
	 * 
	 * @since 1.0
	 */
	public void backToLists(View v) {
		// Calls the method that is called when the back button is pressed
		onBackPressed();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, ListWithElementsActivity.class);
		intent.putExtra("listID", getIntent().getExtras().getInt("listID"));
		startActivityForResult(intent, 0);
		// Transition animation
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}

	@Override
	public void surfaceCreated(SurfaceHolder sh) {
	}

}
