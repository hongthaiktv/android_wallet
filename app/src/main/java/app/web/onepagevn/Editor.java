package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.text.*;

public class Editor extends Activity
{
	private GridLayout editorTopLayout;
	private Button btnClear, btnAdd, btnCancel;
	private StatusBar stt;
	private SQLiteDB db;
	private String tblName = "expends";
	private String[] colNames, colTypes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
		
		editorTopLayout = findViewById(R.id.editorTopLayout);
		btnClear = findViewById(R.id.editorBtnClear);
		btnAdd = findViewById(R.id.editorBtnAdd);
		btnCancel = findViewById(R.id.editorBtnCancel);
		stt = new StatusBar(this).init();
		db = new SQLiteDB();
		tblName = !"balance".equals(getIntent().getStringExtra("tblName")) ? getIntent().getStringExtra("tblName") : "expends";
		setTitle(tblName.toUpperCase());
		
		colNames = db.getColumnNames(tblName);
		colTypes = db.getColumnTypes(tblName);
		editorTopLayout.setRowCount(colNames.length - 1);
		for (int i = 1; i < colNames.length; i++) {
			GridLayout.LayoutParams tvParams = new GridLayout.LayoutParams();
			tvParams.setMargins(20, 0, 20, 0);
			TextView tvName = new TextView(this);
			tvName.setLayoutParams(tvParams);
			tvName.setText(colNames[i].toUpperCase());
			GridLayout.LayoutParams iptParams = new GridLayout.LayoutParams();
			iptParams.width = 0;
			iptParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
			iptParams.setMargins(20, 0, 20, 0);
			EditText input = new EditText(this);
			input.setLayoutParams(iptParams);
			setViewID(input, i);
			editorTopLayout.addView(tvName);
			editorTopLayout.addView(input);
		}
		
		btnClear.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					Toast.makeText(Editor.this, "" + colTypes[1], Toast.LENGTH_SHORT).show();
				}
			});
			
		btnAdd.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{
					int numRow = editorTopLayout.getRowCount();
					ContentValues data = new ContentValues();
					for (int i = 1; i <= numRow; i++) {
						EditText input = (EditText) getViewByIndex(i);
						String value = input.getText().toString();
						if ("".equals(value)) continue;
						String type = colTypes[i];
						switch (type) {
							case "DATETIME":
								if (value.length() <= 10) value += " 00:00:00";
								SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								long date = -1;
								try
								{
									date = formater.parse(value).getTime() / 1000L;
								}
								catch (ParseException e)
								{stt.error(e.getMessage());}
								data.put(colNames[i], date);
								break;
							default:
								data.put(colNames[i], value);
						}
					}
					if (data.size() > 0) db.insert(tblName, data);
				}
			});
			
		btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view)
				{

				}
			});
	}
	
	private void setViewID(View view, int index) {
		int id = -1;
		switch (index) {
			case 0:
				id = R.string.view00;
				break;
			case 1:
				id = R.string.view01;
				break;
			case 2:
				id = R.string.view02;
				break;
			case 3:
				id = R.string.view03;
				break;
			case 4:
				id = R.string.view04;
				break;
			case 5:
				id = R.string.view05;
				break;
			case 6:
				id = R.string.view06;
				break;
			case 7:
				id = R.string.view07;
				break;
			case 8:
				id = R.string.view08;
				break;
			case 9:
				id = R.string.view09;
				break;
			case 10:
				id = R.string.view10;
				break;
			case 11:
				id = R.string.view11;
				break;
			case 12:
				id = R.string.view12;
				break;
			case 13:
				id = R.string.view13;
				break;
			case 14:
				id = R.string.view14;
				break;
			case 15:
				id = R.string.view15;
				break;
			case 16:
				id = R.string.view16;
				break;
			case 17:
				id = R.string.view17;
				break;
			case 18:
				id = R.string.view18;
				break;
			case 19:
				id = R.string.view19;
				break;
			case 20:
				id = R.string.view20;
				break;
		}
		view.setId(id);
	}
	
	private View getViewByIndex(int index) {
		int id = -1;
		switch (index) {
			case 0:
				id = R.string.view00;
				break;
			case 1:
				id = R.string.view01;
				break;
			case 2:
				id = R.string.view02;
				break;
			case 3:
				id = R.string.view03;
				break;
			case 4:
				id = R.string.view04;
				break;
			case 5:
				id = R.string.view05;
				break;
			case 6:
				id = R.string.view06;
				break;
			case 7:
				id = R.string.view07;
				break;
			case 8:
				id = R.string.view08;
				break;
			case 9:
				id = R.string.view09;
				break;
			case 10:
				id = R.string.view10;
				break;
			case 11:
				id = R.string.view11;
				break;
			case 12:
				id = R.string.view12;
				break;
			case 13:
				id = R.string.view13;
				break;
			case 14:
				id = R.string.view14;
				break;
			case 15:
				id = R.string.view15;
				break;
			case 16:
				id = R.string.view16;
				break;
			case 17:
				id = R.string.view17;
				break;
			case 18:
				id = R.string.view18;
				break;
			case 19:
				id = R.string.view19;
				break;
			case 20:
				id = R.string.view20;
				break;
		}
		return findViewById(id);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		stt.update();
		
		Intent intent = getIntent();
		String intAction = intent.getAction();
		String intType = intent.getType();
		
		if (Intent.ACTION_SEND.equals(intAction) && intType != null) {
			String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
			Toast.makeText(this, sharedText, Toast.LENGTH_SHORT).show();
		}
	}
}
