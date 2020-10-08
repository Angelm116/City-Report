package com.example.angelmendez.cityreport;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;


public class ReportsFragment extends Fragment {


    private RecyclerView recyclerView;
    private ListAdapter adapter;
    ArrayList<ReportObject> reportObject;

    public ReportsFragment(ArrayList<ReportObject> reportObject) {
        // Required empty public constructor
        this.reportObject = reportObject;

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).startNewReport();
            }
        });

        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        adapter = new ListAdapter(reportObject);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
       // getReports();

        // Inflate the layout for this fragment
        return view;
    }

    public void updateReports(ArrayList<ReportObject> reports)
    {
        Log.d("updateReports", "updateReports: " + reports.size());
        adapter.setData(reports);
        adapter.notifyDataSetChanged();
    }


}