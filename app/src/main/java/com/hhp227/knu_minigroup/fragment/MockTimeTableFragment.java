package com.hhp227.knu_minigroup.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hhp227.knu_minigroup.databinding.FragmentMockTimetableBinding;
import com.hhp227.knu_minigroup.databinding.TimetableInputDigBinding;
import com.hhp227.knu_minigroup.dto.TimetableItem;
import com.hhp227.knu_minigroup.ui.TimetableView;
import com.hhp227.knu_minigroup.viewmodel.MockTimeTableViewModel;

public class MockTimeTableFragment extends Fragment {
    public static final String TAG = "시간표";

    private MockTimeTableViewModel mViewModel;

    private FragmentMockTimetableBinding mBinding;

    public MockTimeTableFragment() {
        // Required empty public constructor
    }

    public static MockTimeTableFragment newInstance() {
        return new MockTimeTableFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentMockTimetableBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(MockTimeTableViewModel.class);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.timetableView.setOnCellClickListener(new TimetableView.OnCellClickListener() {
            @Override
            public void onCellClick(int id) {
                TimetableItem timetableItem = mViewModel.getTimetableItem(id);

                if (timetableItem != null) {
                    showUpdateTimetableDialog(timetableItem);
                } else {
                    showAddTimetableDialog(id);
                }
            }
        });
        observeViewModelData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void observeViewModelData() {
        mViewModel.getMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /*데이터가 없는 곳을 클릭했을 때 띄어주는 다이얼로그*/
    private void showAddTimetableDialog(final int id) {
        final TimetableInputDigBinding binding = TimetableInputDigBinding.inflate(getLayoutInflater());

        new AlertDialog.Builder(requireContext())
                .setTitle("시간표")
                .setView(binding.getRoot())
                .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mViewModel.addTimetable(id, binding.inputSubject.getText().toString(), binding.inputClassroom.getText().toString());
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    /*클릭한 곳에 데이터가 있을 경우 띄어주는 수정가능한 다이얼로그*/
    private void showUpdateTimetableDialog(final TimetableItem timetableItem) {
        final TimetableInputDigBinding binding = TimetableInputDigBinding.inflate(getLayoutInflater());

        binding.inputSubject.setText(timetableItem.subject);
        binding.inputClassroom.setText(timetableItem.classroom);
        // 강의명, 강의실을 적는 창을 각각 클릭했을 때 출력된 데이터를 지워준다.
        binding.inputSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.inputSubject.setText(null);
            }
        });
        binding.inputClassroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.inputClassroom.setText(null);
            }
        });
        new AlertDialog.Builder(requireContext())
                .setTitle("TimeTable")
                .setView(binding.getRoot())
                .setPositiveButton("수정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mViewModel.updateTimetable(timetableItem.id, binding.inputSubject.getText().toString(), binding.inputClassroom.getText().toString());
                    }
                })
                .setNegativeButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mViewModel.deleteTimetable(timetableItem.id);
                    }
                })
                .show();
    }
}
