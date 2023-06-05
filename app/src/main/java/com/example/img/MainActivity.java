package com.example.img;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private Button selectImageButton;
    private FrameLayout frameLayout1;
    private FrameLayout frameLayout2;
    private ImageView imageView1;
    private ImageView imageView2;
    private Bitmap originalBitmap;
    private Bitmap scaledBitmap;
    private Matrix matrix1;
    private Matrix matrix2;
    private float scaleFactor = 1f;
    private float previousX;
    private float previousY;
    private float previousTouchX;
    private float previousTouchY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectImageButton = findViewById(R.id.selectImageButton);
        frameLayout1 = findViewById(R.id.frameLayout1);
        frameLayout2 = findViewById(R.id.frameLayout2);
        imageView1 = findViewById(R.id.imageView1);
        imageView2 = findViewById(R.id.imageView2);

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        matrix1 = new Matrix();
        matrix2 = new Matrix();

        frameLayout1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handleTouch(event, imageView1, matrix1, imageView2, matrix2);
                return true;
            }
        });

        frameLayout2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handleTouch(event, imageView2, matrix2, imageView1, matrix1);
                return true;
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            try {
                originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                scaledBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

                int halfHeight = scaledBitmap.getHeight() / 2;
                Bitmap topBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), halfHeight);
                Bitmap bottomBitmap = Bitmap.createBitmap(scaledBitmap, 0, halfHeight, scaledBitmap.getWidth(), halfHeight);

                imageView1.setImageBitmap(topBitmap);
                imageView2.setImageBitmap(bottomBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleTouch(MotionEvent event, ImageView imageView1, Matrix matrix1, ImageView imageView2, Matrix matrix2) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                previousX = event.getX();
                previousY = event.getY();
                previousTouchX = event.getRawX();
                previousTouchY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getRawX() - previousTouchX;
                float deltaY = event.getRawY() - previousTouchY;

                matrix1.reset();
                matrix1.setTranslate(deltaX, deltaY);

                float scale = event.getY() / previousY;
                scaleFactor *= scale;
                matrix1.postScale(scaleFactor, scaleFactor, imageView1.getWidth() / 2, imageView1.getHeight() / 2);

                imageView1.setImageMatrix(matrix1);

                // Reflejar cambios de zoom en la otra mitad
                matrix2.reset();
                matrix2.set(matrix1);
                matrix2.postScale(1f / scaleFactor, 1f / scaleFactor, imageView2.getWidth() / 2, imageView2.getHeight() / 2);

                imageView2.setImageMatrix(matrix2);

                previousX = event.getX();
                previousY = event.getY();
                previousTouchX = event.getRawX();
                previousTouchY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                scaleFactor = 1f;
                break;
        }
    }
}
