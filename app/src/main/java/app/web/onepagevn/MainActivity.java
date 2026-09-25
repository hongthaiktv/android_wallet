package app.web.onepagevn;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.util.*;
import java.text.*;

public class MainActivity extends Activity 
{
	TextView tvTable;
	Button btnAdmin;
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
		
		tvTable = findViewById(R.id.mainTvTable);
		btnAdmin = findViewById(R.id.mainBtnAdmin);
		ibFAB = findViewById(R.id.mainIbFAB);
		ibWallet = findViewById(R.id.mainIbWallet);
		ivBGround = findViewById(R.id.mainIvBGround);
		mainLayout = findViewById(R.id.mainLayout);
		stt = new StatusBar(this).init();
		db = new SQLiteDB();
		srv = AppSetting.getAppContext().getServiceConnection();
		tblData = findViewById(R.id.mainTableData);
		
		tvTable.setText(getBalanceRemain());
		final String sqlTblBalance = "SELECT expends.id, CASE WHEN strftime('%Y', expends.date, 'unixepoch', 'localtime') = strftime('%Y', 'now', 'localtime') THEN strftime('%d-%m %H:%M', expends.date, 'unixepoch', 'localtime') ELSE strftime('%d-%m-%Y %H:%M', expends.date, 'unixepoch', 'localtime') END AS date, suppliers.type || '. ' || suppliers.name AS supplier, products.name AS product, PRINTF('%,.0f', expends.price * expends.quantity) AS total FROM expends INNER JOIN suppliers USING (supplierid) INNER JOIN products USING (productid) ORDER BY expends.date DESC";
		tblData.setSQL(sqlTblBalance);
		
		ibWallet.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_dollar_sign, 0, R.drawable.ico_caret_down, 0);
					ivBGround.setImageResource(R.drawable.bg_wallet_crypto);
					tblData.setSQL(sqlTblBalance).show();
					tvTable.setText(getBalanceRemain());
					tblName = "balance";
				}
			});
		
		tvTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view)
			{
				List<Map<String, Object>> listTable = db.sql("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%' AND name!='logs' AND name!='setting' AND name!='cmdhistory' ORDER BY name COLLATE NOCASE", null);
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
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_money_bill, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_budget);
									tblData.setSQL("SELECT id, CASE WHEN strftime('%Y', date, 'unixepoch', 'localtime') = strftime('%Y', 'now', 'localtime') THEN strftime('%d-%m %H:%M', date, 'unixepoch', 'localtime') ELSE strftime('%d-%m-%Y %H:%M', date, 'unixepoch', 'localtime') END AS date, PRINTF('%,.0f', amount) AS amount FROM budget ORDER BY date");
									tvTable.setText(getTotalBudget());
									break;
								case "expends":
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_shopping_cart, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_shopping_cart);
									tblData.setSQL("SELECT expends.id, CASE WHEN strftime('%Y', expends.date, 'unixepoch', 'localtime') = strftime('%Y', 'now', 'localtime') THEN strftime('%d-%m %H:%M', expends.date, 'unixepoch', 'localtime') ELSE strftime('%d-%m-%Y %H:%M', expends.date, 'unixepoch', 'localtime') END AS date, suppliers.type || '. ' || suppliers.name AS supplier, products.name AS product, units.name AS unit, PRINTF('%,.0f', expends.price) AS price, expends.quantity, PRINTF('%,.0f', expends.price * expends.quantity) AS total, IFNULL(expends.note, '') AS note FROM expends INNER JOIN suppliers USING (supplierid) INNER JOIN products USING (productid) INNER JOIN units USING (unitid) ORDER BY expends.date DESC");
									tvTable.setText(getTotalExpend());
									break;
								case "product_types":
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_database, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_database);
									tblData.setSQL("SELECT * FROM product_types ORDER BY name COLLATE NOCASE");
									tvTable.setText(tblName);
									break;
								case "products":
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_box, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_product);
									tblData.setSQL("SELECT products.productid AS id, products.name, PRINTF('%,.0f', products.price) AS price, units.name AS unit, product_types.name AS type FROM products INNER JOIN units USING (unitid) INNER JOIN product_types USING (ptypeid) ORDER BY products.name COLLATE NOCASE");
									tvTable.setText(tblName);
									break;
								case "suppliers":
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_address_book, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_address_book);
									tblData.setSQL("SELECT * FROM suppliers ORDER BY type COLLATE NOCASE, name COLLATE NOCASE, address COLLATE NOCASE");
									tvTable.setText(tblName);
									break;
								case "units":
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_database, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_database);
									tblData.setSQL("SELECT * FROM units ORDER BY name COLLATE NOCASE");
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
				intEditor.putExtra("tblName", tblName);
				startActivity(intEditor);
			}
		});
    }
	
	private String getBalanceRemain() {
		List<Map<String, Object>> budgets = db.sql("SELECT date(max(date), 'unixepoch', 'localtime') AS date, PRINTF('%.0f', amount) AS amount FROM budget", null);
		Map<String, Object> budget = budgets.get(0);
		double amount = Double.valueOf(budget.get("amount").toString());
		List<Map<String, Object>> totalExpends = db.sql("SELECT PRINTF('%.0f', sum(price * quantity)) AS total FROM expends", null);
		Map<String, Object> totalExpend = totalExpends.get(0);
		double expend = Double.valueOf(totalExpend.get("total").toString());
		double remain = amount - expend;
		DecimalFormat numFormatter = new DecimalFormat("#,###");
		return numFormatter.format(remain);
	}
	
	private String getTotalExpend() {
		List<Map<String, Object>> totalExpends = db.sql("SELECT PRINTF('%,.0f', sum(price * quantity)) AS total FROM expends", null);
		Map<String, Object> totalExpend = totalExpends.get(0);
		return totalExpend.get("total").toString();
	}
	
	private String getTotalBudget() {
		List<Map<String, Object>> budgets = db.sql("SELECT date(max(date), 'unixepoch', 'localtime') AS date, PRINTF('%,.0f', amount) AS amount FROM budget", null);
		Map<String, Object> budget = budgets.get(0);
		return budget.get("amount").toString();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		tblData.show();
		switch (tblName) {
			case "budget":
				tvTable.setText(getTotalBudget());
				break;
			case "expends":
				tvTable.setText(getTotalExpend());
				break;
			case "balance":
				tvTable.setText(getBalanceRemain());
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

