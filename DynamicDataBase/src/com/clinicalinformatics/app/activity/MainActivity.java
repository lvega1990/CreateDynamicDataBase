/**
 * @author Rheti Inc
 * 
 * The Main UI, merge the user interface with Database Management. 
 * Display three 'List View' for each component (Database, Table or Field).
 * 
 * Actions:	Add, Edit, Delete or View (Only for table)
 * 
 * Created 2013 September 25
 * 
 */
package com.clinicalinformatics.app.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.clinicalinformatics.app.ClinicalApplication;
import com.clinicalinformatics.app.R;
import com.clinicalinformatics.app.db.DataBaseManager;
import com.clinicalinformatics.app.fragments.ListViewFragment;
import com.clinicalinformatics.app.helper.FieldHelper;
import com.clinicalinformatics.app.model.FieldModel;

public class MainActivity extends FragmentActivity implements android.view.View.OnClickListener
{
	public static final String TAG = "Clinical Informatics"; //Default Tag
	//	============================
	//	Constants
	//	============================

	public final String TABLE = "table";	// Tag for enable table buttons
	public final String FIELD = "field";	// Tag for enable field buttons

	//	============================
	//	Variables
	//	============================

	// Database (ListView) buttons actions
	private Button 	mBtnDbAdd;			// Add new database (always available)
	private Button 	mBtnDbEdit;			// Edit selected database private Button 	
	private Button 	mBtnDbDelete;		// Delete selected database

	// Table (ListView) buttons actions
	private Button 	mBtnTableAdd;		// Add new table (enable when a database is selected)
	private Button 	mBtnTableEdit;		// Edit selected table (Rename)
	private Button 	mBtnTableView;		// View fields of selected table (Start FragmentActivity for grid like spreadsheet)
	private Button 	mBtnTableDelete;	// Delete selected table

	// Fields (ListView) buttons actions
	private Button 	mBtnFieldAdd;		// Add new field (enable when a table is selected)
	private Button 	mBtnFieldEdit;		// Edit selected field	(Rename, 
	private Button 	mBtnFieldDelete;	// Delete selected field

	private Component 	mComponent;		// Component can be (Database, Table, Field)
	private Action		mAction;		// Actions can be (Add or Edit) edit action is managed in different way

	// Fragments 
	private ListViewFragment mFragmentDatabase;		// ListFragment for the database. Display all databases (UI component)
	private ListViewFragment mFragmentTable;		// ListFragment for the tables. Display all tables 		(UI component)
	private ListViewFragment mFragmentField;		// ListFragment for the fields. Display all fields 		(UI component)

	private DataBaseManager mDatabaseManager;		// Manage the database
	private ArrayAdapter<String> mCurrentAdpater;	// Store the current adapter
	private Context mContext;						// Store the references of the current activity (Dialog purposes)						

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;

		// == GET VIEWS ==

		// Database components
		mBtnDbAdd 		= (Button) findViewById (R.id.footer_buttons_database_add);
		mBtnDbEdit 		= (Button) findViewById (R.id.footer_buttons_database_edit);
		mBtnDbDelete 	= (Button) findViewById (R.id.footer_buttons_database_delete);

		// Tables components
		mBtnTableAdd 		= (Button) findViewById (R.id.footer_buttons_table_add);
		mBtnTableEdit 		= (Button) findViewById (R.id.footer_buttons_table_edit);
		mBtnTableView 		= (Button) findViewById (R.id.footer_buttons_table_view);
		mBtnTableDelete 	= (Button) findViewById (R.id.footer_buttons_table_delete);

		// Fields components
		mBtnFieldAdd 		= (Button) findViewById (R.id.footer_buttons_field_add);
		mBtnFieldEdit 		= (Button) findViewById (R.id.footer_buttons_field_edit);
		mBtnFieldDelete 	= (Button) findViewById (R.id.footer_buttons_field_delete);

		// Fragments
		mFragmentDatabase  	= (ListViewFragment) getSupportFragmentManager().findFragmentById(R.id.listfragment_database);
		mFragmentTable 		= (ListViewFragment) getSupportFragmentManager().findFragmentById(R.id.listfragment_tables);
		mFragmentField 		= (ListViewFragment) getSupportFragmentManager().findFragmentById(R.id.listfragment_fields);

