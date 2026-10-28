package app.web.onepagevn;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

public class AppLog extends AppCompatActivity
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
		
		tblLog = new SQLiteDBLayout(this, "logs", new SQLiteDBLayout.OnRowClickListener() {

				@Override
				public void onClick(View row, int index, int id)
				{
					row.setBackgroundColor(Color.RED);
				}
			});
		
		tblLog.setOnRowLongClickListener(new SQLiteDBLayout.OnRowLongClickListener() {

				@Override
				public boolean onLongClick(View row, int index, int id)
				{
					row.setBackgroundColor(Color.YELLOW);
					return true;
				}
			});
		int logSize = app.getMaxLogs();
		tblLog.setSQL("SELECT id, timestamp, type, component, message FROM logs ORDER BY timestamp DESC LIMIT " + logSize);
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
