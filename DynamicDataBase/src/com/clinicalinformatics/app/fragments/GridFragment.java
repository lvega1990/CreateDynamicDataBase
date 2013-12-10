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
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.clinicalinformatics.app.ClinicalApplication;
import com.clinicalinformatics.app.R;
import com.clinicalinformatics.app.adapters.GridAdapter;
import com.clinicalinformatics.app.db.DataBaseManager;
import com.clinicalinformatics.app.db.TransactionManager;
import com.clinicalinformatics.app.helper.FieldHelper;
import com.clinicalinformatics.app.model.FieldModel;
import com.clinicalinformatics.app.model.FieldValues;
import com.clinicalinformatics.app.model.UpdateValues;

public class GridFragment extends ListFragment 
{
	//	============================
	//	Constants
	//	============================
	
	public final static String DATA_BASE_NAME 	= "dbName";		// Key to get database argument
	public final static String TABLE_NAME 		= "tName";		// Key to get table argument
	
	// Dialog titles
	public final static String TITLE_INSERT_ROW = "Insert Row";		// Insert title for dialog
	
	// Simple messages
	public final static String MSG_CREATE_FIELDS = "Create a new field first before insert a row";	// Message if user press insert and no are any fields created
	
	//	============================
	//	Variables
	//	============================
	
	// Components names
	private String mDBName;			// Current database
	private String mTableName;		// Current table

	private DataBaseManager mDatabaseManager;	// Manage the database

	// Views
	private Button mInsert;			// Button to insert new row (grid dialog)
	private Button mEdit;			// Button to edit selected row (grid dialog)
	private Button mDelete;			// Button to delete selected row (grid dialog)
	private Button mCancel;			// Button to dismiss grid dialog

	private String[] mFields;		// All fields of current (database, table)
	private Activity mActivity;		// Store activity references (dialog purpose)

	// Adapter
	private GridAdapter mAdapter;	// Store current adapter

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
		mDBName = getArguments().getString(DATA_BASE_NAME);
		mTableName = getArguments().getString(TABLE_NAME);

		mActivity = getActivity();

		mDatabaseManager = DataBaseManager
				.getInstance((ClinicalApplication) getActivity().getApplicationContext());

		// get fields
		mFields = mDatabaseManager.select(mDBName, mTableName);
		ArrayList<JSONArray> test = mDatabaseManager.selectData(mDBName, mTableName);

		// set adapter
		mAdapter = new GridAdapter(getActivity(), test);
		setListAdapter(mAdapter);

		// Components
		mInsert = (Button) getActivity().findViewById(R.id.btnAdd);
		mEdit 	= (Button) getActivity().findViewById(R.id.btnEdit);
		mDelete = (Button) getActivity().findViewById(R.id.btnDelete);
		mCancel = (Button) getActivity().findViewById(R.id.btnCancel);

