package app.web.onepagevn;
import android.database.*;
import android.database.sqlite.*;
import android.widget.*;
import java.util.*;

public class SQLiteDB extends SQLiteOpenHelper
{
	private static SQLiteDB instance;
	private StatusBar stt = StatusBar.getInstance();
	
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
					instance.stt.error(msg);
					Toast.makeText(AppSetting.getAppContext(), msg, Toast.LENGTH_LONG).show();
				}
			});
			
		List<HashMap<String, Object>> result = query("SELECT sqlite_version() AS version");
		String version = (String) result.get(0).get("version");
		stt.info("SQLite version: "+ version);
	}
	
	public static SQLiteDB getInstance() {
		if (instance == null) instance = new SQLiteDB();
		return instance;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		stt.info("Database created.");
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
		stt.info("Database upgraded.");
	}
	
	public List<HashMap<String, Object>> query(String sql) {
		SQLiteDatabase db = getWritableDatabase();
		SQLiteDBCursor cursor = (SQLiteDBCursor) db.rawQuery(sql, null);
		List<HashMap<String, Object>> result = cursor.getList();
		cursor.close();
		db.close();
		return result;
	}
}
