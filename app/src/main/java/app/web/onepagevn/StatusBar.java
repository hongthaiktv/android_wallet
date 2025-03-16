package app.web.onepagevn;
import android.widget.*;

public class StatusBar
{
	private static StatusBar instance;
	private ImageView iconType;
	private TextView status;

	private StatusBar()
	{}

	public static StatusBar getInstance(LinearLayout statusBar)
	{
		if (instance == null) instance = new StatusBar();
		instance.iconType = (ImageView) statusBar.getChildAt(0);
		HorizontalScrollView scrollBar = (HorizontalScrollView) statusBar.getChildAt(1);
		instance.status = (TextView) scrollBar.getChildAt(0);
		//instance.status.setText("static and private ref");
		return instance;
	}
	
	public static void error(String message) {
		instance.status.setText(message);
	}
}
