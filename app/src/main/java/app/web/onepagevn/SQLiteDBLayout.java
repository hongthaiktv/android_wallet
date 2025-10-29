package app.web.onepagevn;
import android.content.*;
import android.content.res.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import android.graphics.*;

public class SQLiteDBLayout extends LinearLayout
{
	private Context context;
	private String tblName = "", ordHeader = "NO.";
	private boolean hScroll = false, hideID = true;
	private Set<Integer> hideColumns = new HashSet<>();
	private ViewGroup container;
	private TableLayout table, tBody;
	private RowOnClickListener listener;
	private int index = -1;
	private StatusBar stt = new StatusBar("SQLiteDBLayout");
	private SQLiteDB db = new SQLiteDB();
	
	public SQLiteDBLayout(Context context, String tblName) {
		super(context);
		init(context, tblName, null, null);
	}
	
	public SQLiteDBLayout(Context context, String tblName, RowOnClickListener onClick) {
		super(context);
		init(context, tblName, null, onClick);
	}
	
	public SQLiteDBLayout(Context context, String tblName, ViewGroup container) {
		super(context);
		init(context, tblName, container, null);
	}
	
	public SQLiteDBLayout(Context context, String tblName, ViewGroup container, RowOnClickListener onClick) {
		super(context);
		init(context, tblName, container, onClick);
	}
	
	public SQLiteDBLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		
		TypedArray attrsMain = context.obtainStyledAttributes(attrs, R.styleable.SQLiteDBLayout);
		if (attrsMain.hasValue(R.styleable.SQLiteDBLayout_table)) tblName = attrsMain.getString(R.styleable.SQLiteDBLayout_table);
		if (attrsMain.hasValue(R.styleable.SQLiteDBLayout_ordHeader)) ordHeader = attrsMain.getString(R.styleable.SQLiteDBLayout_ordHeader);
		hScroll = attrsMain.getBoolean(R.styleable.SQLiteDBLayout_hScroll, hScroll);
		hideID = attrsMain.getBoolean(R.styleable.SQLiteDBLayout_hideID, hideID);
		if (hideID) hideColumns.add(1);
		int colIndex = attrsMain.getInt(R.styleable.SQLiteDBLayout_hideColumn, -1);
		if (colIndex >= 1) hideColumns.add(--colIndex);
		attrsMain.recycle();
		
