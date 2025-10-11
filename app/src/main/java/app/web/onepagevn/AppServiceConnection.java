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
		if (callBack != null) callBack.run();
		stt.setMessage("Service started.");
	}

	@Override
	public void onServiceDisconnected(ComponentName comp)
	{
		connected = false;
		serviceMessenger = null;
		stt.setMessage("Service disconnected.");
		Toast.makeText(context, "Service disconnected.", Toast.LENGTH_LONG).show();
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
		} catch (Exception e)
		{Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();}
	}
	
	public void send(Message msg) {
		try
		{
			if (connected) serviceMessenger.send(msg);
			else Toast.makeText(context, "Service not yet started.", Toast.LENGTH_LONG).show();
		}
		catch (Exception e)
		{Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();}
	}
	
	public boolean isConnected() {
		return connected;
	}
}
