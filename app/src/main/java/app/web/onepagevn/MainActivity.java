package app.web.onepagevn;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

public class MainActivity extends Activity 
{
	TextView tvTable;
	Button btnAdmin;
	ImageButton ibFAB;
	ImageView ivBGround;
	String tblName;
	private LinearLayout mainLayout;
	private StatusBar stt;
	private AppServiceConnection srv;
	
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
		srv = AppSetting.getAppContext().getServiceConnection();
		
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
									ivBGround.setImageResource(R.drawable.bg_shopping_cart);
									break;
								case "customers":
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_address_book, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_address_book);
									break;
								case "products":
									tvTable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ico_box, 0, R.drawable.ico_caret_down, 0);
									ivBGround.setImageResource(R.drawable.bg_product);
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

