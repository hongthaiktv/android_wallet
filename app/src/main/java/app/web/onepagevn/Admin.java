package app.web.onepagevn;
import android.app.*;
import android.os.*;
import android.graphics.*;
import android.widget.*;
import android.view.View.*;
import android.view.*;

public class Admin extends Activity
{
	private Button btnRun;
	private StatusBar stt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.admin);
		
		btnRun = findViewById(R.id.admBtnRun);
		stt = new StatusBar(this);
		
		btnRun.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View p1)
				{
					stt.success("admin msg");
				}
			});
	}
}
