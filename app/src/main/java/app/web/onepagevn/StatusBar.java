package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.util.*;
import android.graphics.*;

public class StatusBar
{
	private static StatusBar instance;
	
	private Context context;
	private String title;
	private ImageView iconType;
	private HorizontalScrollView scrollBar;
	private TextView status;
	private String message = "";
	private List<HashMap<String, Object>> logs;
	
	private StatusBar()
	{}

	public static StatusBar getInstance(Context context, LinearLayout parent)
	{
		if (instance == null) instance = new StatusBar();
		LayoutInflater sttInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
		sttInflater.inflate(R.layout.status_bar, parent);
		instance.update(context);
		
		instance.iconType.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					AlertDialog.Builder dlgType = new AlertDialog.Builder(instance.context);
					dlgType.setTitle("Filter Type").setIcon(R.drawable.ico_question_circle);
					List<HashMap<String, Object>> types = new ArrayList<HashMap<String, Object>>();
					for (LogType type : LogType.values()) {
						HashMap<String, Object> line = new HashMap<String, Object>();
						line.put("type", type);
						String message = type.toString().toLowerCase();
						line.put("message", message.substring(0, 1).toUpperCase() + message.substring(1));
						types.add(line);
					}
					ListAdapter typeAdapter = new StatusAdapter(instance.context, types, DialogSize.LARGE);
					dlgType.setAdapter(typeAdapter, null);
					dlgType.create().show();
				}
			});
			
		instance.status.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					String[] logs = {"abc ovivi ivivi ovho ogogov fifucu yxyxyxy blkbk ivivi ivivi", "bills", "customers", "products",
						"abc", "bills", "customerss", "products",
						"abc", "bills", "customers", "products",
						"abc", "bills", "customers", "products",
						"abc", "bills", "customers", "products",
						"abc", "bills", "customers", "products",
						"abc", "bills", "customers", "products",
						"abc", "bills", "customers", "products",
						"abc", "bills", "customers", "products",
						"abc", "bills", "customers", "products",
						"abc", "bills", "customers", "products",
						"abc", "bills", "customers", "products"
					};
					LinearLayout lo = new LinearLayout(instance.context);
					lo.setOrientation(LinearLayout.VERTICAL);
					lo.setPadding(50, 50, 50, 50);

					for (String log : logs) {
						TextView tv = new TextView(instance.context);
						tv.setText(log);
						//tv.setLayoutParams(params);
						switch (log) {
							case "customerss":
								tv.setBackgroundColor(Color.RED);
								break;
						}

						lo.addView(tv);
					}
					TextView tvLast = new TextView(instance.context);
					String lastLog = instance.status.getText().toString();
					tvLast.setText(lastLog);
					lo.addView(tvLast);
					
					AlertDialog.Builder dlgStatus = new AlertDialog.Builder(instance.context);
					dlgStatus.setTitle("Status Logs")
						.setIcon(R.drawable.ico_clipboard_list)
						.setPositiveButton("Ok", null);
					String logTitle = instance.context.getString(R.string.act_appLog);
					if (!instance.title.equals(logTitle)) {
						dlgStatus.setNeutralButton("Detail", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int index)
								{
									Intent appLog = new Intent(instance.context, AppLog.class);
									instance.context.startActivity(appLog);
								}
							});
					}
					List<HashMap<String, Object>> stts = new ArrayList<HashMap<String, Object>>();
					for (int i = 0; i < 20; i++) {
						HashMap<String, Object> log = new HashMap<String, Object>();
						switch (i) {
							case 0:
								log.put("type", LogType.WARNING);
								log.put("message", "wrn msg... dndb dhdb sjrj kdndn dndnnnd sndjdj djrjjrn kejrjdnn dbdbbdh djrjjr nzndn eiriir.......");
								log.put("timestamp", new Date("01 Jan 2026 20:30:40"));
								break;
							case 1:
								log.put("type", LogType.SUCCESS);
								log.put("message", "success msg...");
								log.put("timestamp", new Date("21 Mar 2025 00:50:40"));
								break;
							case 2:
								log.put("type", LogType.ERROR);
								log.put("message", "err msg...");
								log.put("timestamp", new Date("12 Mar 2025 23:55:40"));
								break;
							case 3:
								log.put("type", LogType.INFO);
								log.put("message", "info msg...");
								break;
							case 18:
								log.put("type", LogType.ERROR);
								log.put("message", "pass log record...");
								Date yesterday = new Date("07 Mar 2025 18:30:40");
								log.put("timestamp", yesterday);
								break;
							case 19:
								log.put("type", LogType.WARNING);
								log.put("message", "pass log record...");
								Date preDay = new Date("05 Mar 2024 19:50:10");
								log.put("timestamp", preDay);
								break;
							default:
								log.put("type", LogType.LOG);
								log.put("message", "log msg...");
						}
						if (log.get("timestamp") == null) {
							Date now = new Date();
							log.put("timestamp", now);
						}
						stts.add(log);
					}
					ListAdapter sttAdapter = new StatusAdapter(instance.context, stts);
					dlgStatus.setAdapter(sttAdapter, null);
					dlgStatus.create().show();
				}
			});
		
		return instance;
	}
	
	public void update(Context context)
	{
		this.context = context;
		LinearLayout statusBar = ((Activity) context).findViewById(R.id.statusLayout);
		title = ((Activity) context).getTitle().toString();
		iconType = (ImageView) statusBar.getChildAt(0);
		scrollBar = (HorizontalScrollView) statusBar.getChildAt(1);
		status = (TextView) instance.scrollBar.getChildAt(0);

		String sttMessage = status.getText().toString();
		if (!sttMessage.equals(message)) {
			status.setText(message);
			scrollBar.scrollTo(0, 0);
		}
	}

	public ImageView getIconType()
	{
		return iconType;
	}

	public HorizontalScrollView getScrollBar()
	{
		return scrollBar;
	}

	public TextView getStatus()
	{
		return status;
	}

	public void setMessage(String message)
	{
		this.message = message;
		this.status.setText(message);
		this.scrollBar.scrollTo(0, 0);
	}

	public String getMessage()
	{
		return message;
	}
}
