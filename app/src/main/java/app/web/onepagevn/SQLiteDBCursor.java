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
		
		if (editTable != null && !"".equals(editTable) && !"aggregate".equals(editTable)) {
			Cursor cursorTypes = null;
			int numCol = getColumnCount();
			types = new DataType[numCol];
			try {
				cursorTypes = db.rawQuery("PRAGMA table_info(" + editTable + ")", null);
				if (cursorTypes.getCount() > 0) {
					String[] colNames = getColumnNames();
					while (cursorTypes.moveToNext()) {
						String defineName = cursorTypes.getString(1);
						for (int i = 0; i < colNames.length; i++) {
							String colName = colNames[i];
							if (defineName.equals(colName)) types[i] = DataType.valueOf(cursorTypes.getString(2));
						}
					}
				}
			} catch (Exception e) {
				Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			if (cursorTypes != null) cursorTypes.close();
		}
		
		if ("aggregate".equals(editTable)) {
			if (moveToFirst()) {
				int numCol = getColumnCount();
				types = new DataType[numCol];
				for (int i = 0; i < numCol; i++) {
					int columnType = getType(i);
					switch (columnType) {
						case Cursor.FIELD_TYPE_INTEGER:
							types[i] = DataType.INTEGER;
							break;
						case Cursor.FIELD_TYPE_FLOAT:
							types[i] = DataType.DOUBLE;
							break;
						case Cursor.FIELD_TYPE_STRING:
							types[i] = DataType.TEXT;
							break;
						case Cursor.FIELD_TYPE_BLOB:
							types[i] = DataType.BLOB;
							break;
						case Cursor.FIELD_TYPE_NULL:
							types[i] = DataType.NULL;
							break;
					}
				}
				moveToPosition(-1);
			}
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
	
	public Map<String, DataType> getDataTypes() {
		Map<String, DataType> colDataTypes = new HashMap<>();
		try {
			while (moveToNext()) {
				colDataTypes.put(getString(1), DataType.valueOf(getString(2)));
			}
			moveToPosition(-1);
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		return colDataTypes;
	}
	
	public List<Map<String, Object>> getForeignColumns() {
		List<Map<String, Object>> columns = new ArrayList<Map<String, Object>>();
		try {
			while (moveToNext()) {
				Map<String, Object> column = new LinkedHashMap<String, Object>();
				column.put("dstColumn", getString(4));
				column.put("dstTable", getString(2));
				column.put("srcColumn", getString(3));
				columns.add(column);
			}
			moveToPosition(-1);
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		return columns;
	}
	
	public List<Object> getCatalog() {
		List<Object> catalog = new ArrayList<Object>();
		Integer[] ids = new Integer[getCount()];
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
	
	public Integer[] getCatalogID() {
		Integer[] ids = new Integer[getCount()];
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
				case INTEGER: case INT:
					row.put(getColumnName(i), getInt(i));
					break;
				case NUMERIC: case NUM: case LONG:
					row.put(getColumnName(i), getLong(i));
					break;
				case REAL: case FLOAT: case DOUBLE:
					row.put(getColumnName(i), getDouble(i));
					break;
				case TEXT:
					row.put(getColumnName(i), getString(i));
					break;
				case BOOLEAN:
					row.put(getColumnName(i), getInt(i) == 1 ? true : false);
					break;
				case DATETIME:
					row.put(getColumnName(i), new Date(getLong(i) * 1000L));
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
				case NULL:
					row.put(getColumnName(i), null);
					break;
				case BLOB: default:
					int dataType = getType(i);
					switch (dataType) {
						case Cursor.FIELD_TYPE_INTEGER:
							row.put(getColumnName(i), getInt(i));
							break;
						case Cursor.FIELD_TYPE_FLOAT:
							row.put(getColumnName(i), getDouble(i));
							break;
						case Cursor.FIELD_TYPE_STRING:
							row.put(getColumnName(i), getString(i));
							break;
						case Cursor.FIELD_TYPE_BLOB:
							row.put(getColumnName(i), getBlob(i));
							break;
						case Cursor.FIELD_TYPE_NULL:
							row.put(getColumnName(i), null);
							break;
						default:
							row.put(getColumnName(i), getString(i));
					}
			}
		}
		return row;
	}
}
