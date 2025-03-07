package app.web.onepagevn;

import android.content.*;
import android.view.*;
import android.widget.*;
import android.util.*;
import java.util.*;
import java.text.*;

public class StatusAdapter extends ArrayAdapter<HashMap<String, Object>> {
	private Context context;
	private DialogSize size;
	
	public StatusAdapter(Context context, List<HashMap<String, Object>> logs) {
		super(context, R.layout.status_logs, logs);
		initial(context, DialogSize.SMALL);
	}
	
	public StatusAdapter(Context context, List<HashMap<String, Object>> logs, DialogSize size) {
		super(context, R.layout.status_logs, logs);
		initial(context, size);
	}
	
	private void initial(Context context, DialogSize size) {
		this.context = context;
		this.size = size;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.status_logs, parent, false);
		LinearLayout container = view.findViewById(R.id.status_logsLayoutContainer);
		ImageView imgType = view.findViewById(R.id.status_logsIvType);
		TextView tvTime = view.findViewById(R.id.status_logsTvTime);
		TextView tvStatus = view.findViewById(R.id.status_logsTvStatus);
		int iconSize, iconMarginH, iconMarginTop, cPaddingH, cPaddingV;
		float fontSize;
		
		HashMap<String, Object> log = getItem(position);
		
		switch (size) {
			case MEDIUM:
				fontSize = context.getResources().getDimension(R.dimen.dialogFontMD);
				iconSize = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMD);
				iconMarginH = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMHorizontalMD);
				iconMarginTop = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMTopMD);
				cPaddingH = context.getResources().getDimensionPixelSize(R.dimen.dialogCPHorizontalMD);
				cPaddingV = context.getResources().getDimensionPixelSize(R.dimen.dialogCPVerticalMD);
				break;
			case LARGE:
				fontSize = context.getResources().getDimension(R.dimen.dialogFontLG);
				iconSize = context.getResources().getDimensionPixelSize(R.dimen.dialogIconLG);
				iconMarginH = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMHorizontalLG);
				iconMarginTop = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMTopLG);
				cPaddingH = context.getResources().getDimensionPixelSize(R.dimen.dialogCPHorizontalLG);
				cPaddingV = context.getResources().getDimensionPixelSize(R.dimen.dialogCPVerticalLG);
				break;
			default:
				fontSize = context.getResources().getDimension(R.dimen.dialogFontSM);
				iconSize = context.getResources().getDimensionPixelSize(R.dimen.dialogIconSM);
				iconMarginH = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMHorizontalSM);
				iconMarginTop = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMTopSM);
				cPaddingH = context.getResources().getDimensionPixelSize(R.dimen.dialogCPHorizontalSM);
				cPaddingV = context.getResources().getDimensionPixelSize(R.dimen.dialogCPVerticalSM);
		}
		
		Date timeStamp = (Date) log.get("timestamp");
		if (timeStamp != null) {
			DateFormat formater = DateFormat.getDateInstance(DateFormat.MEDIUM);
			Date dateNow = null, dateLog = null;
			try
			{
				dateNow = formater.parse(formater.format(new Date()));
				dateLog = formater.parse(formater.format(timeStamp));
			}
			catch (ParseException e)
			{Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();}
			SimpleDateFormat sFormater = new SimpleDateFormat("HH:mm:ss");
			if (dateLog.after(dateNow) && dateLog.getYear() > dateNow.getYear()) {
				sFormater.applyPattern("'EST:' dd MMM yyyy HH:mm");
			} else if (dateLog.after(dateNow)) {
				sFormater.applyPattern("'EST:' dd MMM HH:mm");
			} else if (dateLog.before(dateNow) && dateLog.getYear() < dateNow.getYear()) {
				sFormater.applyPattern("dd MMM yyyy HH:mm");
			} else if (dateLog.before(dateNow)) {
				sFormater.applyPattern("dd MMM HH:mm");
			}
			String dateTime = sFormater.format(timeStamp) + " :";
			tvTime.setText(dateTime);
			tvTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
		}
		
		String type = (String) log.get("type");
		switch (type) {
			case "Error":
				imgType.setImageResource(R.drawable.ico_bug);
				break;
			case "Success":
				imgType.setImageResource(R.drawable.ico_check_circle);
				break;
			case "Warning":
				imgType.setImageResource(R.drawable.ico_exclamation_triangle);
				break;
			case "Info":
				imgType.setImageResource(R.drawable.ico_info_circle);
				break;
			default:
				imgType.setImageResource(R.drawable.ico_feather);
		}
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
		params.setMargins(iconMarginH, iconMarginTop, iconMarginH, 0);
		imgType.setLayoutParams(params);
		
		String status = (String) log.get("message");
		tvStatus.setText(status);
		tvStatus.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
		
		container.setPadding(cPaddingH, cPaddingV, cPaddingH, cPaddingV);
		return view;
	}
}
