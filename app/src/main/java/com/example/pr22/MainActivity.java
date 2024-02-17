package com.example.pr22;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private ActivityResultLauncher<Intent> pickPictureLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<Intent> dispatchTakePictureIntentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        pickPictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        assert result.getData() != null;
                        imageView.setImageURI(result.getData().getData());
                    }
                });

        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        dispatchTakePictureIntentLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                    }
                });

        dispatchTakePictureIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result != null && result.getResultCode() == RESULT_OK) {
                        assert result.getData() != null;
                        imageView.setImageBitmap((Bitmap) Objects.requireNonNull(result.getData().getExtras()).get("data"));
                    }
                });

        Button galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(v -> pickPictureLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)));

        Button cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntentLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
    }
}
