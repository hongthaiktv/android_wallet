package app.web.onepagevn;
import android.database.*;
import android.database.sqlite.*;
import android.widget.*;
import java.util.*;
import android.content.*;

public class SQLiteDB extends SQLiteOpenHelper
{
	private AppSetting app = AppSetting.getAppContext();
	private static SQLiteDatabase.CursorFactory cursorFactory = new SQLiteDatabase.CursorFactory() {

		@Override
		public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query)
		{
			return new SQLiteDBCursor(db, driver, editTable, query);
		}
	};
	
	private static DatabaseErrorHandler errorHandler = new DatabaseErrorHandler() {

		@Override
		public void onCorruption(SQLiteDatabase db)
		{
			String msg = "Database initial error.";
			Toast.makeText(AppSetting.getAppContext(), msg, Toast.LENGTH_LONG).show();
		}
	};
	
	public SQLiteDB() {
		super(AppSetting.getAppContext(), "wallet.db", cursorFactory, 1, errorHandler);
	}
	
	public SQLiteDB(String dbName, int version) {
		super(AppSetting.getAppContext(), dbName, cursorFactory, version, errorHandler);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		try {
			db.execSQL("CREATE TABLE logs(id INTEGER PRIMARY KEY, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, type TEXT, component TEXT, message TEXT)");
			db.execSQL("CREATE TABLE setting(id INTEGER PRIMARY KEY, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, lastupdate DATETIME DEFAULT CURRENT_TIMESTAMP, name TEXT NOT NULL UNIQUE, value TEXT NOT NULL, type TEXT NOT NULL)");
			db.execSQL("INSERT INTO setting(name, value, type) VALUES('dialogMaxLogs', '100', 'INTEGER')");
			insertLog("Database created.", LogType.SUCCESS, "Database", db);
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onOpen(SQLiteDatabase db)
	{
		super.onOpen(db);
		
		try {
			db.execSQL("PRAGMA foreign_keys = true");
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		insertLog("Database upgraded.", LogType.SUCCESS, "Database", db);
	}
	
	public String getVersion() {
		List<Map<String, Object>> result = query("SELECT sqlite_version() AS version");
		String version = (String) result.get(0).get("version");
		version = "SQLite version: " + version;
		return version;
	}
	
	public List<Map<String, Object>> query(String sql) {
		SQLiteDatabase db = getWritableDatabase();
		SQLiteDBCursor cursor = (SQLiteDBCursor) db.rawQuery(sql, null);
		List<Map<String, Object>> result = cursor.getList();
		cursor.close();
		db.close();
		return result;
	}
	
	public long insert(String table, ContentValues values) {
		SQLiteDatabase db = getWritableDatabase();
		long result = db.insert(table, null, values);
		db.close();
		return result;
	}
	
	public int updateSetting(String keyName, Object value) {
		SQLiteDatabase db = getWritableDatabase();
		int result = -1;
		ContentValues values = new ContentValues();
		if (value instanceof String) values.put("value", (String) value);
		else if (value instanceof Integer) values.put("value", (Integer) value);
		else if (value instanceof Long) values.put("value", (Long) value);
		else if (value instanceof Float) values.put("value", (Float) value);
		else if (value instanceof Double) values.put("value", (Double) value);
		else if (value instanceof Boolean) values.put("value", (Boolean) value);
		String where = "name = ?";
		String[] args = {keyName};
		try {
			result = db.update("setting", values, where, args);
		} catch (Exception e) {
			insertLog(e.getMessage(), LogType.ERROR, "Database");
		}
		db.close();
		return result;
	}
	
	public List<Map<String, Object>> getSetting() {
		String sql = "SELECT timestamp, lastupdate, name, value, type FROM setting ORDER BY name";
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = null;
		try {
			cursor = (SQLiteDBCursor) db.rawQuery(sql, null);
		} catch (Exception e) {
			insertLog(e.getMessage(), LogType.ERROR, "Database");
		}
		List<Map<String, Object>> result = new ArrayList<>();
		while (cursor.moveToNext()) {
			Map<String, Object> row = new LinkedHashMap<>();
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				String key = cursor.getColumnName(i);
				switch (i) {
					case 0: case 1:
						row.put(key, new Date(cursor.getLong(i)));
						break;
					case 2:
						row.put(key, cursor.getString(i));
						break;
					case 3:
						DataType type = DataType.valueOf(cursor.getString(4));
						switch (type) {
							case STRING:
								row.put(key, cursor.getString(i));
								break;
							case INTEGER:
								row.put(key, cursor.getInt(i));
								break;
							case LONG:
								row.put(key, cursor.getLong(i));
								break;
							case FLOAT:
								row.put(key, cursor.getFloat(i));
								break;
							case DOUBLE:
								row.put(key, cursor.getDouble(i));
								break;
							case BOOLEAN:
								row.put(key, Boolean.valueOf(cursor.getString(i)));
								break;
							case BLOB:
								row.put(key, cursor.getBlob(i));
								break;
							case DATETIME:
								row.put(key, new Date(cursor.getString(i)));
								break;
						}
						break;
					case 4:
						row.put(key, DataType.valueOf(cursor.getString(i)));
						break;
				}
			}
			result.add(row);
		}
		cursor.close();
		db.close();
		return result;
	}
	
	public long insertLog(String msg, LogType type, String compName) {
		return insertLog(msg, type, compName, null);
	}
	
	private long insertLog(String msg, LogType type, String compName, SQLiteDatabase db) {
		boolean isNullDB = db == null ? true : false;
		if (isNullDB) db = getWritableDatabase();
		ContentValues values = new ContentValues();
		if (compName == null || "".equals(compName)) compName = "Logger";
		if (type == null) type = LogType.LOG;
		long timestamp = new Date().getTime();
		values.put("timestamp", timestamp);
		values.put("type", type.name());
		values.put("component", compName);
		values.put("message", msg);
		long result = -1;
		try {
			result = db.insert("logs", null, values);
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		if (isNullDB) db.close();
		return result;
	}
	
	public List<Map<String, Object>> getLogs() {
		String sql = "SELECT timestamp, type, component, message FROM logs ORDER BY timestamp DESC";
		return getLogs(sql, null);
	}
	
	public List<Map<String, Object>> getLogs(int limit) {
		String sql = "SELECT timestamp, type, component, message FROM logs ORDER BY timestamp DESC LIMIT ?";
		String[] args = {String.valueOf(limit)};
		return getLogs(sql, args);
	}
	
	public List<Map<String, Object>> getLogs(LogType type) {
		String sql; String[] args;
		int dialogMaxLogs = app.getDialogMaxLogs();
		if (type == null) {
			sql = "SELECT timestamp, type, component, message FROM logs ORDER BY timestamp DESC LIMIT ?";
			args = new String[] {String.valueOf(dialogMaxLogs)};
		} else {
			sql = "SELECT timestamp, type, component, message FROM logs WHERE type = ? ORDER BY timestamp DESC LIMIT ?";
			args = new String[] {type.name(), String.valueOf(dialogMaxLogs)};
		}
		return getLogs(sql, args);
	}
	
	private List<Map<String, Object>> getLogs(String sql, String[] args) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = null;
		try {
			cursor = (SQLiteDBCursor) db.rawQuery(sql, args);
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		List<Map<String, Object>> result = new ArrayList<>();
		while (cursor.moveToNext()) {
			Map<String, Object> row = new LinkedHashMap<>();
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				String key = cursor.getColumnName(i);
				switch (i) {
					case 0:
						row.put(key, new Date(cursor.getLong(i)));
						break;
					case 1:
						row.put(key, LogType.valueOf(cursor.getString(i)));
						break;
					default:
						row.put(key, cursor.getString(i));
				}
			}
			result.add(row);
		}
		cursor.close();
		db.close();
		return result;
	}
	
	public void clearLogs() {
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.execSQL("DELETE FROM logs");
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		db.close();
	}
}
