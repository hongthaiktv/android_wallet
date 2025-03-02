package app.web.onepagevn;
import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import android.util.*;
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
		ImageView imgStatus = view.findViewById(R.id.status_logsIvStatus);
		TextView tvTime = view.findViewById(R.id.status_logsTvTime);
		TextView tvStatus = view.findViewById(R.id.status_logsTvStatus);
		int iconSize, iconMarginH, cPaddingH, cPaddingV;
		float fontSize;
		
		HashMap<String, Object> log = getItem(position);
		String type = (String) log.get("type");
		switch (type) {
			case "Error":
				imgStatus.setImageResource(R.drawable.ico_bug);
				break;
			case "Success":
				imgStatus.setImageResource(R.drawable.ico_check_circle);
				break;
			case "Warning":
				imgStatus.setImageResource(R.drawable.ico_exclamation_triangle);
				break;
			case "Info":
				imgStatus.setImageResource(R.drawable.ico_info_circle);
				break;
			default:
				imgStatus.setImageResource(R.drawable.ico_feather);
		}
		
		switch (size) {
			case MEDIUM:
				fontSize = context.getResources().getDimension(R.dimen.dialogFontMD);
				iconSize = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMD);
				iconMarginH = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMHorizontalMD);
				cPaddingH = context.getResources().getDimensionPixelSize(R.dimen.dialogCPHorizontalMD);
				cPaddingV = context.getResources().getDimensionPixelSize(R.dimen.dialogCPVerticalMD);
				break;
			case LARGE:
				fontSize = context.getResources().getDimension(R.dimen.dialogFontLG);
				iconSize = context.getResources().getDimensionPixelSize(R.dimen.dialogIconLG);
				iconMarginH = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMHorizontalLG);
				cPaddingH = context.getResources().getDimensionPixelSize(R.dimen.dialogCPHorizontalLG);
				cPaddingV = context.getResources().getDimensionPixelSize(R.dimen.dialogCPVerticalLG);
				break;
			default:
				fontSize = context.getResources().getDimension(R.dimen.dialogFontSM);
				iconSize = context.getResources().getDimensionPixelSize(R.dimen.dialogIconSM);
				iconMarginH = context.getResources().getDimensionPixelSize(R.dimen.dialogIconMHorizontalSM);
				cPaddingH = context.getResources().getDimensionPixelSize(R.dimen.dialogCPHorizontalSM);
				cPaddingV = context.getResources().getDimensionPixelSize(R.dimen.dialogCPVerticalSM);
		}
		
		Date timeStamp = (Date) log.get("timestamp");
		if (timeStamp != null) {
			DateFormat vnDateTime = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.UK);
			String time = vnDateTime.format(timeStamp) + " :";
			tvTime.setText(time);
			tvTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
		}
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
		params.setMargins(iconMarginH, 0, iconMarginH, 0);
		imgStatus.setLayoutParams(params);
		String status = (String) log.get("message");
		tvStatus.setText(status);
		tvStatus.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
		container.setPadding(cPaddingH, cPaddingV, cPaddingH, cPaddingV);
		return view;
	}
}
