package net.thangvnnc.appmap.ui.stores.locations;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import net.thangvnnc.appmap.R;
import net.thangvnnc.appmap.database.FirebaseDB;
import net.thangvnnc.appmap.databinding.ActivityLocationsBinding;
import net.thangvnnc.appmap.databinding.ActivityLocationsItemBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LocationsActivity extends AppCompatActivity {
    private static final String TAG = "LocationsActivity";
    private ActivityLocationsBinding mBind = null;
    private Context mContext = null;
    private List<FirebaseDB.FBLocation> fbLocations = new ArrayList<>();
    private LocationsAdapter mLocationsAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mBind = ActivityLocationsBinding.inflate(LayoutInflater.from(mContext));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(mBind.getRoot());
        initialize();
    }

    private void initialize() {
        initRcvLocationDetails();
    }

    private void initRcvLocationDetails() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mBind.rcvLocations.setLayoutManager(layoutManager);
        mLocationsAdapter = new LocationsAdapter(fbLocations);
        mBind.rcvLocations.setAdapter(mLocationsAdapter);

        DividerItemDecoration itemDecorator = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(mContext, R.drawable.divider_rcv));
        mBind.rcvLocations.addItemDecoration(itemDecorator);
        ProgressDialog progressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.message_waiting));
        FirebaseDB.FBLocation.getAll().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fbLocations.clear();
                List<FirebaseDB.FBLocation> fbLocationGets = new ArrayList<>();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    FirebaseDB.FBLocation fbLocation = postSnapshot.getValue(FirebaseDB.FBLocation.class);
                    fbLocationGets.add(fbLocation);
                }
                fbLocations.addAll(fbLocationGets);
                Collections.reverse(fbLocations);
                mLocationsAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        throw new IllegalStateException("Unexpected value: " + item.getItemId());
    }

    private class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.LocationsViewHolder> {
        private List<FirebaseDB.FBLocation> fbLocations = null;

        private class LocationsViewHolder extends RecyclerView.ViewHolder{
            private ActivityLocationsItemBinding mItemBind = null;

            public LocationsViewHolder(ActivityLocationsItemBinding mItemBind){
                super(mItemBind.getRoot());
                this.mItemBind = mItemBind;
            }
        }

        public LocationsAdapter(List<FirebaseDB.FBLocation> fbLocations){
            this.fbLocations = fbLocations;
        }

        @NonNull
        @Override
        public LocationsAdapter.LocationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            ActivityLocationsItemBinding binding = ActivityLocationsItemBinding.inflate(LayoutInflater.from(mContext), parent, false);
            return new LocationsAdapter.LocationsViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(LocationsAdapter.LocationsViewHolder holder, int position){
            FirebaseDB.FBLocation fbLocation = fbLocations.get(position);
            holder.mItemBind.txtLocationName.setText(fbLocation.name);
            holder.mItemBind.txtLocationDescription.setText(fbLocation.description);
            holder.mItemBind.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, LocationDetailsActivity.class);
                    intent.putExtra(LocationDetailsActivity.LOCATION_DETAIL, fbLocation);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount(){
            return fbLocations.size();
        }
    }
}