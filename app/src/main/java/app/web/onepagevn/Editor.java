package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.util.*;

public class Editor extends Activity
{
	private GridLayout editorTopLayout;
	private StatusBar stt;
	private SQLiteDB db;
	private String tblName = "expends";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
		
		editorTopLayout = findViewById(R.id.editorTopLayout);
		stt = new StatusBar(this).init();
		db = new SQLiteDB();
		tblName = !"balance".equals(getIntent().getStringExtra("tblName")) ? getIntent().getStringExtra("tblName") : "expends";
		setTitle(tblName.toUpperCase());
		
		List<Map<String, Object>> data = db.getData("SELECT * FROM " + tblName + " LIMIT 1", null);
		Map<String, Object> rowData = data.get(0);
		Set<String> keys = rowData.keySet();
		String[] colNames = keys.toArray(new String[0]);
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
			editorTopLayout.addView(tvName);
			editorTopLayout.addView(input);
		}
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
