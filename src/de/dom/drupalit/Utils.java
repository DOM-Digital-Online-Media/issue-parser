package de.dom.drupalit;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

public class Utils {

	private static ProgressDialog loadingDialog;

	public static Bitmap rotateImageWithEXIFInformation(Context context,
			Uri uri, Bitmap bitmap) {
		float rotation = 0;
		try {
			ExifInterface exif = new ExifInterface(uri.getPath());
			int rot = (int) exifOrientationToDegrees(exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL));
			rotation = rot;
		} catch (IOException e) {
			//
		}
		Matrix matrix = new Matrix();
		if (rotation != 0f) {
			matrix.preRotate(rotation);
		}

		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);

		return resizedBitmap;
	}

	private static float exifOrientationToDegrees(int exifOrientation) {
		if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
			return 90;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
			return 180;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
			return 270;
		}
		return 0;
	}

	public static boolean removeLoadingDialog() {

		if (loadingDialog != null && loadingDialog.isShowing()) {
			loadingDialog.dismiss();
			return true;
		}
		return false;
	}

	public static void showLoadingDialog(Context context,
			OnCancelListener cancelListener) {
		try {

			removeLoadingDialog();
			loadingDialog = ProgressDialog.show(context, "",
					context.getString(R.string.please_wait), true);
			loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			if (cancelListener != null) {
				loadingDialog.setCancelable(true);
				loadingDialog.setOnCancelListener(cancelListener);
			} else {
				loadingDialog.setCancelable(false);
			}
		} catch (Exception e) {
			//
		}

	}

	public static void showConnectionErrorDialog(Context context) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.error_connection)
				.setNeutralButton("OK", null);
		final AlertDialog alert = builder.create();
		alert.show();
	}

}
