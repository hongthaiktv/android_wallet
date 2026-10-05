package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.ViewGroup.*;
import android.view.inputmethod.*;
import android.widget.*;
import android.widget.AdapterView.*;
import java.text.*;
import java.util.*;
import org.json.*;

import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;

public class Editor extends Activity
{
	private GridLayout layoutTop;
	private LinearLayout layoutButtons;
	private Button btnClear, btnAdd, btnCancel;
	private StatusBar stt;
	private SQLiteDB db;
	private String tblName = "expends", mode = "insert";
	private String[] colNames, labels;
	private Object[] colValues;
	private DataType[] dataTypes;
	private List<Map<String, Object>> foreignColumns;
	private Date pickedDate, pickedTime;
	private int pickedRow = -1, currentBudget = 0;
	private Long[][] pickedRows = new Long[20][2];
	private Integer[][][] catalogRowIDs = new Integer[20][2][];
	private String[][][] catalogRowNames = new String[20][2][];
	private SimpleDateFormat df, dff, tf, tff, ff, flocal;
	private NumberFormat numConverter;
	private Map<String, Integer> indexViews = new HashMap<>();
	private AppAssetManager dirImage;
	private ImageButton ibBudget;
	private Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
		
		layoutTop = findViewById(R.id.editorLayoutTop);
		layoutButtons = findViewById(R.id.editorLayoutButtons);
		btnClear = findViewById(R.id.editorBtnClear);
		btnAdd = findViewById(R.id.editorBtnAdd);
		btnCancel = findViewById(R.id.editorBtnCancel);
		stt = new StatusBar(this).init();
		db = new SQLiteDB();
		intent = getIntent();
		tblName = !"balance".equals(intent.getStringExtra("tblName")) ? intent.getStringExtra("tblName") : "expends";
		if ("budgets".equals(tblName)) tblName = "budgets_protected";
		setTitle(tblName.toUpperCase());
		df = new SimpleDateFormat("dd-MM");
		dff = new SimpleDateFormat("dd-MM-yyyy");
		tf = new SimpleDateFormat("HH:mm");
		tff = new SimpleDateFormat("HH:mm:ss");
		ff = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		flocal = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		numConverter = NumberFormat.getInstance(Locale.US);
		dirImage = new AppAssetManager(this, "images/");
		ibBudget = findViewById(R.id.editorIbBudget);
		
		if (!"expends".equals(tblName)) {
			ibBudget.setVisibility(ImageButton.INVISIBLE);
			RelativeLayout.LayoutParams layoutButtonsParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutButtonsParams.addRule(RelativeLayout.BELOW, R.id.editorLayoutTop);
			layoutButtonsParams.setMargins(0, 60, 0, 0);
			layoutButtons.setLayoutParams(layoutButtonsParams);
			layoutButtons.setOrientation(LinearLayout.HORIZONTAL);
			layoutButtons.setGravity(Gravity.CENTER);
		}
		
		String intAction = intent.getAction();
		int rowID = intent.getIntExtra("rowID", -1);
		if (!Intent.ACTION_EDIT.equals(intAction)) colNames = db.getColumnNames(tblName);
		else {
			if (rowID >= 0) {
				mode = "edit";
				Map<String, Object> rowData = db.getRowData(tblName, rowID);
				colNames = rowData.keySet().toArray(new String[0]);
				colValues = rowData.values().toArray();
			} else {
				stt.error("Missing Intent rowID.");
				finish();
				return;
			}
		}
		
		switch (tblName) {
			case "expends":
			{
				labels = new String[] {"ID", "DATE", "SUPPLIER", "PRODUCT", "UNIT", "PRICE", "QUANTITY", "NOTE"};
				List<String> rows = new ArrayList<String>(Arrays.asList(colNames));
				rows.remove(colNames.length - 1);
				colNames = rows.toArray(new String[0]);
				break;
			}
		}
		
