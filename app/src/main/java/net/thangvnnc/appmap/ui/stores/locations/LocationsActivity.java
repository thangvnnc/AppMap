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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import net.thangvnnc.appmap.R;
import net.thangvnnc.appmap.database.FBDirection;
import net.thangvnnc.appmap.database.FBLocation;
import net.thangvnnc.appmap.databinding.ActivityLocationsBinding;
import net.thangvnnc.appmap.databinding.ActivityLocationsItemBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static net.thangvnnc.appmap.database.FirebaseDB.generalId;

public class LocationsActivity extends AppCompatActivity {
    private static final String TAG = "LocationsActivity";
    private ActivityLocationsBinding mBind = null;
    private Context mContext = null;
    private final List<Integer> positionSelected = new ArrayList<>();
    private final List<FBLocation> fbLocations = new ArrayList<>();
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
            locationIds.add(fbLocations.get(positionSelected.get(0)).id);
            locationIds.add(fbLocations.get(positionSelected.get(1)).id);
            saveDirection(locationIds);
        }
    };

    private void saveDirection(List<String> locationIds) {
        long sessionUserId = 0;
        FBDirection fbDirection = new FBDirection();
        fbDirection.locations = new ArrayList<>();
        fbDirection.locations.addAll(locationIds);
        fbDirection.locationDetails = new ArrayList<>();
        fbDirection.isUsing = true;
        fbDirection.updatedBy = sessionUserId;
        fbDirection.updatedAt = new Date();

        // Insert new
        if (fbDirection.id == null) {
            fbDirection.id = generalId();
            fbDirection.createdBy = sessionUserId;
            fbDirection.createdAt = new Date();
        }

        ProgressDialog progressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.message_waiting));
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext);
        fbDirection.insertOrUpdateUser().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                materialAlertDialogBuilder.setMessage(R.string.message_success);
                materialAlertDialogBuilder.setPositiveButton(R.string.alert_dialog_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        positionSelected.clear();
                        mLocationsAdapter.notifyDataSetChanged();
                    }
                });
                materialAlertDialogBuilder.show();
            }
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
//                List<FBDirection> fbDirections = new ArrayList<>();
//                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
//                    FBDirection fbDirectionSet = postSnapshot.getValue(FBDirection.class);
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
        mLocationsAdapter = new LocationsAdapter(fbLocations);
        mBind.rcvLocations.setAdapter(mLocationsAdapter);

        DividerItemDecoration itemDecorator = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(mContext, R.drawable.rcv_divider));
        mBind.rcvLocations.addItemDecoration(itemDecorator);
        ProgressDialog progressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.message_waiting));
        FBLocation.getAll().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fbLocations.clear();
                List<FBLocation> fbLocationGets = new ArrayList<>();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    FBLocation fbLocation = postSnapshot.getValue(FBLocation.class);
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
        private List<FBLocation> fbLocations = null;

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

            private final MenuItem.OnMenuItemClickListener onMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int idItem = item.getItemId();
                    int position = item.getOrder();
                    FBLocation fbLocation = fbLocations.get(position);

                    switch (idItem) {
                        case MENU_ITEM_EDIT:
                            Intent intent = new Intent(mContext, LocationDetailsActivity.class);
                            intent.putExtra(LocationDetailsActivity.LOCATION_DETAIL, fbLocation);
                            startActivity(intent);
                            break;

                        case MENU_ITEM_REMOVE:
                            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext);
                            materialAlertDialogBuilder.setTitle(R.string.confirm_title_remove);
                            materialAlertDialogBuilder.setMessage(R.string.confirm_content_remove);
                            materialAlertDialogBuilder.setPositiveButton(R.string.confirm_btn_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    fbLocation.remove();
                                }
                            });
                            materialAlertDialogBuilder.setNegativeButton(R.string.confirm_btn_no, null);
                            materialAlertDialogBuilder.show();
                            break;

                        default:
                            break;
                    }
                    return true;
                }
            };
        }

        public LocationsAdapter(List<FBLocation> fbLocations){
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
            FBLocation fbLocation = fbLocations.get(position);
            holder.mItemBind.txtLocationName.setText(fbLocation.name);
            holder.mItemBind.txtLocationDescription.setText(fbLocation.description);
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
            return fbLocations.size();
        }
    }
}