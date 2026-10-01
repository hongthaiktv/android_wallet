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
		
		final String sqlTblBalance = "SELECT expends.expendid, CASE WHEN strftime('%Y', expends.date, 'unixepoch', 'localtime') = strftime('%Y', 'now', 'localtime') THEN strftime('%d-%m %H:%M', expends.date, 'unixepoch', 'localtime') ELSE strftime('%d-%m-%Y %H:%M', expends.date, 'unixepoch', 'localtime') END AS date, suppliers.type || '. ' || suppliers.name AS supplier, products.name AS product, printf('%,.0f', expends.price * expends.quantity) AS total FROM expends INNER JOIN suppliers USING (supplierid) INNER JOIN products USING (productid) WHERE expends.date > (SELECT max(date) FROM funds) ORDER BY expends.date DESC";
		tblData.setSQL(sqlTblBalance);
		btnTable.setText(String.format(Locale.US, "%,.0f", db.getBalanceRemain()));
		db.getSetting();
		ibWallet.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					tblName = "balance";
					btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_dollar_sign, 0, R.drawable.ico_caret_down, 0);
					ivBGround.setImageResource(R.drawable.bg_wallet_crypto);
					tblData.setSQL(sqlTblBalance).show();
					btnTable.setText(String.format(Locale.US, "%,.0f", db.getCashRemain()));
				}
			});
		
		btnTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view)
			{
				List<Map<String, Object>> listTable = db.query("SELECT name FROM sqlite_master WHERE (type='table' OR type='view') AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%' AND name!='logs' AND name!='setting' AND name!='cmdhistory' AND name!='budgets_protected' ORDER BY name COLLATE NOCASE");
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
								case "budgets":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_store, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_budget);
									tblData.setSQL("SELECT budgetid, name, printf('%,.0f', balance) AS balance, note FROM budgets ORDER BY name COLLATE NOCASE");
									btnTable.setText(tblName);
									break;
								case "funds":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_money_bill, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_fund);
									tblData.setSQL("SELECT funds.fundid, CASE WHEN strftime('%Y', funds.date, 'unixepoch', 'localtime') = strftime('%Y', 'now', 'localtime') THEN strftime('%d-%m %H:%M', funds.date, 'unixepoch', 'localtime') ELSE strftime('%d-%m-%Y %H:%M', funds.date, 'unixepoch', 'localtime') END AS date, printf('%,.0f', funds.cash) AS cash, bank1.name AS bank1, printf('%,.0f', funds.balance1) AS balance1, bank2.name AS bank2, printf('%,.0f', funds.balance2) AS balance2, printf('%,.0f', funds.cash + funds.balance1 + funds.balance2) AS total, funds.note FROM funds INNER JOIN budgets AS bank1 ON funds.bank1=bank1.budgetid INNER JOIN budgets AS bank2 ON funds.bank2=bank2.budgetid ORDER BY funds.date");
									btnTable.setText(String.format(Locale.US, "%,.0f", db.getBalanceRemain()));
									break;
								case "expends":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_shopping_cart, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_shopping_cart);
									tblData.setSQL("SELECT expends.expendid, CASE WHEN strftime('%Y', expends.date, 'unixepoch', 'localtime') = strftime('%Y', 'now', 'localtime') THEN strftime('%d-%m %H:%M', expends.date, 'unixepoch', 'localtime') ELSE strftime('%d-%m-%Y %H:%M', expends.date, 'unixepoch', 'localtime') END AS date, suppliers.type || '. ' || suppliers.name AS supplier, products.name AS product, units.name AS unit, printf('%,.0f', expends.price) AS price, printf('%,.2f', expends.quantity) AS quantity, printf('%,.0f', expends.price * expends.quantity) AS total, budgets.name AS budget, ifnull(expends.note, '') AS note FROM expends INNER JOIN suppliers USING (supplierid) INNER JOIN products USING (productid) INNER JOIN units USING (unitid) INNER JOIN budgets USING (budgetid) ORDER BY expends.date DESC");
									btnTable.setText(String.format(Locale.US, "%,.0f", db.getTotalExpends()));
									break;
								case "product_types":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_table, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_grocery_list);
									tblData.setSQL("SELECT * FROM product_types ORDER BY name COLLATE NOCASE");
									btnTable.setText(tblName);
									break;
								case "products":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_box, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_product);
									tblData.setSQL("SELECT products.productid, products.name, printf('%,.0f', products.price) AS price, units.name AS unit, product_types.name AS type FROM products INNER JOIN units USING (unitid) INNER JOIN product_types USING (ptypeid) ORDER BY products.name COLLATE NOCASE");
									btnTable.setText(tblName);
									break;
								case "suppliers":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_address_book, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_address_book);
									tblData.setSQL("SELECT * FROM suppliers ORDER BY type COLLATE NOCASE, name COLLATE NOCASE, address COLLATE NOCASE");
									btnTable.setText(tblName);
									break;
								case "units":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_chart_bar, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_measurement);
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

	@Override
	protected void onResume()
	{
		super.onResume();
		tblData.show();
		switch (tblName) {
			case "funds":
				btnTable.setText(String.format(Locale.US, "%,.0f", db.getBalanceRemain()));
				break;
			case "expends":
				btnTable.setText(String.format(Locale.US, "%,.0f", db.getTotalExpends()));
				break;
			case "balance":
				btnTable.setText(String.format(Locale.US, "%,.0f", db.getCashRemain()));
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

