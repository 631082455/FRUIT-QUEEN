package com.example.secret.fruit2.UI;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secret.fruit2.R;
import com.example.secret.fruit2.bean.Recipe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

public class UploadActivity extends AppCompatActivity {

    //填菜谱信息
    private EditText etName, etMaterial, etProcedure;
    //完成按钮
    private TextView btnFinish;
    //上传图片按钮
    private ImageView btnAddPic;
    //返回按钮
    private ImageView btnBack;

    //使用flag来判断是否选择图片
    int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        initView();
        initEvent();
    }

    private void initView() {

        etName = findViewById(R.id.etName);
        etMaterial = findViewById(R.id.etMaterial);
        etProcedure = findViewById(R.id.etProcedure);

        btnAddPic = findViewById(R.id.btnAddPic);

        btnFinish = findViewById(R.id.btnFinish);

        btnBack = findViewById(R.id.btnBack);

    }

    private void initEvent() {


        //返回按钮的点击事件
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //上传图片点击事件
        btnAddPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent1, 1);
            }
        });

        //完成按钮点击事件
        final BmobUser currentUser = BmobUser.getCurrentUser();
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser==null){
                    toast("请先登录后再上传食谱");
                    return;
                }
                if (TextUtils.isEmpty(etName.getText()) || TextUtils.isEmpty(etMaterial.getText())
                        || TextUtils.isEmpty(etProcedure.getText()) || flag == 0) {
                    //使用TextUtils来判断输入的文本是否为空。使用flag来判断是否选中图片
                    toast("请填齐信息后再上传");
                    return;
                }

                final ProgressDialog progressDialog = new ProgressDialog(UploadActivity.this);
                progressDialog.setMessage("正在上传中...");
                progressDialog.show();

                final String name = etName.getText().toString();
                final String material = etMaterial.getText().toString();
                final String procedure = etProcedure.getText().toString();
                final BmobFile file = new BmobFile(new File(picPath));
                //上传文件
                file.upload(new UploadFileListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e==null){
                            //必须先上传图片后才能把图片存在食谱里面。
                            Recipe recipe = new Recipe();
                            recipe.setName(name);
                            recipe.setMaterial(material);
                            recipe.setProcedure(procedure);
                            recipe.setPic(file);
                            recipe.setUser(currentUser);
                            recipe.setAuthor("云卿");
                            //随机生成评分
                            float score = (float) (Math.random() * 10);
                            //把评分保留一位小数
                            DecimalFormat format = new DecimalFormat("#.#");
                            recipe.setScore(format.format(score));
                            //随机生成做过人数
                            int num = 1000 + (int) (Math.random() * 2000);
                            recipe.setNum(num);
                            //随机生成点赞数
                            int likeNum = 1000 + (int) (Math.random() * 2000);
                            recipe.setLikeNum(likeNum);

                            recipe.save(new SaveListener<String>() {
                                @Override
                                public void done(String s, BmobException e) {
                                    if (e==null){
                                        toast("上传成功");
                                    }else {
                                        toast("上传失败,"+e.getMessage());
                                    }
                                    progressDialog.dismiss();
                                    //上传成功后界面消失
                                    finish();
                                }
                            });
                        }else {
                            toast("上传失败,"+e.getMessage());
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        });


    }

    /*
    以下为选择图片与裁剪图片
     */
    String picPath;
    private Bitmap pic;//图片Bitmap

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1://选择图片
                if (resultCode == RESULT_OK) {
                    cropPhoto(data.getData());//裁剪图片
                }
                break;
            case 2://裁剪图片后
                if (data != null) {//判断是否选择了图片
                    //获取裁剪后的照片
                    Bundle extras = data.getExtras();
                    pic = extras.getParcelable("data");
                    //把裁剪后的照片存储起来
                    saveNewPic(pic);
                    //把照片显示出来
                    btnAddPic.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    btnAddPic.setImageBitmap(pic);
                    flag = 1;
                }
        }
    }

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
        startActivityForResult(intent, 2);
    }




    private void saveNewPic(Bitmap mBitmap) {
        String path = this.getFilesDir().getPath();//获取当前程序的path
        FileOutputStream b = null;
        File file = new File(path);
        file.mkdirs();// 创建文件夹

        String fileName = path + "pic.jpg";//图片名字
        try {

            b = new FileOutputStream(fileName);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
            File file1 = new File(fileName);
            picPath = file1.getAbsolutePath();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭流
                b.flush();
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
