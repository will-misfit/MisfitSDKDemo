package com.misfit.misfitsdkdemo.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.misfit.misfitsdkdemo.R;

import org.houxg.rangebar.RangeBar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RangePreference extends LinearLayout implements RangeBar.ProgressChangedListener, Preference<Integer> {

    @BindView(R.id.tv_title)
    TextView mTitleTv;
    @BindView(R.id.rangebar)
    RangeBar mRangeBar;
    @BindView(R.id.tv_value)
    TextView mValueTv;

    private String[] mValues;

    public RangePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RangePreference);
        int max = array.getInt(R.styleable.RangePreference_range_max, 1);
        int min = array.getInt(R.styleable.RangePreference_range_min, 0);
        String title = array.getString(R.styleable.RangePreference_title_text);
        array.recycle();
        LayoutInflater.from(context).inflate(R.layout.range_preference, this, true);
        ButterKnife.bind(this);
        mTitleTv.setText(title);
        mRangeBar.setMin(min);
        mRangeBar.setMax(max);
        mRangeBar.setProgressChangedListener(this);
    }

    public void setValues(String[] values) {
        this.mValues = values;
    }

    @Override
    public void onProgressChanged(RangeBar rangeBar, int i) {
        if (mValues == null) {
            mValueTv.setText(String.valueOf(i));
        } else {
            try {
                mValueTv.setText(mValues[i - rangeBar.getMin()]);
            } catch (Exception e) {
                mValueTv.setText("OUT_OF_RANGE");
            }
        }
    }

    @Override
    public void setTitle(String title) {
        mTitleTv.setText(title);
    }

    @Override
    public void setValue(Integer value) {
        mRangeBar.setProgress(value);
    }

    @Override
    public Integer getValue() {
        return mRangeBar.getProgress();
    }
}
