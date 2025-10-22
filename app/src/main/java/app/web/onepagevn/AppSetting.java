package app.web.onepagevn;
import android.app.*;
import java.util.*;

public class AppSetting extends Application
{
	private static AppSetting appContext;
	private StatusBar stt;
	private AppServiceConnection appSrvConn;
	private SQLiteDB db;
	private int dlgMaxLogs = 100;
	private RecentList<String> cmdHistory;

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
		
		List<Map<String, Object>> setting = db.getSetting();
		for (int i = 0; i < setting.size(); i++) {
			Map<String, Object> row = setting.get(i);
			String name = (String) row.get("name");
			if ("dlgMaxLogs".equals(name)) dlgMaxLogs = row.get("value");
		}
		cmdHistory = new RecentList<>();
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
	
	public void setDlgMaxLogs(int limit) {
		if (limit <= 0) stt.error("Dialog max logs must >= 1");
		else {
			dlgMaxLogs = limit;
			stt.success("Dialog max logs changed to " + dlgMaxLogs);
		}
	}
	
	public int getDlgMaxLogs() {
		return dlgMaxLogs;
	}
	
	public RecentList<String> getCmdHistory() {
		return cmdHistory;
	}
}
