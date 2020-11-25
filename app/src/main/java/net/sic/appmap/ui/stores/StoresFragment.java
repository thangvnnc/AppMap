package net.sic.appmap.ui.stores;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.sic.appmap.R;
import net.sic.appmap.databinding.FragmentStoresBinding;
import net.sic.appmap.databinding.FragmentStoresItemBinding;
import net.sic.appmap.ui.stores.directions.DirectionsActivity;
import net.sic.appmap.ui.stores.locations.LocationsActivity;

public class StoresFragment extends Fragment {
    private static final String TAG = "StoresFragment";
    private Context mContext = null;
    private FragmentStoresBinding mBind = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBind = FragmentStoresBinding.inflate(inflater, container, false);
        mContext = getContext();
        initialize();
        return mBind.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar supportActionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (supportActionBar != null){
            supportActionBar.show();
        }
    }


    private void initialize() {
        PopupMenu popupMenu = new PopupMenu(mContext, null);
        popupMenu.inflate(R.menu.store_menu);
        Menu menu = popupMenu.getMenu();
        initRcvStores(menu);
    }

    private void initRcvStores(Menu menu) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mBind.rcvStores.setLayoutManager(layoutManager);
        mBind.rcvStores.setAdapter(new StoresAdapter(menu));

        DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(mContext, R.drawable.divider_rcv));
        mBind.rcvStores.addItemDecoration(itemDecorator);
    }

    private class StoresAdapter extends RecyclerView.Adapter<StoresAdapter.StoresViewHolder> {
        private Menu menu = null;

        private class StoresViewHolder extends RecyclerView.ViewHolder{
            private FragmentStoresItemBinding mItemBind = null;

            public StoresViewHolder(FragmentStoresItemBinding mItemBind){
                super(mItemBind.getRoot());
                this.mItemBind = mItemBind;
            }
        }

        public StoresAdapter(Menu menu){
            this.menu = menu;
        }

        @NonNull
        @Override
        public StoresViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            return new StoresViewHolder(FragmentStoresItemBinding.inflate(getLayoutInflater()));
        }

        @Override
        public void onBindViewHolder(StoresViewHolder holder, int position){
            String title = menu.getItem(position).getTitle().toString();
            Drawable imgRowDraw = menu.getItem(position).getIcon();
            holder.mItemBind.txtTitle.setText(title);
            holder.mItemBind.imgRow.setImageDrawable(imgRowDraw);

            holder.mItemBind.viewMain.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public void onClick(View v) {
                    switch (menu.getItem(position).getItemId()) {
                        case R.id.store_menu_item_location:
                            Intent locationIntent = new Intent(mContext, LocationsActivity.class);
                            startActivity(locationIntent);
                            break;

                        case R.id.store_menu_item_direction:
                            Intent directionIntent = new Intent(mContext, DirectionsActivity.class);
                            startActivity(directionIntent);
                            break;

                        default:
                            throw new IllegalStateException("Unexpected value: " + menu.getItem(position).getItemId());
                    }

                }
            });
        }

        @Override
        public int getItemCount(){
            return menu.size();
        }
    }

}