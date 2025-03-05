package com.hhp227.knu_minigroup.fragment;

import static com.hhp227.knu_minigroup.viewmodel.StudentMealViewModel.KEY_BREAKFAST;
import static com.hhp227.knu_minigroup.viewmodel.StudentMealViewModel.KEY_DINNER;
import static com.hhp227.knu_minigroup.viewmodel.StudentMealViewModel.KEY_LAUNCH;

import android.os.Bundle;
import android.text.Html;
import android.util.Pair;
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
import com.hhp227.knu_minigroup.viewmodel.StudentMealViewModel;

import java.util.List;
import java.util.Map;

// TODO
public class StudentMealFragment extends Fragment {
    private Pair<String, TextView>[] mMenuView;

    private FragmentDormmealBinding mBinding;

    public static StudentMealFragment newInstance(int id) {
        StudentMealFragment fragment = new StudentMealFragment();
        Bundle args = new Bundle();

        args.putInt("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentDormmealBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StudentMealViewModel viewModel = new ViewModelProvider(this).get(StudentMealViewModel.class);
        mMenuView = new Pair[] {
                new Pair<>(KEY_BREAKFAST, mBinding.breakfast),
                new Pair<>(KEY_LAUNCH, mBinding.lunch),
                new Pair<>(KEY_DINNER, mBinding.dinner)
        };
        viewModel.getState().observe(getViewLifecycleOwner(), new Observer<StudentMealViewModel.State>() {
            @Override
            public void onChanged(StudentMealViewModel.State state) {
                if (state.isLoading) {
                    showProgressBar();
                } else if (!state.list.isEmpty()) {
                    hideProgressBar();
                    setTextView(StudentMealViewModel.groupBy(state.list));
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

    private void setTextView(Map<String, List<Pair<String, String>>> stringListMap) {
        for (Pair<String, TextView> pair: mMenuView) {
            pair.second.setText(Html.fromHtml(extractText(pair.first, stringListMap)));
        }
    }

    private String extractText(String key, Map<String, List<Pair<String, String>>> stringListMap) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Pair<String, String>> stringPairMap = stringListMap.get(key);

        if (stringPairMap != null) {
            for (Pair<String, String> pair : stringPairMap) {
                stringBuilder.append(pair.second);
            }
        }
        return stringBuilder.toString();
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
