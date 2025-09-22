package app.web.onepagevn;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.View.*;
import android.view.*;

public class AppLog extends Activity
{
	private Button button, btnExit;
	private LinearLayout mainLayout, sttLayout;
	private StatusBar stt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.applog);
		
		button = findViewById(R.id.applogButton);
		btnExit = findViewById(R.id.applogBtnExit);
		mainLayout = findViewById(R.id.applogMainLayout);
		
		LayoutInflater sttInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		sttLayout = (LinearLayout) sttInflater.inflate(R.layout.status_bar, null);
		mainLayout.addView(sttLayout);
		stt = StatusBar.getInstance(sttLayout);
		
		button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					stt.setCrossPro("new stt");
					//Toast.makeText(AppLog.this, txt, Toast.LENGTH_SHORT).show();
				}
			});
			
		btnExit.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					stt.setCrossPro("New str from AppLog");
					finish();
				}
			});
	}
}
