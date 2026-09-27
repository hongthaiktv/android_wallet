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
	Button btnTable, btnAdmin;
	ImageButton ibFAB, ibWallet;
	ImageView ivBGround;
	String tblName = "balance";
	private LinearLayout mainLayout;
	private StatusBar stt;
	private SQLiteDB db;
	private AppServiceConnection srv;
	private SQLiteDBLayout tblData;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		
		btnTable = findViewById(R.id.mainBtnTable);
		btnAdmin = findViewById(R.id.mainBtnAdmin);
		ibFAB = findViewById(R.id.mainIbFAB);
		ibWallet = findViewById(R.id.mainIbWallet);
		ivBGround = findViewById(R.id.mainIvBGround);
		mainLayout = findViewById(R.id.mainLayout);
		stt = new StatusBar(this).init();
		db = new SQLiteDB();
		srv = AppSetting.getAppContext().getServiceConnection();
		tblData = findViewById(R.id.mainTableData);
		
		final String sqlTblBalance = "SELECT expends.id, CASE WHEN strftime('%Y', expends.date, 'unixepoch', 'localtime') = strftime('%Y', 'now', 'localtime') THEN strftime('%d-%m %H:%M', expends.date, 'unixepoch', 'localtime') ELSE strftime('%d-%m-%Y %H:%M', expends.date, 'unixepoch', 'localtime') END AS date, suppliers.type || '. ' || suppliers.name AS supplier, products.name AS product, printf('%,.0f', expends.price * expends.quantity) AS total FROM expends INNER JOIN suppliers USING (supplierid) INNER JOIN products USING (productid) ORDER BY expends.date DESC";
		tblData.setSQL(sqlTblBalance);
		btnTable.setText(String.format(Locale.US, "%,.0f", getBalanceRemain()));
		db.getSetting();
		ibWallet.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_dollar_sign, 0, R.drawable.ico_caret_down, 0);
					ivBGround.setImageResource(R.drawable.bg_wallet_crypto);
					tblData.setSQL(sqlTblBalance).show();
					btnTable.setText(String.format(Locale.US, "%,.0f", getBalanceRemain()));
					tblName = "balance";
				}
			});
		
		btnTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view)
			{
				List<Map<String, Object>> listTable = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%' AND name!='logs' AND name!='setting' AND name!='cmdhistory' ORDER BY name COLLATE NOCASE");
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
							tblData.setSQL(null);
							switch (tblName) {
								case "budget":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_money_bill, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_budget);
									tblData.setSQL("SELECT id, CASE WHEN strftime('%Y', date, 'unixepoch', 'localtime') = strftime('%Y', 'now', 'localtime') THEN strftime('%d-%m %H:%M', date, 'unixepoch', 'localtime') ELSE strftime('%d-%m-%Y %H:%M', date, 'unixepoch', 'localtime') END AS date, printf('%,.0f', amount) AS amount FROM budget ORDER BY date");
									btnTable.setText(String.format(Locale.US, "%,.0f", getTotalBudget()));
									break;
								case "expends":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_shopping_cart, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_shopping_cart);
									tblData.setSQL("SELECT expends.id, CASE WHEN strftime('%Y', expends.date, 'unixepoch', 'localtime') = strftime('%Y', 'now', 'localtime') THEN strftime('%d-%m %H:%M', expends.date, 'unixepoch', 'localtime') ELSE strftime('%d-%m-%Y %H:%M', expends.date, 'unixepoch', 'localtime') END AS date, suppliers.type || '. ' || suppliers.name AS supplier, products.name AS product, units.name AS unit, printf('%,.0f', expends.price) AS price, expends.quantity, printf('%,.0f', expends.price * expends.quantity) AS total, ifnull(expends.note, '') AS note FROM expends INNER JOIN suppliers USING (supplierid) INNER JOIN products USING (productid) INNER JOIN units USING (unitid) ORDER BY expends.date DESC");
									btnTable.setText(String.format(Locale.US, "%,.0f", getTotalExpend()));
									break;
								case "product_types":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_database, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_database);
									tblData.setSQL("SELECT * FROM product_types ORDER BY name COLLATE NOCASE");
									btnTable.setText(tblName);
									break;
								case "products":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_box, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_product);
									tblData.setSQL("SELECT products.productid AS id, products.name, printf('%,.0f', products.price) AS price, units.name AS unit, product_types.name AS type FROM products INNER JOIN units USING (unitid) INNER JOIN product_types USING (ptypeid) ORDER BY products.name COLLATE NOCASE");
									btnTable.setText(tblName);
									break;
								case "suppliers":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_address_book, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_address_book);
									tblData.setSQL("SELECT * FROM suppliers ORDER BY type COLLATE NOCASE, name COLLATE NOCASE, address COLLATE NOCASE");
									btnTable.setText(tblName);
									break;
								case "units":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_database, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_database);
									tblData.setSQL("SELECT * FROM units ORDER BY name COLLATE NOCASE");
									btnTable.setText(tblName);
									break;
								default:
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_database, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_database);
									btnTable.setText(tblName);
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
				intAdmin.putExtra("tblName", tblName);
				startActivity(intAdmin);
			}
		});
		
		ibFAB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view)
			{
				Intent intEditor = new Intent(MainActivity.this, Editor.class);
				intEditor.putExtra("tblName", tblName);
				startActivity(intEditor);
			}
		});
    }
	
	private double getBalanceRemain() {
		double budget = getTotalBudget(),
		expend = getTotalExpend();
		double remain = budget - expend;
		return remain;
	}
	
	private double getTotalExpend() {
		double expend = 0;
		List<Map<String, Object>> totalExpends = db.sql("aggregate", "SELECT CAST(printf('%.0f', sum(price * quantity)) AS REAL) AS total FROM expends", null);
		if (totalExpends.size() > 0) {
			Map<String, Object> totalExpend = totalExpends.get(0);
			expend = totalExpend.get("total");
		}
		return expend;
	}
	
	private double getTotalBudget() {
		double budget = 0;
		List<Map<String, Object>> totalBudgets = db.sql("aggregate", "SELECT max(date) AS date, CAST(printf('%.0f', amount) AS REAL) AS amount FROM budget", null);
		if (totalBudgets.size() > 0) {
			Map<String, Object> totalBudget = totalBudgets.get(0);
			budget = totalBudget.get("amount");
		}
		return budget;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		tblData.show();
		switch (tblName) {
			case "budget":
				btnTable.setText(String.format(Locale.US, "%,.0f", getTotalBudget()));
				break;
			case "expends":
				btnTable.setText(String.format(Locale.US, "%,.0f", getTotalExpend()));
				break;
			case "balance":
				btnTable.setText(String.format(Locale.US, "%,.0f", getBalanceRemain()));
		}
		stt.update();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		stt.exit();
	}
}

