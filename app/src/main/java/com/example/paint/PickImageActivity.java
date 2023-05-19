package com.example.paint;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.dhaval2404.imagepicker.ImagePickerActivity;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import iamutkarshtiwari.github.io.ananas.BaseActivity;
import iamutkarshtiwari.github.io.ananas.editimage.EditImageActivity;
import iamutkarshtiwari.github.io.ananas.editimage.ImageEditorIntentBuilder;
import iamutkarshtiwari.github.io.ananas.editimage.utils.BitmapUtils;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PickImageActivity extends AppCompatActivity implements View.OnClickListener{

    Button photo_picker, edit_image;
    public static final int REQUEST_PERMISSION_STORAGE = 1;
    public static final int ACTION_REQUEST_EDITIMAGE = 9;

    private ImageView imgView;
    private Bitmap mainBitmap;
    private Dialog loadingDialog;

    private int imageWidth, imageHeight;
    File outPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures");
    String fileName;

    private String path;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    ActivityResultLauncher<Intent> editResultLauncher;
    ActivityResultLauncher<Intent> pickResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_image);
        photo_picker = findViewById(R.id.photo_picker);
        edit_image = findViewById(R.id.edit_image);
        setupActivityResultLaunchers();
        initView();
        //photo_picker.setOnClickListener(view -> photo_picker());
        //edit_image.setOnClickListener(view -> edit_image());
    }
    private void setupActivityResultLaunchers() {
        pickResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        handleSelectFromAlbum(data);
                    }
                });
        editResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        handleEditorImage(data);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void initView() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        imageWidth = metrics.widthPixels;
        imageHeight = metrics.heightPixels;

        imgView = findViewById(R.id.img);

        View selectAlbum = findViewById(R.id.photo_picker);
        View editImage = findViewById(R.id.edit_image);
        selectAlbum.setOnClickListener(this);
        editImage.setOnClickListener(this);

        loadingDialog = BaseActivity.getLoadingDialog(this, R.string.iamutkarshtiwari_github_io_ananas_loading,
                false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_image:
                editImageClick();
                break;
            case R.id.photo_picker:
                selectFromAlbum();
                break;
        }
    }

    private void editImageClick() {
        //File outputFile = FileUtilsKt.generateEditFile();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String date = format.format(new Date());
        fileName = outPath + "/" + date + ".jpg";
        if (!outPath.exists()) {
            outPath.mkdirs();
        }

        try {
            HashMap<String, Typeface> fonts = new HashMap<>();
            fonts.put("Sans", Typeface.SANS_SERIF);
            fonts.put("Monospace", Typeface.MONOSPACE);
            fonts.put("Serif", Typeface.SERIF);
            //path = "/storage/emulated/0/Pictures/3c5434a1-e370 -470d-8098-5299e1bdf649.png.jpg";
            Intent intent = new ImageEditorIntentBuilder(this, path, fileName)
                    .withAddText()
                    .withPaintFeature()
                    .withFilterFeature()
                    .withRotateFeature()
                    .withCropFeature()
                    .withBrightnessFeature()
                    .withSaturationFeature()
                    .withBeautyFeature()
                    .withEditorTitle("Photo Editor")
                    //.withFonts(fonts)
                    .forcePortrait(true)
                    .setSupportActionBarVisibility(false)
                    .build();

            EditImageActivity.start(editResultLauncher, intent, this);
        } catch (Exception e) {
            Toast.makeText(this, R.string.iamutkarshtiwari_github_io_ananas_not_selected, Toast.LENGTH_SHORT).show();
            Log.e("Demo App", e.getMessage());
        }
    }

    private void initPath(Intent data) {
        Uri uri = data.getData();
        path = uri.getPath();
    }

    private void selectFromAlbum() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            openAlbumWithPermissionsCheck();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            openAlbum();
        }
    }

    private void openAlbum() {
        //Intent intent = new Intent(this, ImagePickerActivity.class);
        //pickResultLauncher.launch(intent);
        ImagePicker.with(this)
                //  Path: /storage/sdcard0/Android/data/package/files/Pictures
                .saveDir(getExternalFilesDir(Environment.DIRECTORY_PICTURES))
                //.crop()	    			//Crop image(Optional), Check Customization for more option
                //.compress(1024)			//Final image size will be less than 1 MB(Optional)
                //.maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start();

    }

    private void openAlbumWithPermissionsCheck() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_STORAGE);
            return;
        }
        openAlbum();
    }

    private void handleEditorImage(Intent data) {
        String newFilePath = data.getStringExtra(ImageEditorIntentBuilder.OUTPUT_PATH);
        boolean isImageEdit = data.getBooleanExtra(EditImageActivity.IS_IMAGE_EDITED, false);

        if (isImageEdit) {
            Toast.makeText(this, getString(R.string.ananas_image_editor_save_path, newFilePath), Toast.LENGTH_LONG).show();
        } else {
            newFilePath = data.getStringExtra(ImageEditorIntentBuilder.SOURCE_PATH);

        }
        loadImage(newFilePath);
    }

    private void handleSelectFromAlbum(Intent data) {
        Uri uri = data.getData();
        path = FileHelper.getRealPathFromURI(this,uri);
        loadImage(path);
    }

    private void loadImage(String imagePath) {
        Disposable applyRotationDisposable = loadBitmapFromFile(imagePath)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(subscriber -> loadingDialog.show())
                .doFinally(() -> loadingDialog.dismiss())
                .subscribe(
                        this::setMainBitmap,
                        e -> { e.printStackTrace();
                            Toast.makeText(
                                    this, R.string.iamutkarshtiwari_github_io_ananas_load_error, Toast.LENGTH_SHORT).show();}
                );

        compositeDisposable.add(applyRotationDisposable);
    }

    private void setMainBitmap(Bitmap sourceBitmap) {
        if (mainBitmap != null) {
            mainBitmap.recycle();
            mainBitmap = null;
            System.gc();
        }
        mainBitmap = sourceBitmap;
        imgView.setImageBitmap(mainBitmap);
    }

    private Single<Bitmap> loadBitmapFromFile(String filePath) {
        return Single.fromCallable(() ->
                BitmapUtils.getSampledBitmap(
                        filePath,
                        imageWidth / 4,
                        imageHeight / 4
                )
        );
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();
        imgView.setImageURI(uri);
        path = FileHelper.getRealPathFromURI(this,uri);

    }



}