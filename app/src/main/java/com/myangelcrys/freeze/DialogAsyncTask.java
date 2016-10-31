package com.myangelcrys.freeze;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by cs on 16-10-31.
 */

class DialogAsyncTask extends AsyncTask<Object,Object,Object>{
    ProgressDialog dialog;
    Runnable runnable;
    public DialogAsyncTask(Context context,Runnable runnable){
        dialog=new ProgressDialog(context);
        dialog.setTitle("loading...");
        dialog.setCancelable(false);
        this.runnable=runnable;
    }

    public ProgressDialog getDialog() {
        return dialog;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        runnable.run();
        return null;
    }

    @Override
    protected void onPreExecute() {
        dialog.show();
    }

    @Override
    protected void onCancelled() {
        dialog.dismiss();
    }

    @Override
    protected void onPostExecute(Object o) {
        dialog.dismiss();
    }
}
