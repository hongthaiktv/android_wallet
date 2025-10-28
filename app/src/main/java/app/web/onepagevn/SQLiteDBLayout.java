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
	private Integer[] hideCols;
	private ViewGroup container;
	private TableLayout table, tBody;
	private RowOnClickListener listener;
	private int index = -1;
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
		TypedArray attrsArr = context.obtainStyledAttributes(attrs, R.styleable.SQLiteDBLayout);
		if (attrsArr.hasValue(R.styleable.SQLiteDBLayout_table)) tblName = attrsArr.getString(R.styleable.SQLiteDBLayout_table);
		if (attrsArr.hasValue(R.styleable.SQLiteDBLayout_ordHeader)) ordHeader = attrsArr.getString(R.styleable.SQLiteDBLayout_ordHeader);
		hScroll = attrsArr.getBoolean(R.styleable.SQLiteDBLayout_hScroll, hScroll);
		hideID = attrsArr.getBoolean(R.styleable.SQLiteDBLayout_hideID, hideID);
		List<Integer> hideCols = new ArrayList<>();
		int colIndex = attrsArr.getInt(R.styleable.SQLiteDBLayout_hideColumn, -1);
		if (colIndex >= 0) hideCols.add(colIndex);
		int colIndex2 = attrsArr.getInt(R.styleable.SQLiteDBLayout_hideColumn2, -1);
		if (colIndex2 >= 0) hideCols.add(colIndex2);
		int colIndex3 = attrsArr.getInt(R.styleable.SQLiteDBLayout_hideColumn3, -1);
		if (colIndex3 >= 0) hideCols.add(colIndex3);
		int colIndex4 = attrsArr.getInt(R.styleable.SQLiteDBLayout_hideColumn4, -1);
		if (colIndex4 >= 0) hideCols.add(colIndex4);
		int colIndex5 = attrsArr.getInt(R.styleable.SQLiteDBLayout_hideColumn5, -1);
		if (colIndex5 >= 0) hideCols.add(colIndex5);
		this.hideCols = hideCols.toArray(new Integer[0]);
		attrsArr.recycle();
		show();
	}
	
	private void init(Context context, String tblName, ViewGroup container, RowOnClickListener listener) {
		this.context = context;
		this.tblName = tblName;
		this.container = container;
		this.listener = listener;
		LayoutParams layout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1);
		setLayoutParams(layout);
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
		return this;
	}
	
	public SQLiteDBLayout setOrdHeader(String colName) {
		this.ordHeader = colName;
		return this;
	}
	
	public SQLiteDBLayout hideColumn(int index) {
		table.setColumnCollapsed(index, true);
		tBody.setColumnCollapsed(index, true);
		return this;
	}

	public SQLiteDBLayout unhideColumn(int index) {
		table.setColumnCollapsed(index, false);
		tBody.setColumnCollapsed(index, false);
		return this;
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
		if (hideID) hideColumn(1);
		if (hideCols != null && hideCols.length > 0) {
			for (int index : hideCols) {
				hideColumn(index);
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
