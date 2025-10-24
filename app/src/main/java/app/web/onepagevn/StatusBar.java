package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.util.*;

public class StatusBar
{
	private static StatusBar lastSttBar;
	private static LogType lastType;
	private static String lastStt = "";
	private static LogType filterType;
	
	private Context context;
	private ImageView iconType;
	private HorizontalScrollView scrollBar;
	private TextView status;
	private boolean isInit = false;
	private String compName = "";
	private SQLiteDB db = new SQLiteDB();
	
	public StatusBar(String compName) {
		this.compName = compName;
	}
	
	public StatusBar(Context context)
	{
		this.context = context;
		if (context instanceof Application) {
			compName = "Setting";
		} else if (context instanceof Service) {
			compName = "Service";
		} else if (context instanceof Activity) {
			compName = ((Activity) context).getComponentName().getShortClassName().substring(1);
		}
	}
	
	public StatusBar init() {
		LinearLayout rootLayout = (LinearLayout) ((ViewGroup) ((Activity) context).findViewById(android.R.id.content)).getChildAt(0);
		init(rootLayout);
		return this;
	}

	public StatusBar init(LinearLayout parent)
	{
		LayoutInflater sttInflater = LayoutInflater.from(context);
		LinearLayout sttLayout = (LinearLayout) sttInflater.inflate(R.layout.status_bar, null);
		parent.addView(sttLayout);
		iconType = (ImageView) sttLayout.getChildAt(0);
		scrollBar = (HorizontalScrollView) sttLayout.getChildAt(1);
		status = (TextView) scrollBar.getChildAt(0);
		
		iconType.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					AlertDialog.Builder dlgType = new AlertDialog.Builder(context);
					dlgType.setTitle("Filter Type").setIcon(R.drawable.ico_question_circle);
					final List<Map<String, Object>> types = new ArrayList<>();
					Map<String, Object> allType = new HashMap<>();
					allType.put("message", "All");
					types.add(allType);
					for (LogType type : LogType.values()) {
						Map<String, Object> line = new HashMap<>();
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
								filterType = (LogType) types.get(index).get("type");
								List<Map<String, Object>> logs = db.getLogs(filterType);
								if (logs.size() == 0) clear();
								else {
									lastType = (LogType) logs.get(0).get("type");
									lastStt = (String) logs.get(0).get("message");
									update();
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
					if (!"AppLog".equals(compName)) {
						dlgStatus.setNeutralButton("Detail", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int index)
								{
									Intent appLog = new Intent(context, AppLog.class);
									context.startActivity(appLog);
								}
							});
					}
					
					final List<Map<String, Object>> logs = db.getLogs(filterType);
					ListAdapter sttAdapter = new StatusAdapter(context, logs);
					dlgStatus.setAdapter(sttAdapter, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int index)
							{
								ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
								ClipData data = ClipData.newPlainText("Status log item: " + index, (String) logs.get(index).get("message"));
								clipboard.setPrimaryClip(data);
							}
						});
					dlgStatus.create().show();
				}
			});
		isInit = true;
		lastSttBar = this;
		return this;
	}
	
	public void error(String msg) {
		add(msg, LogType.ERROR, compName);
	}

	public void success(String msg) {
		add(msg, LogType.SUCCESS, compName);
	}
	
	public void warning(String msg) {
		add(msg, LogType.WARNING, compName);
	}
	
	public void info(String msg) {
		add(msg, LogType.INFO, compName);
	}
	
	public void log(String msg) {
		add(msg, LogType.LOG, compName);
	}
	
	public void add(String msg, LogType type, String compName) {
		db.insertLog(msg, type, compName);
		if (filterType == null || filterType == type) {
			lastType = type;
			lastStt = msg;
			update();
		}
	}
	
	public void clear() {
		clear(false);
	}
	
	public void clear(boolean clearData) {
		if (clearData) db.clearLogs();
		lastType = filterType;
		lastStt = "";
		update();
	}
	
	public void exit() {
		lastType = null;
		lastStt = "";
		filterType = null;
	}
	
	public void update()
	{
		if (lastSttBar == null) return;
		if (isInit && !compName.equals(lastSttBar.compName)) lastSttBar = this;
		setIconType(lastType);
		String sttMessage = lastSttBar.status.getText().toString();
		if (!lastStt.equals(sttMessage)) {
			lastSttBar.status.setText(lastStt);
			lastSttBar.scrollBar.scrollTo(0, 0);
		}
	}
	
	private void setIconType(LogType type) {
		switch (type) {
			case ERROR:
				lastSttBar.iconType.setImageResource(R.drawable.ico_bug);
				break;
			case SUCCESS:
				lastSttBar.iconType.setImageResource(R.drawable.ico_check_circle);
				break;
			case WARNING:
				lastSttBar.iconType.setImageResource(R.drawable.ico_exclamation_triangle);
				break;
			case INFO:
				lastSttBar.iconType.setImageResource(R.drawable.ico_info_circle);
				break;
			case LOG:
				lastSttBar.iconType.setImageResource(R.drawable.ico_feather);
				break;
			default:
				lastSttBar.iconType.setImageResource(R.drawable.ico_clipboard_list_white);
		}
	}
}
