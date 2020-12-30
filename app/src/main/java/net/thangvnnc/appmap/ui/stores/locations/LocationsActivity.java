package net.thangvnnc.appmap.ui.stores.locations;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import net.thangvnnc.appmap.R;
import net.thangvnnc.appmap.common.DateUtils;
import net.thangvnnc.appmap.database.Direction;
import net.thangvnnc.appmap.database.Location;
import net.thangvnnc.appmap.database.User;
import net.thangvnnc.appmap.database.firebase.FBDirection;
import net.thangvnnc.appmap.database.firebase.FBLocation;
import net.thangvnnc.appmap.database.sqlite3.SQLiLocation;
import net.thangvnnc.appmap.databinding.ActivityLocationsBinding;
import net.thangvnnc.appmap.databinding.ActivityLocationsItemBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocationsActivity extends AppCompatActivity {
    private static final String TAG = "LocationsActivity";
    private ActivityLocationsBinding mBind = null;
    private Context mContext = null;
    private final List<Integer> positionSelected = new ArrayList<>();
    private final List<Location> locations = new ArrayList<>();
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

        mBind.btnAddDirection.setOnClickListener(btnAddDirectionClick);
    }

    private final View.OnClickListener btnAddDirectionClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            List<String> locationIds = new ArrayList<>();
            locationIds.add(locations.get(positionSelected.get(0)).id);
            locationIds.add(locations.get(positionSelected.get(1)).id);
            saveDirection(locationIds);
        }
    };

    private void saveDirection(List<String> locationIds) {
        Direction direction = new Direction();
        direction.locations = new ArrayList<>();
        direction.locations.addAll(locationIds);
        direction.locationDetails = new ArrayList<>();
        direction.isUsing = true;
        direction.updatedBy = User.getSession().id;
        direction.updatedAt = DateUtils.getCurrent();

        // Insert new
        if (direction.id == null) {
            direction.id = Direction.generalId();
            direction.createdBy = User.getSession().id;
            direction.createdAt = DateUtils.getCurrent();
        }

        ProgressDialog progressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.message_waiting));
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext);
        FBDirection.insertOrUpdate(direction).addOnSuccessListener(aVoid -> {
            materialAlertDialogBuilder.setMessage(R.string.message_success);
            materialAlertDialogBuilder.setPositiveButton(R.string.alert_dialog_button_ok, (dialog, which) -> {
                dialog.dismiss();
                positionSelected.clear();
                mLocationsAdapter.notifyDataSetChanged();
            });
            materialAlertDialogBuilder.show();
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                materialAlertDialogBuilder.setMessage(R.string.message_failed);
                materialAlertDialogBuilder.setPositiveButton(R.string.alert_dialog_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                materialAlertDialogBuilder.show();
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                Log.d(TAG, "onComplete");
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.d(TAG, "onCanceled");
            }
        });

//        FBDirection.getChild().addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                List<Direction> fbDirections = new ArrayList<>();
//                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
//                    Direction fbDirectionSet = postSnapshot.getValue(Direction.class);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                materialAlertDialogBuilder.setMessage(R.string.message_failed);
//                materialAlertDialogBuilder.setPositiveButton(R.string.alert_dialog_button_ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                materialAlertDialogBuilder.show();
//            }
//        });
    }

    private void initRcvLocationDetails() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mBind.rcvLocations.setLayoutManager(layoutManager);
        mLocationsAdapter = new LocationsAdapter(locations);
        mBind.rcvLocations.setAdapter(mLocationsAdapter);

        DividerItemDecoration itemDecorator = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(mContext, R.drawable.rcv_divider));
        mBind.rcvLocations.addItemDecoration(itemDecorator);
        ProgressDialog progressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.message_waiting));
        FBLocation.getAll().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                locations.clear();
                locations.addAll(Location.parseLocations(true, snapshot));
