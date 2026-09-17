package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import android.widget.LinearLayout.*;
import java.util.*;

public class Editor extends Activity
{
	private LinearLayout editorTopLayout;
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
		
		List<Map<String, Object>> data = db.getData("SELECT * FROM " + tblName + " LIMIT 1", null);
		Map<String, Object> rowData = data.get(0);
		Set<String> keys = rowData.keySet();
		String[] colNames = keys.toArray(new String[0]);
		for (int i = 1; i < colNames.length; i++) {
			LinearLayout row = new LinearLayout(this);
			row.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			row.setLayoutParams(rowParams);
			TextView tvName = new TextView(this);
			tvName.setText(colNames[i]);
			EditText input = new EditText(this);
			input.setLayoutParams(rowParams);
			row.addView(tvName);
			row.addView(input);
			editorTopLayout.addView(row);
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
