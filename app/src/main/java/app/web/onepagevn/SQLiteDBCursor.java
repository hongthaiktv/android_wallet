package app.web.onepagevn;

import android.database.sqlite.*;
import android.widget.*;
import java.util.*;

public class SQLiteDBCursor extends SQLiteCursor
{
	private AppSetting app = AppSetting.getAppContext();
	private List<Map<String, Object>> list = new ArrayList<>();
	
	public SQLiteDBCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
		super(db, driver, editTable, query);
		try {
			while (moveToNext()) {
				Map<String, Object> row = new LinkedHashMap<>();
				for (int i = 0; i < getColumnCount(); i++) {
					//switch column type to get
					row.put(getColumnName(i), getString(i));
				}
				list.add(row);
			}
			moveToPosition(-1);
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	public List<Map<String, Object>> getList() {
		return list;
	}
	
	public String[] getColumnTypes() {
		String[] types = new String[getCount()];
		try {
			int i = 0;
			while (moveToNext()) {
				types[i] = getString(2);
				++i;
			}
			moveToPosition(-1);
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		return types;
	}
}
