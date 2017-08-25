package com.wang.bookloadingview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.wang.bookloading.widget.BookLoadingView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.book_loading_view)
    BookLoadingView mBookLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.show_btn)
    public void onMShowBtnClicked() {
        //start loading
        mBookLoadingView.setVisibility(View.VISIBLE, 0);
    }

    @OnClick(R.id.hide_btn)
    public void onMHideBtnClicked() {
        //stop loading
        mBookLoadingView.setVisibility(View.GONE);
    }
}
