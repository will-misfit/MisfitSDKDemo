package com.misfit.misfitsdkdemo.view;

public interface Preference<T> {
    void setTitle(String title);

    void setValue(T value);

    T getValue();
}
