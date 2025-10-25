package app.web.onepagevn;
import android.content.*;
import android.content.res.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class SQLiteDBLayout extends TableLayout
{
	private Context context;
	private String table = "";
	private ViewGroup container;
	private SQLiteDB db = new SQLiteDB();
	
	public SQLiteDBLayout(Context context, String table) {
		super(context);
		this.context = context;
		this.table = table;
		show();
	}
	
	public SQLiteDBLayout(Context context, String table, ViewGroup container) {
		super(context);
		this.context = context;
		this.table = table;
		this.container = container;
		show();
	}
	
	public SQLiteDBLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		TypedArray attrsArr = context.obtainStyledAttributes(attrs, R.styleable.SQLiteDBLayout);
		table = attrsArr.getString(R.styleable.SQLiteDBLayout_table);
		attrsArr.recycle();
		show();
	}
	
	public void setTable(String table) {
		this.table = table;
		show();
	}
	
	private void show() {
		if (table == null && "".equals(table)) return;
		List<Map<String, Object>> data = db.getLogs();//pass table
		if (data.size() < 1) return;

		//Row Header
		TableRow header = new TableRow(context);
		for (String colName : data.get(0).keySet()) {
			TextView col = new TextView(context);
			col.setText(colName);
			header.addView(col);
		}
		addView(header);

		//Table Body
		ScrollView scroll = new ScrollView(context);
		HorizontalScrollView hScroll = new HorizontalScrollView(context);
		TableLayout body = new TableLayout(context);
		for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
			Map<String, Object> rowData = data.get(rowIndex);
			TableRow row = new TableRow(context);
			TableRow.LayoutParams rLayout = new TableRow.LayoutParams(200, LayoutParams.WRAP_CONTENT);
			row.setLayoutParams(rLayout);
			for (Object colData : rowData.values()) {
				TextView col = new TextView(context);
				col.setLayoutParams(rLayout);
				col.setText(String.valueOf(colData));
				row.addView(col);
			}
			body.addView(row);
		}
		ScrollView.LayoutParams vsLayout = new ScrollView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		scroll.setLayoutParams(vsLayout);
		HorizontalScrollView.LayoutParams hsLayout = new HorizontalScrollView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		hScroll.setLayoutParams(hsLayout);
		TableLayout.LayoutParams tbLayout = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		body.setLayoutParams(tbLayout);
		scroll.addView(hScroll);
		hScroll.addView(body);
		addView(scroll);
		if (container != null) container.addView(this);
	}
}
