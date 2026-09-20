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
	SQLiteDBLayout tblLog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.applog);
		
		mainLayout = findViewById(R.id.applogMainLayout);
		stt = new StatusBar(this).init();
		tblLog = new SQLiteDBLayout(this, "logs", new SQLiteDBLayout.RowOnClickListener() {

				@Override
				public void onClick(View row, int index, int id)
				{
					row.setBackgroundColor(Color.RED);
				}
			});
		
		tblLog.setSQL("SELECT id, iif(strftime('%Y', timestamp, 'unixepoch', 'localtime') = strftime('%Y', 'now', 'localtime'), strftime('%d-%m %T', timestamp, 'unixepoch', 'localtime'), strftime('%d-%m-%Y %T', timestamp, 'unixepoch', 'localtime')) AS timestamp, type, component, message FROM logs");
		mainLayout.addView(tblLog, 0);
		tblLog.setOrdHeader("#").setHScroll(true)
		.show();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		stt.update();
	}
}
