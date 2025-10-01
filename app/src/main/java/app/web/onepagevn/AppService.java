package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.os.*;
import java.util.concurrent.*;

public class AppService extends Service
{
	private StatusBar stt;
	private ScheduledExecutorService sexe;
	private static BlockingQueue<Runnable> tasks;

	@Override
	public void onCreate()
	{
		super.onCreate();
		
		stt = StatusBar.getInstance();
		sexe = Executors.newScheduledThreadPool(1);
		if (tasks == null) tasks = new LinkedBlockingQueue<>();
		
		stt.setMessage("Service started.");
	}
	
	@Override
	public int onStartCommand(final Intent intent, int flags, int startId)
	{
		final String task = intent.getStringExtra("task");
		if (task == null || task.equals("")) return START_STICKY;
		
		switch (task) {
			case "status":
				stt.setMessage(intent.getStringExtra("message"));
				break;
			case "sync":
				stt.setMessage("Test service job.");
				break;
			case "run":
				//get last job submit and wait to determine end task.
				//prevent additional invoke
				String total = "Total: " + tasks.size() + " ";
				while (!tasks.isEmpty()) {
					Runnable queue = tasks.poll();
					sexe.execute(queue);
				}
				stt.setMessage(total + "task jobs running...");
				break;
		}

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	public static void setTask(Runnable task)
	{
		tasks.offer(task);
	}

	public static BlockingQueue<Runnable> getTasks()
	{
		return tasks;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		sexe.shutdown();
		tasks.clear();
		stt.setMessage("Service stoped.");
	}
}
