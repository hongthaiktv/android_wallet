package app.web.onepagevn;
import android.widget.*;

public class StatusBar
{
	private static StatusBar instance;
	private ImageView iconType;
	private HorizontalScrollView scrollBar;
	private TextView status;
	private String message = "abc def scroll me... scroll me... scroll me... scroll me... scroll me... scroll me...";

	private StatusBar()
	{}

	public static StatusBar getInstance(LinearLayout statusBar)
	{
		if (instance == null) instance = new StatusBar();
		instance.iconType = (ImageView) statusBar.getChildAt(0);
		instance.scrollBar = (HorizontalScrollView) statusBar.getChildAt(1);
		instance.status = (TextView) instance.scrollBar.getChildAt(0);
		instance.status.setText(instance.message);
		return instance;
	}

	public ImageView getIconType()
	{
		return iconType;
	}

	public HorizontalScrollView getScrollBar()
	{
		return scrollBar;
	}

	public TextView getStatus()
	{
		return status;
	}

	public void setMessage(String message)
	{
		this.message = message;
		this.status.setText(message);
		this.scrollBar.scrollTo(0, 0);
	}

	public String getMessage()
	{
		return message;
	}
}
