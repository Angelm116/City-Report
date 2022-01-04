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
import java.util.Set;


public class FragmentHome extends Fragment {


    private RecyclerView recyclerView;
    private RecyclerListAdapter adapter;
    ArrayList<ReportObject> reportObjectList;

    public FragmentHome(ArrayList<ReportObject> reportObjectList) {

        this.reportObjectList = reportObjectList;
    }


    // This function is called when the fragment displays
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout of this fragment
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        // Set up the floating action button
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

        // set up RecyclerView
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext())); // TODO IS THIS NEEDED?

        // set up the adapter for the RecyclerView
        adapter = new RecyclerListAdapter(reportObjectList, getContext(), recyclerView, (MainActivity)this.getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        return view;
    }

    // Called whenever a new report is added
    // updates the list to reflect the change in the list of reports
    public void updateReportsList(ArrayList<ReportObject> reports)
    {


        adapter.setData(reports);
        adapter.notifyDataSetChanged();

        // Move the list to the last report
        recyclerView.post(new Runnable() {
            @Override
            public void run() {

                if (adapter.getItemCount() > 0) {
                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                }

            }
        });
    }


}