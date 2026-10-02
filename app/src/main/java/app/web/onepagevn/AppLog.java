package app.web.onepagevn;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.View.*;
import android.view.*;
import android.content.*;
import android.widget.LinearLayout.*;
import android.graphics.*;

public class AppLog extends Activity
{
	private LinearLayout mainLayout;
	private StatusBar stt;
	private SQLiteDBLayout tblLog;
	private AppSetting app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.applog);
		
		mainLayout = findViewById(R.id.applogMainLayout);
		stt = new StatusBar(this).init();
		app = AppSetting.getAppContext();
		
		tblLog = new SQLiteDBLayout(this, "logs", new SQLiteDBLayout.RowOnClickListener() {

				@Override
				public void onClick(View row, int index, int id)
				{
					row.setBackgroundColor(Color.RED);
				}
			});
		
		tblLog.setRowOnLongClickListener(new SQLiteDBLayout.RowOnLongClickListener() {

				@Override
				public boolean onLongClick(View row, int index, int id)
				{
					row.setBackgroundColor(Color.YELLOW);
					return true;
				}
			});
		int logSize = app.getMaxLogs();
		tblLog.setSQL("SELECT id, CASE WHEN strftime('%Y', timestamp, 'unixepoch', 'localtime') = strftime('%Y', 'now', 'localtime') THEN strftime('%d-%m %H:%M:%S', timestamp, 'unixepoch', 'localtime') ELSE strftime('%d-%m-%Y %H:%M:%S', timestamp, 'unixepoch', 'localtime') END AS timestamp, type, component, message FROM logs ORDER BY timestamp DESC LIMIT " + logSize);
		mainLayout.addView(tblLog, 0);
		tblLog.setOrdHeader("#").setHScroll(true);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		tblLog.show();
		stt.update();
	}
}
