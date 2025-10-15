package app.web.onepagevn;
import android.app.*;
import java.util.*;

public class AppSetting extends Application
{
	private static AppSetting appContext;
	private StatusBar stt = StatusBar.getInstance();
	private AppServiceConnection appSrvConn;

	@Override
	public void onCreate()
	{
		super.onCreate();
		
		appContext = this;
		appSrvConn = new AppServiceConnection(this, new Runnable() {

				@Override
				public void run()
				{
					stt.log("Startup tasks...");
				}
			});
		
		appSrvConn.connect();
	}
	
	public static AppSetting getAppContext()
	{
		return appContext;
	}
	
	public AppServiceConnection getServiceConnection() {
		return appSrvConn;
	}
}
