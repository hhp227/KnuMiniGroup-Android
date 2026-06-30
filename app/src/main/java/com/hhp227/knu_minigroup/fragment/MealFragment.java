package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.activity.MainActivity;
import com.hhp227.knu_minigroup.databinding.FragmentMealBinding;
import com.hhp227.knu_minigroup.viewmodel.MealViewModel;

import java.util.List;

public class MealFragment extends Fragment {
    private MealViewModel mViewModel;

    private FragmentMealBinding mBinding;

    public static MealFragment newInstance() {
        return new MealFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentMealBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(MealViewModel.class);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<MealViewModel.MealPage> mealPages = mViewModel.getMealPages();

        mBinding.setViewModel(mViewModel);
        mBinding.executePendingBindings();
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return createMealFragment(mealPages.get(position));
            }

            @Override
            public int getCount() {
                return mealPages.size();
            }
        };

        ((MainActivity) requireActivity()).setAppBar(mBinding.toolbar, getString(R.string.meal));
        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mBinding.viewPager));
        mBinding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mBinding.tabLayout));
        mBinding.viewPager.setAdapter(adapter);
    }

    private Fragment createMealFragment(MealViewModel.MealPage mealPage) {
        if (mealPage.isStudentMeal()) {
            return StudentMealFragment.newInstance(mealPage.getStudentMealId());
        } else if (mealPage.isDCDormMeal()) {
            return DCDormMealFragment.newInstance();
        } else if (mealPage.isBTLDormMeal()) {
            return BTLDormMealFragment.newInstance();
        } else if (mealPage.isSCDormMeal()) {
            return SCDormMealFragment.newInstance();
        }
        throw new IllegalArgumentException("Unknown meal page: " + mealPage.getTitle());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.viewPager.clearOnPageChangeListeners();
        mBinding.tabLayout.clearOnTabSelectedListeners();
        mBinding.tabLayout.removeAllTabs();
        mBinding = null;
    }
}
