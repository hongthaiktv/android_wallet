package app.web.onepagevn;
import android.app.*;
import android.widget.*;
import android.content.*;

public class AppSetting extends Application
{
	private Intent appService;

	@Override
	public void onCreate()
	{
		super.onCreate();
		
		appService = new Intent(this, AppService.class);
		startService(appService);
	}
}
