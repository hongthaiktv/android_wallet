package app.web.onepagevn;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class WidgetPopup extends AppCompatActivity
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
