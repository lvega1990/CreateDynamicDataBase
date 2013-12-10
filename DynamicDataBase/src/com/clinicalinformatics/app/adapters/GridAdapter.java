package com.clinicalinformatics.app.adapters;
/**
 * 
 * @author Rheti Inc
 * 
 * Application class
 * 		
 * Created 2013 Sep 25
 *  
 */
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.clinicalinformatics.app.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<JSONArray> mValues;
	private int mCount = 0;
	private int mPositionSelected = -1;
	private View mSelectedView;
	public GridAdapter(Context context, ArrayList<JSONArray> values)
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
	public Object getItem(int position) {
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
		if (mPositionSelected > 0)
			this.mSelectedView.setBackgroundColor(Color.CYAN);
	}

	public int getSelectedPosition()
	{
		return this.mPositionSelected;
	}

	public JSONArray getSelectedJSONValues()
	{
		return mValues.get(mPositionSelected - 1);
	}

	public String getSelectedId()
	{
		try {
			String text = mValues.get(mPositionSelected - 1).getString(0);
			return text.substring(1, text.length()-1);
		} catch (JSONException e) {
			return null;
		}
	}

    @Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		LinearLayout row = new LinearLayout(mContext);
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setMinimumHeight(40);
		try {
			if(position == 0 && mCount>0)
			{
				JSONArray json = (JSONArray) getItem(position);

				for(int i = 0; i<json.length();i++)
				{
					JSONObject obj = json.getJSONObject(i);
					TextView columnView = new TextView(mContext);
					columnView.setWidth(200);
					columnView.setMinimumHeight(40);
					columnView.setTypeface(null, Typeface.BOLD);
					columnView.setTextColor(Color.parseColor("#000000"));
					columnView.setGravity(Gravity.CENTER);
					columnView.setText(obj.keys().next().toString());
					columnView.setEllipsize(TruncateAt.END);
					columnView.setSingleLine(true);
					columnView.setBackgroundResource(R.drawable.cell_background);
					row.addView(columnView);
				}
			}
			else
			{
				JSONArray json = (JSONArray) getItem(position-1);
				for(int i = 0; i<json.length();i++)
				{
					JSONObject obj = json.getJSONObject(i);
					TextView columnView = new TextView(mContext);
					columnView.setWidth(200);
					columnView.setTextColor(Color.parseColor("#000000"));
					columnView.setMinimumHeight(40);
					columnView.setText(obj.get(obj.keys().next().toString()).toString());
					columnView.setEllipsize(TruncateAt.END);
					columnView.setSingleLine(true);
					columnView.setBackgroundResource(R.drawable.cell_background);
					row.addView(columnView);
				}
			}
		} catch (JSONException e) {
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
