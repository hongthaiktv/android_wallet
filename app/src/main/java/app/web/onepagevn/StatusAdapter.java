package app.web.onepagevn;
import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class StatusAdapter extends ArrayAdapter<HashMap<String, String>> {
	private Context context;
	private DialogSize size;
	
	public StatusAdapter(Context context, List<HashMap<String, String>> logs) {
		super(context, R.layout.status_logs, logs);
		initial(context, DialogSize.SMALL);
	}
	
	public StatusAdapter(Context context, List<HashMap<String, String>> logs, DialogSize size) {
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
		
		HashMap<String, String> log = getItem(position);
		String type = log.get("type");
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
				int fontSize = context.getResources().getDimensionPixelSize(R.dimen.dialogFontMD);
				int iconSize = context.getResources().getDimensionPixelSize(R.dimen.iconWidth20);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
				params.setMargins(0, 0, 30, 0);
				imgStatus.setLayoutParams(params);
				container.setPadding(65, 20, 65, 20);
				break;
			case LARGE:
				int fontSize = context.getResources().getDimensionPixelSize(R.dimen.dialogFontLG);
				int iconSize = context.getResources().getDimensionPixelSize(R.dimen.iconWidth24);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
				params.setMargins(0, 0, 30, 0);
				imgStatus.setLayoutParams(params);
				container.setPadding(65, 20, 65, 20);
				break;
			default:
				int fontSize = context.getResources().getDimensionPixelSize(R.dimen.dialogFontSM);
				int iconSize = context.getResources().getDimensionPixelSize(R.dimen.iconWidth16);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
				params.setMargins(0, 0, 30, 0);
				imgStatus.setLayoutParams(params);
				container.setPadding(65, 20, 65, 20);
		}
		
		String status = log.get("message");
		tvStatus.setText(status);
		return view;
	}
}
