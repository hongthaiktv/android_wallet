package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.view.View.*;

public class Editor extends Activity
{
	private Button btnService, btnSync, btnClick;
	private StatusBar stt;
	private Intent appService;
	private int count = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);

		btnService = findViewById(R.id.editorBtnService);
		btnSync = findViewById(R.id.editorBtnSync);
		btnClick = findViewById(R.id.editorBtnClick);
		stt = StatusBar.initial(this);
		appService = new Intent(this, AppService.class);
		
		btnService.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					String message = count++ + ": Status message.";
					appService.putExtra("message", message);
					appService.putExtra("task", "status");
					startService(appService);
				}
			});
			
		btnSync.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					for (int i = 1; i < 6; i++) {
						final String message = i + ": task job.";
						AppService.setTask(new Runnable() {

								@Override
								public void run()
								{
									try
									{
										Thread.sleep(3000);
										stt.setMessage(message);
									}
									catch (InterruptedException e)
									{}
								}
							});
					}
					
					appService.putExtra("task", "run");
					startService(appService);
				}
			});
			
		btnClick.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					stopService(appService);
				}
			});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		stt.update(this);
		
		Intent intent = getIntent();
		String intAction = intent.getAction();
		String intType = intent.getType();
		
		if (Intent.ACTION_SEND.equals(intAction) && intType != null) {
			String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
			Toast.makeText(this, sharedText, Toast.LENGTH_SHORT).show();
		}
	}
}
