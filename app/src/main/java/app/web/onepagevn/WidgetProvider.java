package app.web.onepagevn;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.util.*;

public class WidgetProvider extends AppWidgetProvider
{
	private Context context;
	private AppWidgetManager widgetManager;
	private RemoteViews widget;
	private int[] widgetIDs;
	private static WidgetProvider instance;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		this.context = context;
		this.widgetManager = appWidgetManager;
		this.widgetIDs = appWidgetIds;
		instance = this;
		
		int flag = PendingIntent.FLAG_UPDATE_CURRENT;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			flag |= PendingIntent.FLAG_IMMUTABLE;
		}
		
		Intent intBtnOpen = new Intent(context, MainActivity.class);
		PendingIntent pendBtnOpen = PendingIntent.getActivity(context, 0, intBtnOpen, flag);
		Intent intBtnUpdate = new Intent(context, WidgetProvider.class);
		intBtnUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intBtnUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIDs);
		PendingIntent pendBtnUpdate = PendingIntent.getBroadcast(context, 0, intBtnUpdate, flag);
		widget = new RemoteViews(context.getPackageName(), R.layout.widget);
		widget.setOnClickPendingIntent(R.id.widgetBtnOpen, pendBtnOpen);
		widget.setOnClickPendingIntent(R.id.widgetBtnUpdate, pendBtnUpdate);
		widgetManager.updateAppWidget(widgetIDs, widget);
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);
		
		widget.setTextViewText(R.id.widgetTitle, new Date().toLocaleString());
		widgetManager.updateAppWidget(widgetIDs, widget);
		Toast.makeText(context, "updating...", Toast.LENGTH_SHORT).show();
	}
	
	public static WidgetProvider getInstance() {
		return instance;
	}
	
	public void update() {
		Toast.makeText(context, "wg msg", Toast.LENGTH_SHORT).show();
	}
}
