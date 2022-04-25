package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hhp227.knu_minigroup.databinding.FragmentDormmealBinding;
import com.hhp227.knu_minigroup.viewmodel.DCDormMealViewModel;

public class DCDormMealFragment extends Fragment {
    private TextView[] mMenuView;

    private FragmentDormmealBinding mBinding;

    public static DCDormMealFragment newInstance() {
        return new DCDormMealFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentDormmealBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DCDormMealViewModel viewModel = new ViewModelProvider(this).get(DCDormMealViewModel.class);
        mMenuView = new TextView[] {
                mBinding.breakfast,
                mBinding.lunch,
                mBinding.dinner
        };

        viewModel.mState.observe(getViewLifecycleOwner(), new Observer<DCDormMealViewModel.State>() {
            @Override
            public void onChanged(DCDormMealViewModel.State state) {
                if (state.isLoading) {
                    showProgressBar();
                } else if (!state.list.isEmpty()) {
                    hideProgressBar();
                    for (int i = 0; i < mMenuView.length; i++)
                        mMenuView[i].setText(state.list.get(i));
                } else if (state.message != null && !state.message.isEmpty()) {
                    hideProgressBar();
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void showProgressBar() {
        if (mBinding.progressBar.getVisibility() == View.GONE)
            mBinding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.progressBar.getVisibility() == View.VISIBLE)
            mBinding.progressBar.setVisibility(View.GONE);
    }
}
