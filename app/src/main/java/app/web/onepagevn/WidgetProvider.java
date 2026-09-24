package app.web.onepagevn;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.widget.*;

public class WidgetProvider extends AppWidgetProvider
{
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		for (int i = 0; i < appWidgetIds.length; i++) {
			int widgetID = appWidgetIds[i];
			Intent intWidget = new Intent(context, MainActivity.class);
            int flag = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flag |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intWidget, flag);
			RemoteViews widgets = new RemoteViews(context.getPackageName(), R.layout.widget);
			widgets.setOnClickPendingIntent(R.id.widgetBtnOpen, pendingIntent);
			appWidgetManager.updateAppWidget(widgetID, widgets);
		}
	}
	
}
