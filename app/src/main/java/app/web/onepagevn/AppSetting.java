package app.web.onepagevn;
import android.app.*;
import java.util.*;

public class AppSetting extends Application
{
	private static AppSetting appContext;
	
	private StatusBar stt;
	private AppServiceConnection appSrvConn;
	private SQLiteDB db;
	private Map<String, Object> setting;

	@Override
	public void onCreate()
	{
		super.onCreate();
		
		appContext = this;
		stt = new StatusBar(this);
		db = new SQLiteDB();
		setting = db.getSetting();
		
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
	
	public void setMaxLogs(int limit) {
		if (limit <= 0) stt.error("Dialog max logs must >= 1");
		else {
			db.updateSetting("dlgMaxLogs", limit);
			setting.put("dlgMaxLogs", limit);
			stt.success("Max logs changed to " + limit);
		}
	}
	
	public int getMaxLogs() {
		return setting.get("dlgMaxLogs");
	}
}
