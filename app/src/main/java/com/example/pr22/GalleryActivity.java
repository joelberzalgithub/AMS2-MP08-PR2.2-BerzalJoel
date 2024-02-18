package com.example.pr22;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        List<File> photoFiles = getPhotoFiles();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ImageAdapter(this, photoFiles));

        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> startActivity(new Intent(GalleryActivity.this, MainActivity.class)));
    }

    private List<File> getPhotoFiles() {
        List<File> photoFiles = new ArrayList<>();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        assert storageDir != null;
        File[] files = storageDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isPhotoFile(file)) {
                    photoFiles.add(file);
                }
            }
        }
        return photoFiles;
    }

    private boolean isPhotoFile(File file) {
        String[] supportedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
        for (String extension : supportedExtensions) {
            if (file.getName().toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private static class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
        private final Context context;
        private final List<File> photoFiles;
        public ImageAdapter(Context context, List<File> imageFiles) {
            this.context = context;
            this.photoFiles = imageFiles;
        }

        public void showPhotoFileName(final String photoFileName) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Nom de la imatge sel·leccionada");
            builder.setMessage(photoFileName);
            builder.setPositiveButton("Ok", (dialog, whichButton) -> dialog.dismiss());
            builder.create().show();
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            File photoFile = photoFiles.get(position);
            holder.imageView.setImageURI(Uri.fromFile(photoFile));
            holder.imageView.setOnClickListener(v -> showPhotoFileName(photoFile.getAbsolutePath()));
        }

        @Override
        public int getItemCount() {
            return photoFiles.size();
        }

        public static class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }
}
