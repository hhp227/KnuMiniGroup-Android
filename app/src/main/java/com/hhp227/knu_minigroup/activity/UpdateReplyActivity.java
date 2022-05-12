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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.databinding.ActivityUpdateReplyBinding;
import com.hhp227.knu_minigroup.databinding.ModifyTextBinding;
import com.hhp227.knu_minigroup.viewmodel.UpdateReplyViewModel;

public class UpdateReplyActivity extends AppCompatActivity {
    private Holder mHolder;

    private ProgressDialog mProgressDialog;

    private ActivityUpdateReplyBinding mBinding;

    private UpdateReplyViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityUpdateReplyBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(UpdateReplyViewModel.class);
        mProgressDialog = new ProgressDialog(this);

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("전송중...");
        mBinding.rvWrite.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvWrite.setAdapter(new RecyclerView.Adapter<Holder>() {
            @NonNull
            @Override
            public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                mHolder = new Holder(ModifyTextBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
                return mHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull Holder holder, int position) {
                holder.bind(mViewModel.getReply());
            }

            @Override
            public int getItemCount() {
                return 1;
            }
        });
        mViewModel.mState.observe(this, new Observer<UpdateReplyViewModel.State>() {
            @Override
            public void onChanged(UpdateReplyViewModel.State state) {
                if (state.isLoading) {
                    showProgressDialog();
                } else if (state.text != null && !state.text.isEmpty()) {
                    Intent intent = new Intent(UpdateReplyActivity.this, ArticleActivity.class);

                    // 입력 자판 숨기기
                    View view = UpdateReplyActivity.this.getCurrentFocus();

                    if (view != null) {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    hideProgressDialog();
                    intent.putExtra("update_reply", state.text);
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (state.replyFormState != null) {
                    Snackbar.make(getCurrentFocus(), state.replyFormState.replyError, Snackbar.LENGTH_LONG).show();
                } else if (state.message != null && !state.message.isEmpty()) {
                    hideProgressDialog();
                    Snackbar.make(getCurrentFocus(), state.message, Snackbar.LENGTH_LONG).show();
                }
            }
        });
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
                final String text = mHolder.mBinding.etReply.getText().toString().trim();

                mViewModel.actionSend(text);
                return true;
        }
        return super.onOptionsItemSelected(item);
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

        public void bind(String reply) {
            mBinding.etReply.setText(reply);
        }
    }
}
