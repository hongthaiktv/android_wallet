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
	private Map<String, DataType> colDataTypes = new LinkedHashMap<>();
	
	public SQLiteDBCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query, UUID queryID) {
		super(db, driver, editTable, query);
		
		if (getCount() < 1) return;
		Map<String, DataType> cachedDataType = SQLiteDB.getCachedDataTypeQuery(queryID);
		if (cachedDataType == null) {
			if (editTable != null && !"".equals(editTable)) {
				Cursor cursorTypes = null;
				try {
					cursorTypes = db.rawQuery("PRAGMA table_info(" + editTable + ")", null);
					if (cursorTypes.getCount() > 0) {
						moveToFirst();
						String[] colNames = getColumnNames();
						for (int i = 0; i < colNames.length; i++) {
							String colName = colNames[i];
							while (cursorTypes.moveToNext()) {
								String defineName = cursorTypes.getString(1);
								if (colName.equals(defineName)) {
									String sDataType = cursorTypes.getString(2).toUpperCase();
									if (sDataType != null && !"".equals(sDataType)) {
										DataType type = DataType.valueOf(sDataType);
										colDataTypes.put(colName, type);
									}
									break;
								}
							}
							cursorTypes.moveToPosition(-1);
							if (colDataTypes.get(colName) == null) {
								int dataType = getType(i);
								switch (dataType) {
									case Cursor.FIELD_TYPE_INTEGER:
										colDataTypes.put(colName, DataType.INT);
										break;
									case Cursor.FIELD_TYPE_FLOAT:
										colDataTypes.put(colName, DataType.REAL);
										break;
									case Cursor.FIELD_TYPE_STRING:
										colDataTypes.put(colName, DataType.TEXT);
										break;
									case Cursor.FIELD_TYPE_BLOB:
										colDataTypes.put(colName, DataType.BLOB);
										break;
									case Cursor.FIELD_TYPE_NULL:
										colDataTypes.put(colName, DataType.NULL);
										break;
									default:
										colDataTypes.put(colName, DataType.TEXT);
								}
							}
						}
						moveToPosition(-1);
					}
				} catch (Exception e) {
					Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
				}
				if (cursorTypes != null) cursorTypes.close();
				if (colDataTypes.size() > 0) SQLiteDB.addDataTypeQuery(queryID, colDataTypes);
			}
		}
		else colDataTypes = cachedDataType;
		
		try {
			while (moveToNext()) {
				DataType[] types = colDataTypes.values().toArray(new DataType[0]);
				if (types == null || types.length < 1) types = new DataType[getColumnCount()];
				list.add(getTypedRow(types));
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
		Map<String, DataType> colDataTypes = new LinkedHashMap<>();
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
