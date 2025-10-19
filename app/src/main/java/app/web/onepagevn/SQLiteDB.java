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
		int maxDialogLogs = app.getMaxDialogLogs();
		if (type == null) {
			sql = "SELECT timestamp, type, component, message FROM logs ORDER BY timestamp DESC LIMIT ?";
			if (maxDialogLogs <= 0) args = new String[] {String.valueOf(maxDialogLogs)};
			else args = new String[] {String.valueOf(maxDialogLogs + 1)};
		} else {
			sql = "SELECT timestamp, type, component, message FROM logs WHERE type = ? ORDER BY timestamp DESC LIMIT ?";
			if (maxDialogLogs <= 0) args = new String[] {type.name(), String.valueOf(maxDialogLogs)};
			else args = new String[] {type.name(), String.valueOf(maxDialogLogs + 1)};
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
