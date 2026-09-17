package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import android.view.View.*;
import java.util.concurrent.*;
import java.util.*;

public class Editor extends Activity
{
	private StatusBar stt;
	private SQLiteDB db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
		
		stt = new StatusBar(this).init();
		db = new SQLiteDB();
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
