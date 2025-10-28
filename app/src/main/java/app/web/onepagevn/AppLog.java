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
	private Button button, btnExit;
	private StatusBar stt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.applog);
		
		button = findViewById(R.id.applogButton);
		btnExit = findViewById(R.id.applogBtnExit);
		mainLayout = findViewById(R.id.applogMainLayout);
		stt = new StatusBar(this).init();
		SQLiteDBLayout tblLog = new SQLiteDBLayout(this, "logs", new SQLiteDBLayout.RowOnClickListener() {

				@Override
				public void onClick(View row, int index, int id)
				{
					TableLayout parent = (TableLayout) row.getParent();
					row.setBackgroundColor(Color.RED);
					View nextSibling = parent.getChildAt(index + 1);
					if (nextSibling != null) nextSibling.setBackgroundColor(Color.YELLOW);
				}
			});
		
		mainLayout.addView(tblLog, 1);
		tblLog.setOrdHeader("STT").setHScroll(true)
		.show();
		
		button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					Intent service = new Intent(AppLog.this, AppService.class);
					startService(service);
				}
			});
			
		btnExit.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					stt.log("New str from AppLog");
					finish();
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
