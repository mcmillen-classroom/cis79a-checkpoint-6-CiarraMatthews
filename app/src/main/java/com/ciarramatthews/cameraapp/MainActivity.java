package com.ciarramatthews.cameraapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mCameraButton;
    private Button mAddPictoGallery;
    private Button mShareButton;
    private Button mEmailButton;
    private ImageView mImageView;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    Uri mCurrentPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraButton = (Button) findViewById(R.id.camera_button);
        mImageView = (ImageView) findViewById(R.id.image_view);
        mAddPictoGallery = (Button) findViewById(R.id.add_button);
        mShareButton = (Button) findViewById(R.id.share_button);
        mEmailButton = (Button) findViewById(R.id.email_button);
        mCameraButton.setOnClickListener(this);
        mAddPictoGallery.setOnClickListener(this);
        mImageView.setOnClickListener(this);
        mShareButton.setOnClickListener(this);
        mEmailButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        //ImageView mImageView = (ImageView) view;
        if (view.getId() == R.id.camera_button) {
            dispatchTakePictureIntent();
        }
        if (view.getId() == R.id.add_button && mCurrentPhotoUri != null)
        {
            galleryAddPic();
        }
        if (view.getId() == R.id.share_button)
        {
            dispatchSharePicture();
        }
        if (view.getId() == R.id.email_button)
        {
            dispatchEmailPicture();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            mImageView.setImageURI(mCurrentPhotoUri);
        }
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            //...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoUri = Uri.fromFile(image);
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(String.valueOf(mCurrentPhotoUri));
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(String.valueOf(mCurrentPhotoUri), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(mCurrentPhotoUri), bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    private void dispatchSharePicture()
    {
        File image = new File(mCurrentPhotoUri.getPath());
        Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", image);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
        shareIntent.setType("image/jpeg");

        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.generic_string)));
    }

    private void dispatchEmailPicture()
    {
        File image = new File(mCurrentPhotoUri.getPath());
        Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", image);

        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out my pic plz");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Taken using my app");
        emailIntent.putExtra(Intent.EXTRA_STREAM, photoURI);

        if(emailIntent.resolveActivity(getPackageManager()) != null)
        {
            startActivity(emailIntent);
        }
        else {
            Toast.makeText(this, "No email app configured", Toast.LENGTH_LONG).show();
        }
    }
}
