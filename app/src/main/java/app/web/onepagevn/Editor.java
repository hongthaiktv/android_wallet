package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.widget.*;
import java.text.*;
import java.util.*;
import android.widget.AdapterView.*;

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
						String name = view.getClass().getSimpleName();
						switch (name) {
							case "EditText":
								{
									EditText input = (EditText) view;
									String value = input.getText().toString();
									if ("".equals(value)) continue;
									DataType type = dataTypes[i];
									switch (type) {
										case DATETIME:
											if (value.length() <= 10) value += " 00:00:00";
											SimpleDateFormat formater = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
											long date = -1;
											try
											{
												date = formater.parse(value).getTime() / 1000L;
											}
											catch (ParseException e)
											{stt.error(e.getMessage());}
											data.put(colNames[i], date);
											break;
										default:
											data.put(colNames[i], value);
									}
									break;
								}
								
							case "Spinner":
								{
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
					finish();
				}
			});
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
					DataType type = dataTypes[i];
					switch (type) {
						case DATETIME:
							SimpleDateFormat formater = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
							String date = formater.format(new Date());
							input.setHint(date);
							break;
						default:
							if (!isFocus) {
								input.requestFocus();
								InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
								if (imm != null) {
									imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
								}
								isFocus = true;
							}
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
