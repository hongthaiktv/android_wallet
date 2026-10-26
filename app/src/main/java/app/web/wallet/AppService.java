package app.web.wallet;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

public class AppService extends Service
{
	public static final int FAILED = 0;
	public static final int SUCCESS = 1;
	public static final int RUN = 2;
	public static final int QUEUE = 3;
	public static final int SYNC = 4;
	
	private StatusBar stt;
	private ScheduledExecutorService sexe;
	private BlockingQueue<Runnable> tasks;
	private Messenger messenger;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		stt = new StatusBar(this);
		sexe = Executors.newScheduledThreadPool(1);
		tasks = new LinkedBlockingQueue<>();
		messenger = new Messenger(new ServiceHandler());
	}
	
	@Override
	public int onStartCommand(final Intent intent, int flags, int startId)
	{
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return messenger.getBinder();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		sexe.shutdown();
		tasks.clear();
		messenger = null;
		String msg = "Service stoped.";
		stt.warning(msg);
		Toast.makeText(AppSetting.getAppContext(), msg, Toast.LENGTH_LONG).show();
	}
	
	private class ServiceHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what) {
				case AppService.QUEUE:
					stt.error("Service: Action : QUEUE : " + new Date().getSeconds());
					break;
				case AppService.SYNC:
					stt.add("Service: Action : SYNC : " + new Date().getSeconds(), LogType.INFO, "Service");
					break;
				case AppService.RUN:
					stt.warning("Service: Action : RUN");
					break;
			}
		}
	}
}