		TypedArray attrsColumns = context.obtainStyledAttributes(attrs, R.styleable.SQLiteDBLayout_columns);
		for (int index = 0; index < R.styleable.SQLiteDBLayout_columns.length; index++) {
			if (!attrsColumns.getBoolean(index, true)) hideColumns.add(index);
		}
		attrsColumns.recycle();
		show();
	}
	
	private void init(Context context, String tblName, ViewGroup container, RowOnClickListener listener) {
		this.context = context;
		this.tblName = tblName;
		this.container = container;
		this.listener = listener;
		LayoutParams layout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1);
		setLayoutParams(layout);
		if (hideID) hideColumns.add(1);
		if (container != null) container.addView(this);
	}
	
	public SQLiteDBLayout setTable(String tblName) {
		this.tblName = tblName;
		return this;
	}
	
	public String getTable() {
		return tblName;
	}
	
	public SQLiteDBLayout setHScroll(boolean hScroll) {
		this.hScroll = hScroll;
		return this;
	}
	
	public SQLiteDBLayout setHideID(boolean hideID) {
		this.hideID = hideID;
		if (hideID) {
			hideColumns.add(1);
			hideColumn(1, true);
		} else {
			hideColumns.remove(1);
			hideColumn(1, false);
		}
		return this;
	}
	
	public SQLiteDBLayout setOrdHeader(String colName) {
		this.ordHeader = colName;
		return this;
	}
	
	public SQLiteDBLayout hideColumn(int index) {
		hideColumns.add(index);
		hideColumn(index, true);
		return this;
	}

	public SQLiteDBLayout unhideColumn(int index) {
		hideColumns.remove(index);
		hideColumn(index, false);
		return this;
	}
	
	private void hideColumn(int index, boolean hide) {
		if (hide) {
			table.setColumnCollapsed(index, true);
			tBody.setColumnCollapsed(index, true);
		} else {
			table.setColumnCollapsed(index, false);
			tBody.setColumnCollapsed(index, false);
		}
	}
	
	public SQLiteDBLayout setRowOnClickListener(RowOnClickListener listener) {
		this.listener = listener;
		return this;
	}
	
	public TableLayout getTableLayout() {
		return table;
	}
	
	public TableLayout getTableBody() {
		return tBody;
	}
	
	public int getIndex() {
		index = ((ViewGroup) getParent()).indexOfChild(this);
		return index;
	}
	
	public int getHidden() {
		return hideColumns.size();
	}
	
	public void show() {
		removeAllViews();
		if (tblName == null && "".equals(tblName)) return;
		List<Map<String, Object>> data = db.getData(tblName);
		if (data.size() < 1) return;
		
		table = new TableLayout(context);
		TableLayout.LayoutParams tblLayout = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		table.setLayoutParams(tblLayout);
		TableRow rowHeader = new TableRow(context);
		rowHeader.setBackgroundColor(Color.GRAY);
		rowHeader.addView(createColHeader(ordHeader));
		String[] colNames = data.get(0).keySet().toArray(new String[0]);
		int colNums = data.get(0).keySet().size();
		for (int colIndex = 0; colIndex < colNums; colIndex++) {
			String colName = colNames[colIndex];
			rowHeader.addView(createColHeader(colName));
		}
		
		ScrollView scroll = new ScrollView(context);
		tBody = new TableLayout(context);
		int rowIndex = 0;
		for (Map<String, Object> rowData : data) {
			final Object[] colValues = rowData.values().toArray();
			TableRow row = new TableRow(context);
			final int index = rowIndex;
			if (listener != null) row.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view)
						{
							int id = Integer.valueOf((String) colValues[0]);
							listener.onClick(view, index, id);
						}
					});
			if (rowIndex++ % 2 != 0) row.setBackgroundColor(Color.LTGRAY);
			row.addView(createColumn(rowIndex));
			for (int colIndex = 0; colIndex < colNums; colIndex++) {
				Object colData = rowData.get(colNames[colIndex]);
				row.addView(createColumn(colData));
			}
			tBody.addView(row);
		}
		if (hideColumns.size() > 0) {
			for (int index : hideColumns) {
				hideColumn(index, true);
			}
		}
		scroll.addView(tBody);
		table.addView(rowHeader);
		table.addView(scroll);
		if (hScroll) {
			HorizontalScrollView hScroll = new HorizontalScrollView(context);
			HorizontalScrollView.LayoutParams hsLayout = new HorizontalScrollView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			hScroll.setLayoutParams(hsLayout);
			hScroll.addView(table);
			addView(hScroll);
		} else addView(table);
	}
	
	private TextView createColumn(Object text) {
		TableRow.LayoutParams colLayout = new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
		TableRow.LayoutParams colHSLayout = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		TextView col = new TextView(context);
		if (hScroll) col.setLayoutParams(colHSLayout);
		else col.setLayoutParams(colLayout);
		col.setPadding(25, 25, 25, 25);
		String colText = text == null ? "" : String.valueOf(text);
		col.setText(colText);
		return col;
	}
	
	private TextView createColHeader(String text) {
		TextView header = createColumn(text.toUpperCase());
		header.setGravity(Gravity.CENTER);
		header.setTextColor(Color.WHITE);
		header.setTypeface(null, Typeface.BOLD);
		return header;
	}
	
	public static interface RowOnClickListener {
		public void onClick(View row, int index, int id);
	}
}
