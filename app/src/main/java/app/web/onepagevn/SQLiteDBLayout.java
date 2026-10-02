package app.web.onepagevn;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class SQLiteDBLayout extends LinearLayout
{
	private Context context;
	private String tblName = "", ordHeader = "#";
	private boolean hScroll = false, hideID = true, autoLoad = true;
	private Set<Integer> hideColumns = new HashSet<>();
	private Map<String, Object> layoutAttrs = new HashMap<String, Object>();
	private String sql = "";
	private ViewGroup container;
	private TableLayout table, tBody;
	private RowOnClickListener onClickListener;
	private RowOnLongClickListener onLongClickListener;
	private int[] rowIDs;
	private int index = -1;
	private StatusBar stt = new StatusBar("SQLiteDBLayout");
	private SQLiteDB db = new SQLiteDB();
	private List<Map<String, Object>> tblData;
	
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
		autoLoad = attrsMain.getBoolean(R.styleable.SQLiteDBLayout_autoLoad, autoLoad);
		layoutAttrs.put("limit", attrsMain.getInt(R.styleable.SQLiteDBLayout_limit, -1));
		layoutAttrs.put("sort", attrsMain.getString(R.styleable.SQLiteDBLayout_sort));
		sql = attrsMain.getString(R.styleable.SQLiteDBLayout_sql);
		if (hideID) hideColumns.add(1);
		int colIndex = attrsMain.getInt(R.styleable.SQLiteDBLayout_hideColumn, -1);
		if (colIndex > 0) hideColumns.add(--colIndex);
		attrsMain.recycle();
		
		TypedArray attrsColumns = context.obtainStyledAttributes(attrs, R.styleable.SQLiteDBLayout_columns);
		for (int index = 0; index < R.styleable.SQLiteDBLayout_columns.length; index++) {
			if (!attrsColumns.getBoolean(index, true)) hideColumns.add(index);
		}
		attrsColumns.recycle();
		if (autoLoad) show();
	}
	
	private void init(Context context, String tblName, ViewGroup container, RowOnClickListener onClickListener) {
		this.context = context;
		this.tblName = tblName;
		this.container = container;
		this.onClickListener = onClickListener;
		LayoutParams layout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1);
		setLayoutParams(layout);
		if (hideID) hideColumns.add(1);
		if (container != null) container.addView(this);
	}
	
	public SQLiteDBLayout setTable(String tblName) {
		this.tblName = tblName;
		return this;
	}
	
	public String getTableName() {
		return tblName;
	}
	
	public SQLiteDBLayout setHScroll(boolean hScroll) {
		this.hScroll = hScroll;
		return this;
	}
	
	public SQLiteDBLayout setHideID(boolean hideID) {
		this.hideID = hideID;
		if (hideID) hideColumn(2);
		else unhideColumn(2);
		return this;
	}
	
	public SQLiteDBLayout setOrdHeader(String colName) {
		this.ordHeader = colName;
		return this;
	}
	
	public SQLiteDBLayout setLimit(int limit) {
		layoutAttrs.put("limit", limit);
		return this;
	}
	
	public SQLiteDBLayout setSQL(String sql) {
		this.sql = sql;
		return this;
	}
	
	public SQLiteDBLayout setSort(String sort) {
		layoutAttrs.put("sort", sort);
		return this;
	}
	
	public SQLiteDBLayout hideColumn(int index) {
		if (index > 0) {
			--index;
			if (index == 1) hideID = true;
			hideColumns.add(index);
			hideColumn(index, true);
		} else stt.error("hideColumn(?): Wrong column index.");
		return this;
	}

	public SQLiteDBLayout unhideColumn(int index) {
		if (index > 0) {
			--index;
			if (index == 1) hideID = false;
			hideColumns.remove(index);
			hideColumn(index, false);
		} else stt.error("unhideColumn(?): Wrong column index.");
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
		this.onClickListener = listener;
		return this;
	}
	
	public SQLiteDBLayout setRowOnLongClickListener(RowOnLongClickListener listener) {
		this.onLongClickListener = listener;
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
	
	public int[] getRowIDs()
	{
		return rowIDs;
	}
	
	public List<Map<String, Object>> getTableData()
	{
		return tblData;
	}
	
	public void show() {
		removeAllViews();
		if (tblName == null && "".equals(tblName)) return;
		List<Map<String, Object>> data = sql == null || "".equals(sql) ? db.getData(tblName, layoutAttrs) : db.sql(tblName, sql, null);
		if (data.size() < 1) return;
		
		tblData = data;
		table = new TableLayout(context);
		TableLayout.LayoutParams tblLayout = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		table.setLayoutParams(tblLayout);
		TableRow rowHeader = new TableRow(context);
		rowHeader.setBackgroundColor(Color.GRAY);
		rowHeader.setGravity(Gravity.CENTER);
		rowHeader.addView(createColHeader(ordHeader, 1));
		String[] colNames = data.get(0).keySet().toArray(new String[0]);
		int colNums = data.get(0).keySet().size();
		int lastCol = colNums - 1;
		for (int colIndex = 0; colIndex < colNums; colIndex++) {
			String colName = colNames[colIndex];
			switch (colIndex) {
				case 0:
					rowHeader.addView(createColHeader(colName, 1));
					break;
				default:
					if (colIndex == lastCol) {
						rowHeader.addView(createColHeader(colName, 4));
					} else rowHeader.addView(createColHeader(colName, 2));
			}
		}
		
		ScrollView scroll = new ScrollView(context);
		ScrollView.LayoutParams svLayout = new ScrollView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1);
		scroll.setLayoutParams(svLayout);
		tBody = new TableLayout(context);
		tBody.setLayoutParams(tblLayout);
		int rowIndex = 0;
		rowIDs = new int[data.size()];
		for (Map<String, Object> rowData : data) {
			final Object[] colValues = rowData.values().toArray();
			rowIDs[rowIndex] = colValues[0];
			TableRow row = new TableRow(context);
			final int index = rowIndex;
			if (onClickListener != null) row.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view)
						{
							int id = colValues[0];
							onClickListener.onClick(view, index, id);
						}
					});
			if (onLongClickListener != null) row.setOnLongClickListener(new OnLongClickListener() {

						@Override
						public boolean onLongClick(View view)
						{
							int id = colValues[0];
							return onLongClickListener.onLongClick(view, index, id);
						}
					});
			if (rowIndex++ % 2 != 0) row.setBackgroundColor(Color.LTGRAY);
			row.setGravity(Gravity.CENTER);
			row.addView(createColumn(rowIndex, 1));
			for (int colIndex = 0; colIndex < colNums; colIndex++) {
				Object colData = colValues[colIndex];
				switch (colIndex) {
					case 0:
						row.addView(createColumn(colData, 1));
						break;
					default:
						if (colIndex == lastCol) {
							row.addView(createColumn(colData, 4));
						} else row.addView(createColumn(colData, 2));
				}
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
	
	private TextView createColumn(Object text, int size) {
		TableRow.LayoutParams colLayoutSM = new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.1f);
		TableRow.LayoutParams colLayoutMD = new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2);
		TableRow.LayoutParams colLayoutXM = new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3);
		TableRow.LayoutParams colLayoutLG = new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 4);
		TableRow.LayoutParams colHSLayout = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		TextView col = new TextView(context);
		if (!hScroll) {
			switch (size) {
				case 1:
					col.setLayoutParams(colLayoutSM);
					col.setGravity(Gravity.CENTER);
					break;
				case 3:
					col.setLayoutParams(colLayoutXM);
					break;
				case 4:
					col.setLayoutParams(colLayoutLG);
					break;
				default:
					col.setLayoutParams(colLayoutMD);
					col.setGravity(Gravity.CENTER);
			}
		} else col.setLayoutParams(colHSLayout);
		col.setPadding(15, 15, 15, 15);
		String colText = text == null ? "" : String.valueOf(text);
		col.setText(colText);
		return col;
	}
	
	private TextView createColHeader(String text, int size) {
		TextView header = createColumn(text.toUpperCase(), size);
		header.setGravity(Gravity.CENTER);
		header.setTextColor(Color.WHITE);
		header.setTypeface(null, Typeface.BOLD);
		return header;
	}
	
	public static interface RowOnClickListener {
		public void onClick(View row, int index, int id);
	}
	
	public static interface RowOnLongClickListener {
		public boolean onLongClick(View row, int index, int id);
	}
}