//                SQLiLocation.getDao().inserts(locations);
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
        private List<Location> locations = null;

        private class LocationsViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
            private ActivityLocationsItemBinding mItemBind = null;
            private final int MENU_ITEM_EDIT = 1;
            private final int MENU_ITEM_REMOVE = 2;

            public LocationsViewHolder(ActivityLocationsItemBinding mItemBind) {
                super(mItemBind.getRoot());
                this.mItemBind = mItemBind;
                this.mItemBind.getRoot().setOnCreateContextMenuListener(this);
            }

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                MenuItem btnEditMenuItem = menu.add(Menu.NONE, MENU_ITEM_EDIT, getAdapterPosition(), R.string.locations_context_menu_item_edit);
                btnEditMenuItem.setOnMenuItemClickListener(onMenuItemClickListener);

                MenuItem btnRemoveMenuItem = menu.add(Menu.NONE, MENU_ITEM_REMOVE, getAdapterPosition(), R.string.locations_context_menu_item_remove);
                btnRemoveMenuItem.setOnMenuItemClickListener(onMenuItemClickListener);
            }

            private final MenuItem.OnMenuItemClickListener onMenuItemClickListener = item -> {
                int idItem = item.getItemId();
                int position = item.getOrder();
                Location location = locations.get(position);

                switch (idItem) {
                    case MENU_ITEM_EDIT:
                        Intent intent = new Intent(mContext, LocationDetailsActivity.class);
                        intent.putExtra(LocationDetailsActivity.LOCATION_DETAIL, location);
                        startActivity(intent);
                        break;

                    case MENU_ITEM_REMOVE:
                        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext);
                        materialAlertDialogBuilder.setTitle(R.string.confirm_title_remove);
                        materialAlertDialogBuilder.setMessage(R.string.confirm_content_remove);
                        materialAlertDialogBuilder.setPositiveButton(R.string.confirm_btn_ok, (dialog, which) -> FBLocation.removeById(location.id));
                        materialAlertDialogBuilder.setNegativeButton(R.string.confirm_btn_no, null);
                        materialAlertDialogBuilder.show();
                        break;

                    default:
                        break;
                }
                return true;
            };
        }

        public LocationsAdapter(List<Location> locations){
            this.locations = locations;
        }

        @NonNull
        @Override
        public LocationsAdapter.LocationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            ActivityLocationsItemBinding binding = ActivityLocationsItemBinding.inflate(LayoutInflater.from(mContext), parent, false);
            return new LocationsAdapter.LocationsViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(LocationsAdapter.LocationsViewHolder holder, int position){
            Location location = locations.get(position);
            holder.mItemBind.txtLocationName.setText(location.name);
            holder.mItemBind.txtLocationDescription.setText(location.description);
            holder.mItemBind.icSeleted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBind.btnAddDirection.setVisibility(View.GONE);
                    if (positionSelected.size() > 1) {
                        positionSelected.clear();
                        mLocationsAdapter.notifyDataSetChanged();
                        return;
                    }

                    if (positionSelected.size() == 1) {
                        int positionCheck = positionSelected.get(0);
                        // Không add 2 lần 1 item
                        if (positionCheck == position) {
                            return;
                        }
                    }

                    positionSelected.add(position);

                    if (positionSelected.size() == 2) {
                        mBind.btnAddDirection.setVisibility(View.VISIBLE);
                    }
                    mLocationsAdapter.notifyDataSetChanged();
                }
            });

            boolean isSeleted = false;
            for (int idxPosition: positionSelected) {
                if (idxPosition == position) {
                    isSeleted = true;
                    break;
                }
            }

            if (isSeleted) {
                holder.mItemBind.icSeleted.setImageResource(R.drawable.ic_select);
            }
            else {
                holder.mItemBind.icSeleted.setImageResource(R.drawable.ic_non_select);
            }

        }

        @Override
        public int getItemCount(){
            return locations.size();
        }
    }
}