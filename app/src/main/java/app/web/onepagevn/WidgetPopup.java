package app.web.onepagevn;
import android.app.*;
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
}
