package com.example.secret.fruit2.UI;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.imageclassify.AipImageClassify;
import com.example.secret.fruit2.R;
import com.example.secret.fruit2.bean.Fruit;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class ArActivity extends AppCompatActivity implements View.OnClickListener {
    //拍照
    public static final int TAKE_PHOTO = 1;
    //选择图片
    public static final int CHOOSE_PHOTO = 2;
    //选择裁剪
    public static final int CROP_PHOTO = 3;
    //图片的uri
    private Uri imageUri;

    //侧滑栏
    private SlidingMenu slidingMenu;
    //侧滑栏的按钮
    private LinearLayout btnCollect, btnMyRecipe, btnAboutUs, btnSetting;
    //退出按钮
    private Button btnLogout;
    //登录按钮
    private TextView btnLogin;

    //主界面的三个按钮
    private ImageView btnAlbum, btnTakePhoto, btnVegetable;
    //显示侧滑栏的按钮
    private Button btnShowMenu;

    //侧滑栏的view
    private View view;

    //获取当前用户
    private BmobUser currentUser;

    //加载框
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        //初始化bmob
        Bmob.initialize(this, "d6f6f80e5aed95694944dc606ebaad74");

        view = LayoutInflater.from(this).inflate(R.layout.layout_menu, null);

        initView();
        initEvent();

    }

    private void initView() {

        btnAlbum = findViewById(R.id.btn_album);
        btnTakePhoto = findViewById(R.id.btn_takephoto);
        btnVegetable = findViewById(R.id.btn_vegetable);
        btnShowMenu = findViewById(R.id.btn_showMenu);


        btnCollect = view.findViewById(R.id.btnCollect);
        btnMyRecipe = view.findViewById(R.id.btnMyRecipe);
        btnAboutUs = view.findViewById(R.id.btnAboutUs);
        btnSetting = view.findViewById(R.id.btnSetting);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnLogout = view.findViewById(R.id.btnLogout);

    }

    private void initEvent() {


        currentUser = BmobUser.getCurrentUser();
        if (currentUser == null) {
            btnLogin.setText("登录");
        } else {
            btnLogin.setText("欢迎回来");
            btnLogin.setEnabled(false);
        }


        //设置限制模式，强制使post网络请求可以不在线程里面写
        //主要用于识别图像
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //加载侧滑栏
        slidingMenu = new SlidingMenu(this);
        //设置侧滑栏是左滑
        slidingMenu.setMode(SlidingMenu.LEFT);
        //侧滑栏的范围
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        //侧滑栏对应哪个activity
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        //侧滑栏的位移
        slidingMenu.setBehindOffset(200);
        //侧滑栏右边的阴影
        slidingMenu.setOffsetFadeDegree(0.4f);
        //侧滑栏显示的view
        slidingMenu.setMenu(view);

        /*
        侧滑栏的点击事件
         */
        btnLogout.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnSetting.setOnClickListener(this);
        btnAboutUs.setOnClickListener(this);
        btnMyRecipe.setOnClickListener(this);
        btnCollect.setOnClickListener(this);

        //主界面的点击事件
        btnAlbum.setOnClickListener(this);
        btnTakePhoto.setOnClickListener(this);
        btnVegetable.setOnClickListener(this);
        btnShowMenu.setOnClickListener(this);

        //加载框
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在识别中....");

    }

    //点击事件
    @Override
    public void onClick(View view) {


        switch (view.getId()) {
            //显示侧滑栏
            case R.id.btn_showMenu:
                slidingMenu.showMenu();
                break;

            //选择图片
            case R.id.btn_album:
                //获取权限
                //如果没有权限则先请求获取权限
                //若已有权限，则直接打开相册
                if (ContextCompat.checkSelfPermission(ArActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ArActivity.this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
                break;

            //拍照
            case R.id.btn_takephoto:
                //创建File对象   用于存储拍照后的照片
                File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(ArActivity.this,
                            "com.example.cameraalbumtests.fileprovider1", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }
                //启动相机程序
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
                break;

            //跳转到果蔬界面
            case R.id.btn_vegetable:
                startActivity(new Intent(ArActivity.this, VegetableActivity.class));
                break;


            //跳转到收藏界面
            case R.id.btnCollect:
                if (currentUser == null) {
                    toast("请先登录");
                    return;
                }
                startActivity(new Intent(ArActivity.this, CollectActivity.class));
                break;

            //跳转到我的菜谱
            case R.id.btnMyRecipe:
                if (currentUser == null) {
                    toast("请先登录");
                    return;
                }
                startActivity(new Intent(ArActivity.this, MyRecipeActivity.class));
                break;

            //跳转到关于我们界面
            case R.id.btnAboutUs:
                startActivity(new Intent(ArActivity.this, AboutUsActivity.class));
                break;

            //跳转到设置界面
            case R.id.btnSetting:
                startActivity(new Intent(ArActivity.this, SettingActivity.class));
                break;

            //登录按钮
            case R.id.btnLogin:
                startActivity(new Intent(ArActivity.this, LoginActivity.class));
                break;

            //退出按钮
            case R.id.btnLogout:
                if (currentUser == null) {
                    return;
                }
                BmobUser.logOut();
                startActivity(new Intent(ArActivity.this, LoginActivity.class));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    cropPhoto(imageUri);
//                    progressDialog.show();
//                    verify(imageUri.getPath());
//                    ImageRec.filePath = imageUri.getPath();
////                    compressPixel(imageUri.getPath());
//                    String plantname = ImageRec.plant();
//                    Toast.makeText(ArActivity.this, "图片识别结果为: " + plantname, Toast.LENGTH_LONG).show();

                }
                break;

            case CROP_PHOTO:
                if (data != null) {//判断是否选择了图片
                    progressDialog.show();
                    //获取裁剪后的照片
                    Bundle extras = data.getExtras();
                    pic = extras.getParcelable("data");
                    //把裁剪后的照片存储起来
//                    saveNewPic(pic);
                    String path = this.getFilesDir().getPath();//获取当前程序的path
                    String fileName = path + "pic.jpg";//图片名字
                    File file = new File(fileName);
                    sizeCompress(pic,file);


                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {

                    progressDialog.show();
                    verify(getImagePath(data.getData(), null));

                }
                break;
            default:
                break;
        }
    }

    /*
    以下为选择图片与裁剪图片
    */
    private String picPath;
    private Bitmap pic;//图片Bitmap

    /**
     * 调用系统的裁剪
     *
     * @param uri
     */
    public void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1.625);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 180);
        intent.putExtra("outputY", 110);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 3);
    }
    //压缩图片
    public  void sizeCompress(Bitmap bmp, File file) {
        // 尺寸压缩倍数,值越大，图片尺寸越小
        int ratio = 8;
        // 压缩Bitmap到对应尺寸
        Bitmap result = Bitmap.createBitmap(bmp.getWidth() / ratio, bmp.getHeight() / ratio, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Rect rect = new Rect(0, 0, bmp.getWidth() / ratio, bmp.getHeight() / ratio);
        canvas.drawBitmap(bmp, null, rect, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 把压缩后的数据存放到baos中
        result.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            picPath = file.getAbsolutePath();
            verify(picPath);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取图片的路径
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri 和 selection  来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void verify(final String image) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.out.println("===dialog data:" + data);
                String id = "11427928";
                String key = "MfGPONGsmc28MbSthhTb60r4";
                String secret = "za2t3piP8jNyU29XzTIj2QGLgAIK9X4N";

                AipImageClassify client = new AipImageClassify(id, key, secret);
                // 传入可选参数调用接口
                HashMap<String, String> options = new HashMap<String, String>();
//        options.put("image",)
//                String image = getImagePath(data.getData(), null);
                JSONObject res = client.advancedGeneral(image, options);

                JSONObject jsobj = null;
//                System.out.println("=="+res.toString());
                String s = res.toString();
                System.out.println("====res:" + s);
                try {
                    jsobj = new JSONObject(s);
                    org.json.JSONArray jsary = jsobj.getJSONArray("result");
                    String name = jsary.getJSONObject(0).getString("keyword");
                    String score = jsary.getJSONObject(0).getString("score");
                    BmobQuery<Fruit> query = new BmobQuery<>();
                    query.addWhereEqualTo("name", name);
                    query.findObjects(new FindListener<Fruit>() {
                        @Override
                        public void done(List<Fruit> list, BmobException e) {
                            if (e == null) {
                                progressDialog.dismiss();
                                if (list.isEmpty()) {
                                    toast("无此蔬果");
                                    return;
                                }

                                Fruit fruit = list.get(0);
                                Intent intent = new Intent(ArActivity.this, FruitDetailActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("fruit", fruit);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        }
                    });
                    System.out.println(name + "------------------------------------------" + score);
                } catch (JSONException e) {
//                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            toast("识别失败，请重新识别");
                        }
                    });

                }


            }
        }.start();

    }

    //打开相册
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); //打开相册
    }

    //获取权限的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.
                        PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
//        slidingMenu.toggle();
        BmobUser user = BmobUser.getCurrentUser();
        if (user == null) {
            btnLogin.setText("登录");
            btnLogin.setEnabled(true);
        } else {
            btnLogin.setText("欢迎回来");
            btnLogin.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (slidingMenu.isMenuShowing()) {
            slidingMenu.toggle();
        } else {
            super.onBackPressed();
        }

    }
}
