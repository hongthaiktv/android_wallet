package app.web.onepagevn;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class SQLiteDBLayout extends LinearLayout
{
	private Context context;
	private AppSetting app;
	private String tblName = "", ordHeader = "#", title;
	private boolean hScroll = false, hideID = true, hideTitle = false, hideHeader = false, striped = true, hideOrder = false, autoLoad = true, expandLastColumn = true;
	private Set<Integer> hideColumns = new HashSet<>();
	private Map<String, Object> layoutAttrs = new HashMap<String, Object>();
	private String sql = "", query = "";
	private ViewGroup container;
	private TableLayout table, tBody;
	private OnTitleClickListener onTitleClickListener;
	private OnRowClickListener onClickListener;
	private OnRowLongClickListener onLongClickListener;
	private OnCreateView onCreateView;
	private int[] rowIDs;
	private int index = -1;
	private StatusBar stt = new StatusBar("SQLiteDBLayout");
	private SQLiteDB db = new SQLiteDB();
	private List<Map<String, Object>> tblData;
	private String[] colNames;
	private DataType[] colTypes;
	private Map<String, DataType> dataTypes;
	private NumberFormat numFormatter;
	
	public SQLiteDBLayout(Context context, String tblName) {
		super(context);
		init(context, tblName, null, null);
	}
	
	public SQLiteDBLayout(Context context, String tblName, OnRowClickListener onClick) {
		super(context);
		init(context, tblName, null, onClick);
	}
	
	public SQLiteDBLayout(Context context, String tblName, ViewGroup container) {
		super(context);
		init(context, tblName, container, null);
	}
	
	public SQLiteDBLayout(Context context, String tblName, ViewGroup container, OnRowClickListener onClick) {
		super(context);
		init(context, tblName, container, onClick);
	}
	
	public SQLiteDBLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		app = AppSetting.getAppContext();
		numFormatter = NumberFormat.getInstance(Locale.US);
		numFormatter.setMaximumFractionDigits(7);
		
		TypedArray attrsMain = context.obtainStyledAttributes(attrs, R.styleable.SQLiteDBLayout);
		if (attrsMain.hasValue(R.styleable.SQLiteDBLayout_table)) tblName = attrsMain.getString(R.styleable.SQLiteDBLayout_table);
		if (attrsMain.hasValue(R.styleable.SQLiteDBLayout_ordHeader)) ordHeader = attrsMain.getString(R.styleable.SQLiteDBLayout_ordHeader);
		title = attrsMain.getString(R.styleable.SQLiteDBLayout_title);
		hScroll = attrsMain.getBoolean(R.styleable.SQLiteDBLayout_hScroll, hScroll);
		hideID = attrsMain.getBoolean(R.styleable.SQLiteDBLayout_hideID, hideID);
		hideTitle = attrsMain.getBoolean(R.styleable.SQLiteDBLayout_hideTitle, hideTitle);
		hideHeader = attrsMain.getBoolean(R.styleable.SQLiteDBLayout_hideHeader, hideHeader);
		striped = attrsMain.getBoolean(R.styleable.SQLiteDBLayout_striped, striped);
		hideOrder = attrsMain.getBoolean(R.styleable.SQLiteDBLayout_hideOrder, hideOrder);
		autoLoad = attrsMain.getBoolean(R.styleable.SQLiteDBLayout_autoLoad, autoLoad);
		expandLastColumn = attrsMain.getBoolean(R.styleable.SQLiteDBLayout_expandLastColumn, expandLastColumn);
		layoutAttrs.put("limit", attrsMain.getInt(R.styleable.SQLiteDBLayout_limit, -1));
		if (attrsMain.hasValue(R.styleable.SQLiteDBLayout_sort)) layoutAttrs.put("sort", attrsMain.getString(R.styleable.SQLiteDBLayout_sort));
		else layoutAttrs.put("sort", "rowid");
		sql = attrsMain.getString(R.styleable.SQLiteDBLayout_sql);
		if (hideOrder) hideColumns.add(0);
		if (hideID) hideColumns.add(1);
		int colIndex = attrsMain.getInt(R.styleable.SQLiteDBLayout_hideColumn, -1);
		if (colIndex > 0) hideColumns.add(--colIndex);
		attrsMain.recycle();
		
		TypedArray attrsColumns = context.obtainStyledAttributes(attrs, R.styleable.SQLiteDBLayout_columns);
		for (int index = 0; index < R.styleable.SQLiteDBLayout_columns.length; index++) {
			if (!attrsColumns.getBoolean(index, true)) hideColumns.add(index);
		}
		attrsColumns.recycle();
		setOrientation(LinearLayout.VERTICAL);
		if (autoLoad) show();
	}

	private void init(Context context, String tblName, ViewGroup container, OnRowClickListener onClickListener) {
		this.context = context;
		this.tblName = tblName;
		this.container = container;
		this.onClickListener = onClickListener;
		numFormatter = NumberFormat.getInstance(Locale.US);
		numFormatter.setMaximumFractionDigits(7);
		layoutAttrs.put("sort", "rowid");
		layoutAttrs.put("limit", -1);
		LayoutParams layout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1);
		setLayoutParams(layout);
		setOrientation(LinearLayout.VERTICAL);
		if (hideID) hideColumns.add(1);
		if (container != null) container.addView(this);
	}
	
	public SQLiteDBLayout setTable(String tblName) {
		this.tblName = tblName;
		return this;
	}
	
	public SQLiteDBLayout setTitle(String title) {
		this.title = title;
		return this;
	}
	
	public String getTableName() {
		return tblName;
	}
	
	public SQLiteDBLayout setHScroll(boolean hScroll) {
		this.hScroll = hScroll;
		return this;
	}
	
	public SQLiteDBLayout setHideID(boolean hide) {
		this.hideID = hide;
		if (hide) hideColumn(2);
		else unhideColumn(2);
		return this;
	}
	
	public SQLiteDBLayout setHideOrder(boolean hide) {
		this.hideOrder = hide;
		if (hide) hideColumn(1);
		else unhideColumn(1);
		return this;
	}
	
	public SQLiteDBLayout setHideTitle(boolean hide) {
		hideTitle = hide;
		return this;
	}
	
	public SQLiteDBLayout setHideHeader(boolean hide) {
		hideHeader = hide;
		return this;
	}
	
	public SQLiteDBLayout setStriped(boolean striped) {
		this.striped = striped;
		return this;
	}
	
	public SQLiteDBLayout setOrdHeader(String colName) {
		this.ordHeader = colName;
		return this;
	}
	
	public SQLiteDBLayout setExpandLastColumn(boolean expand) {
		expandLastColumn = expand;
		return this;
	}
	
	public SQLiteDBLayout setSort(String sort) {
		layoutAttrs.put("sort", sort);
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
	
	private void hideColumn(int index, boolean hide) {
		if (hide) {
			table.setColumnCollapsed(index, true);
			tBody.setColumnCollapsed(index, true);
		} else {
			table.setColumnCollapsed(index, false);
			tBody.setColumnCollapsed(index, false);
		}
	}
	
	public SQLiteDBLayout hideColumn(int index) {
		if (index > 0) {
			--index;
			if (index == 1) hideID = true;
			else if (index == 0) hideOrder = true;
			hideColumns.add(index);
			hideColumn(index, true);
		} else stt.error("hideColumn(?): Wrong column index.");
		return this;
	}

	public SQLiteDBLayout unhideColumn(int index) {
		if (index > 0) {
			--index;
			if (index == 1) hideID = false;
			else if (index == 0) hideOrder = false;
			hideColumns.remove(index);
			hideColumn(index, false);
		} else stt.error("unhideColumn(?): Wrong column index.");
		return this;
	}
	
	public SQLiteDBLayout setOnTitleClickListener(OnTitleClickListener listener) {
		this.onTitleClickListener = listener;
		return this;
	}
	
	public SQLiteDBLayout setOnRowClickListener(OnRowClickListener listener) {
		this.onClickListener = listener;
		return this;
	}
	
	public SQLiteDBLayout setOnRowLongClickListener(OnRowLongClickListener listener) {
		this.onLongClickListener = listener;
		return this;
	}
	
	public SQLiteDBLayout setOnCreateView(OnCreateView callback) {
		this.onCreateView = callback;
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

	public Map<String, DataType> getDataTypes()
	{
		return dataTypes;
	}
	
	public void show() {
		removeAllViews();
		if (tblName == null || "".equals(tblName)) return;
		query = (sql != null && !"".equals(sql)) ? sql : "SELECT * FROM " + tblName + " ORDER BY " + layoutAttrs.get("sort") + " LIMIT " + layoutAttrs.get("limit");
		
		if (title != null && !"".equals(title)) {
			LinearLayout layoutTitle = new LinearLayout(context);
			LinearLayout.LayoutParams layoutTitleParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutTitle.setLayoutParams(layoutTitleParams);
			layoutTitle.setOrientation(LinearLayout.HORIZONTAL);
			layoutTitle.setBackgroundColor(getResources().getColor(R.color.primary));
			layoutTitle.setGravity(Gravity.CENTER);
			
			TextView tvTitle = new TextView(context);
			LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			tvTitle.setLayoutParams(titleParams);
			tvTitle.setGravity(Gravity.CENTER);
			tvTitle.setTextColor(Color.WHITE);
			tvTitle.setTypeface(null, Typeface.BOLD);
			tvTitle.setPadding(15, 15, 15, 15);
			tvTitle.setText(title);
			
			final View vTitle;
			if (onCreateView != null) vTitle = onCreateView.onCreate(layoutTitle, tvTitle, -1, -1, title, DataType.TEXT);
			else vTitle = tvTitle;
			
			if (onTitleClickListener != null) layoutTitle.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v)
						{
							onTitleClickListener.onClick((LinearLayout) v, vTitle, title);
						}
					});

			layoutTitle.addView(vTitle);
			if (!hideTitle) addView(layoutTitle);
		}
		
		List<Map<String, Object>> data = sql == null || "".equals(sql) ? db.getData(tblName, layoutAttrs) : db.sql(tblName, sql, null);
		if (data.size() < 1) return;
		colNames = data.get(0).keySet().toArray(new String[0]);
		dataTypes = SQLiteDB.getCachedDataTypeQuery(query);
		colTypes = dataTypes.values().toArray(new DataType[0]);
		
		tblData = data;
		table = new TableLayout(context);
		TableLayout.LayoutParams tblLayout = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		table.setLayoutParams(tblLayout);
		
		int colNums = data.get(0).keySet().size();
		int lastCol = colNums - 1;
		
		if (!hideHeader) {
			TableRow rowHeader = new TableRow(context);
			rowHeader.setBackgroundColor(Color.GRAY);
			rowHeader.setGravity(Gravity.CENTER);
			rowHeader.addView(createColHeader(ordHeader, 1, 0, -1, rowHeader));
			
			for (int colIndex = 0; colIndex < colNums; colIndex++) {
				String colName = colNames[colIndex];
				switch (colIndex) {
					case 0:
						rowHeader.addView(createColHeader(colName, 1, 0, colIndex, rowHeader));
						break;
					default:
						if (colIndex == lastCol && expandLastColumn) rowHeader.addView(createColHeader(colName, 4, 0, colIndex, rowHeader));
						else rowHeader.addView(createColHeader(colName, 2, 0, colIndex, rowHeader));
				}
			}
			table.addView(rowHeader);
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
			TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			row.setLayoutParams(rowParams);
			row.setGravity(Gravity.CENTER_VERTICAL);
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
			if (striped && rowIndex % 2 != 0) row.setBackgroundColor(Color.LTGRAY);
			row.setGravity(Gravity.CENTER);
			++rowIndex;
			row.addView(createColumn(rowIndex, 1, rowIndex, -1, row));
			for (int colIndex = 0; colIndex < colNums; colIndex++) {
				Object colData = colValues[colIndex];
				switch (colIndex) {
					case 0:
						row.addView(createColumn(colData, 1, rowIndex, colIndex, row));
						break;
					default:
						if (colIndex == lastCol && expandLastColumn) {
							row.addView(createColumn(colData, 4, rowIndex, colIndex, row));
						} else row.addView(createColumn(colData, 2, rowIndex, colIndex, row));
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
		table.addView(scroll);
		if (hScroll) {
			HorizontalScrollView hScroll = new HorizontalScrollView(context);
			HorizontalScrollView.LayoutParams hsLayout = new HorizontalScrollView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			hScroll.setLayoutParams(hsLayout);
			hScroll.addView(table);
			addView(hScroll);
		} else addView(table);
	}
	
	private View createColumn(Object data, int size, int rowIndex, int colIndex, TableRow row) {
		View viewColumn = null;
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
		String colText = "";
		DataType colType;
		if (rowIndex < 1) colType = DataType.TEXT;
		else if (colIndex == -1) colType = DataType.INT;
		else if (colIndex == 0) colType = DataType.INTEGER;
		else colType = colTypes[colIndex];
		if (data != null && rowIndex >= 1 && colIndex >= 1) {
			switch (colType) {
				case INT: case INTEGER: case NUMERIC: case REAL:
					colText = numFormatter.format(data);
					col.setGravity(Gravity.RIGHT);
					break;
				case BOOLEAN:
					boolean checked = (Boolean) data;
					CheckBox checkBox = createCheckbox(checked);
					viewColumn = checkBox;
					break;
				case DATETIME:
					colText = SQLiteDB.formatShortedDate((Date) data);
					break;
				case NULL: break;
				default:
					colText = String.valueOf(data);
			}
		} else if (data != null) colText = String.valueOf(data);
		
		if (viewColumn == null) {
			col.setText(colText);
			viewColumn = col;
		}
		if (onCreateView != null) viewColumn = onCreateView.onCreate(row, viewColumn, rowIndex, colIndex, data, colType);
		return viewColumn;
	}
	
	private View createColHeader(String text, int size, int rowIndex, int colIndex, TableRow rowHeader) {
		View header = createColumn(text.toUpperCase(), size, rowIndex, colIndex, rowHeader);
		if (header instanceof TextView) {
			TextView tvHeader = (TextView) header;
			tvHeader.setGravity(Gravity.CENTER);
			tvHeader.setTextColor(Color.WHITE);
			tvHeader.setTypeface(null, Typeface.BOLD);
			return tvHeader;
		}
		return header;
	}
	
	private CheckBox createCheckbox(boolean checked) {
		CheckBox checkBox = new CheckBox(context);
		TableRow.LayoutParams checkBoxParams = new TableRow.LayoutParams(90, 50);
		checkBoxParams.gravity = Gravity.CENTER;
		checkBox.setLayoutParams(checkBoxParams);
		checkBox.setGravity(Gravity.CENTER);
		checkBox.setChecked(checked);
		checkBox.setClickable(false);
		return checkBox;
	}
	
	public static interface OnTitleClickListener {
		public void onClick(LinearLayout parent, View title, String text);
	}
	
	public static interface OnRowClickListener {
		public void onClick(View row, int index, int id);
	}
	
	public static interface OnRowLongClickListener {
		public boolean onLongClick(View row, int index, int id);
	}
	
	public static interface OnCreateView {
		public View onCreate(ViewParent parent, View view, int rowIndex, int colIndex, Object data, DataType type);
	}
}