		// Listeners
		mInsert.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mFields.length != 0)
					insertDialog().show();
				else
					showAlert(TITLE_INSERT_ROW, MSG_CREATE_FIELDS);
			}
		});
		mEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) 
			{
				/**
				 * Retrieve values from selected row, then set them into respective
				 * fields inside the dialog
				 */
				if (mAdapter.getSelectedPosition() != -1 && mAdapter.getSelectedPosition() != 0)
				{
					JSONArray jsonArray = mAdapter.getSelectedJSONValues();

					mFields = mDatabaseManager.select(mDBName, mTableName);
					int Id=0;
					ArrayList<String> fields = new ArrayList<String>();
					ArrayList<String> values = new ArrayList<String>();

					try {
						for (int i = 0; i < jsonArray.length(); i++)
						{
							String json = jsonArray.getJSONObject(i).toString();
							json = json.substring(1, json.length() - 1);
							FieldValues values_ = FieldHelper.StringToFieldValues(json);
							if (values_.getName().equals(TransactionManager.DEFAULT_FIELD_KEY)){
								Id = Integer.parseInt(values_.getValue().toString());
							}
							for (int j=0;j < mFields.length;j++){
								if (FieldHelper.StringToFieldModel(mFields[j]).getName().equals(values_.getName())){
									fields.add(mFields[j]);
									values.add(values_.getValue().toString());
								}
							}
						}
						editDialog(fields, values,Id).show();
					} catch (JSONException e) {
					}

				}
			}
		});
		mDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mAdapter.getSelectedPosition() != -1 && mAdapter.getSelectedPosition() != 0)
				{
					// Delete selected row
					dialogDelete("Delete", "Do you want to delete?", 
							new String[]{mAdapter.getSelectedId()}).show();
				}
			}
		});
		mCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		GridAdapter adapter = ((GridAdapter)getListAdapter());
		adapter.setPositionSelected(position,v);
	}

	public AlertDialog editDialog(ArrayList<String> fields, ArrayList<String> values,final int Id_update)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		View view = mActivity.getLayoutInflater().inflate(R.layout.insert_dialog, null);

		final HashMap<String, EditText>	mTextViewsEdit = new HashMap<String, EditText>();

		/**
		 * Generate dynamically (LinearLayouts, textViews, editText) for each field in the
		 * selected table. Then display in a dialog.
		 */
		for (int i = 0; i < fields.size(); i++)
		{
			LinearLayout inner = new LinearLayout(mActivity);
			inner.setOrientation(LinearLayout.HORIZONTAL);

			LinearLayout.LayoutParams innerparams = 
					new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

			inner.setWeightSum(2);
			

			inner.setLayoutParams(innerparams);

			TextView text = new TextView(mActivity);
			text.setText(fields.get(i).toString());
			LayoutParams tvParams = new LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1); 
			text.setLayoutParams(tvParams);

			EditText editText = new EditText(mActivity);
			LayoutParams eParams = new LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1); 
			editText.setLayoutParams(eParams);
			
			editText.setText(values.get(i).toString());
			if (FieldHelper.StringToFieldModel(fields.get(i)).getType().endsWith("INTEGER"))
				editText.setInputType(InputType.TYPE_CLASS_NUMBER);
			else if (FieldHelper.StringToFieldModel(fields.get(i)).getType().endsWith("REAL"))
				editText.setInputType(InputType.TYPE_CLASS_NUMBER);
			else if (FieldHelper.StringToFieldModel(fields.get(i)).getType().endsWith("TEXT"))
				editText.setInputType(InputType.TYPE_CLASS_TEXT);
			mTextViewsEdit.put(fields.get(i), editText);

			inner.addView(text);
			inner.addView(editText);

			((LinearLayout) view).addView(inner);
		}

		// Add the buttons
		builder.setView(view);
		builder.setTitle("Edit Row");
		builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				UpdateValues updateValues = new UpdateValues();
				updateValues.setId(Id_update);

				for (int i = 0; i < mFields.length; i++)
				{
					FieldModel fieldModel = FieldHelper.StringToFieldModel(mFields[i]);
					EditText text = mTextViewsEdit.get(mFields[i]);
					updateValues.setValues(fieldModel.getName(), text.getText().toString());
				}
				new UpdateTask(mDBName, mTableName, updateValues).execute();

			}
		});
		// Do nothing
		builder.setNegativeButton(R.string.cancel, null);
		return builder.create();           
	}

	/**
	 *	Insert a new Row 
	 */
	public AlertDialog insertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		View view = mActivity.getLayoutInflater().inflate(R.layout.insert_dialog, null);

		mFields = mDatabaseManager.select(mDBName, mTableName);

		final HashMap<String, FieldValues> mValues = new HashMap<String, FieldValues>();
		final HashMap<String, EditText>	mTextViews = new HashMap<String, EditText>();

		for (int i = 0; i < mFields.length; i++)
		{	
			LinearLayout inner = new LinearLayout(mActivity);
			inner.setOrientation(LinearLayout.HORIZONTAL);

			LinearLayout.LayoutParams innerparams = 
					new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

			inner.setWeightSum(2);
			inner.setLayoutParams(innerparams);

			TextView text = new TextView(mActivity);
			text.setText(mFields[i].toString());
			LayoutParams tvParams = new LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1); 
			text.setLayoutParams(tvParams);

			EditText editText = new EditText(mActivity);
			LayoutParams eParams = new LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1); 
			editText.setLayoutParams(eParams);
			
			if (FieldHelper.StringToFieldModel(mFields[i]).getType().endsWith("INTEGER"))
				editText.setInputType(InputType.TYPE_CLASS_NUMBER);
			else if (FieldHelper.StringToFieldModel(mFields[i]).getType().endsWith("REAL"))
				editText.setInputType(InputType.TYPE_CLASS_NUMBER);
			else if (FieldHelper.StringToFieldModel(mFields[i]).getType().endsWith("TEXT"))
				editText.setInputType(InputType.TYPE_CLASS_TEXT);

			mTextViews.put(mFields[i], editText);

			inner.addView(text);
			inner.addView(editText);

			((LinearLayout) view).addView(inner);
		}

		// Add the buttons
		builder.setView(view);
		builder.setTitle("Insert Row");
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				for (int i = 0; i < mFields.length; i++)
				{
					FieldValues fieldValue = new FieldValues();
					FieldModel fieldModel = FieldHelper.StringToFieldModel(mFields[i]);

					fieldValue.setName(fieldModel.getName());
					fieldValue.setType(fieldModel.getType());
					EditText text = mTextViews.get(mFields[i]);
					fieldValue.setValue(text.getText().toString());
					mValues.put(fieldModel.getName(), fieldValue);
				}

				new InsertTask(mDBName, mTableName, mValues).execute();
			}
		});
		// Do nothing
		builder.setNegativeButton(R.string.cancel, null);
		return builder.create();           
	}

	public AlertDialog dialogDelete(String title, String msg, final String[] dtfields)
	{	
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(title);
		builder.setMessage(msg);

		// Add the buttons
		builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				new DeleteTask(mDBName, mTableName, dtfields).execute();
				
			}
		});
		// Do nothing
		builder.setNegativeButton(R.string.cancel, null);
		return builder.create();
	}
	public class DeleteTask extends AsyncTask<String, Void, Integer> {
		private ProgressDialog dialog = new ProgressDialog(getActivity());
		private String dbName;
		private String tableName;
		private String[] dtfields;

		public DeleteTask(String database_name,String tableName,String[] dtfields ){
			this.dbName = database_name;
			this.tableName = tableName;
			this.dtfields= dtfields;
		}
		protected void onPreExecute() {
			dialog.setMessage("Loading...");
			dialog.setCancelable(false);
			dialog.show();
		}

		protected Integer doInBackground(String... url) {
			return mDatabaseManager.Inactivation(dbName, tableName, dtfields);
			
		}

		protected void onPostExecute(Integer result) {
			dialog.dismiss();
			if (result>0){
				showAlert("Database ", "Success " + result);
				mAdapter = new GridAdapter(mActivity, 
						(ArrayList<JSONArray>) mDatabaseManager.selectData(mDBName, mTableName));

				setListAdapter(mAdapter);
				mAdapter.notifyDataSetChanged();
				mAdapter.notifyDataSetInvalidated();

			}else{
				showAlert("Database ", "Error");
			}
		}
	}
	public class UpdateTask extends AsyncTask<String, Void, Boolean> {
		private ProgressDialog dialog = new ProgressDialog(getActivity());
		private String dbName;
		private String tableName;
		private UpdateValues values;

		public UpdateTask(String database_name,String tableName,UpdateValues values){
			this.dbName = database_name;
			this.tableName = tableName;
			this.values= values;
		}
		protected void onPreExecute() {
			dialog.setMessage("Loading...");
			dialog.setCancelable(false);
			dialog.show();
		}

		protected Boolean doInBackground(String... url) {
			return mDatabaseManager.update(dbName, tableName, values);
		}

		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			if (result){
				showAlert("Database ", "Success");
				mAdapter = new GridAdapter(mActivity, 
						(ArrayList<JSONArray>) mDatabaseManager.selectData(mDBName, mTableName));

				setListAdapter(mAdapter);
				mAdapter.notifyDataSetChanged();
				mAdapter.notifyDataSetInvalidated();
			}else{
				showAlert("Database ", "Error");
			}
		}
	}
	public class InsertTask extends AsyncTask<String, Void, Boolean> {
		private ProgressDialog dialog = new ProgressDialog(getActivity());
		private String dbName;
		private String tableName;
		private HashMap<String, FieldValues> values;

		public InsertTask(String database_name,String tableName,HashMap<String, FieldValues> values){
			this.dbName = database_name;
			this.tableName = tableName;
			this.values= values;
		}
		protected void onPreExecute() {
			dialog.setMessage("Loading...");
			dialog.setCancelable(false);
			dialog.show();
		}

		protected Boolean doInBackground(String... url) {
			return mDatabaseManager.insert(dbName, tableName, values);
		}

		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			if (result){
				showAlert("Database ", "Success");
				mAdapter = new GridAdapter(mActivity, 
						(ArrayList<JSONArray>) mDatabaseManager.selectData(mDBName, mTableName));

				setListAdapter(mAdapter);
				mAdapter.notifyDataSetChanged();
				mAdapter.notifyDataSetInvalidated();

			}else{
				showAlert("Database ", "Error");
			}
		}
	}
	private void showAlert(String title, String message){
		AlertDialog alert = new AlertDialog.Builder(getActivity())
		.setTitle(title)
		.setPositiveButton("OK", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

			}
		})
		.setMessage(message).create();
		alert.show();
	}
}
