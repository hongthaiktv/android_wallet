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
	private Button btnService, btnSync, btnClear, btnClick;
	private StatusBar stt;
	private AppSetting app = AppSetting.getAppContext();
	private AppServiceConnection conn;
	private SQLiteDB db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
		
		btnService = findViewById(R.id.editorBtnService);
		btnSync = findViewById(R.id.editorBtnSync);
		btnClear = findViewById(R.id.editorBtnClear);
		btnClick = findViewById(R.id.editorBtnClick);
		stt = new StatusBar(this).init();
		conn = app.getServiceConnection();
		db = new SQLiteDB();
		
		btnService.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					Message msg = Message.obtain(null, AppService.QUEUE);
					conn.send(msg);
					Map<String, Object> m = new HashMap<>();
					m.put("value", 1);
					int i = m.get("value");
				}
			});
			
		btnSync.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					Message msg = Message.obtain(null, AppService.SYNC);
					conn.send(msg);
				}
			});
			
		btnClear.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					stt.clear(true);
				}
			});
			
		btnClick.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					Message msg = Message.obtain(null, new Runnable() {
							ScheduledFuture sf;
							Context context = Editor.this;
						
							@Override
							public void run()
							{
								ScheduledExecutorService sexe = Executors.newScheduledThreadPool(1);
								Runnable run = new Runnable() {
									int counter = 1;

									@Override
									public void run()
									{
										stt.log(counter++ + ": exe received Runnable obj");
//										try {
//											TextView stt = ((Activity) context).findViewById(R.id.statusTvStatus);
//											stt.setText(counter++ + ": exe received Runnable obj");
//										} catch (Exception e) {
//											Toast.makeText(AppSetting.getAppContext(), e.getMessage(), Toast.LENGTH_LONG).show();
//										}
										if (counter >= 6) {
											sf.cancel(false);
											stt.log("schedule finish.");
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
		
		stt.update();
		
		Intent intent = getIntent();
		String intAction = intent.getAction();
		String intType = intent.getType();
		
		if (Intent.ACTION_SEND.equals(intAction) && intType != null) {
			String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
			Toast.makeText(this, sharedText, Toast.LENGTH_SHORT).show();
		}
	}
}
