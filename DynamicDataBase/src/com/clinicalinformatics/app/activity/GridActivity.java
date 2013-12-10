package com.clinicalinformatics.app.activity;
/**
 * 
 * @author Rheti Inc
 * 
 * Application class
 * 		
 * Created 2013 Sep 26
 *  
 */
import com.clinicalinformatics.app.R;
import com.clinicalinformatics.app.fragments.GridFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class GridActivity extends FragmentActivity 
{	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_grid_layout);
		
		GridFragment gridFragment = new GridFragment();
		Bundle b = new Bundle();
		b.putString(GridFragment.DATA_BASE_NAME,  getIntent().getStringExtra(GridFragment.DATA_BASE_NAME));
		b.putString(GridFragment.TABLE_NAME, getIntent().getStringExtra(GridFragment.TABLE_NAME));
		gridFragment.setArguments(b);
		getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer,gridFragment, "grid").commit();
	}
}
