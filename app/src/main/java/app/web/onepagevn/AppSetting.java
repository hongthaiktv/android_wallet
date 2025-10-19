package app.web.onepagevn;
import android.app.*;
import android.widget.*;

public class AppSetting extends Application
{
	private static AppSetting appContext;
	private StatusBar stt;
	private AppServiceConnection appSrvConn;
	private SQLiteDB db;
	private int maxDialogLogs = 100;

	@Override
	public void onCreate()
	{
		super.onCreate();
		
		appContext = this;
		stt = new StatusBar(this);
		db = new SQLiteDB();
		appSrvConn = new AppServiceConnection(this, new Runnable() {

				@Override
				public void run()
				{
					stt.log("Startup tasks...");
				}
			});
		
		appSrvConn.connect();
		stt.info(db.getVersion());
	}
	
	public static AppSetting getAppContext()
	{
		return appContext;
	}
	
	public AppServiceConnection getServiceConnection() {
		return appSrvConn;
	}
	
	public void setMaxDialogLogs(int limit) {
		if (limit <= 0) stt.error("Max dialog logs must >= 1");
		else maxDialogLogs = limit;
	}
	
	public int getMaxDialogLogs() {
		return maxDialogLogs;
	}
}
