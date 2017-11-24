package edu.cornell.yh742.cs5450lab4;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
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
    private Button mProcessed;
    private Button mAuthButton;
    private CheckBox mPrivCheck;
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
        mImageView = (ImageView)findViewById(R.id.imageView);
        mProcessed = (Button)findViewById(R.id.search_p_button);
        mAuthButton = (Button)findViewById(R.id.auth_button);

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
                if (mFilePath != null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                    builder.setTitle("Enter Image Description");
                    final EditText input = new EditText(MenuActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            uploadHandler(input.getText().toString());
                        }
                    });
                    builder.show();
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.uping_err, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // this should start the gallery intent
        mPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, GalleryActivity.class);
                startActivity(intent);
            }
        });

        // start gallery, but using processed pictures
        mProcessed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, GalleryActivity.class);
                intent.putExtra("processed", true);
                startActivity(intent);
            }
        });

        // use this button to go back to sign in activity
        mAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    // handle when upload button is clicked
    private void uploadHandler(String description) {
        // show progress dialog while uploading
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.uping_txt);
        progressDialog.show();

        // setup metadata for the image
        String userName = mAuth.getCurrentUser() == null ? "anon" :
                mAuth.getCurrentUser().toString();
        final String secureLevel = mPrivCheck.isChecked() ? "private" : "public";
        final String desText = description == null ? "" : description;

        // create metadata
        StorageMetadata imageMetaData = new StorageMetadata.Builder()
                .setCustomMetadata("User", userName)
                .setCustomMetadata("Security", secureLevel)
                .setCustomMetadata("Uri", mFilePath.toString())
                .setCustomMetadata("Description", desText)
                .build();

        String storagePath = "";
        String timeStamp = new SimpleDateFormat("yy_MM_dd_HH_mm_ss").format(new Date());

        // determine if the storage is public or private
        if (!mPrivCheck.isChecked()) {
            storagePath = "images/" + timeStamp +
                    mFilePath.toString().substring(mFilePath.toString().lastIndexOf("/") + 1);
        } else {
            storagePath = "private_images/" + timeStamp +
                    mFilePath.toString().substring(mFilePath.toString().lastIndexOf("/") + 1);
        }

        // handlers for when storage upload completes
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
                        int progress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage(progress + "%");
                    }
                });
    }

    // handle events after coming back from photo selection
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO && resultCode == RESULT_OK) {
            if (data == null) {
                Log.d(TAG, "data received from image selection is empty");
                return;
            }
            try {
                // get file and add to imageview as bitmap
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
