package app.web.onepagevn;

import android.content.*;
import android.os.*;
import android.widget.*;

public class AppServiceConnection implements ServiceConnection
{
	private Context context;
	private Intent intService;
	private Messenger serviceMessenger;
	private Runnable callBack;
	private StatusBar stt;
	private boolean connected = false;
	
	public AppServiceConnection(Context context) {
		init(context);
	}
	
	public AppServiceConnection(Context context, Runnable callBack) {
		init(context);
		this.callBack = callBack;
	}
	
	private void init(Context context) {
		this.context = context;
		intService = new Intent(context, AppService.class);
		stt = StatusBar.getInstance();
	}

	@Override
	public void onServiceConnected(ComponentName comp, IBinder binder)
	{
		connected = true;
		serviceMessenger = new Messenger(binder);
		stt.success("Service started.");
		if (callBack != null) callBack.run();
	}

	@Override
	public void onServiceDisconnected(ComponentName comp)
	{
		connected = false;
		serviceMessenger = null;
		stt.warning("Service disconnected.");
		Toast.makeText(AppSetting.getAppContext(), "Service disconnected.", Toast.LENGTH_LONG).show();
	}
	
	public void connect() {
		if (!connected) context.bindService(intService, this, context.BIND_AUTO_CREATE);
	}
	
	public void connect(Runnable callBack) {
		if (!connected) {
			this.callBack = callBack;
			context.bindService(intService, this, context.BIND_AUTO_CREATE);
		}
	}
	
	public void disconnect() {
		connected = false;
		serviceMessenger = null;
		try {
			if (connected) context.unbindService(this);
		} catch (Exception e) {
			stt.error(e.getMessage());
			Toast.makeText(AppSetting.getAppContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	public void send(Message msg) {
		try
		{
			if (connected) serviceMessenger.send(msg);
			else {
				stt.error("Service not yet started.");
				Toast.makeText(AppSetting.getAppContext(), "Service not yet started.", Toast.LENGTH_LONG).show();
			}
		}
		catch (Exception e) {
			stt.error(e.getMessage());
			Toast.makeText(AppSetting.getAppContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	public boolean isConnected() {
		return connected;
	}
}
