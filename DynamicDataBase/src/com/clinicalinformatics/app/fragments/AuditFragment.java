package com.clinicalinformatics.app.fragments;
/**
 * 
 * @author Rheti Inc
 * 
 * Application class
 * 		
 * Created 2013 Sep 26
 *  
 */
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.clinicalinformatics.app.ClinicalApplication;
import com.clinicalinformatics.app.R;
import com.clinicalinformatics.app.adapters.AuditAdapter;
import com.clinicalinformatics.app.model.AuditModel;

public class AuditFragment extends ListFragment 
{
	//	============================
	//	Variables
	//	============================

	private AuditAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_grid_layout, container,false);
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		getListView().setDividerHeight(0);
		getListView().setDivider(null);

		ClinicalApplication mApp = (ClinicalApplication) getActivity().getApplicationContext();
		ArrayList<AuditModel> list_audit = mApp.getCurrentAuditDataBase().selectAll();
		mAdapter = new AuditAdapter(getActivity(), list_audit);
		setListAdapter(mAdapter);

		// Components
		getActivity().findViewById(R.id.btnAdd).setVisibility(View.GONE);
		getActivity().findViewById(R.id.btnEdit).setVisibility(View.GONE);
		getActivity().findViewById(R.id.btnDelete).setVisibility(View.GONE);
		getActivity().findViewById(R.id.btnCancel).setVisibility(View.GONE);

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

	}
}
