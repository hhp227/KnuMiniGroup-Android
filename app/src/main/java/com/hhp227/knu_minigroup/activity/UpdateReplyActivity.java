package com.hhp227.knu_minigroup.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.databinding.ActivityUpdateReplyBinding;
import com.hhp227.knu_minigroup.databinding.ModifyTextBinding;
import com.hhp227.knu_minigroup.viewmodel.UpdateReplyViewModel;

public class UpdateReplyActivity extends AppCompatActivity {
    private ProgressDialog mProgressDialog;

    private ActivityUpdateReplyBinding mBinding;

    private UpdateReplyViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_update_reply);
        mViewModel = new ViewModelProvider(this).get(UpdateReplyViewModel.class);
        mProgressDialog = new ProgressDialog(this);

        setAppBar(mBinding.toolbar);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("전송중...");
        mBinding.rvWrite.setAdapter(new RecyclerView.Adapter<Holder>() {
            @NonNull
            @Override
            public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ModifyTextBinding binding = ModifyTextBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

                binding.setLifecycleOwner(UpdateReplyActivity.this);
                binding.setViewModel(mViewModel);
                return new Holder(binding);
            }

            @Override
            public void onBindViewHolder(@NonNull Holder holder, int position) {
                holder.bind();
            }

            @Override
            public int getItemCount() {
                return 1;
            }
        });
        observeViewModelData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.write, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send:
                String text = mViewModel.text.getValue();

                mViewModel.actionSend(text);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setAppBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void observeViewModelData() {
        mViewModel.isLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (isLoading) {
                    showProgressDialog();
                } else {
                    hideProgressDialog();
                }
            }
        });
        mViewModel.getReply().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String reply) {
                if (!reply.isEmpty()) {
                    Intent intent = new Intent(UpdateReplyActivity.this, ArticleActivity.class);

                    // 입력 자판 숨기기
                    View view = UpdateReplyActivity.this.getCurrentFocus();

                    if (view != null) {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    intent.putExtra("update_reply", reply);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
        mViewModel.getMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    Snackbar.make(getCurrentFocus(), message, Snackbar.LENGTH_LONG).show();
                }
            }
        });
        mViewModel.getReplyError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String replyError) {
                Snackbar.make(getCurrentFocus(), replyError, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void showProgressDialog() {
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private final ModifyTextBinding mBinding;

        Holder(ModifyTextBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind() {
            mBinding.executePendingBindings();
        }
    }
}
