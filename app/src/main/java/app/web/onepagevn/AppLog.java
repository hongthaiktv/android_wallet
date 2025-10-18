package app.web.onepagevn;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.View.*;
import android.view.*;
import android.content.*;

public class AppLog extends Activity
{
	private Button button, btnExit;
	private StatusBar stt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.applog);
		
		button = findViewById(R.id.applogButton);
		btnExit = findViewById(R.id.applogBtnExit);
		
		stt = new StatusBar(this).init();
		
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
