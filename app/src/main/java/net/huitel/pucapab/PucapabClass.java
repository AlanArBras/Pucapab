package net.huitel.pucapab;

import android.content.Context;

/**
 * Created by root on 1/22/18.
 */

public class PucapabClass {
    private Context context;

    public PucapabClass(Context context){
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context){
        this.context = context;
    }
}
