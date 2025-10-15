package app.web.onepagevn;

import android.database.sqlite.*;
import java.util.*;

public class SQLiteDBCursor extends SQLiteCursor
{
	private List<Map<String, Object>> list = new ArrayList<>();
	
	public SQLiteDBCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
		super(db, driver, editTable, query);
		
		while (moveToNext()) {
			Map<String, Object> row = new LinkedHashMap<>();
			for (int i = 0; i < getColumnCount(); i++) {
				//switch column type to get
				row.put(getColumnName(i), getString(i));
			}
			list.add(row);
		}
		moveToPosition(-1);
	}
	
	public List<Map<String, Object>> getList() {
		return list;
	}
}
