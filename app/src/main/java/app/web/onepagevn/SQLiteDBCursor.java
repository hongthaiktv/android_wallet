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
	
	public DataType[] getDataTypes() {
		DataType[] types = new DataType[getCount()];
		try {
			int i = 0;
			while (moveToNext()) {
				types[i] = DataType.valueOf(getString(2));
				++i;
			}
			moveToPosition(-1);
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		return types;
	}
	
	public List<Map<String, String>> getForeignColumns() {
		List<Map<String, String>> columns = new ArrayList<Map<String, String>>();
		try {
			int i = 0;
			while (moveToNext()) {
				Map<String, String> column = new LinkedHashMap<String, String>();
				column.put("dstColumn", getString(4));
				column.put("dstTable", getString(2));
				column.put("srcColumn", getString(3));
				columns.add(column);
				++i;
			}
			moveToPosition(-1);
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		return columns;
	}
	
	public String[] getCatalog() {
		String[] catalog = new String[getCount()];
		try {
			int i = 0;
			while (moveToNext()) {
				catalog[i] = getString(0);
				++i;
			}
			moveToPosition(-1);
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		return catalog;
	}
	
	public int[] getCatalogID() {
		int[] ids = new int[getCount()];
		try {
			int i = 0;
			while (moveToNext()) {
				ids[i] = getInt(0);
				++i;
			}
			moveToPosition(-1);
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		return ids;
	}
}
