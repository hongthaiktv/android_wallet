package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import android.view.*;

public class Editor extends Activity
{
	private LinearLayout mainLayout, sttLayout;
	private StatusBar stt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);

		mainLayout = findViewById(R.id.editorMainLayout);

		LayoutInflater sttInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		sttLayout = (LinearLayout) sttInflater.inflate(R.layout.status_bar, null);
		mainLayout.addView(sttLayout);
		stt = StatusBar.getInstance(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		stt.update(this);
		
		Intent intent = getIntent();
		String intAction = intent.getAction();
		String intType = intent.getType();
		
		if (Intent.ACTION_SEND.equals(intAction) && intType != null) {
			String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
			Toast.makeText(this, sharedText, Toast.LENGTH_SHORT).show();
		}
	}
}
