package net.thangvnnc.appmap.ui.stores.directions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import net.thangvnnc.appmap.R;
import net.thangvnnc.appmap.database.FBDirection;
import net.thangvnnc.appmap.database.FBLocation;
import net.thangvnnc.appmap.databinding.ActivityDirectionsBinding;
import net.thangvnnc.appmap.databinding.ActivityDirectionsItemBinding;
import net.thangvnnc.appmap.databinding.ActivityLocationsItemBinding;
import net.thangvnnc.appmap.ui.stores.locations.LocationDetailsActivity;
import net.thangvnnc.appmap.ui.stores.locations.LocationsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class DirectionsActivity extends AppCompatActivity {
    public static final String PACKAGE_BROADCAST = "net.thangvnnc.appmap.ui.stores.directions.DirectionsActivity";

    private static final String TAG = "DirectionsActivity";
    private ActivityDirectionsBinding mBind = null;
    private Context mContext = null;
    private final List<FBDirection> fbDirections = new ArrayList<>();
    private DirectionsAdapter mDirectionsAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mBind = ActivityDirectionsBinding.inflate(LayoutInflater.from(mContext));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(mBind.getRoot());
        initialize();
    }

    private void initialize() {
        initRcvDirections();
    }

    private void initRcvDirections() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mBind.rcvDirections.setLayoutManager(layoutManager);
        mDirectionsAdapter = new DirectionsAdapter(fbDirections);
        mBind.rcvDirections.setAdapter(mDirectionsAdapter);

        DividerItemDecoration itemDecorator = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(mContext, R.drawable.rcv_divider));
        mBind.rcvDirections.addItemDecoration(itemDecorator);
        ProgressDialog progressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.message_waiting));
        FBDirection.getAll().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fbDirections.clear();
                List<FBDirection> fbDirectionGets = new ArrayList<>();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    FBDirection fbDirection = postSnapshot.getValue(FBDirection.class);
                    fbDirectionGets.add(fbDirection);
                }
                fbDirections.addAll(fbDirectionGets);
                Collections.reverse(fbDirections);
                mDirectionsAdapter.notifyDataSetChanged();
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

    private class DirectionsAdapter extends RecyclerView.Adapter<DirectionsAdapter.DirectionsViewHolder> {
        private List<FBDirection> fbDirections = null;

        private class DirectionsViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
            private ActivityDirectionsItemBinding mItemBind = null;
            private final int MENU_ITEM_REMOVE = 2;

            public DirectionsViewHolder(ActivityDirectionsItemBinding mItemBind) {
                super(mItemBind.getRoot());
                this.mItemBind = mItemBind;
                this.mItemBind.getRoot().setOnCreateContextMenuListener(this);
            }

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                MenuItem btnRemoveMenuItem = menu.add(Menu.NONE, MENU_ITEM_REMOVE, getAdapterPosition(), R.string.locations_context_menu_item_remove);
                btnRemoveMenuItem.setOnMenuItemClickListener(onMenuItemClickListener);
            }

            private final MenuItem.OnMenuItemClickListener onMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int idItem = item.getItemId();
                    int position = item.getOrder();
                    FBDirection fbDirection = fbDirections.get(position);

                    switch (idItem) {
                        case MENU_ITEM_REMOVE:
                            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext);
                            materialAlertDialogBuilder.setTitle(R.string.confirm_title_remove);
                            materialAlertDialogBuilder.setMessage(R.string.confirm_content_remove);
                            materialAlertDialogBuilder.setPositiveButton(R.string.confirm_btn_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    fbDirection.remove();
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

        public DirectionsAdapter(List<FBDirection> fbDirections){
            this.fbDirections = fbDirections;
        }

        @NonNull
        @Override
        public DirectionsAdapter.DirectionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            ActivityDirectionsItemBinding binding = ActivityDirectionsItemBinding.inflate(LayoutInflater.from(mContext), parent, false);
            return new DirectionsAdapter.DirectionsViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(DirectionsAdapter.DirectionsViewHolder holder, int position){
            FBDirection fbDirection = fbDirections.get(position);
            String locationIdFirst = fbDirection.locations.get(0);

            FBLocation.getChild().child(locationIdFirst).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    FBLocation fbLocationFrom = snapshot.getValue(FBLocation.class);
                    holder.mItemBind.txtFromName.setText(fbLocationFrom.name);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            String locationIdLast = fbDirection.locations.get(fbDirection.locations.size() - 1);
            FBLocation.getChild().child(locationIdLast).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    FBLocation fbLocationTo = snapshot.getValue(FBLocation.class);
                    holder.mItemBind.txtToName.setText(fbLocationTo.name);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            holder.mItemBind.viewMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PACKAGE_BROADCAST);
                    intent.putExtra("directionId", fbDirection.id);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    finish();
                }
            });

            holder.mItemBind.viewMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Active event context menu
                    return false;
                }
            });
        }

        @Override
        public int getItemCount(){
            return fbDirections.size();
        }
    }
}