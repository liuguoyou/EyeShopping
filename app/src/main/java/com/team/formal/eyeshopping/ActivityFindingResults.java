/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.team.formal.eyeshopping;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ActivityFindingResults extends Activity {

    private static final String TAG = ActivityFindingResults.class.getSimpleName();
    private ViewGroup mRelativeLayout;
    private Bitmap mNaverPrImg;
    private Mat userSelImg = null;

    static {
        System.loadLibrary("native-lib");
    }

    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);

    public native void CornerHarrisDemo(long addrInputImage, long addrOutput);

    public native int AkazeFeatureMatching(long userSelImage, long naverPrImage);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finding_results);

        mRelativeLayout = (ViewGroup) findViewById(R.id.contentMain);

        try {
            // TODO 이것도 naverPrImgTarget 처럼.. url로 처리
            userSelImg = Utils.loadResource(this, R.drawable.user_image, CvType.CV_8UC4); // return BGR 순
            // naverPrImg = Utils.loadResource(this, R.drawable.marmont_bag, CvType.CV_8UC4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // SET DUMMY DATA
        final Shop dummyShop = new Shop();
        dummyShop.setImage("http://shopping.phinf.naver.net/main_1144437/11444373299.jpg?type=f140");
        dummyShop.setTitle("Marmont Handbag");
        dummyShop.setLprice(2340958);

        // Thread로 웹서버에 접속
        new Thread() {
            public void run() {
                mNaverPrImg = getBitmapFromURL(dummyShop.getImage()); // 입력 이미지 Url

                Bundle bun = new Bundle();
                bun.putSerializable("productInfo", dummyShop);
                Message msg = detectHandler.obtainMessage();
                msg.setData(bun);
                detectHandler.sendMessage(msg);
            }
        }.start();
    }

    Handler detectHandler = new Handler() {
        public void handleMessage(Message msg) {

            Bundle bun = msg.getData();
            Shop dummyShop = (Shop) bun.getSerializable("productInfo");

            Mat userSelImgTarget = new Mat(userSelImg.width(), userSelImg.height(), CvType.CV_8UC4);
            Mat naverPrImgTarget = new Mat(mNaverPrImg.getWidth(), mNaverPrImg.getHeight(), CvType.CV_8UC4);

            Utils.bitmapToMat(mNaverPrImg, naverPrImgTarget);

            Imgproc.cvtColor(userSelImg, userSelImgTarget, Imgproc.COLOR_BGR2RGB);

            Imgproc.cvtColor(naverPrImgTarget, naverPrImgTarget, Imgproc.COLOR_RGBA2RGB);

            int ret = AkazeFeatureMatching(userSelImgTarget.getNativeObjAddr(),
                    naverPrImgTarget.getNativeObjAddr());

            if(ret == 1) { // find one!

                for(int i=0; i<3; i++) {
                    View productLayout = LayoutInflater.from(getBaseContext()).inflate(R.layout.product, mRelativeLayout, false);

                    TextView productName = (TextView) productLayout.findViewById(R.id.productName);
                    productName.setText(dummyShop.getTitle());

                    ImageView productThumb = (ImageView) productLayout.findViewById(R.id.Thumbnail);
                    productThumb.setImageBitmap(mNaverPrImg);

                    TextView productPrice = (TextView) productLayout.findViewById(R.id.price);
                    productPrice.setText(String.valueOf(dummyShop.getLprice()));

                    mRelativeLayout.addView(productLayout);
                }

            } else {
                // goto next thumbnail img or next comb. keyword
            }

            // Bitmap bmp = Bitmap.createBitmap(addrOutput.cols(), addrOutput.rows(), Bitmap.Config.ARGB_8888);
            // Utils.matToBitmap(addrOutput, bmp);

            // mMainImage.setImageBitmap(bmp);

            Log.i("complete", "complete");
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResume :: OpenCV library found inside package. Using it!");
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public Bitmap getBitmapFromURL(String src) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(src);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap myBitmap = BitmapFactory.decodeStream(input, null, op);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

}