		// == LISTENER ==

		// Database buttons (Listener)
		mBtnDbAdd.setOnClickListener(this);
		mBtnDbEdit.setOnClickListener(this);
		mBtnDbDelete.setOnClickListener(this);

		// Tables buttons (Listener)
		mBtnTableAdd.setOnClickListener(this);
		mBtnTableEdit.setOnClickListener(this);
		mBtnTableView.setOnClickListener(this);
		mBtnTableDelete.setOnClickListener(this);

		// Fields buttons (Listener)
		mBtnFieldAdd.setOnClickListener(this);
		mBtnFieldEdit.setOnClickListener(this);
		mBtnFieldDelete.setOnClickListener(this);

		// Database Manager
		mDatabaseManager = DataBaseManager
				.getInstance((ClinicalApplication) this.getApplicationContext());

		// initialize empty adapter
		mCurrentAdpater	= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, 
				mDatabaseManager.select());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Main Listener
	 * All click buttons are executed here.
	 */
	@Override
	public void onClick(View view)
	{
		switch (view.getId()) 
		{
		case R.id.footer_buttons_database_add:
			mComponent 	= Component.DATABASE;
			mAction		= Action.ADD;
			dialogAddorEdit("Add Database").show();
			break;
		case R.id.footer_buttons_database_edit:
			mComponent	= Component.DATABASE;
			mAction		= Action.EDIT;
			dialogAddorEdit("Edit name").show();
			break;
		case R.id.footer_buttons_database_delete:
			mComponent	= Component.DATABASE;
			dialogDelete("Delete", "Do you want to delete?").show();
			break;
		case R.id.footer_buttons_table_add:
			mComponent	= Component.TABLE;
			mAction		= Action.ADD;
			dialogAddorEdit("Add Table").show();
			break;
		case R.id.footer_buttons_table_edit:
			mComponent	= Component.TABLE;
			mAction		= Action.EDIT;
			dialogAddorEdit("Edit name").show();
			break;
		case R.id.footer_buttons_table_view:

			// New intent to start GridActivity.
			// Display all fields of selected table
			Intent intent = new Intent(this, GridActivity.class);
			String dbName = mDatabaseManager.select()[mFragmentDatabase.getListView().getCheckedItemPosition()];
			String tableName = mDatabaseManager.select(dbName)[mFragmentTable.getListView().getCheckedItemPosition()];

			// Intent parameters (reference GridActivity.class)
			intent.putExtra("dbName", dbName);
			intent.putExtra("tName", tableName);
			startActivity(intent);
			break;
		case R.id.footer_buttons_table_delete:
			mComponent	= Component.TABLE;
			dialogDelete("Delete", "Do you want to delete?").show();
			break;
		case R.id.footer_buttons_field_add:
			mComponent	= Component.FIELD;
			mAction		= Action.ADD;
			dialogAddField("Add Field").show();
			break;
		case R.id.footer_buttons_field_edit:
			mComponent	= Component.FIELD;
			mAction		= Action.EDIT;
			dialogAddField("Edit Field").show();
			break;
		case R.id.footer_buttons_field_delete:
			mComponent	= Component.FIELD;
			dialogDelete("Delete", "Do you want to delete?").show();
			break;
		default:
			break;
		}
	}

	/**
	 * DIALOG - Delete
	 * Execute delete process for selected item
	 */
	public AlertDialog dialogDelete(String title, String msg)
	{	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(msg);

		// Add the buttons
		builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String dbName, tableName;
				dialog.dismiss();
				switch (mComponent)
				{
				case DATABASE:
					new DeleteTask(mDatabaseManager.select()[mFragmentDatabase.getListView().getCheckedItemPosition()]).execute();
					break;
				case TABLE:
					dbName = mDatabaseManager.select()[mFragmentDatabase.getListView().getCheckedItemPosition()];
					new DeleteTask(dbName ,mDatabaseManager.select(dbName)[mFragmentTable.getListView().getCheckedItemPosition()]).execute();
					break;
				case FIELD:
					// Get database name & table name for field purpose
					dbName 		= mDatabaseManager.select()[mFragmentDatabase.getListView().getCheckedItemPosition()];
					tableName 	= mDatabaseManager.select(dbName)[mFragmentTable.getListView().getCheckedItemPosition()];
					new DeleteTask(dbName ,tableName,
							mDatabaseManager.select(dbName, tableName)[mFragmentField.getListView().getCheckedItemPosition()]).execute();
					break;
				}
			}
		});
		// Do nothing
		builder.setNegativeButton(R.string.cancel, null);
		return builder.create();
	}

	/**
	 * DIALOG - Add or Edit
	 * This dialog only for "Database" and "Table". This can be
	 * used for 'add' or 'edit' a database or table.
	 */
	public AlertDialog dialogAddorEdit(String title)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View view = getLayoutInflater().inflate(R.layout.dialog_edit, null);

		final EditText name = (EditText) view.findViewById(R.id.edit_name);

		String[] arryDatabaseName = mDatabaseManager.select();

		// Only for edit purpose. Set the current name of selected item and set it
		// in the edit text of the dialog
		if (arryDatabaseName.length > 0 && mAction != Action.ADD)
		{
			String databaseName = arryDatabaseName[mFragmentDatabase.getListView().getCheckedItemPosition()];
			if (mComponent == Component.DATABASE && databaseName != null)
				name.setText(databaseName);
			else if (databaseName != null)
			{
				String[] dbTables = mDatabaseManager.select(databaseName);
				if (dbTables.length > 0)
					name.setText(dbTables[mFragmentTable.getListView().getCheckedItemPosition()]);
			}
		}
		// Add the buttons
		builder.setView(view);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String dbName = "";
				dialog.dismiss();
				switch (mAction) {
				case ADD:
					if (mComponent == Component.DATABASE){
						// Add database
						new AddTask(name.getText().toString()).execute();
					}else if (mComponent == Component.TABLE){
						// Add table
						dbName = mDatabaseManager.select()[mFragmentDatabase.getListView().getCheckedItemPosition()];
						new AddTask(dbName,name.getText().toString()).execute();
					}
					break;
				case EDIT:
					if (mComponent == Component.DATABASE)
						// Edit database
					{
						new EditTask(mDatabaseManager.select()[mFragmentDatabase.getListView().getCheckedItemPosition()], 
								name.getText().toString()).execute();
					}
					else if (mComponent == Component.TABLE)
						// Edit table
					{
						dbName = mDatabaseManager.select()[mFragmentDatabase.getListView().getCheckedItemPosition()];
						new EditTask(dbName,mDatabaseManager.select(dbName)[mFragmentTable.getListView().getCheckedItemPosition()], 
								name.getText().toString()).execute();
					}
					break;
				default:
					break;
				}
			}
		});
		// Do nothing
		builder.setNegativeButton(R.string.cancel, null);
		return builder.create();
	}

	// Dialog - Used only to "Add Field"
	public AlertDialog dialogAddField(String title)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View view = getLayoutInflater().inflate(R.layout.dialog_edit_field, null);

		// Get views
		final EditText name = (EditText) view.findViewById(R.id.edit_name_field);
		final Spinner  spin = (Spinner) view.findViewById(R.id.field_spinner);
		final RadioButton radio_yes = (RadioButton) view.findViewById(R.id.radio_yes);

		// Adapter for spinner (Input Type)
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.type_array, android.R.layout.simple_spinner_item);

		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Apply the adapter to the spinner
		spin.setAdapter(adapter);

		// Only for edit purpose. Set the current values of selected field and set it
		// in the edit text, spinner and radial button
		if (mAction == Action.EDIT)
		{
			// Get data from database
			String dbName 	= mDatabaseManager.select()[mFragmentDatabase.getListView().getCheckedItemPosition()];
			String dbTable 	= mDatabaseManager.select(dbName)[mFragmentTable.getListView().getCheckedItemPosition()];
			String dbField 	= mDatabaseManager.select(dbName, dbTable)[mFragmentField.getListView().getCheckedItemPosition()];

			// Field Helper to manage current field
			FieldModel fieldModel = FieldHelper.StringToFieldModel(dbField);

			name.setText(fieldModel.getName());

			RadioButton radio_no = (RadioButton) view.findViewById(R.id.radio_no);

			// Set radio buttons with selected values
			if (fieldModel.getIndexed())
				radio_yes.setChecked(true);
			else
				radio_no.setChecked(true);

			// Set spinner with selected values
			if (fieldModel.getType().endsWith("INTEGER"))
				spin.setSelection(0);
			else if (fieldModel.getType().endsWith("REAL"))
				spin.setSelection(1);
			else if (fieldModel.getType().endsWith("TEXT"))
				spin.setSelection(2);
		}

		// Add the buttons
		builder.setView(view);
		builder.setTitle(title);
		builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String dbName = mDatabaseManager.select()[mFragmentDatabase.getListView().getCheckedItemPosition()];
				String dbTable = mDatabaseManager.select(dbName)[mFragmentTable.getListView().getCheckedItemPosition()];
				String radio_text = null;

				if (radio_yes.isChecked()) 
					radio_text = "true";
				else
					radio_text = "false";
				dialog.dismiss();
				switch (mAction) {
				case ADD:
					new AddTask(dbName, dbTable, new String[] {name.getText().toString(),
							spin.getSelectedItem().toString(), radio_text}).execute();
					break;
				case EDIT:
					new EditTask(dbName, dbTable, 
							mDatabaseManager.select(dbName, dbTable)[mFragmentField.getListView().getCheckedItemPosition()],
							new String[] {name.getText().toString(),
						spin.getSelectedItem().toString(), radio_text}).execute();
					break;
				default:
					break;
				}
			}
		});
		// Do nothing
		builder.setNegativeButton(R.string.cancel, null);
		return builder.create();
	}

	//	============================
	//	REFRESH Adapter
	//	============================

	/**
	 * 	Help to refresh current data in the list
	 */

	public void setCurrentDatabase(ArrayAdapter<String> adapter)
	{	
		this.mCurrentAdpater = adapter;
		mFragmentTable.setListAdapter(this.mCurrentAdpater);
	}

	public void setCurrentTable(String tableName)
	{
		// Get all tables from selected database
		String[] fieldsNames = mDatabaseManager.select(
				mDatabaseManager.select()[mFragmentDatabase.getListView().getCheckedItemPosition()], tableName);

		this.mCurrentAdpater = new ArrayAdapter<String>(
				this,
				android.R.layout.simple_list_item_single_choice,
				fieldsNames);

		mFragmentField.setListAdapter(mCurrentAdpater);
	}

	public void setCurrentField(ArrayAdapter<String> adapter)
	{
		this.mCurrentAdpater = adapter;
		mFragmentField.setListAdapter(this.mCurrentAdpater);
	}

	//	============================
	//	ENABLE & DISABLE BUTTONS
	//	============================

	/**
	 * Buttons enable depends of the selected item. So when the user
	 * select an item some buttons will be disable in onClickItemLister
	 * inside 'ListViewFragment.class'
	 * 
	 * @param flag: true = enable or false = disable
	 */

	public void enableDatabaseButtons(boolean flag)
	{
		mBtnDbEdit.setEnabled(flag);
		mBtnDbDelete.setEnabled(flag);
	}

	public void enableTableButtons(boolean flag)
	{
		mBtnTableEdit.setEnabled(flag);
		mBtnTableView.setEnabled(flag);
		mBtnTableDelete.setEnabled(flag);
	}

	public void enableFieldButtons(boolean flag)
	{
		mBtnFieldEdit.setEnabled(flag);
		mBtnFieldDelete.setEnabled(flag);
	}

	/**
	 * 
	 * @param option: can be "Table" or "Field" name
	 * @param flag:	this let us to clear the list
	 */
	public void enableAddButton(String option, boolean flag)
	{
		if (option.equals(TABLE))
			mBtnTableAdd.setEnabled(flag);
		else if (option.equals(FIELD))
			mBtnFieldAdd.setEnabled(flag);
	}

	//	============================
	//	REFRESHING LISTS
	//	============================

	/**
	 * @param values [dbName, tableName]
	 * Zero: for all databases
	 * One: for Tables
	 * Two: for Fields
	 */
	public void refreshDatabaseView(String...values)
	{ 	
		if(values.length == 1)
		{
			mCurrentAdpater	= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, 
					mDatabaseManager.select(values[0]));

			mFragmentTable.setListAdapter(mCurrentAdpater);
		}
		else if (values.length == 2)
		{
			mCurrentAdpater	= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, 
					mDatabaseManager.select(values[0], values[1]));

			mFragmentField.setListAdapter(mCurrentAdpater);
		}
		else
		{
			mCurrentAdpater	= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, 
					mDatabaseManager.select());

			mFragmentDatabase.setListAdapter(mCurrentAdpater);
		}
	}

	// Refresh the database list
	public void refreshDatabaseView()
	{
		mFragmentDatabase.setListAdapter(mCurrentAdpater	= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, 
				mDatabaseManager.select()));
		mCurrentAdpater.notifyDataSetChanged();
		mCurrentAdpater.notifyDataSetInvalidated();
	}

	// Refresh the table list
	public void refreshTableView(String[] tables, boolean flag)
	{
		mFragmentTable.setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, 
				flag ? tables : new String[0]));
		mCurrentAdpater.notifyDataSetChanged();
		mCurrentAdpater.notifyDataSetInvalidated();
	}

	// Refresh the field list
	public void refreshFieldView(String[] fields, boolean flag)
	{
		mFragmentField.setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, 
				flag ? fields : new String[0]));
		mCurrentAdpater.notifyDataSetChanged();
		mCurrentAdpater.notifyDataSetInvalidated();
	}

	/**
	 * Simple Dialog for "Success" or getString(R.string.error) at the end of transaction
	 */
	private void showAlert(String title, String message){
		AlertDialog alert = new AlertDialog.Builder(mContext)
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


	// Components - The three type of components that already exist.
	public enum Component {
		DATABASE,
		TABLE,
		FIELD
	}

	// Actions that can be executed for the buttons (defined only the necessary)
	public enum Action {
		ADD,
		EDIT
	}

	//	============================
	//	THREADS
	//	============================

	/**
	 * Each task have the corresponding transaction.
	 * This tasks are called each for each dialog success, corresponding
	 * the transaction that was selected for the user.
	 */

	public class AddTask extends AsyncTask<String, Void, Boolean> 
	{
		private ProgressDialog dialog = new ProgressDialog(mContext);
		private String dbName;
		private String tableName;
		private String[] field;
		public AddTask(String database_name){
			this.dbName = database_name;
		}
		public AddTask(String database_name,String tableName){
			this.tableName = tableName;
			this.dbName = database_name;
		}
		public AddTask(String database_name,String tableName,String[] field){
			this.tableName = tableName;
			this.dbName = database_name;
			this.field = field;
		}
		protected void onPreExecute() {
			dialog.setMessage("Loading...");
			dialog.setCancelable(false);
			dialog.show();
		}

		protected Boolean doInBackground(String... url) {
			if (field != null){
				if (!field.equals(""))
					return mDatabaseManager.add(dbName, tableName,field);
				else
					return false;
			}else if (tableName!=null){
				if (!tableName.equals(""))
					return mDatabaseManager.add(dbName, tableName);	
				else
					return false;
			}else{
				if (!dbName.equals(""))
					return mDatabaseManager.add(dbName);
				else
					return false;
			}
		}

		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			if (result){
				showAlert(TAG, getString(R.string.success));
				if (field != null){
					refreshFieldView(mDatabaseManager.select(dbName, tableName), true);
					enableAddButton(FIELD, true);
					enableFieldButtons(false);
				}else if (tableName!=null){
					refreshTableView(mDatabaseManager.select(dbName), true);
					refreshFieldView(new String[0], false);
					enableAddButton(TABLE, true);
					enableTableButtons(false);
					enableAddButton(FIELD, false);
					enableFieldButtons(false);
				}else{
					refreshDatabaseView();
					refreshTableView(new String[0], false);
					refreshFieldView(new String[0], false);
					enableDatabaseButtons(false);
					enableAddButton(TABLE, false);
					enableTableButtons(false);
					enableAddButton(FIELD, false);
					enableFieldButtons(false);
				}
			}else{
				showAlert(TAG, getString(R.string.error));
			}
		}
	}   

	public class EditTask extends AsyncTask<String, Void, Boolean> {
		private ProgressDialog dialog = new ProgressDialog(mContext);
		private String dbName;
		private String new_dbName;
		private String tableName;
		private String new_tableName;
		private String fieldName;
		private String[] new_field;
		public EditTask(String database_name,String new_database_name){
			this.dbName = database_name;
			this.new_dbName = new_database_name;
		}
		public EditTask(String database_name,String tableName,String new_tableName){
			this.dbName = database_name;
			this.tableName = tableName;
			this.new_tableName = new_tableName;
		}
		public EditTask(String database_name,String tableName,String fieldName,String[] new_field){
			this.dbName = database_name;
			this.tableName = tableName;
			this.fieldName = fieldName;
			this.new_field = new_field;
		}
		protected void onPreExecute() {
			dialog.setMessage("Loading...");
			dialog.setCancelable(false);
			dialog.show();
		}

		protected Boolean doInBackground(String... url) {
			if (new_field != null){
				if (!new_field.equals(""))
					return mDatabaseManager.edit(dbName, tableName, fieldName, new_field);
				else return false;
			}if (new_tableName!=null){
				if (!new_tableName.equals(""))
					return mDatabaseManager.edit(dbName,tableName, new_tableName);
				else
					return false;
			}else{
				if (!new_dbName.equals(""))
					return mDatabaseManager.edit(dbName, new_dbName);
				else
					return false;
			}
		}

		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			if (result){
				showAlert(TAG, getString(R.string.success));
				if (new_field != null){
					refreshFieldView(mDatabaseManager.select(dbName, tableName), true);
					enableAddButton(FIELD, true);
					enableFieldButtons(false);
				}else if (new_tableName!=null){
					refreshTableView(mDatabaseManager.select(dbName), true);
					refreshFieldView(new String[0], false);
					enableAddButton(TABLE, true);
					enableTableButtons(false);
					enableAddButton(FIELD, false);
					enableFieldButtons(false);
				}else{
					refreshDatabaseView();
					refreshTableView(new String[0], false);
					refreshFieldView(new String[0], false);
					enableDatabaseButtons(false);
					enableAddButton(TABLE, false);
					enableTableButtons(false);
					enableAddButton(FIELD, false);
					enableFieldButtons(false);
				}
			}else{
				showAlert(TAG, getString(R.string.error));
			}
		}
	}

	public class DeleteTask extends AsyncTask<String, Void, Boolean> {
		private ProgressDialog dialog = new ProgressDialog(mContext);
		private String dbName;
		private String tableName;
		private String fieldName;

		public DeleteTask(String database_name){
			this.dbName = database_name;
		}
		public DeleteTask(String database_name,String tableName){
			this.dbName = database_name;
			this.tableName = tableName;
		}
		public DeleteTask(String database_name,String tableName,String fieldName){
			this.dbName = database_name;
			this.tableName = tableName;
			this.fieldName = fieldName;
		}
		protected void onPreExecute() {
			dialog.setMessage("Loading...");
			dialog.setCancelable(false);
			dialog.show();
		}

		protected Boolean doInBackground(String... url) {
			if (fieldName != null){
				return mDatabaseManager.delete(dbName, tableName, fieldName);
			}else if (tableName != null){
				return mDatabaseManager.delete(dbName, tableName);
			}else{
				return mDatabaseManager.delete(dbName);
			}
		}

		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			if (result){
				showAlert(TAG, getString(R.string.success));

				if (fieldName != null){
					refreshFieldView(mDatabaseManager.select(dbName, tableName), true);
					enableFieldButtons(false);
				}else if (tableName != null){
					enableAddButton(FIELD, false);
					enableTableButtons(false);
					enableFieldButtons(false);
					refreshDatabaseView(dbName);
					refreshTableView(mDatabaseManager.select(dbName), true);
					refreshFieldView(new String[0], false);
				}else{
					enableDatabaseButtons(false);
					enableAddButton(TABLE, false);
					enableTableButtons(false);
					enableAddButton(FIELD, false);
					enableFieldButtons(false);
					refreshDatabaseView();
					refreshTableView(new String[0], false);
					refreshFieldView(new String[0], false);
				}

			}else{
				showAlert(TAG,getString(R.string.error));
			}
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.action_settings:
			Intent searchActivity = new Intent(this, AuditActivity.class);
			startActivity(searchActivity);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
