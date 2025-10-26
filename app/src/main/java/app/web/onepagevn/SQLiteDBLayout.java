package app.web.onepagevn;
import android.content.*;
import android.content.res.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import android.graphics.*;

public class SQLiteDBLayout extends TableLayout
{
	private Context context;
	private String table = "";
	private ViewGroup container;
	private boolean isHScroll = false;
	private String ordHeader = "NO.";
	TableLayout tBody;
	private SQLiteDB db = new SQLiteDB();
	
	public SQLiteDBLayout(Context context, String table) {
		super(context);
		this.context = context;
		this.table = table;
		show();
	}
	
	public SQLiteDBLayout(Context context, String table, boolean hScroll) {
		super(context);
		this.context = context;
		this.table = table;
		this.isHScroll = hScroll;
		show();
	}
	
	public SQLiteDBLayout(Context context, String table, ViewGroup container) {
		super(context);
		this.context = context;
		this.table = table;
		this.container = container;
		show();
	}
	
	public SQLiteDBLayout(Context context, String table, ViewGroup container, boolean hScroll) {
		super(context);
		this.context = context;
		this.table = table;
		this.container = container;
		this.isHScroll = hScroll;
		show();
	}
	
	public SQLiteDBLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		TypedArray attrsArr = context.obtainStyledAttributes(attrs, R.styleable.SQLiteDBLayout);
		table = attrsArr.getString(R.styleable.SQLiteDBLayout_table);
		isHScroll = attrsArr.getBoolean(R.styleable.SQLiteDBLayout_hScroll, isHScroll);
		attrsArr.recycle();
		show();
	}
	
	public void setTable(String table) {
		this.table = table;
		show();
	}
	
	public void setHScroll(boolean hScroll) {
		this.isHScroll = hScroll;
		show();
	}
	
	public void setOrdHeader(String colName) {
		this.ordHeader = colName;
	}
	
	public void show() {
		removeAllViews();
		if (table == null && "".equals(table)) return;
		List<Map<String, Object>> data = db.getData(table);//pass table
		if (data.size() < 1) return;
		
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
		TableLayout tBody = new TableLayout(context);
		this.tBody = tBody;
		int rowIndex = 0;
		for (Map<String, Object> rowData : data) {
			TableRow row = new TableRow(context);
			if (rowIndex++ % 2 != 0) row.setBackgroundColor(Color.LTGRAY);
			row.addView(createColumn(rowIndex));
			for (int colIndex = 0; colIndex < colNums; colIndex++) {
				Object colData = rowData.get(colNames[colIndex]);
				row.addView(createColumn(colData));
			}
			tBody.addView(row);
		}
		hideColumn(1);
		if (isHScroll) {
			HorizontalScrollView hScroll = new HorizontalScrollView(context);
			hScroll.addView(tBody);
			scroll.addView(hScroll);
		} else scroll.addView(tBody);
		addView(rowHeader);
		addView(scroll);
		if (container != null) container.addView(this);
	}
	
	private TextView createColumn(Object text) {
		TableRow.LayoutParams colLayout = new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
		TableRow.LayoutParams colHSLayout = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		TextView col = new TextView(context);
		if (isHScroll) col.setLayoutParams(colHSLayout);
		else col.setLayoutParams(colLayout);
		col.setPadding(25, 25, 25, 25);
		col.setText(String.valueOf(text));
		return col;
	}
	
	private TextView createColHeader(String text) {
		TextView header = createColumn(text.toUpperCase());
		header.setGravity(Gravity.CENTER);
		header.setTextColor(Color.WHITE);
		header.setTypeface(null, Typeface.BOLD);
		return header;
	}
	
	public void hideColumn(int index) {
		setColumnCollapsed(index, true);
		tBody.setColumnCollapsed(index, true);
	}

	public void unhideColumn(int index) {
		setColumnCollapsed(index, false);
		tBody.setColumnCollapsed(index, false);
	}
	
	public static interface RowOnClickListener {
		public void onClick(TableRow row, int index, int id);
	}
}
