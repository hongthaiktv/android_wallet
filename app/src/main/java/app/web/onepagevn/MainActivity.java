package app.web.onepagevn;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.ActionMenuView.*;
import java.util.*;

import android.view.View.OnClickListener;
import java.text.*;

public class MainActivity extends Activity 
{
	TextView tvTable, tvStatus;
	Button btnAdmin;
	ImageButton ibFAB;
	ImageView ivIconStatus;
	String tblName;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		tvTable = findViewById(R.id.mainTvTable);
		tvStatus = findViewById(R.id.mainTvStatus);
		btnAdmin = findViewById(R.id.mainBtnAdmin);
		ibFAB = findViewById(R.id.mainIbFAB);
		ivIconStatus = findViewById(R.id.mainIvIconStatus);
		
		enum AppDimen {
			SM,
			MD,
			LG
		}
		
		int px = getResources().getDimensionPixelSize(R.dimen.dialogFontSM);
		Toast.makeText(this, px + "", Toast.LENGTH_SHORT).show();
		LinearLayout testLayout = findViewById(R.id.testLayout);
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.status_logs, testLayout, true);
		TextView testTV = view.findViewById(R.id.status_logsTvStatus);
		testTV.setText("new inf test with return obj");
		
		tvTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view)
			{
				final String[] tableNames = {"bills", "customers", "products"};
				AlertDialog.Builder dlgTable = new AlertDialog.Builder(MainActivity.this);
				dlgTable.setTitle("Select Table")
					.setIcon(R.drawable.ico_table)
					.setItems(tableNames, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface p1, int index)
						{
							tblName = tableNames[index];
							tvTable.setText(tblName);
							switch (tblName) {
								case "bills":
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_shopping_cart, 0, R.drawable.ico_caret_down, 0);
									break;
								case "customers":
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_address_book, 0, R.drawable.ico_caret_down, 0);
									break;
								case "products":
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_box, 0, R.drawable.ico_caret_down, 0);
									break;
								default:
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_database, 0, R.drawable.ico_caret_down, 0);
							}
						}
					});
				dlgTable.create().show();
			}
		});
		
		btnAdmin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view)
			{
				Intent intAdmin = new Intent(MainActivity.this, Admin.class);
				startActivity(intAdmin);
			}
		});
		
		ibFAB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view)
			{
				Intent intEditor = new Intent(MainActivity.this, Editor.class);
				startActivity(intEditor);
			}
		});
		
		ivIconStatus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View p1)
			{
				String[] sttTypes = {"Error", "Success", "Warning", "Info", "Log"};
				AlertDialog.Builder dlgType = new AlertDialog.Builder(MainActivity.this);
				dlgType.setTitle("Filter Type").setIcon(R.drawable.ico_question_circle);
				List<HashMap<String, String>> types = new ArrayList<HashMap<String, String>>();
				for (String type : sttTypes) {
					HashMap<String, String> line = new HashMap<String, String>();
					line.put("type", type);
					line.put("message", type);
					types.add(line);
				}
				ListAdapter typeAdapter = new StatusAdapter(MainActivity.this, types, DialogSize.LARGE);
				dlgType.setAdapter(typeAdapter, null);
				dlgType.create().show();
			}
		});
		
		tvStatus.setOnClickListener(new OnClickListener() {

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
				LinearLayout lo = new LinearLayout(MainActivity.this);
				lo.setOrientation(LinearLayout.VERTICAL);
				lo.setPadding(50, 50, 50, 50);
				LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				
				for (String log : logs) {
					TextView tv = new TextView(MainActivity.this);
					tv.setText(log);
					//tv.setLayoutParams(params);
					switch (log) {
						case "customerss":
							tv.setBackgroundColor(Color.RED);
							break;
					}
					
					lo.addView(tv);
				}
				TextView tvLast = new TextView(MainActivity.this);
				String lastLog = tvStatus.getText().toString();
				tvLast.setText(lastLog);
				lo.addView(tvLast);
				AlertDialog.Builder dlgStatus = new AlertDialog.Builder(MainActivity.this);
				dlgStatus.setTitle("Status Logs")
					.setIcon(R.drawable.ico_clipboard_list)
					.setPositiveButton("Ok", null)
					.setNeutralButton("Detail", null);
				List<HashMap<String, String>> stts = new ArrayList<HashMap<String, String>>();
				for (int i = 0; i < 35; i++) {
					HashMap<String, String> log = new HashMap<String, String>();
					switch (i) {
						case 0:
							log.put("type", "Warning");
							log.put("message", "wrn msg... dndb dhdb sjrj kdndn dndnnnd sndjdj djrjjrn kejrjdnn dbdbbdh djrjjr nzndn eiriir.......");
							break;
						case 1:
							log.put("type", "Success");
							log.put("message", "success msg...");
							break;
						case 2:
							log.put("type", "Error");
							log.put("message", "err msg...");
							break;
						case 3:
							log.put("type", "Info");
							log.put("message", "info msg...");
							break;
						default:
							log.put("type", "Log");
							log.put("message", "log msg...");
					}
					stts.add(log);
				}
				ListAdapter sttAdapter = new StatusAdapter(MainActivity.this, stts);
				dlgStatus.setAdapter(sttAdapter, null);
				dlgStatus.create().show();
			}
		});
    }
}

