package edu.cornell.yh742.cs5450lab4;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.Toast;

public class MenuActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDbRef;
    private StorageReference mStorageRef;
    private Button mBrowse;
    private Button mUpload;
    private Button mPictures;
    private CheckBox mPrivCheck;
    private EditText mTextLine;
    private ImageView mImageView;

    private static final int PICK_PHOTO = 200;
    private static final String TAG = MenuActivity.class.getSimpleName();
    private static final String DB_PATH = "imgdb";
    private Uri mFilePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // get FireBase stuff
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDbRef = FirebaseDatabase.getInstance().getReference(DB_PATH);
        mAuth = FirebaseAuth.getInstance();

        // get UI
        mBrowse = (Button)findViewById(R.id.browse_button);
        mUpload = (Button)findViewById(R.id.upload_button);
        mPictures = (Button)findViewById(R.id.search_b_button);
        mPrivCheck = (CheckBox)findViewById(R.id.checkBox);
        mTextLine = (EditText)findViewById(R.id.editText);
        mImageView = (ImageView)findViewById(R.id.imageView);

        // user is not signed in
        //if (mAuth.getCurrentUser() == null){
            //mUpload.setEnabled(false);
            //mPrivCheck.setEnabled(false);
            //mTextLine.setEnabled(false);
            //mBrowse.setEnabled(false);
        //}

        // start implicit intent when uploading pictures
        mBrowse.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_PHOTO);
            }
        });

        // this should set off uploading mechanism
        mUpload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                uploadHandler();
            }
        });
    }

    private void uploadHandler(){
        if (mFilePath != null){
            // show progress dialog while uploading
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(R.string.uping_txt);
            progressDialog.show();

            // setup metadata for the image
            String userName = mAuth.getCurrentUser() == null ? "anon" :
                    mAuth.getCurrentUser().toString();
            final String secureLevel = mPrivCheck.isChecked() ? "private" : "public";
            final String desText = mTextLine.getText() == null ? "empty" : mTextLine.getText().toString();

            // create metadata
            StorageMetadata imageMetaData = new StorageMetadata.Builder()
                    .setCustomMetadata("User", userName)
                    .setCustomMetadata("Security", secureLevel)
                    .setCustomMetadata("Uri", mFilePath.toString())
                    .setCustomMetadata("Description", desText)
                    .build();

            String storagePath = "";
            String timeStamp = new SimpleDateFormat("yy_MM_dd_HH_mm_ss").format(new Date());
            if (!mPrivCheck.isChecked()) {
                storagePath = "images/" + timeStamp +
                        mFilePath.toString().substring(mFilePath.toString().lastIndexOf("/") + 1);
            }
            else {
                storagePath = "private_images/" + timeStamp +
                        mFilePath.toString().substring(mFilePath.toString().lastIndexOf("/") + 1);
            }
            StorageReference reference = mStorageRef.child(storagePath);
            reference.putFile(mFilePath, imageMetaData)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Image Uploaded", Toast.LENGTH_LONG).show();
                            String primeKey = mDbRef.push().getKey();
                            mDbRef.child(secureLevel).child(primeKey).setValue(
                                    new ImageData(taskSnapshot.getDownloadUrl().toString(), desText));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "You don't have permission to upload.", Toast.LENGTH_LONG).show();
                            Log.e(TAG, e.getMessage());
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress = (int)(100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage(progress + "%");
                        }
                    });
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.uping_err, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO && resultCode == RESULT_OK) {
            if (data == null) {
                Log.d(TAG, "data received from image selection is empty");
                return;
            }
            try {
                mFilePath = data.getData();
                InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(mFilePath);
                Bitmap map = BitmapFactory.decodeStream(inputStream);
                map = Bitmap.createScaledBitmap(map, mImageView.getWidth(), mImageView.getHeight(), false);
                mImageView.setImageBitmap(map);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}