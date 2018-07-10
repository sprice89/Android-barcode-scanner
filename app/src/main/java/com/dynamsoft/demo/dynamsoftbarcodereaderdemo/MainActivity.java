package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dynamsoft.barcode.jni.BarcodeReader;
import com.dynamsoft.barcode.jni.BarcodeReaderException;
import com.dynamsoft.barcode.jni.TextResult;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.bean.RectPoint;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.util.FrameUtil;
import com.dynamsoft.demo.dynamsoftbarcodereaderdemo.weight.HUDCanvasView;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.Frame;
import com.otaliastudios.cameraview.FrameProcessor;
import com.otaliastudios.cameraview.Size;
import com.pierfrancescosoffritti.slidingdrawer.SlidingDrawer;

import org.json.JSONArray;
import org.json.JSONObject;


import java.io.ByteArrayOutputStream;

import java.io.File;
import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
	private static final int PRC_PHOTO_PICKER = 1;
	private static final int RC_CHOOSE_PHOTO = 1;
	private final int DETECT_BARCODE = 0x0001;
	private final int OBTAIN_PREVIEW_SIZE = 0x0002;
	private final int BARCODE_RECT_COORD = 0x0003;

	@BindView(R.id.cameraView)
	CameraView cameraView;
	@BindView(R.id.tv_flash)
	TextView mFlash;
	@BindView(R.id.scanCountText)
	TextView mScanCount;
	@BindView(R.id.hud_view)
	HUDCanvasView hudView;
	@BindView(R.id.toolbar)
	Toolbar toolbar;
	@BindView(R.id.non_slidable_view)
	LinearLayout nonSlidableView;
	@BindView(R.id.drag_view)
	TextView dragView;
	@BindView(R.id.slidable_view)
	FrameLayout slidableView;
	@BindView(R.id.sliding_drawer)
	SlidingDrawer slidingDrawer;
	@BindView(R.id.rl_barcode_list)
	RecyclerView rlBarcodeList;
	private BarcodeReader reader;
	private TextResult[] result;
	private boolean isDetected = true;
	private DBRCache mCache;
	private String name = "";
	private boolean isFlashOn = false;
	private boolean isCameraStarted = false;
	private ArrayList<String> allResultText = new ArrayList<String>();
	private float previewScale;
	private Size previewSize = null;
	private FrameUtil frameUtil;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case DETECT_BARCODE:
					TextResult[] result = (TextResult[]) msg.obj;
					dragView.setText(result[0].barcodeText);
	/*					String barcodeFormat = "";
						switch (result[0].barcodeFormat) {
							case 234882047:
								barcodeFormat = "all";
								break;
							case 1023:
								barcodeFormat = "OneD";
								break;
							case 1:
								barcodeFormat = "CODE_39";
								break;
							case 2:
								barcodeFormat = "CODE_128";
								break;
							case 4:
								barcodeFormat = "CODE_93";
								break;
							case 8:
								barcodeFormat = "CODABAR";
								break;
							case 16:
								barcodeFormat = "ITF";
								break;
							case 32:
								barcodeFormat = "EAN_13";
								break;
							case 64:
								barcodeFormat = "EAN_8";
								break;
							case 128:
								barcodeFormat = "UPC_A";
								break;
							case 256:
								barcodeFormat = "UPC_E";
								break;
							case 512:
								barcodeFormat = "INDUSTRIAL_25";
								break;
							case 33554432:
								barcodeFormat = "PDF417";
								break;
							case 67108864:
								barcodeFormat = "QR_CODE";
								break;
							case 134217728:
								barcodeFormat = "DATAMATAIX";
								break;
							default:
								break;
						}*/
					for (TextResult aResult : result) {
						if (!allResultText.contains(aResult.barcodeText)) {
							allResultText.add(aResult.barcodeText);
							int count = allResultText.size();
							mScanCount.setText(count + " Scanned");
						}
					}

					break;
				case BARCODE_RECT_COORD:
					drawDocumentBox((ArrayList<RectPoint[]>) msg.obj);
					break;
				case OBTAIN_PREVIEW_SIZE:
					obtainPreviewScale();
					break;
				default:
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ButterKnife.bind(this);
		slidingDrawer.setDragView(dragView);
		Logger.addLogAdapter(new AndroidLogAdapter());
		try {
			reader = new BarcodeReader("t0068MgAAAA70elzyXYmS7moRx7im7XPCr58/2f7IyvaQfe2y0go" +
					"R2REXg7tfQ8Mv48LhyuiCPwaCnuPb7CKFYrg9B/Yc30k=");
			JSONObject jsonObject = new JSONObject("{\n" +
					"  \"ImageParameters\": {\n" +
					"    \"Name\": \"Custom_100947_777\",\n" +
					"    \"BarcodeFormatIds\": [\n" +
					"      \"QR_CODE\"\n" +
					"    ],\n" +
					"    \"LocalizationAlgorithmPriority\": [\"ConnectedBlock\", \"Lines\", \"Statistics\", \"FullImageAsBarcodeZone\"],\n" +
					"    \"AntiDamageLevel\": 3,\n" +
					"    \"ScaleDownThreshold\": 1000\n" +
					"  }\n" +
					"}");
			reader.appendParameterTemplate(jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setSupportActionBar(toolbar);
		frameUtil = new FrameUtil();
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setMessage(R.string.about);
				builder.setPositiveButton("Overview", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = Uri.parse("https://www.dynamsoft.com/Products/barcode-scanner-sdk-android.aspx");
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
					}
				});
				builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.show();
			}
		});
		mCache = DBRCache.get(this);
		mCache.put("linear", "1");
		mCache.put("qrcode", "1");
		mCache.put("pdf417", "1");
		mCache.put("matrix", "1");

		cameraView.addCameraListener(new CameraListener() {
			@Override
			public void onCameraOpened(CameraOptions options) {
				super.onCameraOpened(options);
				isCameraStarted = true;
			}
		});
		cameraView.addFrameProcessor(new CodeFrameProcesser());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
	/*		Intent intent = new Intent(MainActivity.this, SettingActivity.class);
			intent.putExtra("type", barcodeType);
			startActivityForResult(intent, 0);*/
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			reader = new BarcodeReader("t0068MgAAAJmtGjsv3J5mDE0ECeH0+ZFEr7BJl7gcdJZFYzqa2sZK" +
					"hpQcsNcQlPZooMc5wDrCWMKnQ72T/+01qsEpM3nwIjc=");
			JSONObject object = new JSONObject("{\n" +
					"  \"ImageParameters\": {\n" +
					"    \"Name\": \"linear\",\n" +
					"    \"BarcodeFormatIds\": [],\n" +
					"    \"DeblurLevel\": 9,\n" +
					"    \"AntiDamageLevel\": 9,\n" +
					"    \"TextFilterMode\": \"Enable\"\n" +
					"  }\n" +
					"}");
			JSONArray jsonArray = object.getJSONObject("ImageParameters").getJSONArray("BarcodeFormatIds");
			if (mCache.getAsString("linear").equals("1")) {
				jsonArray.put("OneD");
			}
			if (mCache.getAsString("qrcode").equals("1")) {
				jsonArray.put("QR_CODE");
			}
			if (mCache.getAsString("pdf417").equals("1")) {
				jsonArray.put("PDF417");
			}
			if (mCache.getAsString("matrix").equals("1")) {
				jsonArray.put("DATAMATRIX");
			}
			Log.d("code type", "type : " + object.toString());
			reader.appendParameterTemplate(object.toString());
			name = "linear";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		cameraView.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		cameraView.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cameraView.destroy();
	}

	@OnClick(R.id.tv_flash)
	public void onFlashClick() {
		if (isFlashOn) {
			isFlashOn = false;
			cameraView.setFlash(Flash.OFF);
		} else {
			isFlashOn = true;
			cameraView.setFlash(Flash.TORCH);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	@Override
	public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

	}

	@Override
	public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

	}

	private void obtainPreviewScale() {
		if (hudView.getWidth() == 0 || hudView.getHeight() == 0) {
			return;
		}
		previewSize = cameraView.getPreviewSize();
		previewScale = frameUtil.calculatePreviewScale(previewSize, hudView.getWidth(), hudView.getHeight());
	}

	private void drawDocumentBox(ArrayList<RectPoint[]> rectCoord) {
		hudView.clear();
		hudView.setBoundaryPoints(rectCoord);
		hudView.invalidate();
		isDetected = true;
	}

	class CodeFrameProcesser implements FrameProcessor {
		@Override
		public void process(@NonNull Frame frame) {
			try {
				if (isDetected && isCameraStarted) {
					isDetected = false;
					if (previewSize == null) {
						Message obtainPreviewMsg = handler.obtainMessage();
						obtainPreviewMsg.what = OBTAIN_PREVIEW_SIZE;
						handler.sendMessage(obtainPreviewMsg);
					}
					long beginHandle = System.currentTimeMillis();
					YuvImage yuvImage = new YuvImage(frame.getData(), frame.getFormat(),
							frame.getSize().getWidth(), frame.getSize().getHeight(), null);
					int wid = frame.getSize().getWidth();
					int hgt = frame.getSize().getHeight();
					long endFormatYuv = System.currentTimeMillis();
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					yuvImage.compressToJpeg(new Rect(0, 0, wid, hgt), 100, os);
					byte[] jpegByteArray = os.toByteArray();
					Bitmap srcBitmap = FrameUtil.rotateBitmap(BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.length));
					long endCon2BmpRot = System.currentTimeMillis();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
					byte[] bmpByte = baos.toByteArray();
					long endHandle = System.currentTimeMillis();
					Logger.d("time : format yuv: " + (endFormatYuv - beginHandle) + " convert to bmp&rotate :" +
							(endCon2BmpRot - endFormatYuv) + " compress byte[] : " + (endHandle - endCon2BmpRot));
					//result = reader.decodeBuffer(yuvImage.getYuvData(), wid, hgt, yuvImage.getStrides()[0], EnumImagePixelFormat.IPF_ARGB_8888, name);
					result = reader.decodeFileInMemory(bmpByte, "Custom_100947_777");
					long endDetect = System.currentTimeMillis();
					Logger.d("detect code time : " + (endDetect - endHandle));
					Logger.d("barcode result" + Arrays.toString(result) + " src width : " + wid + "src height : " + hgt);
					if (result != null && result.length > 0) {
						ArrayList<RectPoint[]> rectCoord = frameUtil.handlePoints(result, previewScale, srcBitmap.getHeight(), srcBitmap.getWidth());
						Message message = handler.obtainMessage();
						message.obj = result;
						message.what = DETECT_BARCODE;
						handler.sendMessage(message);

						Message coordMessage = handler.obtainMessage();
						coordMessage.obj = rectCoord;
						coordMessage.what = BARCODE_RECT_COORD;
						handler.sendMessage(coordMessage);
					} else {
						isDetected = true;
					}
				}
			} catch (BarcodeReaderException e) {
				e.printStackTrace();
			}
		}
	}
}

