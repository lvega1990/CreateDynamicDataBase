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
import com.clinicalinformatics.app.fragments.AuditFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class AuditActivity extends FragmentActivity 
{	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_grid_layout);
		
		AuditFragment auditFragment = new AuditFragment();
		getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer,auditFragment, "grid").commit();
	}
}
