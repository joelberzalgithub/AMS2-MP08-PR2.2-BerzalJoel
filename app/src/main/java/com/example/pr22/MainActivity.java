package com.example.pr22;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private boolean isFullSize;
    private ImageView imageView;
    private Uri photoURI;
    private ActivityResultLauncher<Intent> pickPictureLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> takeFullSizePictureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        File latestPhotoFile = getLatestPhoto();
        if (latestPhotoFile != null) {
            Uri latestPhotoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", latestPhotoFile);
            imageView.setImageURI(latestPhotoURI);
        }

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
                        if (isFullSize) {
                            dispatchTakePictureIntent();
                        } else {
                            takePictureLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                        }
                    }
                });

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result != null && result.getResultCode() == RESULT_OK) {
                        assert result.getData() != null;
                        imageView.setImageBitmap((Bitmap) Objects.requireNonNull(result.getData().getExtras()).get("data"));
                    }
                });

        takeFullSizePictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result != null && result.getResultCode() == RESULT_OK) {
                        imageView.setImageURI(photoURI);
                    }
                });

        Button galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(v -> pickPictureLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)));

        Button cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                isFullSize = false;
                takePictureLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        Button fullSizeButton = findViewById(R.id.fullSizeButton);
        fullSizeButton.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                isFullSize = true;
                dispatchTakePictureIntent();
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
    }
    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.resolveActivity(getPackageManager());
        // Creem el File on hagi d'anar la foto
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // Continuem en cas que el File s'hagi creat amb èxit
        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takeFullSizePictureLauncher.launch(takePictureIntent);
        }
    }
    private File createImageFile() throws IOException {
        // Creem el nom d'arxiu d'una imatge
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* Nom */
                ".jpg",         /* Extensió */
                storageDir      /* Ubicació */
        );
    }
    private File getLatestPhoto() {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        assert storageDir != null;
        // Creem una llista amb els arxius de les fotos emmagatzemades en el dispositiu
        File[] photoFiles = storageDir.listFiles();
        if (photoFiles != null && photoFiles.length > 0) {
            // Ordenem els arxius en funció de l'última timestamp per obtenir l'última foto
            Arrays.sort(photoFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            return photoFiles[0];
        }
        return null;
    }
}
