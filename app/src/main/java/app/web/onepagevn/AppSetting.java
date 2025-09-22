package app.web.onepagevn;
import android.app.*;
import android.widget.*;

public class AppSetting extends Application
{

	@Override
	public void onCreate()
	{
		super.onCreate();
		Toast.makeText(this, "Global Setting Applied.", Toast.LENGTH_LONG).show();
	}
}
