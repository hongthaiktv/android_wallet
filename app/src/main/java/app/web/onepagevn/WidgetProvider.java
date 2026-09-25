package app.web.onepagevn;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.util.*;

public class WidgetProvider extends AppWidgetProvider
{
	private String title = "";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		int flag = PendingIntent.FLAG_UPDATE_CURRENT;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			flag |= PendingIntent.FLAG_IMMUTABLE;
		}
		title = new Date().toLocaleString();
		RemoteViews widget = createWidget(context, flag);
		widget.setTextViewText(R.id.widgetTitle, title);
		appWidgetManager.updateAppWidget(appWidgetIds, widget);
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		title = new Date().toLocaleString();
		super.onReceive(context, intent);
	}
	
	private RemoteViews createWidget(Context context, int flag) {
		RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.widget);
		//fix for appWidgetIds not update all widgets id at onUpdate method
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
		ComponentName widgetComponent = new ComponentName(context, WidgetProvider.class);
		int[] widgetIDs = widgetManager.getAppWidgetIds(widgetComponent);
		
		Intent intBtnOpen = new Intent(context, WidgetPopup.class);
		intBtnOpen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendBtnOpen = PendingIntent.getActivity(context, 0, intBtnOpen, flag);
		widget.setOnClickPendingIntent(R.id.widgetBtnOpen, pendBtnOpen);

		Intent intBtnUpdate = new Intent(context, WidgetProvider.class);
		intBtnUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intBtnUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIDs);
		PendingIntent pendBtnUpdate = PendingIntent.getBroadcast(context, 0, intBtnUpdate, flag);
		widget.setOnClickPendingIntent(R.id.widgetBtnUpdate, pendBtnUpdate);
		return widget;
	}
}
