package edu.cornell.yh742.cs5450lab4;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.app.ProgressDialog;


public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = GalleryActivity.class.getSimpleName();
    private static final String DB_PATH = "imgdb";
    private static final String PS_PATH = "psdb";
    private DatabaseReference mDbRef;
    private Map<ImageData, String> mPicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 1);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_images);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        // check if gallery is used for processed images or not
        final boolean processed = getIntent().getBooleanExtra("processed", false);
        // db root is different for processed vs regular images
        if (processed){
            mDbRef = FirebaseDatabase.getInstance().getReference(PS_PATH);
        }
        else{
            mDbRef = FirebaseDatabase.getInstance().getReference(DB_PATH);
        }

        // show progress dialog
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Loading images...");
        progress.show();

        // this will only fire once
        mDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progress.dismiss();
                int index = 0;
                FirebaseAuth auth = FirebaseAuth.getInstance();
                mPicList = new LinkedHashMap<ImageData, String>();

                // if intent has search associated only lookup pictures with the same description
                String searchTerm = getIntent().getStringExtra("searchTerm");

                // iterate through the database to store URI
                for (DataSnapshot snapshots : dataSnapshot.getChildren()){
                    Log.d(TAG, snapshots.getKey());
                    for (DataSnapshot snapshot : snapshots.getChildren()){
                        Log.d(TAG, snapshot.getKey());
                        Log.d(TAG, snapshot.getValue().toString());
                        ImageData data;
                        // if it is processed store only URI
                        if (processed){
                            String url = snapshot.child("url").getValue(String.class);
                            data = new ImageData(url, "processed" + index);
                            index++;
                        }
                        // if it is not process deserialize into ImageData
                        else{
                            data = snapshot.getValue(ImageData.class);
                        }
                        if (searchTerm != null && !data.description.equals(searchTerm)) {
                            continue;
                        }

                        // make sure we differentiate public and private pictures for security
                        if (snapshots.getKey().toString().equals("private")) {
                            if (auth.getCurrentUser() != null) {
                                mPicList.put(data, "private");
                            }
                        } else {
                            mPicList.put(data, "public");
                        }
                    }
                }
                GalleryAdapter adapter = new GalleryAdapter(GalleryActivity.this, mPicList);
                recyclerView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        // this will fire when the search starts
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: "+ newText);
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String s){
                Log.d(TAG, "Clicked on search icon");
                Log.d(TAG, searchView.getQuery().toString());
                // need to finish current activity, and restart gallery with search pics only
                finish();
                Intent intent = getIntent();
                intent.putExtra("searchTerm", searchView.getQuery().toString());
                startActivity(intent);
                return true;
            }
        });
        return true;
    }
}
