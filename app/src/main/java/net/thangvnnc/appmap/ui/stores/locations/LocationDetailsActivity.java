package net.thangvnnc.appmap.ui.stores.locations;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.thangvnnc.appmap.R;
import net.thangvnnc.appmap.common.DateUtils;
import net.thangvnnc.appmap.database.Location;
import net.thangvnnc.appmap.database.User;
import net.thangvnnc.appmap.database.firebase.FBLocation;
import net.thangvnnc.appmap.databinding.ActivityLocationDetailBinding;

import java.util.Date;

public class LocationDetailsActivity extends AppCompatActivity {
    public static final String TAG  = "LocationDetailsActivity";
    public static final String LOCATION_DETAIL  = "LOCATION_DETAIL";
    private Location locationIntent = null;
    private ActivityLocationDetailBinding mBind = null;
    private Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mBind = ActivityLocationDetailBinding.inflate(LayoutInflater.from(mContext));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(mBind.getRoot());
        initialize();
    }

    private void initialize() {
        locationIntent = (Location) getIntent().getSerializableExtra(LOCATION_DETAIL);
        mBind.btnSave.setOnClickListener(btnSaveClick);

        // Edit
        if (locationIntent.id != null) {
            mBind.edtLocationName.setText(locationIntent.name);
            mBind.edtLocationDescription.setText(locationIntent.description);
        }
    }

    private final View.OnClickListener btnSaveClick = new View.OnClickListener() {
        @Override
        public void onClick(android.view.View v) {
            saveLocation();
        }
    };

    private void saveLocation() {
        // Insert new
        if (locationIntent.id == null) {
            locationIntent.id = Location.generalId();
            locationIntent.createdBy = User.getSession().id;
            locationIntent.createdAt = DateUtils.getCurrent();
        }

        locationIntent.name = mBind.edtLocationName.getText().toString();
        locationIntent.description = mBind.edtLocationDescription.getText().toString();
        locationIntent.isUsing = true;
        locationIntent.updatedBy = User.getSession().id;
        locationIntent.updatedAt = DateUtils.getCurrent();

        ProgressDialog progressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.message_waiting));
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext);
        FBLocation.insertOrUpdate(locationIntent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                materialAlertDialogBuilder.setMessage(R.string.message_success);
                materialAlertDialogBuilder.setPositiveButton(R.string.alert_dialog_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        throw new IllegalStateException("Unexpected value: " + item.getItemId());
    }
}