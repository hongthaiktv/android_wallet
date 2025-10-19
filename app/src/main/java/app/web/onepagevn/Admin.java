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
	private Button btnRun;
	private StatusBar stt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.admin);
		
		edtInput = findViewById(R.id.admEdtInput);
		btnRun = findViewById(R.id.admBtnRun);
		stt = new StatusBar(this);
		
		btnRun.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					try {
						int maxDialogLogs = Integer.valueOf(edtInput.getText().toString());
						AppSetting.getAppContext().setMaxDialogLogs(maxDialogLogs);
					} catch (Exception e) {stt.error(e.getMessage());}
				}
			});
	}
}
