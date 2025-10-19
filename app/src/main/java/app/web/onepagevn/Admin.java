package app.web.onepagevn;
import android.app.*;
import android.os.*;
import android.graphics.*;
import android.widget.*;
import android.view.View.*;
import android.view.*;
import android.content.*;

public class Admin extends Activity
{
	private EditText edtInput;
	private Button btnRun, btnSQL;
	private AppSetting app;
	private StatusBar stt;
	private SQLiteDB db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.admin);
		
		edtInput = findViewById(R.id.admEdtInput);
		btnRun = findViewById(R.id.admBtnRun);
		btnSQL = findViewById(R.id.admBtnSQL);
		app = AppSetting.getAppContext();
		stt = new StatusBar(this).init();
		db = new SQLiteDB();
		
		btnRun.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					try {
						String limit = edtInput.getText().toString();
						db.updateSetting("dialogMaxLogs", limit);
						app.setDialogMaxLogs(Integer.valueOf(limit));
						stt.success("Dialog max logs changed.");
					} catch (Exception e) {stt.error(e.getMessage());}
				}
			});
			
		btnSQL.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					String sql = edtInput.getText().toString();
				}
			});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		stt.update();
	}
}
