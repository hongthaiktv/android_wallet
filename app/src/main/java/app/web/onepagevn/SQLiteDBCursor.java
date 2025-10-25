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
		Toast.makeText(app, driver.toString() + " : ", Toast.LENGTH_LONG).show();
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
			/*if (!"logs".equals(editTable)) insertLog(e.getMessage(), LogType.ERROR, compName);
			else */Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	public List<Map<String, Object>> getList() {
		return list;
	}
}
