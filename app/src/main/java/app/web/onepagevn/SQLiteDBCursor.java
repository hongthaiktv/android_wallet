package app.web.onepagevn;

import android.database.*;
import android.database.sqlite.*;
import android.widget.*;
import java.util.*;
import org.json.*;

public class SQLiteDBCursor extends SQLiteCursor
{
	private AppSetting app = AppSetting.getAppContext();
	private List<Map<String, Object>> list = new ArrayList<>();
	
	public SQLiteDBCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
		super(db, driver, editTable, query);
		DataType[] types = null;
		
		if (editTable != null && !"".equals(editTable) && !"balance".equals(editTable)) {
			Cursor cursorTypes = null;
			try {
				cursorTypes = db.rawQuery("PRAGMA table_info(" + editTable + ")", null);
				if (cursorTypes.getCount() > 0) {
					types = new DataType[cursorTypes.getCount()];
					int i = 0;
					while (cursorTypes.moveToNext()) {
						types[i] = DataType.valueOf(cursorTypes.getString(2));
						++i;
						//Toast.makeText(app, editTable + " : " + cursorTypes.getString(1) + " : " + type.name(), Toast.LENGTH_SHORT).show();
					}
				}
			} catch (Exception e) {
				Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			cursorTypes.close();
		}
		
		try {
			while (moveToNext()) {
				if (types != null && types.length > 0) {
					list.add(getTypedRow(types));
				}
				else {
					Map<String, Object> row = new LinkedHashMap<>();
					for (int i = 0; i < getColumnCount(); i++) {
						row.put(getColumnName(i), getString(i));
					}
					list.add(row);
				}
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
	
	public List<Map<String, Object>> getForeignColumns() {
		List<Map<String, Object>> columns = new ArrayList<Map<String, Object>>();
		try {
			int i = 0;
			while (moveToNext()) {
				Map<String, Object> column = new LinkedHashMap<String, Object>();
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
	
	public List<Object> getCatalog() {
		List<Object> catalog = new ArrayList<Object>();
		int[] ids = new int[getCount()];
		String[] names = new String[getCount()];
		try {
			int i = 0;
			while (moveToNext()) {
				ids[i] = getInt(0);
				names[i] = getString(1);
				++i;
			}
			moveToPosition(-1);
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		catalog.add(ids);
		catalog.add(names);
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
	
	private Map<String, Object> getTypedRow(DataType[] columnTypes) {
		Map<String, Object> row = new LinkedHashMap<>();
		for (int i = 0; i < columnTypes.length; i++) {
			switch (columnTypes[i]) {
				case INTEGER: case INT: case BOOLEAN:
					row.put(getColumnName(i), getInt(i));
					break;
				case NUMERIC: case NUM: case LONG: case DATETIME:
					row.put(getColumnName(i), getLong(i));
					break;
				case REAL: case FLOAT: case DOUBLE:
					row.put(getColumnName(i), getDouble(i));
					break;
				case TEXT:
					row.put(getColumnName(i), getString(i));
					break;
				case BLOB:
					row.put(getColumnName(i), getBlob(i));
					break;
				case JSON:
					String jsonText = getString(i);
					boolean isObject = jsonText.startsWith("{");
					if (isObject) {
						try
						{
							JSONObject jObj = new JSONObject(jsonText);
							row.put(getColumnName(i), jObj);
						}
						catch (JSONException e)
						{Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();}
					}
					else {
						try
						{
							JSONArray jArr = new JSONArray(jsonText);
							row.put(getColumnName(i), jArr);
						}
						catch (JSONException e)
						{Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();}
					}
					break;
			}
		}
		return row;
	}
}
