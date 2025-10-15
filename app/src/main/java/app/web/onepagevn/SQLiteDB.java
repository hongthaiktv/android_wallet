package app.web.onepagevn;
import android.database.*;
import android.database.sqlite.*;
import android.widget.*;
import java.util.*;
import android.content.*;

public class SQLiteDB extends SQLiteOpenHelper
{
	private static SQLiteDB instance;
	
	private SQLiteDB() {
		super(AppSetting.getAppContext(), "wallet.db", new SQLiteDatabase.CursorFactory() {

				@Override
				public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query)
				{
					return new SQLiteDBCursor(db, driver, editTable, query);
				}
			}, 1, new DatabaseErrorHandler() {

				@Override
				public void onCorruption(SQLiteDatabase db)
				{
					String msg = "Database initial error.";
					Toast.makeText(AppSetting.getAppContext(), msg, Toast.LENGTH_LONG).show();
				}
			});
			
		List<Map<String, Object>> result = query("SELECT sqlite_version() AS version");
		String version = (String) result.get(0).get("version");
		insertLog("SQLite version: " + version, LogType.INFO);
	}
	
	public static SQLiteDB getInstance() {
		if (instance == null) instance = new SQLiteDB();
		return instance;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		try {
			db.execSQL("CREATE TABLE logs(id INTEGER PRIMARY KEY, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, type TEXT, message TEXT)");
			insertLog("Database created.", LogType.SUCCESS);
		} catch (Exception e) {
			Toast.makeText(AppSetting.getAppContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onOpen(SQLiteDatabase db)
	{
		super.onOpen(db);
		
		db.execSQL("PRAGMA foreign_keys = true");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		insertLog("Database upgraded.", LogType.SUCCESS);
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
	
	public long insertLog(String msg, LogType type) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		if (type == null) type = LogType.LOG;
		long timestamp = new Date().getTime();
		values.put("timestamp", timestamp);
		values.put("type", type.name());
		values.put("message", msg);
		long result = -1;
		try {
			result = db.insert("logs", null, values);
		} catch (Exception e) {
			Toast.makeText(AppSetting.getAppContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
		db.close();
		return result;
	}
	
	public List<Map<String, Object>> getLogs() {
		SQLiteDatabase db = getWritableDatabase();
		SQLiteDBCursor cursor = null;
		try {
			cursor = (SQLiteDBCursor) db.rawQuery("SELECT timestamp, type, message FROM logs", null);
		} catch (Exception e) {
			Toast.makeText(AppSetting.getAppContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
		List<Map<String, Object>> result = new ArrayList<>();
		while (cursor.moveToNext()) {
			Map<String, Object> row = new LinkedHashMap<>();
			long timestamp; LogType type; String message;
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				switch (i) {
					case 0:
						timestamp = cursor.getLong(i);
						row.put("timestamp", new Date(timestamp));
						break;
					case 1:
						type = LogType.valueOf(cursor.getString(i));
						row.put("type", type);
						break;
					case 2:
						message = cursor.getString(i);
						row.put("message", message);
						break;
				}
			}
			result.add(0, row);
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
