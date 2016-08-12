package com.misfit.misfitsdkdemo;


import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

public class DialogUtils {
    public static void showSlectionDialog(Context context, String[] selections, String title, DialogInterface.OnClickListener listener) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.select_dialog_item,
                selections);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setAdapter(adapter, listener).show();
    }
}
