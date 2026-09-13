package app.web.onepagevn;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity 
{
	TextView tvTable;
	Button btnAdmin;
	ImageButton ibFAB;
	ImageView ivBGround;
	String tblName;
	private LinearLayout mainLayout;
	private StatusBar stt;
	private SQLiteDB db;
	private AppServiceConnection srv;
	private SQLiteDBLayout tblData;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		tvTable = findViewById(R.id.mainTvTable);
		btnAdmin = findViewById(R.id.mainBtnAdmin);
		ibFAB = findViewById(R.id.mainIbFAB);
		ivBGround = findViewById(R.id.mainIvBGround);
		mainLayout = findViewById(R.id.mainLayout);
		stt = new StatusBar(this).init();
		db = new SQLiteDB();
		srv = AppSetting.getAppContext().getServiceConnection();
		tblData = findViewById(R.id.mainTableData);
		
		tvTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view)
			{
				List<Map<String, Object>> listTable = db.getData("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%' AND name NOT LIKE 'logs' AND name NOT LIKE 'setting' AND name NOT LIKE 'cmdhistory' ORDER BY name ASC", null);
				ArrayList<String> tables = new ArrayList<>();
				for (Map<String, Object> table : listTable) {
					tables.add((String) table.get("name"));
				}
				final String[] tableNames = tables.toArray(new String[0]);
				AlertDialog.Builder dlgTable = new AlertDialog.Builder(MainActivity.this);
				dlgTable.setTitle("Select Table")
					.setIcon(R.drawable.ico_table)
					.setItems(tableNames, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int index)
						{
							tblName = tableNames[index];
							switch (tblName) {
								case "balance":
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_dollar_sign, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_wallet_crypto);
									tvTable.setText("99.500.000");
									break;
								case "bills":
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_shopping_cart, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_shopping_cart);
									tvTable.setText(tblName);
									break;
								case "suppliers":
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_address_book, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_address_book);
									tvTable.setText(tblName);
									break;
								case "products":
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_box, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_product);
									tvTable.setText(tblName);
									break;
								default:
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_database, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_database);
									tvTable.setText(tblName);
							}
							tblData.setTable(tblName).show();
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
    }

	@Override
	protected void onResume()
	{
		super.onResume();
		
		stt.update();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		stt.exit();
	}
}

