package app.web.onepagevn;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.View.*;
import android.view.*;

public class AppLog extends Activity
{
	Button button, btnExit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.applog);
		
		button = findViewById(R.id.applogButton);
		btnExit = findViewById(R.id.applogBtnExit);
		
		button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					String txt = StatusBar.getCrossPro();
					Toast.makeText(AppLog.this, txt, Toast.LENGTH_SHORT).show();
				}
			});
			
		btnExit.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					StatusBar.setCrossPro("New str from AppLog");
					finish();
				}
			});
	}
}
