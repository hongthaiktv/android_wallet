package app.web.onepagevn;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Editor extends AppCompatActivity
{
	private GridLayout layoutTop;
	private LinearLayout layoutButtons;
	private Button btnClear, btnAdd, btnCancel;
	private StatusBar stt;
	private SQLiteDB db;
	private String tblName = "expends", mode = "insert", title = "";
	private Map<String, DataType> dataTypes;
	private DataType[] colTypes;
	private String[] colNames, labels;
	private Object[] colValues;
	private List<Map<String, Object>> foreignColumns;
	private Date pickedDate, pickedTime;
	private int rowID = -1, pickedRow = -1, currentBudget = 1;
	private Long[][] pickedRows = new Long[20][2];
	private Integer[][][] catalogRowIDs = new Integer[20][2][];
	private String[][][] catalogRowNames = new String[20][2][];
	private SimpleDateFormat df, dff, tf, tff, ff, flocal;
	private NumberFormat numFormatter;
	private Map<String, Integer> indexViews = new HashMap<>();
	private AppAssetManager dirImage;
	private FileManager fm;
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
		ibBudget = findViewById(R.id.editorIbBudget);
		stt = new StatusBar(this).init();
		db = new SQLiteDB();
		intent = getIntent();
		tblName = intent.getStringExtra("tblName");
		title = tblName.toUpperCase();
		switch (tblName) {
			case "balance": tblName = "expends"; title = tblName.toUpperCase(); break;
			case "budgets": tblName = "budgets_protected"; title = "BUDGETS"; break;
		}
		setTitle(title);
		
		df = new SimpleDateFormat("dd-MM");
		dff = new SimpleDateFormat("dd-MM-yyyy");
		tf = new SimpleDateFormat("HH:mm");
		tff = new SimpleDateFormat("HH:mm:ss");
		ff = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		flocal = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		dirImage = new AppAssetManager(this, "images/");
		dirImage.setImageFile("logo_cash.png", ibBudget);
		fm = new FileManager(this);
		numFormatter = NumberFormat.getInstance(Locale.US);
		numFormatter.setMaximumFractionDigits(7);
		
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
		rowID = intent.getIntExtra("rowID", -1);
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
				List<String> originColNames = new ArrayList<String>(Arrays.asList(colNames));
				originColNames.remove(colNames.length - 1);
				colNames = originColNames.toArray(new String[0]);
				break;
			}
			case "budgets_protected":
			{
				List<String> originColNames = new ArrayList<String>(Arrays.asList(colNames));
				originColNames.remove(colNames.length - 1);
				colNames = originColNames.toArray(new String[0]);
				break;
			}
		}
		
		dataTypes = db.getDataTypes(tblName);
		colTypes = dataTypes.values().toArray(new DataType[0]);
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
						String[] items = (String[]) fColumn.get("names");
						if ("funds".equals(tblName)) {
							List<Integer> idsNoCash = new ArrayList<Integer>(Arrays.asList(ids));
							List<String> itemsNoCash = new ArrayList<String>(Arrays.asList(items));
							idsNoCash.remove(itemsNoCash.indexOf("Cash"));
							itemsNoCash.remove("Cash");
							ids = idsNoCash.toArray(new Integer[0]);
							items = itemsNoCash.toArray(new String[0]);
						}
						catalogRowIDs[i][0] = ids;
						catalogRowNames[i][0] = items;
						catalogRowIDs[i][1] = new Integer[] {catalogRowIDs[i][0][0], 0};
						catalogRowNames[i][1] = new String[] {catalogRowNames[i][0][0], "0"};
						
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
										
										catalogRowIDs[row][1] = new Integer[] {catalogRowIDs[row][0][index], index};
										catalogRowNames[row][1] = new String[] {catalogRowNames[row][0][index], String.valueOf(index)};
										
										Integer indexPrice = indexViews.get("indexPrice");
										if (indexPrice == null) return;
										EditText inputPrice = (EditText) getViewByIndex(indexPrice);
										int itemID = catalogRowIDs[row][1][0];
										
										List<Map<String, Object>> products = db.sql("products", "SELECT price FROM products WHERE productid=?", new String[] {String.valueOf(itemID)});
										double price = products.get(0).get("price");
										String formatedPrice = String.format(Locale.US, "%,.0f", price);
										inputPrice.requestFocus();
										inputPrice.setText(formatedPrice);
										inputPrice.setSelection(formatedPrice.length());
									}

									@Override
									public void onNothingSelected(AdapterView<?> adapter)
									{

									}
								});
						}
						else if ("funds".equals(tblName)) {
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

										Map<String, Double> budgetBalances = db.getBudgetBalances();
										String bankName = catalogRowNames[row][0][index];
										double balance = budgetBalances.get(bankName);
										String formatedBalance = String.format(Locale.US, "%,.0f", balance);
										EditText input = (EditText) getViewByIndex(row + 1);
										input.setText(formatedBalance);
										catalogRowIDs[row][1] = new Integer[] {catalogRowIDs[row][0][index], index};
										catalogRowNames[row][1] = new String[] {catalogRowNames[row][0][index], String.valueOf(index)};
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
										catalogRowIDs[row][1] = new Integer[] {catalogRowIDs[row][0][index], index};
										catalogRowNames[row][1] = new String[] {catalogRowNames[row][0][index], String.valueOf(index)};
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
			
			switch (colTypes[i]) {
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
					layoutPicker.setBackgroundColor(Color.LTGRAY);
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
					
				case BOOLEAN:
					GridLayout.LayoutParams lCheckBoxParams = new GridLayout.LayoutParams();
					lCheckBoxParams.width = 0;
					lCheckBoxParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
					lCheckBoxParams.setMargins(20, 20, 20, 20);
					LinearLayout layoutCheckBox = new LinearLayout(this);
					layoutCheckBox.setLayoutParams(lCheckBoxParams);
					layoutCheckBox.setPadding(0, 0, 0, 0);
					layoutCheckBox.setOrientation(LinearLayout.HORIZONTAL);
					layoutCheckBox.setBaselineAligned(true);
					
					LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					CheckBox checkBox = new CheckBox(this);
					checkBox.setLayoutParams(checkBoxParams);
					checkBox.setGravity(Gravity.CENTER);
					setViewID(checkBox, i);
					
					layoutCheckBox.addView(checkBox);
					layoutCheckBox.setBaselineAlignedChildIndex(0);
					layoutTop.addView(layoutCheckBox);
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
													double price = numFormatter.parse(sPrice).doubleValue();
													double quantity = numFormatter.parse(sQuantity).doubleValue();
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
													double price = numFormatter.parse(sPrice).doubleValue();
													double quantity = numFormatter.parse(sQuantity).doubleValue();
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
					if (currentBudget == 1) {
						dirImage.setImageFile("logo_tpbank.png", ibBudget);
						currentBudget = 2;
					}
					else if (currentBudget == 2) {
						dirImage.setImageFile("logo_vietcombank.png", ibBudget);
						currentBudget = 3;
					}
					else {
						dirImage.setImageFile("logo_cash.png", ibBudget);
						currentBudget = 1;
					}
				}
			});
			
		switch (tblName) {
			case "todos":
				CheckBox checkBox = (CheckBox) getViewByColumnName("complete").get("view");
				checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton v, boolean checked)
						{
							Map<String, Object> completeDate = getViewByColumnName("completedate");
							int index = completeDate.get("index");
							LinearLayout layoutCompleteDate = (LinearLayout) completeDate.get("view");
							Button btnCompleteDatePicker = (Button) layoutCompleteDate.getChildAt(0);
							Button btnCompleteTimePicker = (Button) layoutCompleteDate.getChildAt(1);
							if (checked) {
								Date now = new Date();
								btnCompleteDatePicker.setText(dff.format(now));
								btnCompleteTimePicker.setText(tff.format(now));
								pickedRows[index][0] = now.getTime();
								pickedRows[index][1] = now.getTime();
							}
							else {
								Map<String, Object> createDate = getViewByColumnName("createdate");
								int indexCreateDate = createDate.get("index");
								Date dCreateDate = new Date(pickedRows[indexCreateDate][0]);
								Date dCreateTime = new Date(pickedRows[indexCreateDate][1]);
								String sCreateDate = dff.format(dCreateDate);
								String sCreateTime = tff.format(dCreateTime);
								btnCompleteDatePicker.setText(sCreateDate);
								btnCompleteTimePicker.setText(sCreateTime);
								pickedRows[index][0] = dCreateDate.getTime();
								pickedRows[index][1] = dCreateTime.getTime();
							}
						}
					});
				break;
		}
		
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
						ContentValues data = getFormData();
						if (data.size() > 0) {
							switch (tblName) {
								case "expends":
									data.put("budgetid", currentBudget);
								break;
							}
							long result = db.insert(tblName, data);
							String tblMsgName = "budgets_protected".equals(tblName) ? "BUDGETS" : tblName.toUpperCase();
							if (result != -1) {
								stt.success("Added new record to \"" + tblMsgName + "\".");
								if ("funds".equals(tblName) || "budgets_protected".equals(tblName) || "todos".equals(tblName) || "incomes".equals(tblName)) finish();
								else reset();
							}
							else stt.error("Adding new record to \"" + tblMsgName + "\" failed.");
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
					int result = db.delete(tblName, rowID);
					String tblMsgName = "budgets_protected".equals(tblName) ? "BUDGETS" : tblName.toUpperCase();
					if (result != -1) {
						stt.success("Deleted a record from \"" + tblMsgName + "\".");
						finish();
					}
					else stt.error("Deleting record from \"" + tblMsgName + "\" failed.");
				}
			});
		
		btnAdd.setText("Update");
		btnAdd.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v)
				{
					ContentValues data = getFormData();
					if (data.size() > 0) {
						switch (tblName) {
							case "expends":
								data.put("budgetid", currentBudget);
								break;
						}
						int result = db.update(tblName, rowID, data);
						String tblMsgName = "budgets_protected".equals(tblName) ? "BUDGETS" : tblName.toUpperCase();
						if (result != -1) {
							stt.success("Updated a record from \"" + tblMsgName + "\".");
							finish();
						}
						else stt.error("Updating record from \"" + tblMsgName + "\" failed.");
					}
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
		Map<String, Double> budgetBalances = null;
		if ("funds".equals(tblName)) budgetBalances = db.getBudgetBalances();
		
		for (int i = 1; i <= numRow; i++) {
			View view = getViewByIndex(i);
			if (view == null) continue;
			
			switch (colTypes[i]) {
				case DATETIME:
				{
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
				
				case BOOLEAN:
					CheckBox checkBox = (CheckBox) view;
					checkBox.setChecked(false);
					break;

				case INTEGER: case INT:
					if (view instanceof Spinner) {
						Spinner catalog = (Spinner) view;
						if ("funds".equals(tblName) && "bank2".equals(colNames[i].toLowerCase())) catalog.setSelection(1);
						else if ("todos".equals(tblName) && "priorityid".equals(colNames[i].toLowerCase())) catalog.setSelection(2);
						else catalog.setSelection(0);
						break;
					}
					
				default:
					EditText input = (EditText) view;
					switch (tblName) {
						case "expends":
						{
							Integer indexProduct = indexViews.get("indexProduct");
							Integer indexPrice = indexViews.get("indexPrice");
							Integer indexQuantity = indexViews.get("indexQuantity");
							if (i == indexPrice) {
								int itemID = catalogRowIDs[indexProduct][0][0];
								List<Map<String, Object>> products = db.sql("products", "SELECT price FROM products WHERE productid=?", new String[] {String.valueOf(itemID)});
								double price = products.get(0).get("price");
								String formatedPrice = String.format(Locale.US, "%,.0f", price);
								input.requestFocus();
								InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
								if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
								isFocus = true;
								input.setText(formatedPrice);
								input.setSelection(formatedPrice.length());
							}
							else if (i == indexQuantity) input.setText("1");
							else input.setText("");
							break;
						}
						case "funds":
						{
							switch (colNames[i].toLowerCase()) {
								case "cash":
									double cash = budgetBalances.get("Cash");
									String formatedCash = String.format(Locale.US, "%,.0f", cash);
									input.requestFocus();
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
									isFocus = true;
									input.setText(formatedCash);
									input.setSelection(formatedCash.length());
									break;
								case "balance1":
									int bank1Index = i - 1;
									String bank1Name = catalogRowNames[bank1Index][0][0];
									double balance1 = budgetBalances.get(bank1Name);
									String formatedBalance1 = String.format(Locale.US, "%,.0f", balance1);
									input.setText(formatedBalance1);
									break;
								case "balance2":
									int bank2Index = i - 1;
									String bank2Name = catalogRowNames[bank2Index][0][1];
									double balance2 = budgetBalances.get(bank2Name);
									String formatedBalance2 = String.format(Locale.US, "%,.0f", balance2);
									input.setText(formatedBalance2);
									break;
							}
							break;
						}
						default:
							input.setText("");
					}
					if (!isFocus) {
						input.requestFocus();
						input.setSelection(input.getText().toString().length());
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
						isFocus = true;
					}
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
	
	private Map<String, Object> getViewByColumnName(String colName) {
		Map<String, Object> result = null;
		int numRow = layoutTop.getRowCount();
		for (int i = 1; i <= numRow; i++) {
			if (colName.equals(colNames[i])) {
				result = new HashMap<>();
				result.put("view", getViewByIndex(i));
				result.put("index", i);
				break;
			}
		}
		return result;
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
						double number = numFormatter.parse(sNumber).doubleValue();
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
							double total = numFormatter.parse(sTotal).doubleValue();
							double price = numFormatter.parse(sPrice).doubleValue();
							iptQuantity.setText(numFormatter.format(total / price));
						} else iptQuantity.setText("");
					}
					catch (ParseException e)
					{stt.error(e.getMessage());}
				}
			});

		mainLayout.addView(rowTotal, 1);
		return rowTotal;
	}
	
	private void loadRowData(int rowID) {
		int numRows = layoutTop.getRowCount();
		boolean isFocus = false;
		for (int i = 1; i <= numRows; i++) {
			View view = getViewByIndex(i);
			if (view == null) continue;
			switch (colTypes[i]) {
				case DATETIME:
				{
					if (view instanceof LinearLayout) {
						LinearLayout layoutPicker = (LinearLayout) view;
						Button btnDatePicker = (Button) layoutPicker.getChildAt(0);
						Button btnTimePicker = (Button) layoutPicker.getChildAt(1);
						Date datetime = (Date) colValues[i];
						pickedRows[i][0] = datetime.getTime();
						pickedRows[i][1] = datetime.getTime();
						btnDatePicker.setText(dff.format(datetime));
						btnTimePicker.setText(tff.format(datetime));
					}
					break;
				}
				
				case BOOLEAN:
				{
					CheckBox checkBox = (CheckBox) view;
					boolean checked = (Boolean) colValues[i];
					checkBox.setChecked(checked);
					break;
				}
				
				case INTEGER: case INT:
				{
					if (view instanceof Spinner) {
						Spinner catalog = (Spinner) view;
						int id = colValues[i];
						Integer[] itemIDs = catalogRowIDs[i][0];
						for (int itemIndex = 0; itemIndex < itemIDs.length; itemIndex++) {
							if (id == itemIDs[itemIndex]) {
								catalog.setSelection(itemIndex);
								catalogRowIDs[i][1] = new Integer[] {id, itemIndex};
								catalogRowNames[i][1] = new String[] {catalogRowNames[i][0][itemIndex], String.valueOf(itemIndex)};
								break;
							}
						}
						break;
					}
				}
				
				default:
				{
					EditText input = (EditText) view;
					String value = objectToString(colValues[i], colTypes[i]);
					if (!isFocus) {
						input.requestFocus();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
						isFocus = true;
						if (value != null) input.setText(value);
						input.setSelection(input.getText().toString().length());
					}
					else if (value != null) input.setText(value);
				}
			}
		}
		switch (tblName) {
			case "expends":
				int budget = colValues[numRows + 1];
				switch (budget) {
					case 1:
						dirImage.setImageFile("logo_cash.png", ibBudget);
						currentBudget = 1;
						break;
					case 2:
						dirImage.setImageFile("logo_tpbank.png", ibBudget);
						currentBudget = 2;
						break;
					case 3:
						dirImage.setImageFile("logo_vietcombank.png", ibBudget);
						currentBudget = 3;
						break;
				}
				break;
		}
	}
	
	private Number stringToNumber(String stringNumber) {
		Number number = null;
		if (stringNumber != null && !"".equals(stringNumber)) {
			try
			{
				number = numFormatter.parse(stringNumber);
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
						double number = numFormatter.parse(sNumber).doubleValue();
						if (focus) input.setText(String.format(Locale.US, "%.0f", number));
						else input.setText(String.format(Locale.US, "%,.0f", number));
					}
					catch (ParseException e) {stt.error(e.getMessage());}
				}
			});
	}
	
	private ContentValues getFormData() {
		ContentValues data = new ContentValues();
		int numRows = layoutTop.getRowCount();
		for (int i = 1; i <= numRows; i++) {
			View view = getViewByIndex(i);
			if (view == null) continue;
			switch (colTypes[i]) {
				case DATETIME:
					data.put(colNames[i], getDateTime(i).getTime() / 1000L);
					break;
					
				case BOOLEAN:
					CheckBox checkBox = (CheckBox) view;
					boolean checked = checkBox.isChecked();
					data.put(colNames[i], checked);
					break;

				case INTEGER: case INT:
					if (view instanceof Spinner) {
						int selectedID = catalogRowIDs[i][1][0];
						data.put(colNames[i], selectedID);
						break;
					}

				default:
					EditText input = (EditText) view;
					String inputText = input.getText().toString();
					if ("".equals(inputText)) continue;
					switch (colTypes[i]) {
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
		return data;
	}
	
	public String objectToString(Object value, DataType type) {
		if (value == null) return null;
		String result = null;
		switch (type) {
			case INTEGER: case INT: case NUMERIC: case NUM: case LONG:
			case REAL: case FLOAT: case DOUBLE:
				result = numFormatter.format(value);
				break;
			case TEXT:
				result = (String) value;
				break;
			case BOOLEAN:
				result = (Boolean) value ? "true" : "false";
				break;
			case DATETIME:
				result = flocal.format((Date) value);
				break;
			case JSON:
				try
				{
					if (value instanceof JSONObject) result = ((JSONObject) value).toString(4);
					else result = ((JSONArray) value).toString(4);
				}
				catch (JSONException e)
				{stt.error(e.getMessage());}
				break;
			case NULL:
				result = null;
				break;
			case BLOB: default:
				if (value instanceof String) result = (String) value;
				else if (value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double) result = numFormatter.format(value);
				else if (value instanceof Boolean) result = (Boolean) value ? "true" : "false";
				else if (value instanceof Date) result = flocal.format((Date) value);
				else if (value instanceof JSONObject) {
					try
					{
						result = ((JSONObject) value).toString(4);
					}
					catch (JSONException e)
					{stt.error(e.getMessage());}
				}
				else if (value instanceof JSONArray) {
					try
					{
						result = ((JSONArray) value).toString(4);
					}
					catch (JSONException e)
					{stt.error(e.getMessage());}
				}
		}
		return result;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		fm.onRequestPermissionsResultHandler(requestCode, permissions, grantResults);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		fm.onActivityResultHandler(requestCode, resultCode, data);
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
