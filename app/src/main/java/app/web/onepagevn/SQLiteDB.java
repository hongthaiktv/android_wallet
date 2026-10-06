package app.web.onepagevn;
import android.database.*;
import android.database.sqlite.*;
import android.widget.*;
import java.util.*;
import android.content.*;
import org.json.*;
import java.text.*;

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
			db.execSQL("CREATE TABLE logs (id INTEGER PRIMARY KEY, timestamp DATETIME DEFAULT (CAST(strftime('%s', 'now') AS NUMERIC)) NOT NULL, type TEXT NOT NULL, component TEXT NOT NULL, message TEXT)");
			db.execSQL("CREATE TABLE setting (id INTEGER PRIMARY KEY, timestamp DATETIME DEFAULT (CAST(strftime('%s', 'now') AS NUMERIC)) NOT NULL, name TEXT UNIQUE NOT NULL, value BLOB NOT NULL, datatype COLTYPE NOT NULL)");
			db.execSQL("CREATE TABLE cmdhistory (id INTEGER PRIMARY KEY, timestamp DATETIME DEFAULT (CAST(strftime('%s', 'now') AS NUMERIC)) NOT NULL, datecreate DATETIME DEFAULT (CAST(strftime('%s', 'now') AS NUMERIC)) NOT NULL, command TEXT UNIQUE NOT NULL, times INT NOT NULL)");
			db.execSQL("CREATE TABLE todos (todoid INTEGER PRIMARY KEY, date DATETIME DEFAULT (CAST(strftime('%s', 'now') AS NUMERIC)) NOT NULL, job TEXT, priority INT DEFAULT 3 NOT NULL, complete BOOLEAN DEFAULT 0 NOT NULL)");
			db.execSQL("CREATE TABLE budgets_protected (budgetid INTEGER PRIMARY KEY, date DATETIME DEFAULT (CAST(strftime('%s', 'now') AS NUMERIC)) NOT NULL, logo TEXT, name TEXT UNIQUE NOT NULL, balance REAL DEFAULT 0 NOT NULL, note TEXT, protected BOOLEAN DEFAULT 0 NOT NULL)");
			db.execSQL("CREATE VIEW budgets AS SELECT budgetid, date, logo, name, balance, note FROM budgets_protected");
			db.execSQL("CREATE TRIGGER prevent_delete_budgets BEFORE DELETE ON budgets_protected FOR EACH ROW WHEN OLD.protected=1 BEGIN SELECT RAISE(FAIL, 'This record is protected and cannot be modified.'); END");
			db.execSQL("CREATE TRIGGER prevent_update_budgets BEFORE UPDATE ON budgets_protected FOR EACH ROW WHEN OLD.budgetid != NEW.budgetid AND OLD.protected=1 BEGIN SELECT RAISE(FAIL, 'This record is protected and cannot be modified.'); END");
			db.execSQL("CREATE TABLE funds (fundid INTEGER PRIMARY KEY, date DATETIME DEFAULT (CAST(strftime('%s', 'now') AS NUMERIC)) UNIQUE NOT NULL, cash REAL DEFAULT 0 NOT NULL, bank1 INT REFERENCES budgets_protected (budgetid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL, balance1 REAL DEFAULT 0 NOT NULL, bank2 INT REFERENCES budgets_protected (budgetid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL, balance2 REAL DEFAULT 0 NOT NULL, note TEXT)");
			db.execSQL("CREATE TABLE suppliers (supplierid INTEGER PRIMARY KEY, type TEXT NOT NULL, name TEXT NOT NULL, address TEXT, phone TEXT, gps TEXT, note TEXT)");
			db.execSQL("CREATE TABLE units (unitid INTEGER PRIMARY KEY, name TEXT UNIQUE NOT NULL, note TEXT)");
			db.execSQL("CREATE TABLE product_types (ptypeid INTEGER PRIMARY KEY, name TEXT UNIQUE NOT NULL, note TEXT)");
			db.execSQL("CREATE TABLE products (productid INTEGER PRIMARY KEY, name TEXT UNIQUE NOT NULL, price REAL DEFAULT 0 NOT NULL, unitid INT REFERENCES units (unitid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL, ptypeid INT REFERENCES product_types (ptypeid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL, note TEXT)");
			db.execSQL("CREATE TABLE expends (expendid INTEGER PRIMARY KEY, date DATETIME DEFAULT (CAST(strftime('%s', 'now') AS NUMERIC)) NOT NULL, supplierid INT REFERENCES suppliers (supplierid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL, productid INT REFERENCES products (productid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL, unitid INT REFERENCES units (unitid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL, price REAL DEFAULT 0 NOT NULL, quantity REAL DEFAULT 1 NOT NULL, note TEXT, budgetid INT REFERENCES budgets_protected (budgetid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL)");
			db.execSQL("CREATE TABLE supplier_products (sproductid INTEGER PRIMARY KEY, supplierid INT REFERENCES suppliers (supplierid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL, productid INT REFERENCES products (productid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL, price REAL DEFAULT 0 NOT NULL, unitid INT REFERENCES units (unitid) ON UPDATE CASCADE ON DELETE RESTRICT NOT NULL, startdate DATETIME DEFAULT (CAST(strftime('%s', 'now') AS NUMERIC)) NOT NULL, enddate DATETIME DEFAULT (CAST(strftime('%s', 'now') AS NUMERIC)) NOT NULL, note TEXT)");
			db.execSQL("INSERT INTO setting (name, value, datatype) VALUES ('dlgMaxLogs', 100, 'INTEGER')");
			db.execSQL("INSERT INTO budgets_protected (name, protected) VALUES ('Cash', 1)");
			db.execSQL("INSERT INTO budgets_protected (name, protected) VALUES ('TPBank', 1)");
			db.execSQL("INSERT INTO budgets_protected (name, protected) VALUES ('Vietcombank', 1)");
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
			db.execSQL("INSERT INTO units (name) VALUES ('Litre')");
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
			db.execSQL("INSERT INTO products (name, price, unitid, ptypeid) VALUES ('Xăng', 27080, 12, 4)");
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
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = (SQLiteDBCursor) db.rawQuery(sql, null);
		List<Map<String, Object>> result = cursor.getList();
		cursor.close();
		db.close();
		return result;
	}
	
	public long insert(String table, ContentValues data) {
		SQLiteDatabase db = getWritableDatabase();
		long result = -1;
		try {
			result = db.insert(table, null, data);
		} catch (Exception e) {
			insertLog(e.getMessage(), LogType.ERROR, compName);
		}
		db.close();
		return result;
	}
	
	public int update(String table, int rowID, ContentValues data) {
		SQLiteDatabase db = getWritableDatabase();
		int result = -1;
		String where = "rowid = ?";
		String[] args = {String.valueOf(rowID)};
		try {
			result = db.update(table, data, where, args);
		} catch (Exception e) {
			insertLog(e.getMessage(), LogType.ERROR, compName);
		}
		db.close();
		return result;
	}

	public int delete(String table, int rowID) {
		SQLiteDatabase db = getWritableDatabase();
		int result = -1;
		String where = "rowid = ?";
		String[] args = {String.valueOf(rowID)};
		try {
			result = db.delete(table, where, args);
		} catch (Exception e) {
			insertLog(e.getMessage(), LogType.ERROR, compName);
		}
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
		else if (value instanceof JSONObject) values.put("value", ((JSONObject) value).toString());
		else if (value instanceof JSONArray) values.put("value", ((JSONArray) value).toString());
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
		String table = "setting";
		String sql = "SELECT name, value FROM " + table + " ORDER BY name COLLATE NOCASE";
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = null;
		Map<String, Object> setting = new LinkedHashMap<>();
		try {
			cursor = (SQLiteDBCursor) db.rawQueryWithFactory(cursorFactory, sql, null, table);
			List<Map<String, Object>> settings = cursor.getList();
			for (Map<String, Object> item : settings) {
				setting.put((String) item.get("name"), item.get("value"));
			}
		} catch (Exception e) {
			insertLog(e.getMessage(), LogType.ERROR, compName);
		}
		if (cursor != null) cursor.close();
		db.close();
		return setting;
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
		String table = "cmdhistory";
		String sql = "SELECT command FROM " + table + " ORDER BY timestamp";
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = null;
		RecentList<String> history = new RecentList<>();
		try {
			cursor = (SQLiteDBCursor) db.rawQueryWithFactory(cursorFactory, sql, null, table);
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
		String table = "logs";
		String sql = "SELECT * FROM " + table + " ORDER BY timestamp DESC";
		return getLogs(table, sql, null);
	}
	
	public List<Map<String, Object>> getLogs(int limit) {
		String table = "logs";
		String sql = "SELECT * FROM " + table + " ORDER BY timestamp DESC LIMIT ?";
		String[] args = {String.valueOf(limit)};
		return getLogs(table, sql, args);
	}
	
	public List<Map<String, Object>> getLogs(LogType type) {
		return getLogs(type, -1);
	}
	
	public List<Map<String, Object>> getLogs(LogType type, int limit) {
		String table = "logs", sql;
		String[] args;
		if (type == null) {
			sql = "SELECT * FROM " + table + " ORDER BY timestamp DESC LIMIT ?";
			args = new String[] {String.valueOf(limit)};
		} else {
			sql = "SELECT * FROM " + table + " WHERE type = ? ORDER BY timestamp DESC LIMIT ?";
			args = new String[] {type.name(), String.valueOf(limit)};
		}
		return getLogs(table, sql, args);
	}
	
	private List<Map<String, Object>> getLogs(String table, String sql, String[] args) {
		SQLiteDatabase db = getReadableDatabase();
		List<Map<String, Object>> result = new ArrayList<>();
		SQLiteDBCursor cursor = null;
		try {
			cursor = (SQLiteDBCursor) db.rawQueryWithFactory(cursorFactory, sql, args, table);
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
	
	public List<Map<String, Object>> sql(String table, String sql, String[] args) {
		return getData(table, sql, args, null);
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
			cursor = (SQLiteDBCursor) db.rawQueryWithFactory(cursorFactory, sql, args, table);
			result = cursor.getList();
		} catch (Exception e) {
			if (!"logs".equals(table)) insertLog(e.getMessage(), LogType.ERROR, compName);
			else Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		if (cursor != null) cursor.close();
		db.close();
		return result;
	}
	
	public int clearLogs() {
		String table = "logs";
		SQLiteDatabase db = getWritableDatabase();
		int result = -1;
		try {
			result = db.delete(table, null, null);
		} catch (Exception e) {
			Toast.makeText(app, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		db.close();
		return result;
	}
	
	public String[] getColumnNames(String table) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = (SQLiteDBCursor) db.rawQueryWithFactory(cursorFactory, "SELECT * FROM " + table + " LIMIT 0", null, table);
		String[] names = cursor.getColumnNames();
		cursor.close();
		return names;
	}
	
	public Map<String, Object> getRowData(String table, int rowID) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = (SQLiteDBCursor) db.rawQueryWithFactory(cursorFactory, "SELECT * FROM " + table + " WHERE rowid=? LIMIT 1", new String[] {String.valueOf(rowID)}, table);
		Map<String, Object> rowData = cursor.getList().get(0);
		cursor.close();
		return rowData;
	}
	
	public String[] getColumnTypes(String table) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = (SQLiteDBCursor) db.rawQueryWithFactory(cursorFactory, "PRAGMA table_info(" + table + ")", null, table);
		String[] types = cursor.getColumnTypes();
		cursor.close();
		return types;
	}
	
	public DataType[] getDataTypes(String table) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = (SQLiteDBCursor) db.rawQueryWithFactory(cursorFactory, "PRAGMA table_info(" + table + ")", null, table);
		DataType[] types = cursor.getDataTypes();
		cursor.close();
		return types;
	}
	
	public List<Map<String, Object>> getForeignColumns(String table) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = (SQLiteDBCursor) db.rawQueryWithFactory(cursorFactory, "PRAGMA foreign_key_list(" + table + ")", null, table);
		List<Map<String, Object>> columns = cursor.getForeignColumns();
		cursor.close();
		for (Map<String, Object> column : columns) {
			column.put("srcTable", table);
			String dstTable = (String) column.get("dstTable");
			SQLiteDBCursor dstCursor = (SQLiteDBCursor) db.rawQueryWithFactory(cursorFactory, "SELECT rowid, name FROM " + dstTable + " ORDER BY name COLLATE NOCASE", null, table);
			List<Object> catalog = dstCursor.getCatalog();
			Integer[] ids = (Integer[]) catalog.get(0);
			String[] names = (String[]) catalog.get(1);
			column.put("ids", ids);
			column.put("names", names);
			dstCursor.close();
		}
		return columns;
	}
	
	public List<Object> getCatalog(String table) {
		SQLiteDatabase db = getReadableDatabase();
		SQLiteDBCursor cursor = (SQLiteDBCursor) db.rawQueryWithFactory(cursorFactory, "SELECT rowid, name FROM " + table + " ORDER BY name COLLATE NOCASE", null, table);
		List<Object> catalog = cursor.getCatalog();
		cursor.close();
		return catalog;
	}

	public double getTotalExpends() {
		double totalExpends = 0;
		List<Map<String, Object>> expends = sql("expends", "SELECT CAST(printf('%.0f', sum(price * quantity)) AS REAL) AS total FROM expends WHERE expends.date > (SELECT max(date) FROM funds)", null);
		if (expends.size() > 0) {
			Map<String, Object> expend = expends.get(0);
			totalExpends = expend.get("total");
		}
		return totalExpends;
	}

	public double getTotalFunds() {
		double cash = 0, balance1 = 0, balance2 = 0, totalFunds = 0;
		List<Map<String, Object>> funds = sql("funds", "SELECT max(date) AS date, CAST(printf('%.0f', cash) AS REAL) AS cash, CAST(printf('%.0f', balance1) AS REAL) AS balance1, CAST(printf('%.0f', balance2) AS REAL) AS balance2 FROM funds", null);
		if (funds.size() > 0) {
			Map<String, Object> fund = funds.get(0);
			cash = fund.get("cash");
			balance1 = fund.get("balance1");
			balance2 = fund.get("balance2");
			totalFunds = cash + balance1 + balance2;
		}
		return totalFunds;
	}
	
	public double getBalanceRemain() {
		double totalFunds = getTotalFunds(),
			totalExpends = getTotalExpends();
		double remain = totalFunds - totalExpends;
		return remain;
	}
	
	public double getCashRemain() {
		double cash = 0, cashExpends = 0, cashRemain = 0;
		List<Map<String, Object>> funds = sql("funds", "SELECT max(date) AS date, CAST(printf('%.0f', cash) AS REAL) AS cash FROM funds", null);
		if (funds.size() > 0) {
			Map<String, Object> fund = funds.get(0);
			cash = fund.get("cash");
		}
		List<Map<String, Object>> expends = sql("expends", "SELECT CAST(printf('%.0f', sum(price * quantity)) AS REAL) AS cash_expends FROM expends WHERE expends.date > (SELECT max(date) FROM funds) AND expends.budgetid=1", null);
		if (expends.size() > 0) {
			Map<String, Object> expend = expends.get(0);
			cashExpends = expend.get("cash_expends");
		}
		cashRemain = cash - cashExpends;
		return cashRemain;
	}
	
	public String objectToString(Object object, DataType type) {
		if (object == null) return null;
		SimpleDateFormat flocal = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		NumberFormat numConverter = NumberFormat.getInstance(Locale.US);
		numConverter.setMaximumFractionDigits(7);
		String result = null;
		switch (type) {
			case INTEGER: case INT: case NUMERIC: case NUM: case LONG:
			case REAL: case FLOAT: case DOUBLE:
				result = numConverter.format(object);
				break;
			case TEXT:
				result = (String) object;
				break;
			case BOOLEAN:
				result = (Boolean) object ? "1" : "0";
				break;
			case DATETIME:
				result = flocal.format((Date) object);
				break;
			case JSON:
				try
				{
					if (object instanceof JSONObject) result = ((JSONObject) object).toString(4);
					else result = ((JSONArray) object).toString(4);
				}
				catch (JSONException e)
				{insertLog(e.getMessage(), LogType.ERROR, compName);}
				break;
			case NULL:
				result = null;
				break;
			case BLOB: default:
				if (object instanceof String) result = (String) object;
				else if (object instanceof Integer || object instanceof Long || object instanceof Float || object instanceof Double) result = numConverter.format(object);
				else if (object instanceof Boolean) result = (Boolean) object ? "1" : "0";
				else if (object instanceof Date) result = flocal.format((Date) object);
				else if (object instanceof JSONObject) {
					try
					{
						result = ((JSONObject) object).toString(4);
					}
					catch (JSONException e)
					{insertLog(e.getMessage(), LogType.ERROR, compName);}
				}
				else if (object instanceof JSONArray) {
					try
					{
						result = ((JSONArray) object).toString(4);
					}
					catch (JSONException e)
					{insertLog(e.getMessage(), LogType.ERROR, compName);}
				}
		}
		return result;
	}
}
