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

        mDbRef = FirebaseDatabase.getInstance().getReference(DB_PATH);
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Loading images...");
        progress.show();
        mDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progress.dismiss();
                FirebaseAuth auth = FirebaseAuth.getInstance();
                mPicList = new LinkedHashMap<ImageData, String>();
                String searchTerm = getIntent().getStringExtra("searchTerm");
                for (DataSnapshot snapshots : dataSnapshot.getChildren()){
                    Log.d(TAG, snapshots.getKey());
                    for (DataSnapshot snapshot : snapshots.getChildren()){
                        Log.d(TAG, snapshot.getKey());
                        Log.d(TAG, snapshot.getValue().toString());
                        ImageData data = snapshot.getValue(ImageData.class);
                        if (searchTerm != null && !data.description.equals(searchTerm)) {
                            continue;
                        }
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
