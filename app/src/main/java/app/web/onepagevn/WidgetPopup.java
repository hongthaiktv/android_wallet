package app.web.onepagevn;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class WidgetPopup extends Activity
{
	private LinearLayout rootLayout;
	private Button btnAdd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.widget_popup);
		
		rootLayout = findViewById(R.id.widget_popupRootLayout);
		btnAdd = findViewById(R.id.widget_popupBtnAdd);
		
		rootLayout.setClipToOutline(true);
		if (getWindow() != null) {
            getWindow().setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        btnAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		Intent intent = getIntent();
		String intAction = intent.getAction();
		String intType = intent.getType();

		if (Intent.ACTION_SEND.equals(intAction) && intType != null) {
			String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
			Toast.makeText(this, sharedText, Toast.LENGTH_SHORT).show();
		}
	}
}
