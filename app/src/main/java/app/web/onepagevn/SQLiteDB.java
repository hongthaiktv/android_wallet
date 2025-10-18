package app.web.onepagevn;
import android.database.*;
import android.database.sqlite.*;
import android.widget.*;
import java.util.*;
import android.content.*;

public class SQLiteDB extends SQLiteOpenHelper
{
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
			Toast.makeText(AppSetting.getAppContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onOpen(SQLiteDatabase db)
	{
		super.onOpen(db);
		
		try {
			db.execSQL("PRAGMA foreign_keys = true");
		} catch (Exception e) {
			Toast.makeText(AppSetting.getAppContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
			Toast.makeText(AppSetting.getAppContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
		if (isNullDB) db.close();
		return result;
	}
	
	public List<Map<String, Object>> getLogs() {
		return getLogs(null);
	}
	
	public List<Map<String, Object>> getLogs(LogType type) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = null;
		try {
			if (type != null) {
				String[] whereArgs = {type.name()};
				cursor = (SQLiteDBCursor) db.rawQuery("SELECT timestamp, type, component, message FROM logs WHERE type = ? ORDER BY timestamp DESC LIMIT 101", whereArgs);
			} else cursor = (SQLiteDBCursor) db.rawQuery("SELECT timestamp, type, component, message FROM logs ORDER BY timestamp DESC LIMIT 101", null);
		} catch (Exception e) {
			Toast.makeText(AppSetting.getAppContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
		List<Map<String, Object>> result = new ArrayList<>();
		while (cursor.moveToNext()) {
			Map<String, Object> row = new LinkedHashMap<>();
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				switch (i) {
					case 0:
						row.put("timestamp", new Date(cursor.getLong(i)));
						break;
					case 1:
						row.put("type", LogType.valueOf(cursor.getString(i)));
						break;
					case 2:
						row.put("component", cursor.getString(i));
						break;
					case 3:
						row.put("message", cursor.getString(i));
						break;
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
			Toast.makeText(AppSetting.getAppContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
		db.close();
	}
}
