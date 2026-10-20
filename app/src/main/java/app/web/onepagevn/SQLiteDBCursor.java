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
				String[] colNames = getColumnNames();
				Cursor cursorTypes = null;
				moveToFirst();
				try {
					cursorTypes = db.rawQuery("PRAGMA table_info(" + editTable + ")", null);
					if (cursorTypes.getCount() > 0) {
						for (int i = 0; i < colNames.length; i++) {
							String colName = colNames[i].toLowerCase();
							while (cursorTypes.moveToNext()) {
								String defineName = cursorTypes.getString(1).toLowerCase();
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
								int typeValue = getType(i);
								DataType dataType = getDataTypeByValue(typeValue);
								if (("date".equals(colName) || "timestamp".equals(colName) || "datetime".equals(colName) || "startdate".equals(colName) || "enddate".equals(colName)) &&
									(dataType == DataType.INT || dataType == DataType.INTEGER || dataType == DataType.DOUBLE)) {
									colDataTypes.put(colName, DataType.DATETIME);
								}
								else colDataTypes.put(colName, dataType);
							}
						}
					}
				} catch (Exception e) {
					Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
				}
				if (cursorTypes != null) cursorTypes.close();
				if (colDataTypes.size() < 1) {
					for (int i = 0; i < colNames.length; i++) {
						String colName = colNames[i];
						int typeValue = getType(i);
						DataType dataType = getDataTypeByValue(typeValue);
						if (("date".equals(colName) || "timestamp".equals(colName) || "datetime".equals(colName) || "startdate".equals(colName) || "enddate".equals(colName)) &&
							(dataType == DataType.INT || dataType == DataType.INTEGER || dataType == DataType.DOUBLE)) {
							colDataTypes.put(colName, DataType.DATETIME);
						}
						else colDataTypes.put(colName, dataType);
					}
				}
				if (colDataTypes.size() > 0) SQLiteDB.addDataTypeQuery(queryID, colDataTypes);
				moveToPosition(-1);
			}
		}
		else colDataTypes = cachedDataType;
		
		DataType[] types = colDataTypes.values().toArray(new DataType[0]);
		if (types == null || types.length < 1) types = new DataType[getColumnCount()];
		try {
			while (moveToNext()) {
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
	
	public Map<String, DataType> getDataTypes() {
		Map<String, DataType> colDataTypes = new LinkedHashMap<>();
		try {
			while (moveToNext()) {
				String sDataType = getString(2).toUpperCase();
				if (sDataType != null && !"".equals(sDataType)) {
					DataType type = DataType.valueOf(sDataType);
					colDataTypes.put(getString(1), type);
				}
				else colDataTypes.put(getString(1), DataType.BLOB);
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
	
	private DataType getDataTypeByValue(int typeValue) {
		DataType dataType = null;
		switch (typeValue) {
			case Cursor.FIELD_TYPE_INTEGER:
				dataType = DataType.INT;
				break;
			case Cursor.FIELD_TYPE_FLOAT:
				dataType = DataType.REAL;
				break;
			case Cursor.FIELD_TYPE_STRING:
				dataType = DataType.TEXT;
				break;
			case Cursor.FIELD_TYPE_BLOB:
				dataType = DataType.BLOB;
				break;
			case Cursor.FIELD_TYPE_NULL:
				dataType = DataType.NULL;
				break;
			default:
				dataType = DataType.TEXT;
		}
		return dataType;
	}
	
	private Map<String, Object> getTypedRow(DataType[] columnTypes) {
		Map<String, Object> row = new LinkedHashMap<>();
		for (int i = 0; i < columnTypes.length; i++) {
			String colName = getColumnName(i).toLowerCase();
			switch (columnTypes[i]) {
				case INTEGER: case INT:
					row.put(colName, getInt(i));
					break;
				case NUMERIC: case NUM: case LONG:
					row.put(colName, getLong(i));
					break;
				case REAL: case FLOAT: case DOUBLE:
					row.put(colName, getDouble(i));
					break;
				case TEXT:
					row.put(colName, getString(i));
					break;
				case BOOLEAN:
					row.put(colName, getInt(i) == 1 ? true : false);
					break;
				case DATETIME:
					row.put(colName, new Date(getLong(i) * 1000L));
					break;
				case JSON:
					String jsonText = getString(i);
					boolean isObject = jsonText.startsWith("{");
					if (isObject) {
						try
						{
							JSONObject jObj = new JSONObject(jsonText);
							row.put(colName, jObj);
						}
						catch (JSONException e)
						{Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();}
					}
					else {
						try
						{
							JSONArray jArr = new JSONArray(jsonText);
							row.put(colName, jArr);
						}
						catch (JSONException e)
						{Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();}
					}
					break;
				case NULL:
					row.put(colName, null);
					break;
				case BLOB: default:
					int dataType = getType(i);
					switch (dataType) {
						case Cursor.FIELD_TYPE_INTEGER:
							if ("date".equals(colName) || "timestamp".equals(colName) || "datetime".equals(colName) || "startdate".equals(colName) || "enddate".equals(colName)) {
								row.put(colName, new Date(getLong(i) * 1000L));
							}
							else row.put(colName, getInt(i));
							break;
						case Cursor.FIELD_TYPE_FLOAT:
							if ("date".equals(colName) || "timestamp".equals(colName) || "datetime".equals(colName) || "startdate".equals(colName) || "enddate".equals(colName)) {
								row.put(colName, new Date(getLong(i) * 1000L));
							}
							else row.put(colName, getDouble(i));
							break;
						case Cursor.FIELD_TYPE_STRING:
							row.put(colName, getString(i));
							break;
						case Cursor.FIELD_TYPE_BLOB:
							row.put(colName, getBlob(i));
							break;
						case Cursor.FIELD_TYPE_NULL:
							row.put(colName, null);
							break;
						default:
							row.put(colName, getString(i));
					}
			}
		}
		return row;
	}
}
