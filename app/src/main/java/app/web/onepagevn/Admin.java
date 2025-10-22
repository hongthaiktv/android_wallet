package app.web.onepagevn;
import android.app.*;
import android.database.sqlite.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.util.*;
import android.content.*;

public class Admin extends Activity
{
	private EditText edtInput;
	private Button btnRun, btnSQL, btnTest, btnUp, btnDown;
	private AppSetting app;
	private StatusBar stt;
	private SQLiteDB db;
	private RecentList<String> history;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.admin);
		
		edtInput = findViewById(R.id.admEdtInput);
		btnRun = findViewById(R.id.admBtnRun);
		btnSQL = findViewById(R.id.admBtnSQL);
		btnTest = findViewById(R.id.admBtnTest);
		btnUp = findViewById(R.id.admBtnUp);
		btnDown = findViewById(R.id.admBtnDown);
		app = AppSetting.getAppContext();
		stt = new StatusBar(this).init();
		db = new SQLiteDB();
		history = app.getCmdHistory();
		
		edtInput.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View view, int keyCode, KeyEvent event)
				{
					if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
						String cmd = edtInput.getText().toString();
						history.add(cmd);
						edtInput.setText("");
					}
					return false;
				}
			});
		
		btnRun.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					try {
						int limit = Integer.valueOf(edtInput.getText().toString());
						db.updateSetting("dlgMaxLogs", limit);
						app.setDlgMaxLogs(Integer.valueOf(limit));
					} catch (Exception e) {stt.error(e.getMessage());}
				}
			});
			
		btnSQL.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					String sql = edtInput.getText().toString();
					SQLiteDatabase writer = db.getWritableDatabase();
					boolean isData = false;
					SQLiteDBCursor cursor = null;
					//Toast.makeText(Admin.this, "test: " + Boolean.TRUE, Toast.LENGTH_SHORT).show();
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
					} catch (Exception e) {stt.error(e.getMessage());}
					if (cursor != null && !isData) stt.success("Success run: " + sql);
					if (cursor != null) cursor.close();
					writer.close();
				}
			});
			
		btnTest.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					SQLiteDatabase writer = db.getWritableDatabase();
					SQLiteDBCursor cursor = null;
					try {
						Date date = new Date();
						ContentValues values = new ContentValues();
						values.put("name", date.getTime());
						String sql = "INSERT INTO date(name) VALUES('today')";
						writer.insert("date", null, values);
						
					} catch (Exception e) {stt.error(e.getMessage());}
					if (cursor != null) cursor.close();
					writer.close();
				}
			});
			
		btnUp.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					String prevCmd = history.prev();
					if (prevCmd != null) edtInput.setText(prevCmd);
				}
			});
			
		btnDown.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					String nextCmd = history.next();
					if (nextCmd != null) edtInput.setText(nextCmd);
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
}
