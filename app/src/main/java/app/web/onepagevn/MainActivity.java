package app.web.onepagevn;

import android.content.*;
import android.graphics.*;
import android.os.*;
import android.support.v7.app.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.TableRow.*;
import java.util.*;

import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity
{
	private AppSetting app;
	private Button btnTable, btnAdmin;
	private ImageButton ibFAB, ibWallet;
	private ImageView ivBGround;
	private String tblName = "balance";
	private int todosLimit = -1;
	private LinearLayout mainLayout;
	private StatusBar stt;
	private SQLiteDB db;
	private AppServiceConnection srv;
	private SQLiteDBLayout layoutExpends, layoutTodos;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		app = AppSetting.getAppContext();
		btnTable = findViewById(R.id.mainBtnTable);
		btnAdmin = findViewById(R.id.mainBtnAdmin);
		ibFAB = findViewById(R.id.mainIbFAB);
		ibWallet = findViewById(R.id.mainIbWallet);
		ivBGround = findViewById(R.id.mainIvBGround);
		mainLayout = findViewById(R.id.mainLayout);
		stt = new StatusBar(this).init();
		db = new SQLiteDB();
		srv = AppSetting.getAppContext().getServiceConnection();
		layoutExpends = findViewById(R.id.mainTableData);
		layoutTodos = findViewById(R.id.mainTableTodos);
		
		final String sqlTblBalance = "SELECT expends.expendid, expends.date, suppliers.type || '. ' || suppliers.name AS supplier, products.name AS product, CAST(expends.price * expends.quantity AS REAL) AS total FROM expends INNER JOIN suppliers USING (supplierid) INNER JOIN products USING (productid) WHERE expends.date > (SELECT max(date) FROM funds) ORDER BY expends.date DESC";
		layoutExpends.setSQL(sqlTblBalance);
		
		layoutTodos.setOnCreateView(new SQLiteDBLayout.OnCreateView() {

				@Override
				public View onCreate(ViewParent parent, View view, int rowIndex, int colIndex, Object data, DataType type, final int rowID)
				{
					switch (rowIndex) {
						case -1:
							TypedValue selectableBackGround = new TypedValue();
							getTheme().resolveAttribute(android.R.attr.selectableItemBackground, selectableBackGround, true);
							
							RelativeLayout layoutTitle = new RelativeLayout(MainActivity.this);
							LinearLayout.LayoutParams layoutTitleParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
							layoutTitle.setLayoutParams(layoutTitleParams);
							
							RelativeLayout.LayoutParams tvTitleParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
							tvTitleParams.addRule(RelativeLayout.CENTER_IN_PARENT);
							TextView tvTitle = (TextView) view;
							tvTitle.setLayoutParams(tvTitleParams);
							tvTitle.setGravity(Gravity.CENTER);
							tvTitle.setTextColor(Color.WHITE);
							tvTitle.setTypeface(null, Typeface.BOLD);
							tvTitle.setId(View.generateViewId());
							
							ImageButton ibAdd = new ImageButton(MainActivity.this);
							RelativeLayout.LayoutParams ibAddParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
							ibAddParams.addRule(RelativeLayout.CENTER_IN_PARENT);
							ibAddParams.addRule(RelativeLayout.RIGHT_OF, tvTitle.getId());
							ibAdd.setLayoutParams(ibAddParams);
							ibAdd.setImageResource(R.drawable.ic_edit);
							ibAdd.setScaleType(ImageView.ScaleType.FIT_CENTER);
							ibAdd.setBackgroundResource(selectableBackGround.resourceId);
							ibAdd.setColorFilter(getResources().getColor(R.color.yellow));
							ibAdd.setPadding(15, 15, 15, 15);
							ibAdd.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View p1)
									{
										Intent intEditor = new Intent(MainActivity.this, Editor.class);
										intEditor.putExtra("tblName", layoutTodos.getTableName());
										startActivity(intEditor);
									}
								});
							
							layoutTitle.addView(tvTitle);
							layoutTitle.addView(ibAdd);
							return layoutTitle;
						case 0:
							return view;
					}
					switch (colIndex) {
						case 1:
						{
							ImageView icon = new ImageView(MainActivity.this);
							icon.setPadding(40, 15, 25, 15);
							int priority = data;
							switch (priority) {
								case 1:
									icon.setImageResource(R.drawable.ic_exclamation_circle);
									icon.setColorFilter(Color.RED);
									break;
								case 2:
									icon.setImageResource(R.drawable.ic_bell_alt);
									icon.setColorFilter(getResources().getColor(R.color.orange));
									break;
								case 3:
									icon.setImageResource(R.drawable.ic_flag);
									icon.setColorFilter(getResources().getColor(R.color.darkCyan));
									break;
								case 4:
									icon.setImageResource(R.drawable.ic_bookmark);
									icon.setColorFilter(getResources().getColor(R.color.darkGreen));
									break;
								case 5:
									icon.setImageResource(R.drawable.ic_alarm_snooze);
									icon.setColorFilter(getResources().getColor(R.color.purple));
									break;
							}
							return icon;
						}
						
						case 2:
							TableRow.LayoutParams colLayoutLG = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 4);
							TextView tvJob = (TextView) view;
							tvJob.setLayoutParams(colLayoutLG);
							tvJob.setGravity(Gravity.LEFT);
							return tvJob;
						
						case 3:
							CheckBox checkBox = (CheckBox) view;
							checkBox.setClickable(true);
							boolean isChecked = checkBox.isChecked();
							TableRow row = (TableRow) parent;
							final TextView tvStrikeThrough = (TextView) row.getChildAt(colIndex);
							if (isChecked) tvStrikeThrough.setPaintFlags(tvStrikeThrough.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
							
							checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

									@Override
									public void onCheckedChanged(CompoundButton v, boolean checked)
									{
										String table = layoutTodos.getTableName();
										ContentValues data = new ContentValues();
										if (checked) {
											tvStrikeThrough.setPaintFlags(tvStrikeThrough.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
											data.put("completedate", System.currentTimeMillis() / 1000L);
										}
										else {
											tvStrikeThrough.setPaintFlags(tvStrikeThrough.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
											List<Map<String, Object>> createDates = db.sql(table, "SELECT createdate FROM " + table + " WHERE rowid=?", new String[] {String.valueOf(rowID)});
											long createdate = ((Date) createDates.get(0).get("createdate")).getTime() / 1000L;
											data.put("completedate", createdate);
										}
										data.put("complete", checked);
										int result = db.update(table, rowID, data);
										if (result == -1) stt.error("Set todo job complete status failed.");
										layoutTodos.show();
									}
								});
							return checkBox;
						
						default: return view;
					}
				}
			});
			
		layoutTodos.setOnRowClickListener(new SQLiteDBLayout.OnRowClickListener() {

				@Override
				public void onClick(View row, int index, int id)
				{
					row.setBackgroundColor(Color.GRAY);
					Intent intEditor = new Intent(MainActivity.this, Editor.class);
					intEditor.putExtra("tblName", layoutTodos.getTableName());
					intEditor.putExtra("rowIndex", index);
					intEditor.putExtra("rowID", id);
					intEditor.setAction(Intent.ACTION_EDIT);
					startActivity(intEditor);
				}
			});
		
		layoutExpends.setOnRowClickListener(new SQLiteDBLayout.OnRowClickListener() {

				@Override
				public void onClick(View row, int index, int id)
				{
					row.setBackgroundColor(Color.GRAY);
					Intent intEditor = new Intent(MainActivity.this, Editor.class);
					intEditor.putExtra("tblName", layoutExpends.getTableName());
					intEditor.putExtra("rowIndex", index);
					intEditor.putExtra("rowID", id);
					intEditor.setAction(Intent.ACTION_EDIT);
					startActivity(intEditor);
				}
			});
			
		layoutExpends.setOnCreateView(new SQLiteDBLayout.OnCreateView() {

				@Override
				public View onCreate(ViewParent parent, View view, int rowIndex, int colIndex, Object data, DataType type, int rowID)
				{
					if (rowIndex != -1) return view;
					TextView tvTitle = (TextView) view;
					tvTitle.setText(layoutExpends.getTableName().toUpperCase());
					return tvTitle;
				}
			});
		
		btnTable.setText(String.format(Locale.US, "%,.0f", db.getBalanceRemain()));
		
		ibWallet.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					tblName = "balance";
					layoutExpends.setTable(tblName);
					btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dollar_sign, 0, R.drawable.ic_caret_down, 0);
					ivBGround.setImageResource(R.drawable.bg_wallet_crypto);
					layoutExpends.setSQL(sqlTblBalance).show();
					btnTable.setText(String.format(Locale.US, "%,.0f", db.getCashRemain()));
				}
			});
		
		btnTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view)
			{
				List<Map<String, Object>> listTable = db.query("SELECT name FROM sqlite_master WHERE (type='table' OR type='view') AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%' AND name!='logs' AND name!='setting' AND name!='cmdhistory' AND name!='budgets_protected' AND name!='priorities' AND name!='todos' ORDER BY name COLLATE NOCASE");
				ArrayList<String> tables = new ArrayList<>();
				for (Map<String, Object> table : listTable) {
					tables.add((String) table.get("name"));
				}
				final String[] tableNames = tables.toArray(new String[0]);
				AlertDialog.Builder dlgTable = new AlertDialog.Builder(MainActivity.this);
				dlgTable.setTitle("Select Table")
					.setIcon(R.drawable.ic_table)
					.setItems(tableNames, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int index)
						{
							tblName = tableNames[index];
							switch (tblName) {
								case "budgets":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_money_bag, 0, R.drawable.ic_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_budget);
									layoutExpends.setSQL("SELECT budgetid, date, name, printf('%,.0f', balance) AS balance, note FROM budgets ORDER BY name COLLATE NOCASE");
									btnTable.setText(tblName);
									break;
								case "funds":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dollar, 0, R.drawable.ic_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_fund);
									layoutExpends.setSQL("SELECT funds.fundid, CASE WHEN strftime('%Y', funds.date, 'unixepoch', 'localtime') = strftime('%Y', 'now', 'localtime') THEN strftime('%d-%m %H:%M', funds.date, 'unixepoch', 'localtime') ELSE strftime('%d-%m-%Y %H:%M', funds.date, 'unixepoch', 'localtime') END AS date, printf('%,.0f', funds.cash) AS cash, bank1.name AS bank1, printf('%,.0f', funds.balance1) AS balance1, bank2.name AS bank2, printf('%,.0f', funds.balance2) AS balance2, printf('%,.0f', funds.cash + funds.balance1 + funds.balance2) AS total, funds.note FROM funds INNER JOIN budgets AS bank1 ON funds.bank1=bank1.budgetid INNER JOIN budgets AS bank2 ON funds.bank2=bank2.budgetid ORDER BY funds.date");
									btnTable.setText(String.format(Locale.US, "%,.0f", db.getBalanceRemain()));
									break;
								case "incomes":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_hand_holding_usd, 0, R.drawable.ic_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_income);
									layoutExpends.setSQL("SELECT * FROM incomes ORDER BY date DESC");
									btnTable.setText(tblName);
									break;
								case "expends":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shopping_cart, 0, R.drawable.ic_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_shopping_cart);
									layoutExpends.setSQL("SELECT expends.expendid, CASE WHEN strftime('%Y', expends.date, 'unixepoch', 'localtime') = strftime('%Y', 'now', 'localtime') THEN strftime('%d-%m %H:%M', expends.date, 'unixepoch', 'localtime') ELSE strftime('%d-%m-%Y %H:%M', expends.date, 'unixepoch', 'localtime') END AS date, suppliers.type || '. ' || suppliers.name AS supplier, products.name AS product, units.name AS unit, printf('%,.0f', expends.price) AS price, printf('%,.2f', expends.quantity) AS quantity, printf('%,.0f', expends.price * expends.quantity) AS total, budgets.name AS budget, ifnull(expends.note, '') AS note FROM expends INNER JOIN suppliers USING (supplierid) INNER JOIN products USING (productid) INNER JOIN units USING (unitid) INNER JOIN budgets USING (budgetid) ORDER BY expends.date DESC");
									btnTable.setText(String.format(Locale.US, "%,.0f", db.getTotalExpends()));
									break;
								case "product_types":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_luggage_cart, 0, R.drawable.ic_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_grocery_list);
									layoutExpends.setSQL("SELECT * FROM product_types ORDER BY name COLLATE NOCASE");
									btnTable.setText(tblName);
									break;
								case "products":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_box, 0, R.drawable.ic_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_product);
									layoutExpends.setSQL("SELECT products.productid, products.name, printf('%,.0f', products.price) AS price, units.name AS unit, product_types.name AS type FROM products INNER JOIN units USING (unitid) INNER JOIN product_types USING (ptypeid) ORDER BY products.name COLLATE NOCASE");
									btnTable.setText(tblName);
									break;
								case "supplier_products":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_list, 0, R.drawable.ic_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_address_book);
									layoutExpends.setSQL("SELECT * FROM supplier_products ORDER BY supplierid, productid");
									btnTable.setText(tblName);
									break;
								case "supplier_types":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_store, 0, R.drawable.ic_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_address_book);
									layoutExpends.setSQL("SELECT * FROM supplier_types ORDER BY name COLLATE NOCASE");
									btnTable.setText(tblName);
									break;
								case "suppliers":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_id_card, 0, R.drawable.ic_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_address_book);
									layoutExpends.setSQL("SELECT * FROM suppliers ORDER BY type COLLATE NOCASE, name COLLATE NOCASE, address COLLATE NOCASE");
									btnTable.setText(tblName);
									break;
								case "units":
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_ruler, 0, R.drawable.ic_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_measurement);
									layoutExpends.setSQL("SELECT * FROM units ORDER BY name COLLATE NOCASE");
									btnTable.setText(tblName);
									break;
								default:
									btnTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_database, 0, R.drawable.ic_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_database);
									layoutExpends.setSQL(null);
									btnTable.setText(tblName);
							}
							layoutExpends.setTable(tblName).show();
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
				intEditor.putExtra("tblName", layoutExpends.getTableName());
				startActivity(intEditor);
			}
		});
    }

	@Override
	protected void onResume()
	{
		super.onResume();
		todosLimit = app.getTodosLimit();
		layoutTodos.setSQL("SELECT * FROM (SELECT todoid, priorityid , job, complete FROM todos WHERE complete=0 ORDER BY priorityid, createdate DESC LIMIT " + todosLimit + ") UNION ALL SELECT * FROM (SELECT todoid, priorityid , job, complete FROM todos WHERE complete=1 ORDER BY completedate DESC LIMIT " + todosLimit + ")").show();
		layoutExpends.show();
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

