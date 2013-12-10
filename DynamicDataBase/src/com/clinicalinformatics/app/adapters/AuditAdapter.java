package com.clinicalinformatics.app.adapters;
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

import com.clinicalinformatics.app.R;
import com.clinicalinformatics.app.model.AuditModel;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AuditAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<AuditModel> mValues;
	private int mCount = 0;
	private int mPositionSelected = -1;
	private View mSelectedView;
	public AuditAdapter(Context context, ArrayList<AuditModel> values)
	{
		this.mContext = context;
		this.mValues = values;
		if(values.isEmpty())
			mCount = 0;
		else
			mCount = values.size()+1;
	}

	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public AuditModel getItem(int position) {
		return mValues.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setPositionSelected(int positionSelected,View view) {
		this.mPositionSelected = positionSelected;
		if(mSelectedView!=null)
			mSelectedView.setBackgroundColor(Color.WHITE);
		this.mSelectedView = view;
		this.mSelectedView.setBackgroundColor(Color.CYAN);
	}

	public int getSelectedPosition()
	{
		return this.mPositionSelected;
	}

	public AuditModel getSelectedAuditModel()
	{
		return mValues.get(mPositionSelected-1);
	}



	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		LinearLayout row = new LinearLayout(mContext);
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setMinimumHeight(40);
		if (position == 0){
			//Id
			TextView columnView = new TextView(mContext);
			columnView.setWidth(200);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText("_Id");
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
			//CompID
			columnView = new TextView(mContext);
			columnView.setWidth(200);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText("CompId");
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
			//database
			columnView = new TextView(mContext);
			columnView.setWidth(200);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText("dbase");
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
			//Table
			columnView = new TextView(mContext);
			columnView.setWidth(200);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText("dtable");
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
			//Field
			columnView = new TextView(mContext);
			columnView.setWidth(200);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText("dtfield");
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
			//Action
			columnView = new TextView(mContext);
			columnView.setWidth(1300);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText("action");
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
			//Origination
			columnView = new TextView(mContext);
			columnView.setWidth(300);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText("origination");
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
			//termination
			columnView = new TextView(mContext);
			columnView.setWidth(300);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText("termination");
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
		}else{
			AuditModel model = getItem(position-1);
			//Id
			TextView columnView = new TextView(mContext);
			columnView.setWidth(200);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText(String.valueOf(model.get_id()));
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
			//CompID
			columnView = new TextView(mContext);
			columnView.setWidth(200);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText(String.valueOf(model.getCompID()));
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
			//database
			columnView = new TextView(mContext);
			columnView.setWidth(200);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText(String.valueOf(model.getDbase()));
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
			//Table
			columnView = new TextView(mContext);
			columnView.setWidth(200);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText(model.getDtable());
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
			//Field
			columnView = new TextView(mContext);
			columnView.setWidth(200);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText(model.getDtfield());
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
			//Action
			columnView = new TextView(mContext);
			columnView.setWidth(1300);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText(model.getAction());
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
			//Origination
			columnView = new TextView(mContext);
			columnView.setWidth(300);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText(model.getOrigination());
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
			//termination
			columnView = new TextView(mContext);
			columnView.setWidth(300);
			columnView.setTextColor(Color.parseColor("#000000"));
			columnView.setMinimumHeight(40);
			columnView.setText(model.getTermination());
			columnView.setEllipsize(TruncateAt.END);
			columnView.setSingleLine(true);
			columnView.setBackgroundResource(R.drawable.cell_background);
			row.addView(columnView);
		}
		
		if(mPositionSelected > 0 && mPositionSelected==position )
		{
			mSelectedView = row;
			row.setBackgroundColor(Color.parseColor("#D8F0FC"));
		}
		else{
			row.setBackgroundColor(Color.parseColor("#FFFFFF"));
		}


		return row;
	}

	public int getPositionSelected() 
	{
		return mPositionSelected;
	}

}
