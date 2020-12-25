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
import net.thangvnnc.appmap.database.FBLocation;
import net.thangvnnc.appmap.databinding.ActivityLocationDetailBinding;

import java.util.Date;

import static net.thangvnnc.appmap.database.FirebaseDB.generalId;

public class LocationDetailsActivity extends AppCompatActivity {
    public static final String TAG  = "LocationDetailsActivity";
    public static final String LOCATION_DETAIL  = "LOCATION_DETAIL";
    private FBLocation fbLocationIntent = null;
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
        fbLocationIntent = (FBLocation) getIntent().getSerializableExtra(LOCATION_DETAIL);
        mBind.btnSave.setOnClickListener(btnSaveClick);

        // Edit
        if (fbLocationIntent.id != null) {
            mBind.edtLocationName.setText(fbLocationIntent.name);
            mBind.edtLocationDescription.setText(fbLocationIntent.description);
        }
    }

    private final View.OnClickListener btnSaveClick = new View.OnClickListener() {
        @Override
        public void onClick(android.view.View v) {
            saveLocation();
        }
    };

    private void saveLocation() {
        long sessionUserId = 0;
        fbLocationIntent.name = mBind.edtLocationName.getText().toString();
        fbLocationIntent.description = mBind.edtLocationDescription.getText().toString();
        fbLocationIntent.isUsing = true;
        fbLocationIntent.updatedBy = sessionUserId;
        fbLocationIntent.updatedAt = new Date();

        // Insert new
        if (fbLocationIntent.id == null) {
            fbLocationIntent.id = generalId();
            fbLocationIntent.createdBy = sessionUserId;
            fbLocationIntent.createdAt = new Date();
        }

        ProgressDialog progressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.message_waiting));
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext);
        fbLocationIntent.insertOrUpdate().addOnSuccessListener(new OnSuccessListener<Void>() {
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