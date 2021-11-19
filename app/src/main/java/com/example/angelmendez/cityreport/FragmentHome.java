package com.example.angelmendez.cityreport;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


public class FragmentHome extends Fragment {


    private RecyclerView recyclerView;
    private RecyclerListAdapter adapter;
    ArrayList<ReportObject> reportObject;

    public FragmentHome(ArrayList<ReportObject> reportObject) {

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
                // Check that the User's last known location is not equal to null
                if(((MainActivity) getActivity()).getUserLastKnownLocation() != null)
                {
                    ((MainActivity) getActivity()).startNewReport();
                }
                else
                {
                    Toast.makeText(getActivity(), "Please turn on" + " your wifi connection...", Toast.LENGTH_LONG).show();
                }

            }
        });

        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        adapter = new RecyclerListAdapter(reportObject, getContext(), recyclerView, (MainActivity)this.getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
       // getReports();

        // Inflate the layout for this fragment
        return view;
    }

    public void updateReportsList(ArrayList<ReportObject> reports)
    {
        Log.d("Method", "on ReportsFragment: updateReportsList()");
        Log.d("Method", "on ReportsFragment: updateReportsList(), list size:" + reports.size());

        adapter.setData(reports);
        adapter.notifyDataSetChanged();

        // Moves the list to the last report
        recyclerView.post(new Runnable() {
            @Override
            public void run() {

                if (adapter.getItemCount() > 0) {
                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                }
            }
        });

        Log.d("Method", "out ReportsFragment: updateReportsList()");
    }


}