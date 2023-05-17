package com.example.paint;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.kyanogen.signatureview.SignatureView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {
    int defaultColor;
    SignatureView signatureView;
    ImageButton btnColorPicker, btnDelete, btnSave, btnEraser;
    SeekBar seekBar;
    TextView txtPenSize;
    private static String fileName;
    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ePaints");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askPermission();

        signatureView = findViewById(R.id.signature_view);
        btnColorPicker = findViewById(R.id.btnColorPicker);
        btnDelete = findViewById(R.id.btnDelete);
        btnEraser = findViewById(R.id.btnEraser);
        btnSave = (ImageButton) findViewById(R.id.btnSave);
        seekBar = findViewById(R.id.pensize);
        txtPenSize = findViewById(R.id.txtPenSize);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String date = format.format(new Date());
        fileName = path + "/" + date + ".png";
ы
        if (!path.exists()) {
            path.mkdirs();
        }

        defaultColor = ContextCompat.getColor(MainActivity.this, R.color.black);

        btnColorPicker.setOnClickListener(view -> openColorPicker());
        btnDelete.setOnClickListener(view -> signatureView.clearCanvas());
        btnEraser.setOnClickListener(view -> touchEraser());
        btnSave.setOnClickListener(view -> saveImage());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txtPenSize.setText(i + " dp");
                signatureView.setPenSize(i);
                seekBar.setMax(50);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


    }

    private void askPermission() {
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }


    private void openColorPicker() {
        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                defaultColor = color;
                signatureView.setPenColor(defaultColor);
            }
        });
        ambilWarnaDialog.show();
    }

    private void touchEraser() {
        defaultColor = Color.WHITE;
        signatureView.setPenColor(defaultColor);
    }

    private void saveImage() {
        signatureView.setDrawingCacheEnabled(true);
        String imgSaved = MediaStore.Images.Media.insertImage(getContentResolver(),
                signatureView.getDrawingCache(), UUID.randomUUID().toString()
                        + ".png", "drawing");
        if (imgSaved != null) {
            Toast savedToast = Toast.makeText(getApplicationContext(),
                    "Saved!", Toast.LENGTH_LONG);
            savedToast.show();

        } else {
            Toast unsavedToast = Toast.makeText(getApplicationContext(),
                    "Oops, something went wrong", Toast.LENGTH_LONG);
            unsavedToast.show();
        }

        signatureView.destroyDrawingCache();
    }

/*    private void saveImage2() {
        Toast savedToast = Toast.makeText(getApplicationContext(),
                "Saved!", Toast.LENGTH_LONG);
        savedToast.show();
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}