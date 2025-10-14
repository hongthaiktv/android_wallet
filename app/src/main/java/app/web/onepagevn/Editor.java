package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.view.View.*;
import java.util.concurrent.*;
import java.util.*;

public class Editor extends Activity
{
	private Button btnService, btnSync, btnClick;
	private StatusBar stt;
	private AppSetting app = AppSetting.getAppContext();
	private AppServiceConnection conn = app.getServiceConnection();
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);

		btnService = findViewById(R.id.editorBtnService);
		btnSync = findViewById(R.id.editorBtnSync);
		btnClick = findViewById(R.id.editorBtnClick);
		stt = StatusBar.initial(this);
		//conn = app.getServiceConnection();
		
		btnService.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					Message msg = Message.obtain(null, AppService.QUEUE);
					conn.send(msg);
				}
			});
			
		btnSync.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					
				}
			});
			
		btnClick.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					Message msg = Message.obtain(null, new Runnable() {
							ScheduledFuture sf;
						
							@Override
							public void run()
							{
								ScheduledExecutorService sexe = Executors.newScheduledThreadPool(1);
								Runnable run = new Runnable() {
									int counter = 1;

									@Override
									public void run()
									{
										StatusBar.getInstance().log(counter++ + ": exe received Runnable obj");
										if (counter >= 21) {
											sf.cancel(false);
											StatusBar.getInstance().log("schedule finish.");
										}
									}
								};
								sf = sexe.scheduleAtFixedRate(run, 1, 1, TimeUnit.SECONDS);
							}
						});
					conn.send(msg);
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
