package com.example.angelmendez.cityreport;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {

    private static VolleySingleton mInstance;
    private RequestQueue queue;
    private VolleySingleton(Context context)
    {
        queue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized VolleySingleton getInstance(Context context)
    {
        if (mInstance == null)
        {
            mInstance = new VolleySingleton(context);
        }

        return mInstance;
    }

    public RequestQueue getQueue()
    {
        return queue;
    }
}
