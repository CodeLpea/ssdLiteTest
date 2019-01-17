package com.example.administrator.trylite;

import android.Manifest;
import android.app.Activity;
import android.graphics.Matrix;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * 仅测试
 */
public class tryActivity extends Activity implements View.OnClickListener {
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final int PERMISSIONS_REQUEST = 1;
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED =true;//没有量化的模型需要false。
    private static final String TF_OD_API_MODEL_FILE = "ssd_v2_quntized_v1_2.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/DNN_labels_list.txt";

    private Classifier detectorClassifier;
    private long lastProcessingTimeMs;
    private Bitmap croppedBitmap = null;
    private Bitmap croppedBitmap2 = null;
    private Bitmap cropCopyBitmap = null;
    private Bitmap cropCopyBitmaptest = null;
    private ImageView imageView2;
    private Button btn_Toast1,btn_Toast2;
    private int xx=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try);
        imageView2=(ImageView)findViewById(R.id.im_try2);
        if (hasPermission()) {
            initToastTest();
            initImage();
            init();

        } else {
            requestPermission();
        }
    }



    private void initToastTest() {
        btn_Toast1=(Button)findViewById(R.id.btn_toast1);
        btn_Toast2=(Button)findViewById(R.id.btn_toast2);
        btn_Toast1.setOnClickListener(this);
        btn_Toast2.setOnClickListener(this);

    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_toast1:
                midToast("ceshi");
                break;
            case  R.id.btn_toast2:
                ssdThread ssdThread=new ssdThread(xx);
                ssdThread.start();
                break;
                default:
                    Log.i(TAG, "onClick: ");
                    break;
        }
    }
    private void midToast(String str)
    {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.view_toast_custom,
                (ViewGroup) findViewById(R.id.timo));
        ImageView img_logo = (ImageView) view.findViewById(R.id.img_logo);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        tv_msg.setText(str);
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }

    private void initImage() {
        croppedBitmap=readBitmapFromFileDescriptor(Environment.getExternalStorageDirectory().getPath()+ File.separator+"20.jpg",300,300);
        //croppedBitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
        //cropCopyBitmaptest = croppedBitmap.copy(Bitmap.Config.ARGB_8888, true);//复制一张用来显示绘制框
        croppedBitmap=chageSize(croppedBitmap,300,300);
        Log.i(TAG, "getWidth: "+croppedBitmap.getWidth());
        Log.i(TAG, "getHeight: "+croppedBitmap.getHeight());

        //croppedBitmap2=readBitmapFromFileDescriptor(Environment.getExternalStorageDirectory().getPath()+ File.separator+"11.jpg",300,300);
    }
    private void init() {
        try {
            detectorClassifier = TFLiteObjectDetectionAPIModel.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE, TF_OD_API_IS_QUANTIZED);
        } catch (IOException e) {
            e.printStackTrace();
        }
            final long startTime = SystemClock.uptimeMillis();
            final List<Classifier.Recognition> results = detectorClassifier.recognizeImage(croppedBitmap);
            Log.i(TAG, "init: ");
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
            Log.i(TAG, "lastProcessingTimeMs: "+lastProcessingTimeMs);
            cropCopyBitmap = croppedBitmap.copy(Bitmap.Config.ARGB_8888, true);//复制一张用来显示绘制框
            final Canvas canvas = new Canvas(cropCopyBitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3.0f);
            paint.setTextSkewX(20);

               Paint textPaint = new Paint();          // 创建画笔
               textPaint.setColor(Color.GREEN);        // 设置颜色
               textPaint.setStyle(Paint.Style.FILL);   // 设置样式
               textPaint.setTextSize(15);
        for (final Classifier.Recognition result : results) {
                if(result.getConfidence()>0.1){
                    final RectF location = result.getLocation();
                    Log.i(TAG, "result.getTitle--------------------:"+result.getTitle().toString());
                    Log.i(TAG, "result.getConfidence--------------------:"+result.getConfidence().toString());
                    canvas.drawRect(location, paint);
                    canvas.drawText("herpes："+result.getConfidence().toString(),0,12,location.left,location.top,textPaint);
                    imageView2.setImageBitmap(cropCopyBitmap);
                }
            }
    }

    private class ssdThread extends Thread{
        private Classifier mdetectorClassifier;
     ssdThread(int x){
         Log.i(TAG, "ssdThread: ");

     }

        @Override
        public void run() {
            super.run();
            String wich=xx+".jpg";
            croppedBitmap=readBitmapFromFileDescriptor(Environment.getExternalStorageDirectory().getPath()+ File.separator+wich,300,300);
            //croppedBitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
            //cropCopyBitmaptest = croppedBitmap.copy(Bitmap.Config.ARGB_8888, true);//复制一张用来显示绘制框
            croppedBitmap=chageSize(croppedBitmap,300,300);
            final long startTime = SystemClock.uptimeMillis();

            try {
                mdetectorClassifier = TFLiteObjectDetectionAPIModel.create(getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE, TF_OD_API_IS_QUANTIZED);
            } catch (IOException e) {
                e.printStackTrace();
            }
            final List<Classifier.Recognition> results = mdetectorClassifier.recognizeImage(croppedBitmap);
            Log.i(TAG, "getWidth: "+croppedBitmap.getWidth());
            Log.i(TAG, "getHeight: "+croppedBitmap.getHeight());
            xx++;
            Log.i(TAG, "init: 照片xx:"+xx);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
            Log.i(TAG, "lastProcessingTimeMs: "+lastProcessingTimeMs);
            cropCopyBitmap = croppedBitmap.copy(Bitmap.Config.ARGB_8888, true);//复制一张用来显示绘制框
            final Canvas canvas = new Canvas(cropCopyBitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3.0f);
            paint.setTextSkewX(20);

            Paint textPaint = new Paint();          // 创建画笔
            textPaint.setColor(Color.GREEN);        // 设置颜色
            textPaint.setStyle(Paint.Style.FILL);   // 设置样式
            textPaint.setTextSize(15);
            for (final Classifier.Recognition result : results) {
                if (result.getConfidence() >= 0.1) {
                    final RectF location = result.getLocation();
                    Log.i(TAG, "result.getTitle--------------------:" + result.getTitle().toString());
                    Log.i(TAG, "result.getConfidence--------------------:" + result.getConfidence().toString());
                    canvas.drawRect(location, paint);
                    try {
                    canvas.drawText("herpes：" + result.getConfidence().toString(), 0, 12, location.left, location.top, textPaint);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            Message message=Message.obtain();
            message.what=1;
            message.obj=cropCopyBitmap;
            myHandler.sendMessage(message);

        }
    }
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    imageView2.setImageBitmap((Bitmap) msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    /**
     * 获取本地图片
     * @param filePath
     * @param width
     * @param height
     * @return
     */
    public static Bitmap readBitmapFromFileDescriptor(String filePath, int width, int height) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
            float srcWidth = options.outWidth;
            float srcHeight = options.outHeight;
            int inSampleSize = 1;

            if (srcHeight > height || srcWidth > width) {
                if (srcWidth > srcHeight) {
                    inSampleSize = Math.round(srcHeight / height);
                } else {
                    inSampleSize = Math.round(srcWidth / width);
                }
            }
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            return BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
        } catch (Exception ex) {
            Log.e(TAG, "readBitmapFromFileDescriptor: "+ex.getMessage() );
        }
        return null;
    }

    /**
     * 改变尺寸大小
     * @return
     */
    private Bitmap chageSize(Bitmap bitmap,int widths,int heghts){
        Bitmap bitma = bitmap;
        int width = bitma.getWidth();
        int height = bitma.getHeight();
        // 设置想要的大小
        int newWidth = widths;
        int newHeight = heghts;
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap mbitmap = Bitmap.createBitmap(bitma, 0, 0, width, height, matrix, true);
        return mbitmap;
    }

    private boolean hasPermission() {
        Log.i(TAG, "hasPermission: 判断是否有权限");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }
    private void requestPermission() {
        Log.i(TAG, "requestPermission: 请求权限");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_STORAGE)) {
                midToast("请求权限");
            }
            requestPermissions(new String[] {PERMISSION_STORAGE}, PERMISSIONS_REQUEST);
        }
    }

}
