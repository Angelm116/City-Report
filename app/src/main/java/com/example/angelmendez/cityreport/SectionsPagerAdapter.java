package com.example.angelmendez.cityreport;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class  SectionsPagerAdapter extends FragmentPagerAdapter {

    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    // this method tells the viewpager which fragment to display
    @Override
    public Fragment getItem(int position) {

        switch(position){

            case 0:
                return new locationCheckFragment();

            case 1:
                return new DescriptionFragment();

            default:
                return new DescriptionFragment();
        }
    }


    // this method should return the total number of pages
    @Override
    public int getCount() {
        return 2;
    }
}