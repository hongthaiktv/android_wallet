package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.util.*;

public class StatusBar
{
	private static StatusBar instance;
	
	private Context context;
	private ImageView iconType;
	private HorizontalScrollView scrollBar;
	private TextView status;
	private List<HashMap<String, Object>> logs = new ArrayList<HashMap<String, Object>>();
	private String actName = "";
	private int iconTypeID = R.drawable.ico_clipboard_list;
	private boolean isInit = false;
	private LogType filterType = null;
	
	private StatusBar()
	{}
	
	public static StatusBar getInstance()
	{
		if (instance == null) instance = new StatusBar();
		return instance;
	}
	
	public static StatusBar initial(Context context)
	{
		LinearLayout rootLayout = (LinearLayout) ((ViewGroup) ((Activity) context).findViewById(android.R.id.content)).getChildAt(0);
		return getInstance().init(context, rootLayout);
	}
	
	public static StatusBar initial(Context context, LinearLayout parent)
	{
		return getInstance().init(context, parent);
	}

	private StatusBar init(final Context context, LinearLayout parent)
	{
		isInit = true;
		LayoutInflater sttInflater = LayoutInflater.from(context);
		sttInflater.inflate(R.layout.status_bar, parent);
		update(context);
		
		iconType.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					AlertDialog.Builder dlgType = new AlertDialog.Builder(context);
					dlgType.setTitle("Filter Type").setIcon(R.drawable.ico_question_circle);
					final List<HashMap<String, Object>> types = new ArrayList<HashMap<String, Object>>();
					HashMap<String, Object> allType = new HashMap<String, Object>();
					allType.put("message", "All");
					types.add(allType);
					for (LogType type : LogType.values()) {
						HashMap<String, Object> line = new HashMap<String, Object>();
						line.put("type", type);
						String message = type.toString().toLowerCase();
						line.put("message", message.substring(0, 1).toUpperCase() + message.substring(1));
						types.add(line);
					}
					ListAdapter typeAdapter = new StatusAdapter(context, types, DialogSize.LARGE);
					dlgType.setAdapter(typeAdapter, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int index)
							{
								LogType type = (LogType) types.get(index).get("type");
								setIconType(type);
								switch (type) {
									case ERROR:
										filterType = LogType.ERROR;
										break;
									case SUCCESS:
										filterType = LogType.SUCCESS;
										break;
									case WARNING:
										filterType = LogType.WARNING;
										break;
									case INFO:
										filterType = LogType.INFO;
										break;
									case LOG:
										filterType = LogType.LOG;
										break;
									default:
										filterType = null;
								}
							}
						});
					dlgType.create().show();
				}
			});
			
		status.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					AlertDialog.Builder dlgStatus = new AlertDialog.Builder(context);
					dlgStatus.setTitle("Status Logs")
						.setIcon(R.drawable.ico_clipboard_list)
						.setPositiveButton("Ok", null);
					if (!".AppLog".equals(actName)) {
						dlgStatus.setNeutralButton("Detail", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int index)
								{
									Intent appLog = new Intent(context, AppLog.class);
									context.startActivity(appLog);
								}
							});
					}
					
					ListAdapter sttAdapter = new StatusAdapter(context, logs);
					dlgStatus.setAdapter(sttAdapter, null);
					dlgStatus.create().show();
				}
			});
		
		return this;
	}
	
	public void log(String msg) {
		add(msg, LogType.LOG, null);
	}
	
	public void info(String msg) {
		add(msg, LogType.INFO, null);
	}
	
	public void warning(String msg) {
		add(msg, LogType.WARNING, null);
	}
	
	public void error(String msg) {
		add(msg, LogType.ERROR, null);
	}
	
	public void success(String msg) {
		add(msg, LogType.SUCCESS, null);
	}
	
	public void add(String msg, LogType type, Date date) {
		HashMap<String, Object> log = new HashMap<String, Object>();
		if (date == null) date = new Date();
		if (type == null) type = LogType.LOG;
		log.put("timestamp", date);
		log.put("type", type);
		log.put("message", msg);
		logs.add(0, log);
		update(context);
	}
	
	public void clear() {
		logs.clear();
		setIconType(LogType.LOG);
		status.setText("");
	}
	
	public void update(Context context)
	{
		if (context == null) return;
		this.context = context;
		
		LinearLayout statusBar = ((Activity) context).findViewById(R.id.statusLayout);
		if (statusBar == null) return;
		
		String actName = ((Activity) context).getComponentName().getShortClassName();
		if (!this.actName.equals(actName) || isInit) {
			isInit = false;
			this.actName = actName;
			iconType = (ImageView) statusBar.getChildAt(0);
			scrollBar = (HorizontalScrollView) statusBar.getChildAt(1);
			status = (TextView) scrollBar.getChildAt(0);
		}
		
		if (logs.size() > 0) {
			HashMap<String, Object> log = logs.get(0);
			LogType type = (LogType) log.get("type");
			setIconType(type);
			String sttMessage = status.getText().toString();
			String lastLog = (String) log.get("message");
			if (!sttMessage.equals(lastLog)) {
				status.setText(lastLog);
				scrollBar.scrollTo(0, 0);
			}
		} else status.setText("");
	}
	
	private void setIconType(LogType type) {
		int iconID;
		
		switch (type) {
			case ERROR:
				iconID = R.drawable.ico_bug;
				if (iconTypeID != iconID) {
					iconType.setImageResource(iconID);
					iconTypeID = iconID;
				}
				break;
			case SUCCESS:
				iconID = R.drawable.ico_check_circle;
				if (iconTypeID != iconID) {
					iconType.setImageResource(iconID);
					iconTypeID = iconID;
				}
				break;
			case WARNING:
				iconID = R.drawable.ico_exclamation_triangle;
				if (iconTypeID != iconID) {
					iconType.setImageResource(iconID);
					iconTypeID = iconID;
				}
				break;
			case INFO:
				iconID = R.drawable.ico_info_circle;
				if (iconTypeID != iconID) {
					iconType.setImageResource(iconID);
					iconTypeID = iconID;
				}
				break;
			case LOG:
				iconID = R.drawable.ico_feather;
				if (iconTypeID != iconID) {
					iconType.setImageResource(iconID);
					iconTypeID = iconID;
				}
				break;
			default:
				iconID = R.drawable.ico_clipboard_list;
				if (iconTypeID != iconID) {
					iconType.setImageResource(iconID);
					iconTypeID = iconID;
				}
		}
	}
}
