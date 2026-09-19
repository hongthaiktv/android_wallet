package app.web.onepagevn;
import android.database.*;
import android.database.sqlite.*;
import android.widget.*;
import java.util.*;
import android.content.*;
import org.json.*;

public class SQLiteDB extends SQLiteOpenHelper
{
	private AppSetting app = AppSetting.getAppContext();
	private String compName = "Database";
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
			db.execSQL("CREATE TABLE logs (id INTEGER PRIMARY KEY, timestamp DATETIME DEFAULT (unixepoch()) NOT NULL, type TEXT NOT NULL, component TEXT NOT NULL, message TEXT)");
			db.execSQL("CREATE TABLE setting (id INTEGER PRIMARY KEY, timestamp DATETIME DEFAULT (unixepoch()) NOT NULL, name TEXT NOT NULL UNIQUE, value NOT NULL, datatype TEXT NOT NULL)");
			db.execSQL("CREATE TABLE cmdhistory (id INTEGER PRIMARY KEY, timestamp DATETIME DEFAULT (unixepoch()) NOT NULL, datecreate DATETIME DEFAULT (unixepoch()) NOT NULL, command TEXT UNIQUE NOT NULL, times INTEGER NOT NULL)");
			db.execSQL("CREATE TABLE budget (id INTEGER PRIMARY KEY, date DATETIME DEFAULT (unixepoch()) UNIQUE NOT NULL, amount REAL NOT NULL)");
			db.execSQL("CREATE TABLE suppliers (supplierid INTEGER PRIMARY KEY, type TEXT NOT NULL, name TEXT NOT NULL, address TEXT, phone TEXT, gps TEXT, note TEXT)");
			db.execSQL("CREATE TABLE units (unitid INTEGER PRIMARY KEY, name TEXT UNIQUE NOT NULL)");
			db.execSQL("CREATE TABLE product_types (ptypeid INTEGER PRIMARY KEY, name TEXT UNIQUE NOT NULL)");
			db.execSQL("CREATE TABLE products (productid INTEGER PRIMARY KEY, name TEXT UNIQUE NOT NULL, price REAL NOT NULL, unitid INT REFERENCES units (unitid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL, ptypeid INT REFERENCES product_types (ptypeid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL)");
			db.execSQL("CREATE TABLE expends (id INTEGER PRIMARY KEY, date DATETIME DEFAULT (unixepoch()) NOT NULL, supplierid INT REFERENCES suppliers (supplierid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL, productid INT REFERENCES products (productid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL, unitid INT REFERENCES units (unitid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL, price REAL NOT NULL, quantity REAL NOT NULL, note TEXT)");
			db.execSQL("INSERT INTO setting (name, value, datatype) VALUES ('dlgMaxLogs', 100, 'INTEGER')");
			db.execSQL("INSERT INTO units (name) VALUES ('Kg')");
			db.execSQL("INSERT INTO units (name) VALUES ('Grams')");
			db.execSQL("INSERT INTO units (name) VALUES ('Each')");
			db.execSQL("INSERT INTO units (name) VALUES ('Bag')");
			db.execSQL("INSERT INTO units (name) VALUES ('Pack')");
			db.execSQL("INSERT INTO units (name) VALUES ('Box')");
			db.execSQL("INSERT INTO units (name) VALUES ('Piece')");
			db.execSQL("INSERT INTO units (name) VALUES ('Turn')");
			db.execSQL("INSERT INTO units (name) VALUES ('Hour')");
			db.execSQL("INSERT INTO units (name) VALUES ('Day')");
			db.execSQL("INSERT INTO units (name) VALUES ('Night')");
			db.execSQL("INSERT INTO product_types (name) VALUES ('Food')");
			db.execSQL("INSERT INTO product_types (name) VALUES ('Street Food')");
			db.execSQL("INSERT INTO product_types (name) VALUES ('Gas')");
			db.execSQL("INSERT INTO product_types (name) VALUES ('Fuel')");
			db.execSQL("INSERT INTO product_types (name) VALUES ('Service')");
			db.execSQL("INSERT INTO product_types (name) VALUES ('Work')");
			db.execSQL("INSERT INTO product_types (name) VALUES ('Tool')");
			db.execSQL("INSERT INTO product_types (name) VALUES ('Equipment')");
			db.execSQL("INSERT INTO product_types (name) VALUES ('Personal')");
			db.execSQL("INSERT INTO product_types (name) VALUES ('Donation')");
			db.execSQL("INSERT INTO product_types (name) VALUES ('Phone')");
			db.execSQL("INSERT INTO product_types (name) VALUES ('Motorbike')");
			db.execSQL("INSERT INTO products (name, price, unitid, ptypeid) VALUES ('Bread-SM', 3800, 3, 1)");
			db.execSQL("INSERT INTO products (name, price, unitid, ptypeid) VALUES ('Bread-MD', 7500, 3, 1)");
			db.execSQL("INSERT INTO products (name, price, unitid, ptypeid) VALUES ('Bread-LG', 15000, 3, 1)");
			db.execSQL("INSERT INTO products (name, price, unitid, ptypeid) VALUES ('Ice', 2000, 4, 2)");
			db.execSQL("INSERT INTO products (name, price, unitid, ptypeid) VALUES ('Parking', 2000, 8, 5)");
			db.execSQL("INSERT INTO suppliers (type, name, address) VALUES ('ST', 'Lotte', '58 Đ. 23/10, TP. Nha Trang')");
			db.execSQL("INSERT INTO suppliers (type, name, address) VALUES ('ST', 'Coop Mart', '02 Lê Hồng Phong, TP. Nha Trang')");
			db.execSQL("INSERT INTO suppliers (type, name, address) VALUES ('ST', 'Go', 'Lô 4 Đ. 19/5, TP. Nha Trang')");
			db.execSQL("INSERT INTO suppliers (type, name, address) VALUES ('ST', 'Mega Market', '10 Đ. 23/10, TP. Nha Trang')");
			db.execSQL("INSERT INTO suppliers (type, name, address) VALUES ('TH', 'Dũng', '14 Hương Lộ Ngọc Hiệp, TP. Nha Trang')");
			db.execSQL("INSERT INTO suppliers (type, name, address) VALUES ('TH', 'By Boy', '100 Đ. 2/4, TP. Nha Trang')");
			db.execSQL("INSERT INTO suppliers (type, name, address) VALUES ('TH', 'Đăng Khoa', '57 Nguyễn Đình Chiểu, TP. Nha Trang')");
			insertLog("Database created.", LogType.SUCCESS, compName, db);
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
		insertLog("Database upgraded.", LogType.SUCCESS, compName, db);
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
		else if (value instanceof Boolean) values.put("value", ((Boolean) value).toString());
		else if (value instanceof JSONObject) values.put("value", ((JSONObject) value).toString());
		values.put("timestamp", new Date().getTime() / 1000L);
		String where = "name = ?";
		String[] args = {keyName};
		try {
			result = db.update("setting", values, where, args);
		} catch (Exception e) {
			insertLog(e.getMessage(), LogType.ERROR, compName);
		}
		db.close();
		return result;
	}
	
	public Map<String, Object> getSetting() {
		return getSetting(false).get(0);
	}
	
	public List<Map<String, Object>> getSetting(boolean full) {
		String sql = "SELECT timestamp * 1000 AS timestamp, name, value, datatype FROM setting ORDER BY name";
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = null;
		try {
			cursor = (SQLiteDBCursor) db.rawQuery(sql, null);
		} catch (Exception e) {
			insertLog(e.getMessage(), LogType.ERROR, compName);
		}
		List<Map<String, Object>> result = new ArrayList<>();
		List<Map<String, Object>> brief = new ArrayList<>();
		Map<String, Object> config = new HashMap<>();
		while (cursor.moveToNext()) {
			Map<String, Object> row = new LinkedHashMap<>();
			String configName = "";
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				String key = cursor.getColumnName(i);
				switch (i) {
					case 0:
						row.put(key, new Date(cursor.getLong(i)));
						break;
					case 1:
						row.put(key, cursor.getString(i));
						configName = cursor.getString(i);
						break;
					case 2:
						DataType type = DataType.valueOf(cursor.getString(3));
						switch (type) {
							case TEXT:
								row.put(key, cursor.getString(i));
								config.put(configName, cursor.getString(i));
								break;
							case INTEGER:
								row.put(key, cursor.getInt(i));
								config.put(configName, cursor.getInt(i));
								break;
							case LONG:
								row.put(key, cursor.getLong(i));
								config.put(configName, cursor.getLong(i));
								break;
							case FLOAT:
								row.put(key, cursor.getFloat(i));
								config.put(configName, cursor.getFloat(i));
								break;
							case DOUBLE:
								row.put(key, cursor.getDouble(i));
								config.put(configName, cursor.getDouble(i));
								break;
							case BOOLEAN:
								row.put(key, Boolean.valueOf(cursor.getString(i)));
								config.put(configName, Boolean.valueOf(cursor.getString(i)));
								break;
							case BLOB:
								row.put(key, cursor.getBlob(i));
								config.put(configName, cursor.getBlob(i));
								break;
							case DATETIME:
								row.put(key, new Date(cursor.getLong(i)));
								config.put(configName, new Date(cursor.getLong(i)));
								break;
							case JSON:
								try {
									JSONObject json = new JSONObject(cursor.getString(i));
									row.put(key, json);
									config.put(configName, json);
								}
								catch (Exception e) {
									insertLog(e.getMessage(), LogType.ERROR, compName);
								}
								break;
						}
						break;
					case 3:
						row.put(key, DataType.valueOf(cursor.getString(i)));
						break;
				}
			}
			result.add(row);
		}
		brief.add(config);
		cursor.close();
		db.close();
		if (!full) return brief;
		return result;
	}
	
	public long addCmdHistory(RecentList<String> commands) {
		SQLiteDatabase db = getWritableDatabase();
		long result = -1;
		try {
			db.execSQL("DELETE FROM cmdhistory");
			for (String command : commands) {
				result = addCmdHistory(command);
			}
		} catch (Exception e) {
			insertLog(e.getMessage(), LogType.ERROR, compName);
		}
		db.close();
		return result;
	}
	
	public long addCmdHistory(String command) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("command", command);
		values.put("times", 1);
		long result = -1;
		try {
			result = db.insert("cmdhistory", null, values);
		} catch (Exception e) {
			insertLog(e.getMessage(), LogType.ERROR, compName);
		}
		db.close();
		return result;
	}
	
	public void updateCmdHistory(String command) {
		SQLiteDatabase db = getWritableDatabase();
		long now = new Date().getTime() / 1000L;
		String sql = "UPDATE cmdhistory SET timestamp = " + now + ", times = times + 1 WHERE command = ?";
		String[] args = {command};
		try {
			db.execSQL(sql, args);
		} catch (Exception e) {
			insertLog(e.getMessage(), LogType.ERROR, compName);
		}
	}
	
	public RecentList<String> getCmdHistory() {
		String sql = "SELECT command FROM cmdhistory ORDER BY timestamp";
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = null;
		RecentList<String> history = new RecentList<>();
		try {
			cursor = (SQLiteDBCursor) db.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				history.add(cursor.getString(0));
			}
		} catch (Exception e) {
			insertLog(e.getMessage(), LogType.ERROR, compName);
		}
		if (cursor != null) cursor.close();
		db.close();
		return history;
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
		String sql = "SELECT * FROM logs ORDER BY timestamp DESC";
		return getLogs(sql, null);
	}
	
	public List<Map<String, Object>> getLogs(int limit) {
		String sql = "SELECT * FROM logs ORDER BY timestamp DESC LIMIT ?";
		String[] args = {String.valueOf(limit)};
		return getLogs(sql, args);
	}
	
	public List<Map<String, Object>> getLogs(LogType type) {
		return getLogs(type, -1);
	}
	
	public List<Map<String, Object>> getLogs(LogType type, int limit) {
		String sql; String[] args;
		if (type == null) {
			sql = "SELECT * FROM logs ORDER BY timestamp DESC LIMIT ?";
			args = new String[] {String.valueOf(limit)};
		} else {
			sql = "SELECT * FROM logs WHERE type = ? ORDER BY timestamp DESC LIMIT ?";
			args = new String[] {type.name(), String.valueOf(limit)};
		}
		return getLogs(sql, args);
	}
	
	private List<Map<String, Object>> getLogs(String sql, String[] args) {
		SQLiteDatabase db = getReadableDatabase();
		List<Map<String, Object>> result = new ArrayList<>();
		SQLiteDBCursor cursor = null;
		try {
			cursor = (SQLiteDBCursor) db.rawQuery(sql, args);
			while (cursor.moveToNext()) {
				Map<String, Object> row = new LinkedHashMap<>();
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					String key = cursor.getColumnName(i);
					switch (i) {
						case 0:
							row.put(key, cursor.getInt(i));
							break;
						case 1:
							row.put(key, new Date(cursor.getLong(i) * 1000L));
							break;
						case 2:
							row.put(key, LogType.valueOf(cursor.getString(i)));
							break;
						default:
							row.put(key, cursor.getString(i));
					}
				}
				result.add(row);
			}
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		if (cursor != null) cursor.close();
		db.close();
		return result;
	}
	
	public List<Map<String, Object>> sql(String sql, String[] args) {
		return getData(null, sql, args, null);
	}
	
	public List<Map<String, Object>> getData(String table) {
		return getData(table, null, null, null);
	}
	
	public List<Map<String, Object>> getData(String table, Map<String, Object> attrs) {
		return getData(table, null, null, attrs);
	}
	
	private List<Map<String, Object>> getData(String table, String sql, String[] args, Map<String, Object> attrs) {
		SQLiteDatabase db = getReadableDatabase();
		List<Map<String, Object>> result = new ArrayList<>();
		SQLiteDBCursor cursor = null;
		String sort = "rowid"; int limit = -1;
		if (attrs != null) {
			if ((String) attrs.get("sort") != null && !"".equals((String) attrs.get("sort"))) sort = (String) attrs.get("sort");
			if (attrs.get("limit") != null) limit = attrs.get("limit");
		}
		try {
			if (sql == null || "".equals(sql)) sql = "SELECT * FROM " + table + " ORDER BY " + sort + " LIMIT " + limit;
			cursor = (SQLiteDBCursor) db.rawQuery(sql, args);
			result = cursor.getList();
		} catch (Exception e) {
			if (!"logs".equals(table)) insertLog(e.getMessage(), LogType.ERROR, compName);
			else Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		if (cursor != null) cursor.close();
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
	
	public String[] getColumnNames(String tblName) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = (SQLiteDBCursor) db.rawQuery("SELECT * FROM " + tblName + " LIMIT 0", null);
		String[] names = cursor.getColumnNames();
		cursor.close();
		return names;
	}
	
	public String[] getColumnTypes(String tblName) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = (SQLiteDBCursor) db.rawQuery("PRAGMA table_info(" + tblName + ")", null);
		String[] types = cursor.getColumnTypes();
		cursor.close();
		return types;
	}
	
	public DataType[] getDataTypes(String tblName) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = (SQLiteDBCursor) db.rawQuery("PRAGMA table_info(" + tblName + ")", null);
		DataType[] types = cursor.getDataTypes();
		cursor.close();
		return types;
	}
	
	public List<Map<String, String>> getForeignColumns(String tblName) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = (SQLiteDBCursor) db.rawQuery("PRAGMA foreign_key_list(" + tblName + ")", null);
		List<Map<String, String>> columns = cursor.getForeignColumns();
		for (Map<String, String> column : columns) {
			column.put("srcTable", tblName);
		}
		cursor.close();
		return columns;
	}
	
	public String[] getCatalog(String tblName) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = (SQLiteDBCursor) db.rawQuery("SELECT name FROM " + tblName + " ORDER BY name", null);
		String[] catalog = cursor.getCatalog();
		cursor.close();
		return catalog;
	}
	
	public int[] getCatalogID(String tblName) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = (SQLiteDBCursor) db.rawQuery("SELECT rowid, name FROM " + tblName + " ORDER BY name", null);
		int[] ids = cursor.getCatalogID();
		cursor.close();
		return ids;
	}
}
