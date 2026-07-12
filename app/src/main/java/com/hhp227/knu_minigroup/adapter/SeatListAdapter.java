package com.hhp227.knu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hhp227.knu_minigroup.databinding.SeatItemBinding;
import com.hhp227.knu_minigroup.dto.SeatItem;

import java.util.ArrayList;
import java.util.List;

public class SeatListAdapter extends RecyclerView.Adapter<SeatListAdapter.SeatListHolder> {
    private final List<SeatItem> mSearItemList = new ArrayList<>();

    @NonNull
    @Override
    public SeatListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SeatListHolder(SeatItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SeatListHolder holder, int position) {
        holder.bind(mSearItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return mSearItemList.size();
    }

    public void submitList(List<SeatItem> seatItemList) {
        mSearItemList.clear();
        mSearItemList.addAll(seatItemList);
        notifyDataSetChanged();
    }

    public static class SeatListHolder extends RecyclerView.ViewHolder {
        private final SeatItemBinding mBinding;

        SeatListHolder(SeatItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        private void bind(SeatItem seatItem) {
            mBinding.setSeatItem(seatItem);
            mBinding.executePendingBindings();
        }
    }
}
