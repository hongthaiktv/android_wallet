package app.web.onepagevn;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class Editor extends Activity
{
	private GridLayout editorTopLayout;
	private Button btnClear, btnAdd, btnCancel;
	private StatusBar stt;
	private SQLiteDB db;
	private String tblName = "expends";
	private String[] colNames;
	private DataType[] dataTypes;
	private List<Map<String, Object>> foreignColumns;
	private Date pickedDate, pickedTime;
	private int pickedRow = -1;
	private long[][] pickedRows = new long[20][2];
	private SimpleDateFormat df, tf, tff, ff, flocal;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
		
		editorTopLayout = findViewById(R.id.editorTopLayout);
		btnClear = findViewById(R.id.editorBtnClear);
		btnAdd = findViewById(R.id.editorBtnAdd);
		btnCancel = findViewById(R.id.editorBtnCancel);
		stt = new StatusBar(this).init();
		db = new SQLiteDB();
		tblName = !"balance".equals(getIntent().getStringExtra("tblName")) ? getIntent().getStringExtra("tblName") : "expends";
		setTitle(tblName.toUpperCase());
		df = new SimpleDateFormat("dd-MM-yyyy");
		tf = new SimpleDateFormat("HH:mm");
		tff = new SimpleDateFormat("HH:mm:ss");
		ff = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		flocal = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		
		colNames = db.getColumnNames(tblName);
		dataTypes = db.getDataTypes(tblName);
		foreignColumns = db.getForeignColumns(tblName);
		editorTopLayout.setRowCount(colNames.length - 1);
		for (int i = 1; i < colNames.length; i++) {
			GridLayout.LayoutParams tvParams = new GridLayout.LayoutParams();
			tvParams.setMargins(20, 0, 20, 0);
			TextView tvName = new TextView(this);
			tvName.setLayoutParams(tvParams);
			tvName.setText(colNames[i].toUpperCase());
			editorTopLayout.addView(tvName);
			
			if (foreignColumns.size() > 0) {
				boolean isCatalog = false;
				for (Map<String, Object> fColumn : foreignColumns) {
					String srcColumn = (String) fColumn.get("srcColumn");
					if (srcColumn.equals(colNames[i])) {
						String[] items = (String[]) fColumn.get("names");
						GridLayout.LayoutParams ctlParams = new GridLayout.LayoutParams();
						ctlParams.width = 0;
						ctlParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
						ctlParams.setMargins(20, 50, 20, 0);
						Spinner catalog = new Spinner(this);
						catalog.setLayoutParams(ctlParams);
						ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
						adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						catalog.setAdapter(adapter);
						setViewID(catalog, i);
						editorTopLayout.addView(catalog);
						isCatalog = true;
						break;
					}
				}
				if (isCatalog) continue;
			}
			
			switch (dataTypes[i]) {
				case DATETIME:
					TypedValue backGround = new TypedValue();
					getTheme().resolveAttribute(android.R.attr.selectableItemBackground, backGround, true);
					Date now = new Date();
					final int row = i;
					
					GridLayout.LayoutParams lPickerParams = new GridLayout.LayoutParams();
					lPickerParams.width = 0;
					lPickerParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
					lPickerParams.setMargins(20, 0, 20, 0);
					LinearLayout layoutPicker = new LinearLayout(this);
					layoutPicker.setLayoutParams(lPickerParams);
					layoutPicker.setPadding(0, 0, 0, 0);
					layoutPicker.setOrientation(LinearLayout.HORIZONTAL);
					layoutPicker.setBaselineAligned(true);
					setViewID(layoutPicker, i);
					
					LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(300, 80);
					Button btnDatePicker = new Button(this);
					btnDatePicker.setLayoutParams(dateParams);
					btnDatePicker.setPadding(0, 0, 0, 0);
					btnDatePicker.setTextSize(17f);
					btnDatePicker.setBackgroundResource(backGround.resourceId);
					btnDatePicker.setText(df.format(now));
					pickedRows[row][0] = now.getTime();
					btnDatePicker.setOnClickListener(new OnClickListener() {
						
							@Override
							public void onClick(View v)
							{
								dateTimePicker(R.layout.date_picker);
								pickedRow = row;
							}
						});
					
					LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(150, 80);
					Button btnTimePicker = new Button(this);
					btnTimePicker.setLayoutParams(timeParams);
					btnTimePicker.setPadding(0, 0, 0, 0);
					btnTimePicker.setTextSize(17f);
					btnTimePicker.setBackgroundResource(backGround.resourceId);
					btnTimePicker.setText(tf.format(now));
					pickedRows[row][1] = now.getTime();
					btnTimePicker.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v)
							{
								dateTimePicker(R.layout.time_picker);
								pickedRow = row;
							}
						});
					
					layoutPicker.addView(btnDatePicker);
					layoutPicker.addView(btnTimePicker);
					layoutPicker.setBaselineAlignedChildIndex(0);
					editorTopLayout.addView(layoutPicker);
					continue;
			}
			
			GridLayout.LayoutParams iptParams = new GridLayout.LayoutParams();
			iptParams.width = 0;
			iptParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
			iptParams.setMargins(20, 0, 20, 0);
			EditText input = new EditText(this);
			input.setLayoutParams(iptParams);
			input.setSingleLine();
			setViewID(input, i);
			editorTopLayout.addView(input);
		}
		
		reset();
		
		btnClear.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					reset();
				}
			});
			
		btnAdd.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View button)
				{
					int numRow = editorTopLayout.getRowCount();
					ContentValues data = new ContentValues();
					for (int i = 1; i <= numRow; i++) {
						View view = getViewByIndex(i);
						if (view == null) continue;
						DataType type = dataTypes[i];
						String name = view.getClass().getSimpleName();
						switch (name) {
							case "EditText":
								{
									EditText input = (EditText) view;
									String value = input.getText().toString();
									if ("".equals(value)) continue;
									switch (type) {
										default:
											data.put(colNames[i], value);
									}
									break;
								}
								
							case "Spinner":
								{
									if (foreignColumns.size() < 1) continue;
									Spinner catalog = (Spinner) view;
									String value = (String) catalog.getSelectedItem();
									if ("".equals(value)) continue;
									int id = -1;
									for (Map<String, Object> fColumn : foreignColumns) {
										String srcColumn = (String) fColumn.get("srcColumn");
										if (srcColumn.equals(colNames[i])) {
											int[] ids = (int[]) fColumn.get("ids");
											String[] names = (String[]) fColumn.get("names");
											for (int index = 0; index < names.length; index++) {
												String item = names[index];
												if (item.equals(value)) id = ids[index];
											}
										}
									}
									if (id != -1) data.put(colNames[i], id);
									break;
								}
							case "LinearLayout":
								{
									switch (type) {
										case DATETIME:
											data.put(colNames[i], getDateTime(i).getTime() / 1000L);
									}
									break;
								}
						}
					}
					if (data.size() > 0) {
						long result = db.insert(tblName, data);
						if (result == -1) stt.error("Adding new record to \"" + tblName.toUpperCase() + "\" failed.");
						else stt.success("Added new record to \"" + tblName.toUpperCase() + "\".");
						if ("budget".equals(tblName)) finish();
						else reset();
					}
				}
			});
			
		btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					/*
					AppWidgetManager widgetManager = AppWidgetManager.getInstance(Editor.this);
					ComponentName widget = new ComponentName(Editor.this, WidgetProvider.class);
					int[] widgetIDs = widgetManager.getAppWidgetIds(widget);
					Intent intent = new Intent(Editor.this, WidgetProvider.class);
					intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
					intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIDs);
					Editor.this.sendBroadcast(intent);
					*/
					finish();
				}
			});
	}

	private void dateTimePicker(int resource)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final DateTimePickerAdapter adapter = new DateTimePickerAdapter(this, resource);
		builder.setAdapter(adapter, null);
		builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface d)
				{
					pickedDate = adapter.getPickedDate();
					pickedTime = adapter.getPickedTime();
					LinearLayout layoutPicker = (LinearLayout) getViewByIndex(pickedRow);
					Button btnDatePicker = (Button) layoutPicker.getChildAt(0);
					Button btnTimePicker = (Button) layoutPicker.getChildAt(1);
					if (pickedDate != null) {
						btnDatePicker.setText(df.format(pickedDate));
						pickedRows[pickedRow][0] = pickedDate.getTime();
					}
					if (pickedTime != null) {
						btnTimePicker.setText(tf.format(pickedTime));
						pickedRows[pickedRow][1] = pickedTime.getTime();
					}
				}
			});
		AlertDialog picker = builder.create();
		adapter.setDialog(picker);
		picker.show();
	}
	
	private Date getDateTime(int row) {
		String date = df.format(new Date(pickedRows[row][0]));
		String time = tff.format(new Date(pickedRows[row][1]));
		Date datetime = null;
		try
		{
			datetime = flocal.parse(date + " " + time);
		}
		catch (ParseException e)
		{stt.error(e.getMessage());}
		return datetime;
	}
	
	private void reset() {
		int numRow = editorTopLayout.getRowCount();
		boolean isFocus = false;
		for (int i = 1; i <= numRow; i++) {
			View view = getViewByIndex(i);
			if (view == null) continue;
			String name = view.getClass().getSimpleName();
			switch (name) {
				case "EditText":
					EditText input = (EditText) view;
					input.setText("");
					if (!isFocus) {
						input.requestFocus();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						if (imm != null) {
							imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
						}
						isFocus = true;
					}
					break;
				case "LinearLayout":
					switch (dataTypes[i]) {
						case DATETIME:
							LinearLayout layoutPicker = (LinearLayout) view;
							Button btnDatePicker = (Button) layoutPicker.getChildAt(0);
							Button btnTimePicker = (Button) layoutPicker.getChildAt(1);
							Date now = new Date();
							btnDatePicker.setText(df.format(now));
							btnTimePicker.setText(tf.format(now));
							pickedRows[i][0] = now.getTime();
							pickedRows[i][1] = now.getTime();
							break;
					}
					break;
			}
		}
	}
	
	private void setViewID(View view, int index) {
		int id = -1;
		switch (index) {
			case 0:
				id = R.string.view00;
				break;
			case 1:
				id = R.string.view01;
				break;
			case 2:
				id = R.string.view02;
				break;
			case 3:
				id = R.string.view03;
				break;
			case 4:
				id = R.string.view04;
				break;
			case 5:
				id = R.string.view05;
				break;
			case 6:
				id = R.string.view06;
				break;
			case 7:
				id = R.string.view07;
				break;
			case 8:
				id = R.string.view08;
				break;
			case 9:
				id = R.string.view09;
				break;
			case 10:
				id = R.string.view10;
				break;
			case 11:
				id = R.string.view11;
				break;
			case 12:
				id = R.string.view12;
				break;
			case 13:
				id = R.string.view13;
				break;
			case 14:
				id = R.string.view14;
				break;
			case 15:
				id = R.string.view15;
				break;
			case 16:
				id = R.string.view16;
				break;
			case 17:
				id = R.string.view17;
				break;
			case 18:
				id = R.string.view18;
				break;
			case 19:
				id = R.string.view19;
				break;
			case 20:
				id = R.string.view20;
				break;
		}
		view.setId(id);
	}
	
	private View getViewByIndex(int index) {
		int id = -1;
		switch (index) {
			case 0:
				id = R.string.view00;
				break;
			case 1:
				id = R.string.view01;
				break;
			case 2:
				id = R.string.view02;
				break;
			case 3:
				id = R.string.view03;
				break;
			case 4:
				id = R.string.view04;
				break;
			case 5:
				id = R.string.view05;
				break;
			case 6:
				id = R.string.view06;
				break;
			case 7:
				id = R.string.view07;
				break;
			case 8:
				id = R.string.view08;
				break;
			case 9:
				id = R.string.view09;
				break;
			case 10:
				id = R.string.view10;
				break;
			case 11:
				id = R.string.view11;
				break;
			case 12:
				id = R.string.view12;
				break;
			case 13:
				id = R.string.view13;
				break;
			case 14:
				id = R.string.view14;
				break;
			case 15:
				id = R.string.view15;
				break;
			case 16:
				id = R.string.view16;
				break;
			case 17:
				id = R.string.view17;
				break;
			case 18:
				id = R.string.view18;
				break;
			case 19:
				id = R.string.view19;
				break;
			case 20:
				id = R.string.view20;
				break;
		}
		View view = findViewById(id);
		if (view == null) stt.error("View at index \"" + index + "\" not found.");
		return view;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		stt.update();
		
		Intent intent = getIntent();
		String intAction = intent.getAction();
		String intType = intent.getType();
		
		if (Intent.ACTION_SEND.equals(intAction) && intType != null) {
			String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
			Toast.makeText(this, sharedText, Toast.LENGTH_SHORT).show();
		}
	}
}