		dataTypes = db.getDataTypes(tblName);
		foreignColumns = db.getForeignColumns(tblName);
		layoutTop.setRowCount(colNames.length - 1);
		for (int i = 1; i < colNames.length; i++) {
			final int row = i;
			GridLayout.LayoutParams tvParams = new GridLayout.LayoutParams();
			tvParams.setMargins(20, 0, 20, 0);
			TextView tvLabel = new TextView(this);
			tvLabel.setLayoutParams(tvParams);
			if (labels != null) tvLabel.setText(labels[i].toUpperCase());
			else tvLabel.setText(colNames[i].toUpperCase());
			layoutTop.addView(tvLabel);
			
			if (foreignColumns.size() > 0) {
				boolean isCatalog = false;
				for (Map<String, Object> fColumn : foreignColumns) {
					String srcColumn = (String) fColumn.get("srcColumn");
					if (srcColumn.equals(colNames[i])) {
						Integer[] ids = (Integer[]) fColumn.get("ids");
						catalogRowIDs[i][0] = ids;
						String[] items = (String[]) fColumn.get("names");
						if ("funds".equals(tblName)) {
							List<String> itemsNoCash = new ArrayList<String>(Arrays.asList(items));
							itemsNoCash.remove("Cash");
							items = itemsNoCash.toArray(new String[0]);
						}
						catalogRowNames[i][0] = items;
						
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
						layoutTop.addView(catalog);
						if ("funds".equals(tblName) && "bank2".equals(colNames[i].toLowerCase())) catalog.setSelection(1);
						if ("expends".equals(tblName) && "productid".equals(colNames[i].toLowerCase())) {
							indexViews.put("indexProduct", i);
							catalog.setOnItemSelectedListener(new OnItemSelectedListener() {
									boolean enable = false;
									@Override
									public void onItemSelected(AdapterView<?> parent, View v, int index, long id)
									{
										// fix for Spinner auto active listener on startup
										if ("edit".equals(mode)) {
											if (!enable) {
												enable = true;
												return;
											}
										}
										
										catalogRowIDs[row][1] = new Integer[] {index, catalogRowIDs[row][0][index]};
										catalogRowNames[row][1] = new String[] {String.valueOf(index), catalogRowNames[row][0][index]};
										
										String selected = ((TextView) v).getText().toString();
										Integer indexPrice = indexViews.get("indexPrice");
										if (indexPrice == null) return;
										EditText inputPrice = (EditText) getViewByIndex(indexPrice);
										// check for array catalogRow instead
										List<Map<String, Object>> products = db.sql("products", "SELECT name, price FROM products ORDER BY name COLLATE NOCASE", null);
										for (Map<String, Object> product : products) {
											String pName = (String) product.get("name");
											if (selected.equals(pName)) {
												double price = product.get("price");
												String formatedPrice = String.format(Locale.US, "%,.0f", price);
												inputPrice.setText(formatedPrice);
												inputPrice.requestFocus();
												inputPrice.setSelection(formatedPrice.length());
												break;
											}
										}
									}

									@Override
									public void onNothingSelected(AdapterView<?> adapter)
									{

									}
								});
						}
						else {
							catalog.setOnItemSelectedListener(new OnItemSelectedListener() {
									boolean enable = false;
									@Override
									public void onItemSelected(AdapterView<?> parent, View v, int index, long id)
									{
										// fix for Spinner auto active listener on startup
										if ("edit".equals(mode)) {
											if (!enable) {
												enable = true;
												return;
											}
										}
										catalogRowIDs[row][1] = new Integer[] {index, catalogRowIDs[row][0][index]};
										catalogRowNames[row][1] = new String[] {String.valueOf(index), catalogRowNames[row][0][index]};
									}

									@Override
									public void onNothingSelected(AdapterView<?> adapter)
									{

									}
								});
						}
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
					btnDatePicker.setText(dff.format(now));
					pickedRows[row][0] = now.getTime();
					btnDatePicker.setOnClickListener(new OnClickListener() {
						
							@Override
							public void onClick(View v)
							{
								pickedRow = row;
								dateTimePicker(R.layout.date_picker);
							}
						});
					
					LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(220, 80);
					Button btnTimePicker = new Button(this);
					btnTimePicker.setLayoutParams(timeParams);
					btnTimePicker.setPadding(0, 0, 0, 0);
					btnTimePicker.setTextSize(17f);
					btnTimePicker.setBackgroundResource(backGround.resourceId);
					btnTimePicker.setText(tff.format(now));
					pickedRows[row][1] = now.getTime();
					btnTimePicker.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v)
							{
								pickedRow = row;
								dateTimePicker(R.layout.time_picker);
							}
						});
					
					layoutPicker.addView(btnDatePicker);
					layoutPicker.addView(btnTimePicker);
					layoutPicker.setBaselineAlignedChildIndex(0);
					layoutTop.addView(layoutPicker);
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
			layoutTop.addView(input);
			
			switch (tblName) {
				case "budgets_protected":
					switch (colNames[i].toLowerCase()) {
						case "balance":
							setInputNumberListener(input, false);
							break;
					}
					break;
				
				case "expends":
					if (i == colNames.length - 1) createRowTotal();
					switch (colNames[i].toLowerCase()) {
						case "price":
							{
								indexViews.put("indexPrice", i);
								setInputNumberListener(input, false);
									
								input.addTextChangedListener(new TextWatcher() {

										@Override
										public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4)
										{

										}

										@Override
										public void onTextChanged(CharSequence p1, int p2, int p3, int p4)
										{

										}

										@Override
										public void afterTextChanged(Editable textChanged)
										{
											EditText iptTotal = findViewById(R.id.viewTotal);
											if (iptTotal.isFocused()) return;
											EditText iptQuantity = (EditText) getViewByIndex(indexViews.get("indexQuantity"));
											try
											{
												String sPrice = textChanged.toString();
												String sQuantity = iptQuantity.getText().toString();
												if (!"".equals(sPrice) && !"".equals(sQuantity)) {
													double price = numConverter.parse(sPrice).doubleValue();
													double quantity = numConverter.parse(sQuantity).doubleValue();
													iptTotal.setText(String.format(Locale.US, "%,.0f", price * quantity));
												} else iptTotal.setText("");
											}
											catch (ParseException e)
											{stt.error(e.getMessage());}
										}
									});
								break;
							}
							
						case "quantity":
							{
								indexViews.put("indexQuantity", i);
								input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
								input.addTextChangedListener(new TextWatcher() {

										@Override
										public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4)
										{

										}

										@Override
										public void onTextChanged(CharSequence p1, int p2, int p3, int p4)
										{

										}

										@Override
										public void afterTextChanged(Editable textChanged)
										{
											EditText iptTotal = findViewById(R.id.viewTotal);
											if (iptTotal.isFocused()) return;
											EditText iptPrice = (EditText) getViewByIndex(indexViews.get("indexPrice"));
											try
											{
												String sQuantity = textChanged.toString();
												String sPrice = iptPrice.getText().toString();
												if (!"".equals(sPrice) && !"".equals(sQuantity)) {
													double price = numConverter.parse(sPrice).doubleValue();
													double quantity = numConverter.parse(sQuantity).doubleValue();
													iptTotal.setText(String.format(Locale.US, "%,.0f", price * quantity));
												} else iptTotal.setText("");
											}
											catch (ParseException e)
											{stt.error(e.getMessage());}
										}
									});
								break;
							}
					}
					break;
					
