package app.web.onepagevn;

import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class StatusAdapter extends ArrayAdapter<Map<String, Object>> {
	private Context context;
	private DialogSize size = DialogSize.SMALL;
	private String estPrefix = "'(EST) '";
	private int maxDialogLogs;
	
	public StatusAdapter(Context context, List<Map<String, Object>> logs) {
		super(context, R.layout.status_log, logs);
		initial(context, null, null);
	}
	
	public StatusAdapter(Context context, List<Map<String, Object>> logs, DialogSize size) {
		super(context, R.layout.status_log, logs);
		initial(context, size, null);
	}
	
	public StatusAdapter(Context context, List<Map<String, Object>> logs, String estPrefix) {
		super(context, R.layout.status_log, logs);
		initial(context, null, estPrefix);
	}
	
	public StatusAdapter(Context context, List<Map<String, Object>> logs, DialogSize size, String estPrefix) {
		super(context, R.layout.status_log, logs);
		initial(context, size, estPrefix);
	}
	
	private void initial(Context context, DialogSize size, String estPrefix) {
		this.context = context;
		if (size != null) this.size = size;
		if (estPrefix != null) setEstPrefix(estPrefix);
		maxDialogLogs = AppSetting.getAppContext().getMaxDialogLogs();
	}
	
	public StatusAdapter setSize(DialogSize size)
	{
		this.size = size;
		return this;
	}
	
	public StatusAdapter setEstPrefix(String estPrefix)
	{
		this.estPrefix = estPrefix.equals("") ? "" : "'" + estPrefix + "'";
		return this;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.status_log, parent, false);
		LinearLayout container = view.findViewById(R.id.status_logLayoutContainer);
		ImageView iconType = view.findViewById(R.id.status_logIvType);
		TextView tvTime = view.findViewById(R.id.status_logTvTime);
		TextView tvLog = view.findViewById(R.id.status_logTvLog);
		int iconSize, iconMarginH, iconMarginTop, cPaddingH, cPaddingV, timeWidth;
		float fontSize;
		
		Map<String, Object> log = getItem(position);
		
		switch (size) {
			case MEDIUM:
				fontSize = context.getResources().getDimension(R.dimen.dialogFontMD);
				iconSize = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMD);
				iconMarginH = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMHorizontalMD);
				iconMarginTop = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMTopMD);
				cPaddingH = context.getResources().getDimensionPixelSize(R.dimen.dialogCPHorizontalMD);
				cPaddingV = context.getResources().getDimensionPixelSize(R.dimen.dialogCPVerticalMD);
				timeWidth = context.getResources().getDimensionPixelSize(R.dimen.dialogTimeWidthMD);
				break;
			case LARGE:
				fontSize = context.getResources().getDimension(R.dimen.dialogFontLG);
				iconSize = context.getResources().getDimensionPixelSize(R.dimen.dialogIconLG);
				iconMarginH = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMHorizontalLG);
				iconMarginTop = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMTopLG);
				cPaddingH = context.getResources().getDimensionPixelSize(R.dimen.dialogCPHorizontalLG);
				cPaddingV = context.getResources().getDimensionPixelSize(R.dimen.dialogCPVerticalLG);
				timeWidth = context.getResources().getDimensionPixelSize(R.dimen.dialogTimeWidthLG);
				break;
			default:
				fontSize = context.getResources().getDimension(R.dimen.dialogFontSM);
				iconSize = context.getResources().getDimensionPixelSize(R.dimen.dialogIconSM);
				iconMarginH = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMHorizontalSM);
				iconMarginTop = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMTopSM);
				cPaddingH = context.getResources().getDimensionPixelSize(R.dimen.dialogCPHorizontalSM);
				cPaddingV = context.getResources().getDimensionPixelSize(R.dimen.dialogCPVerticalSM);
				timeWidth = context.getResources().getDimensionPixelSize(R.dimen.dialogTimeWidthSM);
		}
		
		Date timeStamp = (Date) log.get("timestamp");
		if (timeStamp != null) {
			SimpleDateFormat formater = new SimpleDateFormat("MMM d, yyyy");
			Date now = new Date();
			Date dateNow = null, dateLog = null;
			try
			{
				dateNow = formater.parse(formater.format(now));
				dateLog = formater.parse(formater.format(timeStamp));
			}
			catch (ParseException e)
			{Toast.makeText(AppSetting.getAppContext(), e.getMessage(), Toast.LENGTH_LONG).show();}
			if (dateLog.equals(dateNow) && timeStamp.before(now)) {
				formater.applyPattern("HH:mm:ss");
			} else if (dateLog.before(dateNow) && dateLog.getYear() == dateNow.getYear()) {
				formater.applyPattern("dd MMM HH:mm");
			} else if (dateLog.before(dateNow)) {
				formater.applyPattern("dd MMM yyyy HH:mm");
			} else if (dateLog.equals(dateNow) && timeStamp.after(now)) {
				formater.applyPattern(estPrefix + "HH:mm:ss");
			} else if (dateLog.after(dateNow) && dateLog.getYear() == dateNow.getYear()) {
				formater.applyPattern(estPrefix + "dd MMM HH:mm");
			} else formater.applyPattern(estPrefix + "dd MMM yyyy HH:mm");
			String dateTime = formater.format(timeStamp) + " :";
			tvTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
			if (position < maxDialogLogs || position < 10) tvTime.setText(dateTime);
			else {
				tvTime.setText("");
				tvTime.setWidth(timeWidth);
			}
		}
		
		LogType type = (LogType) log.get("type");
		if (position < maxDialogLogs || position < 10) {
			switch (type) {
				case ERROR:
					iconType.setImageResource(R.drawable.ico_bug);
					break;
				case SUCCESS:
					iconType.setImageResource(R.drawable.ico_check_circle);
					break;
				case WARNING:
					iconType.setImageResource(R.drawable.ico_exclamation_triangle);
					break;
				case INFO:
					iconType.setImageResource(R.drawable.ico_info_circle);
					break;
				case LOG:
					iconType.setImageResource(R.drawable.ico_feather);
					break;
			}
		} else iconType.setImageResource(0);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
		params.setMargins(iconMarginH, iconMarginTop, iconMarginH, 0);
		iconType.setLayoutParams(params);
		
		String status = (String) log.get("message");
		tvLog.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
		if (position < maxDialogLogs || position < 10) tvLog.setText(status);
		else tvLog.setText(".........");
		
		container.setPadding(cPaddingH, cPaddingV, cPaddingH, cPaddingV);
		return view;
	}
}
