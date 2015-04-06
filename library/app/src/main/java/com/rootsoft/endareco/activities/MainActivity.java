package com.rootsoft.endareco.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.rootsoft.endareco.R;
import com.rootsoft.endareco.utils.AsyncCompareTask;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{

    //Constants
    private static final String TAG = "EndaReco " + MainActivity.class.toString();
    private static final int SELECT_PHOTO = 100;

    //Attributes
    private CameraBridgeViewBase mOpenCvCameraView;
    private Button btnSelectPicture, btnMatch;
    private ImageView ivPicture, ivScene;
    private int imgNo = 0;
    private Bitmap imgToRecognize, imgScene;
    private boolean mShouldCompare;
    private Mat mCurrentFrame;

    //Constructors

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelectPicture = (Button) findViewById(R.id.btnSelectPicture);
        btnMatch = (Button) findViewById(R.id.btnMatch);
        ivPicture = (ImageView) findViewById(R.id.ivPicture);
        ivScene = (ImageView) findViewById(R.id.ivScene);

        btnMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShouldCompare = true;
            }
        });

        btnSelectPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                imgNo = 1;
            }
        });

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mShouldCompare = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this,
                mLoaderCallback);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        //mCurrentFrame = new Mat(height, width, CvType.CV_32FC2);

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        if (mShouldCompare) {
            mCurrentFrame = inputFrame.rgba();
            compare();
            mShouldCompare = false;
        }
        return inputFrame.rgba();
    }

    public void compare() {
        Log.i(TAG, "In Compare");
        try {
            imgScene = Bitmap.createBitmap(mCurrentFrame.cols(), mCurrentFrame.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mCurrentFrame, imgScene);
        }
        catch (CvException e){Log.d("Exception",e.getMessage());}

        // TODO Auto-generated method stub
        if (imgToRecognize != null && imgScene != null) {

            Log.i(TAG, "Scaling bitmaps");
            //imgToRecognize = Bitmap.createScaledBitmap(imgToRecognize, 100, 100, true);
            //imgScene = Bitmap.createScaledBitmap(imgScene, 100, 100, true);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ivPicture.setImageBitmap(imgToRecognize);
                    ivScene.setImageBitmap(imgScene);

                    AsyncCompareTask task = new AsyncCompareTask(MainActivity.this);
                    task.setObjToRecognize(imgToRecognize);
                    task.setScene(imgScene);
                    task.setMinDistance(100);
                    task.setMaxDistance(0);
                    Log.i(TAG, "Comparing");
                    task.execute();

                }
            });

        } else {
            Log.i(TAG, "Unable to compare");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = menuItem.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(
                                selectedImage);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    imgToRecognize = BitmapFactory.decodeStream(imageStream);
                    if (imgNo == 1) {
                        ivPicture.setImageBitmap(imgToRecognize);
                        ivPicture.invalidate();

                    }
                }
        }
    }
}
