package com.clinicalinformatics.app.fragments;
/**
 * 
 * @author Rheti Inc
 * 
 * Application class
 * 		
 * Created 2013 Sep 25
 *  
 */
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.clinicalinformatics.app.ClinicalApplication;
import com.clinicalinformatics.app.R;
import com.clinicalinformatics.app.activity.MainActivity;
import com.clinicalinformatics.app.db.DataBaseManager;

public class ListViewFragment extends ListFragment 
{	
	//	============================
	//	CONSTANTS
	//	============================
	
	// This tags are defined in layout
	private final static String DATABASE_FRAGMENT 	= "fragment_database";
	private final static String TABLE_FRAGMENT 		= "fragment_table";
	private final static String FIELD_FRAGMENT 		= "fragment_field";
	
	//	============================
	//	VARIABLES
	//	============================
	
	private MainActivity mMainActivity;
	private DataBaseManager mDatabaseManger;
	
	private ArrayAdapter<String> mAdapter;
	
	//	============================
	//	CONSTRUCTOR & METHODS
	//	============================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mMainActivity = (MainActivity) getActivity();

		// Database
		mDatabaseManger = DataBaseManager
				.getInstance((ClinicalApplication) getActivity().getApplicationContext());
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		
		String[] dbNames = mDatabaseManger.select();
		String[] tablesNames = new String[0];
		
		if (getTag().equals(DATABASE_FRAGMENT))
		{	
			if (dbNames.length == 0)
				mMainActivity.enableDatabaseButtons(false);
			
			mAdapter = new ArrayAdapter<String>(
					getActivity(),
					android.R.layout.simple_list_item_single_choice,
					dbNames);
			
			setListAdapter(mAdapter);

			mMainActivity.enableAddButton(mMainActivity.TABLE, false);
			getListView().setItemChecked(0, true);
		}
		else if (getTag().equals(TABLE_FRAGMENT))
		{
			if (dbNames.length > 0)
			{
				tablesNames = mDatabaseManger.select(dbNames[0]);
				mMainActivity.enableFieldButtons(false);
				mMainActivity.enableTableButtons(false);
				mMainActivity.enableAddButton("table", false);
				
				mAdapter = new ArrayAdapter<String>(
						getActivity(),
						android.R.layout.simple_list_item_single_choice,
						tablesNames);
				
				setListAdapter(mAdapter);	
			}
			
			if (tablesNames.length > 0)
				mMainActivity.enableAddButton("table", true);
				
			mMainActivity.enableAddButton("field", false);
			mMainActivity.enableTableButtons(false);
			mMainActivity.enableFieldButtons(false);
		}
		else if (getTag().equals(FIELD_FRAGMENT))
		{
			mMainActivity.enableFieldButtons(false);
			mMainActivity.enableAddButton("field", false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_listview, container, false);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) 
	{
		if (getTag().equals(DATABASE_FRAGMENT))
		{
			mMainActivity.enableAddButton("table", true);
			mMainActivity.enableAddButton("field", false);
			mMainActivity.enableDatabaseButtons(true);
			
			// Get all tables from selected database
			String[] tablesNames = mDatabaseManger.select(mDatabaseManger.select()[position]);
			
			mMainActivity.enableFieldButtons(false);
			mMainActivity.enableTableButtons(false);

			mMainActivity.refreshTableView(tablesNames, true);
			mMainActivity.refreshFieldView(new String[0], false);
		}
		else if (getTag().equals(TABLE_FRAGMENT))
		{
			mMainActivity.enableAddButton("field", true);
			mMainActivity.enableTableButtons(true);
			mMainActivity.enableFieldButtons(false);
			mMainActivity.setCurrentTable((l.getItemAtPosition(position).toString()));
		}
		else if (getTag().equals(FIELD_FRAGMENT))
		{
			mMainActivity.enableFieldButtons(true);
			mMainActivity.enableAddButton(mMainActivity.FIELD, true);
		}
	}
}
