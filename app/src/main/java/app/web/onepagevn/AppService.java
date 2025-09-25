package app.web.onepagevn;
import android.app.*;
import android.os.*;
import android.content.*;
import android.widget.*;

public class AppService extends Service
{
	Context context;
	String txt = "";
	StatusBar stt;

	@Override
	public void onCreate()
	{
		super.onCreate();
		
		stt = StatusBar.getInstance();
		context = stt.getContext();
		Toast.makeText(context, "service created", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Toast.makeText(context, "service started", Toast.LENGTH_SHORT).show();
		new Thread(new Runnable() {

				@Override
				public void run()
				{
					for (int i = 1; i < 6; i++) {
						try
						{
							txt += i + " | ";
							Thread.sleep(1000);
							stt.setMessage(txt);
						}
						catch (InterruptedException e)
						{}
					}
					stopSelf();
				}

			}).start();
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Toast.makeText(context, "service stoped", Toast.LENGTH_SHORT).show();
	}
}
