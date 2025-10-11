package app.web.onepagevn;
import android.app.*;

public class AppSetting extends Application
{
	private static AppSetting appContext;
	
	private AppServiceConnection appSrvConn;
	private StatusBar stt;

	@Override
	public void onCreate()
	{
		super.onCreate();
		
		appContext = this;
		stt = StatusBar.getInstance();
		appSrvConn = new AppServiceConnection(this, new Runnable() {

				@Override
				public void run()
				{
					stt.setMessage("Startup tasks...");
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