				case "funds":
					switch (colNames[i].toLowerCase()) {
						case "cash": case "balance1": case "balance2":
							setInputNumberListener(input, false);
							break;
					}
					break;
					
				case "products":
					switch (colNames[i].toLowerCase()) {
						case "price":
							setInputNumberListener(input, false);
							break;
					}
					break;
			}
		}
		// End of create views
		
		btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					finish();
				}
			});

		ibBudget.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					ImageButton ibBudget = (ImageButton) view;
					if (currentBudget == 0) {
						dirImage.setImageFile("logo_tpbank.jpg", ibBudget);
						currentBudget = 1;
					}
					else if (currentBudget == 1) {
						dirImage.setImageFile("logo_vietcombank.png", ibBudget);
						currentBudget = 2;
					}
					else {
						ibBudget.setImageResource(R.drawable.logo_cash);
						currentBudget = 0;
					}
				}
			});
		
		// Check for Insert mode or Update mode
		if (!Intent.ACTION_EDIT.equals(intAction)) {
			// Insert mode
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
						ContentValues data = new ContentValues();
						int numRows = layoutTop.getRowCount();
						for (int i = 1; i <= numRows; i++) {
							View view = getViewByIndex(i);
							if (view == null) continue;
							switch (dataTypes[i]) {
								case DATETIME:
									data.put(colNames[i], getDateTime(i).getTime() / 1000L);
									break;
										
								case INTEGER: case INT:
									if (view instanceof Spinner) {
										int selectedID = catalogRowIDs[i][1][1];
										data.put(colNames[i], selectedID);
										break;
									}
								default:
									EditText input = (EditText) view;
									String inputText = input.getText().toString();
									if ("".equals(inputText)) continue;
									switch (dataTypes[i]) {
										case INTEGER: case INT:
											Number numInteger = stringToNumber(inputText);
											if (numInteger != null) data.put(colNames[i], numInteger.intValue());
											break;
										case NUMERIC: case NUM: case LONG:
											Number numLong = stringToNumber(inputText);
											if (numLong != null) data.put(colNames[i], numLong.longValue());
											break;
										case REAL: case FLOAT: case DOUBLE:
											Number numDouble = stringToNumber(inputText);
											if (numDouble != null) data.put(colNames[i], numDouble.doubleValue());
											break;
										case BOOLEAN:
											data.put(colNames[i], Boolean.valueOf(inputText));
											break;
										case NULL:
											continue;
										case COLTYPE:
											String colBlobName = colNames[i - 1];
											String colBlobValue = data.getAsString(colBlobName);
											if (colBlobValue == null || "".equals(inputText)) continue;
											DataType blobType = DataType.valueOf(inputText);
											switch (blobType) {
												case INTEGER: case INT:
													Number nInteger = stringToNumber(colBlobValue);
													if (nInteger != null) data.put(colBlobName, nInteger.intValue());
													break;
												case NUMERIC: case NUM: case LONG:
													Number nLong = stringToNumber(colBlobValue);
													if (nLong != null) data.put(colBlobName, nLong.longValue());
													break;
												case REAL: case FLOAT: case DOUBLE:
													Number nDouble = stringToNumber(colBlobValue);
													if (nDouble != null) data.put(colBlobName, nDouble.doubleValue());
													break;
												case BOOLEAN:
													data.put(colBlobName, Boolean.valueOf(colBlobValue));
													break;
											}
											data.put(colNames[i], inputText);
											break;
										default:
											data.put(colNames[i], inputText);
									}
							}
						}
						
						if (data.size() > 0) {
							long result = db.insert(tblName, data);
							if (result == -1) stt.error("Adding new record to \"" + tblName.toUpperCase() + "\" failed.");
							else stt.success("Added new record to \"" + tblName.toUpperCase() + "\".");
							if ("funds".equals(tblName) || "budgets".equals(tblName)) finish();
							else reset();
						}
					}
				});
			reset();
			return;
		}
		
		// Edit mode
		loadRowData(rowID);
		
		btnClear.setText("Delete");
		btnClear.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					
				}
			});
		
		btnAdd.setText("Update");
		btnAdd.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v)
				{

				}
			});
	}
	
	private void dateTimePicker(int resource)
	{
		dateTimePicker(resource, null);
	}

	private void dateTimePicker(int resource, Date datetime)
	{
		if (datetime == null) {
			Long lDate = pickedRows[pickedRow][0];
			Long lTime = pickedRows[pickedRow][1];
			if (lDate == null ) lDate = System.currentTimeMillis();
			if (lTime == null ) lTime = System.currentTimeMillis();
			Date pickedDate = new Date(lDate);
			Date pickedTime = new Date(lTime);
			String date = dff.format(pickedDate);
			String time = tff.format(pickedTime);
			try
			{
				datetime = flocal.parse(date + " " + time);
			}
			catch (ParseException e)
			{stt.error(e.getMessage());}
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final DateTimePickerAdapter adapter = new DateTimePickerAdapter(this, resource, datetime);
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
						btnDatePicker.setText(dff.format(pickedDate));
						pickedRows[pickedRow][0] = pickedDate.getTime();
					}
					if (pickedTime != null) {
						btnTimePicker.setText(tff.format(pickedTime));
						pickedRows[pickedRow][1] = pickedTime.getTime();
					}
				}
			});
		AlertDialog picker = builder.create();
		adapter.setDialog(picker);
		picker.show();
	}
	
	private Date getDateTime(int row) {
		String date = dff.format(new Date(pickedRows[row][0]));
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
		int numRow = layoutTop.getRowCount();
		boolean isFocus = false;
		for (int i = 1; i <= numRow; i++) {
			View view = getViewByIndex(i);
			if (view == null) continue;
			String name = view.getClass().getSimpleName();
			switch (name) {
				case "EditText":
					EditText input = (EditText) view;
					Integer indexProduct = indexViews.get("indexProduct");
					Integer indexPrice = indexViews.get("indexPrice");
					Integer indexQuantity = indexViews.get("indexQuantity");
					// check for catalogRow array instead
					if ("expends".equals(tblName) && i == indexPrice) {
						Spinner catalog = (Spinner) getViewByIndex(indexProduct);
						String selected = (String) catalog.getSelectedItem();
						List<Map<String, Object>> products = db.sql("products", "SELECT name, price FROM products ORDER BY name COLLATE NOCASE", null);
						for (Map<String, Object> product : products) {
							String pName = (String) product.get("name");
							if (selected.equals(pName)) {
								double price = product.get("price");
								input.setText(String.format("%,.0f", price));
								break;
							}
						}
					}
					else if ("expends".equals(tblName) && i == indexQuantity) input.setText("1");
					else input.setText("");
					if (!isFocus) {
						input.requestFocus();
						input.setSelection(input.getText().toString().length());
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
							btnDatePicker.setText(dff.format(now));
							btnTimePicker.setText(tff.format(now));
							pickedRows[i][0] = now.getTime();
							pickedRows[i][1] = now.getTime();
							break;
					}
					break;
			}
		}
		EditText iptTotal = findViewById(R.id.viewTotal);
		if (iptTotal != null && iptTotal.isFocusable()) iptTotal.setFocusable(false);
	}
	
	private void setViewID(View view, int index) {
		int id = -1;
		switch (index) {
			case 0:
				id = R.id.view00;
				break;
			case 1:
				id = R.id.view01;
				break;
			case 2:
				id = R.id.view02;
				break;
			case 3:
				id = R.id.view03;
				break;
			case 4:
				id = R.id.view04;
				break;
			case 5:
				id = R.id.view05;
				break;
			case 6:
				id = R.id.view06;
				break;
			case 7:
				id = R.id.view07;
				break;
			case 8:
				id = R.id.view08;
				break;
			case 9:
				id = R.id.view09;
				break;
			case 10:
				id = R.id.view10;
				break;
			case 11:
				id = R.id.view11;
				break;
			case 12:
				id = R.id.view12;
				break;
			case 13:
				id = R.id.view13;
				break;
			case 14:
				id = R.id.view14;
				break;
			case 15:
				id = R.id.view15;
				break;
			case 16:
				id = R.id.view16;
				break;
			case 17:
				id = R.id.view17;
				break;
			case 18:
				id = R.id.view18;
				break;
			case 19:
				id = R.id.view19;
				break;
			case 20:
				id = R.id.view20;
				break;
		}
		view.setId(id);
	}
	
	private View getViewByIndex(int index) {
		int id = -1;
		switch (index) {
			case 0:
				id = R.id.view00;
				break;
			case 1:
				id = R.id.view01;
				break;
			case 2:
				id = R.id.view02;
				break;
			case 3:
				id = R.id.view03;
				break;
			case 4:
				id = R.id.view04;
				break;
			case 5:
				id = R.id.view05;
				break;
			case 6:
				id = R.id.view06;
				break;
			case 7:
				id = R.id.view07;
				break;
			case 8:
				id = R.id.view08;
				break;
			case 9:
				id = R.id.view09;
				break;
			case 10:
				id = R.id.view10;
				break;
			case 11:
				id = R.id.view11;
				break;
			case 12:
				id = R.id.view12;
				break;
			case 13:
				id = R.id.view13;
				break;
			case 14:
				id = R.id.view14;
				break;
			case 15:
				id = R.id.view15;
				break;
			case 16:
				id = R.id.view16;
				break;
			case 17:
				id = R.id.view17;
				break;
			case 18:
				id = R.id.view18;
				break;
			case 19:
				id = R.id.view19;
				break;
			case 20:
				id = R.id.view20;
				break;
		}
		View view = findViewById(id);
		if (view == null) stt.error("View at index \"" + index + "\" not found.");
		return view;
	}
	
	private LinearLayout createRowTotal() {
		RelativeLayout mainLayout = (RelativeLayout) layoutTop.getParent();
		RelativeLayout.LayoutParams totalRowParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		totalRowParams.setMargins(20, 0, 20, 0);
		totalRowParams.addRule(RelativeLayout.BELOW, R.id.editorLayoutTop);
		LinearLayout rowTotal = new LinearLayout(this);
		rowTotal.setLayoutParams(totalRowParams);
		rowTotal.setOrientation(LinearLayout.HORIZONTAL);
		rowTotal.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		rowTotal.setId(R.id.layoutRowTotal);

		LinearLayout.LayoutParams tvTotalParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		tvTotalParams.setMargins(0, 0, 20, 0);
		TextView tvTotal = new TextView(this);
		tvTotal.setLayoutParams(tvTotalParams);
		tvTotal.setTypeface(tvTotal.getTypeface(), Typeface.BOLD);
		tvTotal.setText("TOTAL :");
		rowTotal.addView(tvTotal);

		LinearLayout.LayoutParams iptTotalParams = new LinearLayout.LayoutParams(310, LayoutParams.WRAP_CONTENT);
		final EditText iptTotal = new EditText(this);
		iptTotal.setLayoutParams(iptTotalParams);
		iptTotal.setTypeface(iptTotal.getTypeface(), Typeface.BOLD);
		iptTotal.setSingleLine();
		iptTotal.setGravity(Gravity.RIGHT);
		iptTotal.setBackground(null);
		iptTotal.setFocusable(false);
		iptTotal.setId(R.id.viewTotal);
		iptTotal.setText("0");
		iptTotal.setInputType(InputType.TYPE_CLASS_NUMBER);
		rowTotal.addView(iptTotal);

		iptTotal.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v)
				{
					if (!v.isFocusable()) {
						EditText iptTotal = (EditText) v;
						iptTotal.setFocusable(true);
						iptTotal.setFocusableInTouchMode(true);
						iptTotal.requestFocus();
						iptTotal.setSelection(iptTotal.getText().toString().length());
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						if (imm != null) {
							imm.showSoftInput(iptTotal, InputMethodManager.SHOW_IMPLICIT);
						}
					}
				}
			});

		iptTotal.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean focus)
				{
					EditText input = (EditText) v;
					if (focus) {
						TypedValue dfEdtBackground = new TypedValue();
						getTheme().resolveAttribute(android.R.attr.editTextBackground, dfEdtBackground, true);
						input.setBackgroundResource(dfEdtBackground.resourceId);
					}
					else input.setBackground(null);
					
					String sNumber = input.getText().toString();
					if ("".equals(sNumber)) return;
					try {
						double number = numConverter.parse(sNumber).doubleValue();
						if (focus) input.setText(String.format(Locale.US, "%.0f", number));
						else input.setText(String.format(Locale.US, "%,.0f", number));
					}
					catch (ParseException e) {stt.error(e.getMessage());}
				}
			});
			
		iptTotal.addTextChangedListener(new TextWatcher() {

				@Override
				public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4)
				{

				}

				@Override
				public void onTextChanged(CharSequence p1, int p2, int p3, int p4)
				{

				}

				@Override
				public void afterTextChanged(Editable textChanged)
				{
					if (!iptTotal.isFocused()) return;
					EditText iptPrice = (EditText) getViewByIndex(indexViews.get("indexPrice"));
					EditText iptQuantity = (EditText) getViewByIndex(indexViews.get("indexQuantity"));
					try
					{
						String sTotal = textChanged.toString();
						String sPrice = iptPrice.getText().toString();
						if (!"".equals(sTotal) && !"".equals(sPrice)) {
							double total = numConverter.parse(sTotal).doubleValue();
							double price = numConverter.parse(sPrice).doubleValue();
							iptQuantity.setText(String.format(Locale.US, "%,.7f", total / price));
						} else iptQuantity.setText("");
					}
					catch (ParseException e)
					{stt.error(e.getMessage());}
				}
			});

		mainLayout.addView(rowTotal, 1);
		return rowTotal;
	}
	
	private String objectToString(Object object, DataType type) {
		if (object == null) return null;
		String result = null;
		switch (type) {
			case INTEGER: case INT: case NUMERIC: case NUM: case LONG:
				result = String.format(Locale.US, "%,d", (Long) object);
				break;
			case REAL: case FLOAT: case DOUBLE:
				result = String.format(Locale.US, "%,.0f", (Double) object);
				break;
			case TEXT:
				result = (String) object;
				break;
			case BOOLEAN:
				result = (Boolean) object ? "1" : "0";
				break;
			case DATETIME:
				result = flocal.format((Date) object);
				break;
			case JSON:
				try
				{
					if (object instanceof JSONObject) result = ((JSONObject) object).toString(4);
					else result = ((JSONArray) object).toString(4);
				}
				catch (JSONException e)
				{stt.error(e.getMessage());}
				break;
			case NULL:
				result = null;
				break;
			case BLOB: default:
				if (object instanceof String) result = (String) object;
				else if (object instanceof Integer || object instanceof Long) result = String.format(Locale.US, "%,d", (Long) object);
				else if (object instanceof Float || object instanceof Double) result = String.format(Locale.US, "%,.0f", (Double) object);
		}
		return result;
	}
	
	private void loadRowData(int rowID) {
		int numRows = layoutTop.getRowCount();
		boolean isFocus = false;
		for (int i = 1; i <= numRows; i++) {
			View view = getViewByIndex(i);
			if (view == null) continue;
			switch (dataTypes[i]) {
				case DATETIME:
				{
					if (view instanceof LinearLayout) {
						LinearLayout layoutPicker = (LinearLayout) view;
						Button btnDatePicker = (Button) layoutPicker.getChildAt(0);
						Button btnTimePicker = (Button) layoutPicker.getChildAt(1);
						final Date datetime = (Date) colValues[i];
						final int row = i;
						btnDatePicker.setText(dff.format(datetime));
						btnDatePicker.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v)
								{
									dateTimePicker(R.layout.date_picker, datetime);
									pickedRow = row;
								}
							});
						
						btnTimePicker.setText(tff.format(datetime));
						btnTimePicker.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v)
								{
									dateTimePicker(R.layout.time_picker, datetime);
									pickedRow = row;
								}
							});
						pickedRows[i][0] = datetime.getTime();
						pickedRows[i][1] = datetime.getTime();
					}
					break;
				}
				case INTEGER: case INT:
				{
					if (view instanceof Spinner) {
						// set selected data to new array catalogRow
						Spinner catalog = (Spinner) view;
						for (Map<String, Object> foreignColumn : foreignColumns) {
							String fColName = (String) foreignColumn.get("srcColumn");
							if (fColName.equals(colNames[i])) {
								int itemID = colValues[i];
								Integer[] ids = (Integer[]) foreignColumn.get("ids");
								if ("funds".equals(tblName)) {
									String[] names = (String[]) foreignColumn.get("names");
									for (int indexName = 0; indexName < names.length; indexName++) {
										if ("Cash".equals(names[indexName])) {
											List<Integer> idsNoCash = new ArrayList<Integer>(Arrays.asList(ids));
											idsNoCash.remove(indexName);
											ids = idsNoCash.toArray(new Integer[0]);
											break;
										}
									}
								}
								for (int indexID = 0; indexID < ids.length; indexID++) {
									if (itemID == ids[indexID]) {
										catalog.setSelection(indexID);
										break;
									}
								}
								break;
							}
						}
						break;
					}
				}
				default:
				{
					EditText input = (EditText) view;
					String value = objectToString(colValues[i], dataTypes[i]);
					if (value != null) input.setText(value);
					if (!isFocus) {
						input.requestFocus();
						input.setSelection(input.getText().toString().length());
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						if (imm != null) {
							imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
						}
						isFocus = true;
					}
				}
			}
		}
	}
	
	private Number stringToNumber(String stringNumber) {
		Number number = null;
		if (stringNumber != null && !"".equals(stringNumber)) {
			try
			{
				number = numConverter.parse(stringNumber);
			}
			catch (ParseException e)
			{stt.error(e.getMessage());}
		}
		return number;
	}
	
	private void setInputNumberListener(View view, boolean decimal) {
		EditText input = (EditText) view;
		if (decimal) input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		else input.setInputType(InputType.TYPE_CLASS_NUMBER);
		input.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean focus)
				{
					EditText input = (EditText) v;
					String sNumber = input.getText().toString();
					if ("".equals(sNumber)) return;
					try {
						double number = numConverter.parse(sNumber).doubleValue();
						input.setText(String.format(Locale.US, "%,.0f", number));
					}
					catch (ParseException e) {stt.error(e.getMessage());}
				}
			});
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
