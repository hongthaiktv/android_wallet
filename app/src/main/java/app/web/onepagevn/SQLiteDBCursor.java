package app.web.onepagevn;

import android.database.sqlite.*;
import java.util.*;

public class SQLiteDBCursor extends SQLiteCursor
{
	private List<HashMap<String, Object>> list = new ArrayList<>();
	
	public SQLiteDBCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
		super(db, driver, editTable, query);
		
		while (moveToNext()) {
			HashMap<String, Object> row = new LinkedHashMap<>();
			for (int i = 0; i < getColumnCount(); i++) {
				//switch column type to get
				row.put(getColumnName(i), getString(i));
			}
			list.add(row);
		}
		moveToPosition(-1);
	}
	
	public List<HashMap<String, Object>> getList() {
		return list;
	}
}
