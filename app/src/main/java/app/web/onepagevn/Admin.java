package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.database.sqlite.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.util.*;

public class Admin extends Activity
{
	private EditText edtInput;
	private Button btnLogSize, btnSQL, btnTable, btnLayout, btnUp, btnDown;
	private AppSetting app;
	private StatusBar stt;
	private SQLiteDB db;
	private RecentList<String> history;
	private SQLiteDBLayout tblData;
	private String tblName = "logs";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.admin);
		
		edtInput = findViewById(R.id.admEdtInput);
		btnLogSize = findViewById(R.id.admBtnLogSize);
		btnSQL = findViewById(R.id.admBtnSQL);
		btnTable = findViewById(R.id.admBtnTable);
		btnLayout = findViewById(R.id.admBtnLayout);
		btnUp = findViewById(R.id.admBtnUp);
		btnDown = findViewById(R.id.admBtnDown);
		tblData = findViewById(R.id.admTableData);
		app = AppSetting.getAppContext();
		stt = new StatusBar(this).init();
		db = new SQLiteDB();
		history = db.getCmdHistory();
		
		edtInput.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View view, int keyCode, KeyEvent event)
				{
					if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) btnSQL.performClick();
					return false;
				}
			});
		
		btnLogSize.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					try {
						int limit = Integer.valueOf(edtInput.getText().toString());
						app.setDlgMaxLogs(limit);
						if ("logs".equals(tblName) || "setting".equals(tblName)) tblData.show();
					} catch (Exception e) {stt.error(e.getMessage());}
				}
			});
			
		btnSQL.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					String sql = edtInput.getText().toString();
					if (history.bringToLast(sql)) db.updateCmdHistory(sql);
					else db.addCmdHistory(sql);
					edtInput.setText("");
					
					SQLiteDatabase writer = db.getWritableDatabase();
					boolean isData = false;
					SQLiteDBCursor cursor = null;
					try {
						cursor = (SQLiteDBCursor) writer.rawQuery(sql, null);
						//Toast.makeText(Admin.this, "test: " + cursor.getType(1), Toast.LENGTH_SHORT).show();
						while(cursor.moveToNext()) {
							String result = "";
							for (int i = 0; i < cursor.getColumnCount(); i++) {
								String name = cursor.getColumnName(i);
								String value = cursor.getString(i);
								result += name + " : " + value + " | ";
							}
							stt.log(result);
							if(!isData) isData = true;
						}
						if (cursor != null && !isData) stt.success(sql);
					} catch (Exception e) {stt.error(e.getMessage());}
					if (cursor != null) cursor.close();
					writer.close();
					tblData.show();
				}
			});
			
		btnTable.setOnClickListener(new OnClickListener() {
			
				@Override
				public void onClick(View p1)
				{
					List<Map<String, Object>> listTable = db.sql("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%' ORDER BY name ASC", null);
					ArrayList<String> tables = new ArrayList<>();
					for (Map<String, Object> table : listTable) {
						tables.add((String) table.get("name"));
					}
					final String[] tableNames = tables.toArray(new String[0]);
					AlertDialog.Builder dlgTable = new AlertDialog.Builder(Admin.this);
					dlgTable.setTitle("Select Table")
						.setIcon(R.drawable.ico_table)
						.setItems(tableNames, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int index)
							{
								tblName = tableNames[index];
								tblData.setSQL(null);
								switch (tblName) {
									case "logs":
										tblData.setSQL("SELECT id, datetime(timestamp, 'unixepoch', 'localtime') AS timestamp, type, component, message FROM logs ORDER BY timestamp DESC LIMIT 100");
										break;
									case "setting":
										tblData.setSQL("SELECT id, datetime(timestamp, 'unixepoch', 'localtime') AS timestamp, name, value, datatype FROM setting");
										break;
									case "cmdhistory":
										tblData.setSQL("SELECT id, datetime(timestamp, 'unixepoch', 'localtime') AS timestamp, datetime(datecreate, 'unixepoch', 'localtime') AS datecreate, command, times FROM cmdhistory ORDER BY timestamp DESC");
										break;
								}
								tblData.setTable(tblName).show();
							}
						});
					dlgTable.create().show();
				}
			});
			
		btnLayout.setOnClickListener(new OnClickListener() {
				private boolean enable = false;
				
				@Override
				public void onClick(View p1)
				{
					if (!enable) {
						tblData.setHScroll(true).show();
						enable = true;
					} else {
						tblData.setHScroll(false).show();
						enable = false;
					}
				}
			});
			
		btnUp.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					String prevCmd = history.prev();
					if (prevCmd != null) {
						edtInput.setText(prevCmd);
						if (!edtInput.isFocused()) edtInput.requestFocus();
						edtInput.setSelection(prevCmd.length());
					}
				}
			});
			
		btnDown.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					String nextCmd = history.next();
					if (nextCmd != null) {
						edtInput.setText(nextCmd);
						edtInput.setSelection(nextCmd.length());
					}
					else if (!history.isNull() && history.isLatest()) edtInput.setText("");
				}
			});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		stt.update();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
}
