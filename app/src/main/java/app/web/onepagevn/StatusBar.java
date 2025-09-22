package app.web.onepagevn;
import android.widget.*;

public class StatusBar
{
	private static StatusBar instance;
	private ImageView iconType;
	private HorizontalScrollView scrollBar;
	private TextView status;
	private String crossPro = "abc def scroll me... scroll me... scroll me... scroll me... scroll me... scroll me...";

	private StatusBar()
	{}

	public void setCrossPro(String crossPro)
	{
		this.crossPro = crossPro;
		this.status.setText(crossPro);
		this.scrollBar.scrollTo(0, 0);
	}

	public String getCrossPro()
	{
		return this.crossPro;
	}

	public static StatusBar getInstance(LinearLayout statusBar)
	{
		if (instance == null) instance = new StatusBar();
		instance.iconType = (ImageView) statusBar.getChildAt(0);
		instance.scrollBar = (HorizontalScrollView) statusBar.getChildAt(1);
		instance.status = (TextView) instance.scrollBar.getChildAt(0);
		instance.status.setText(instance.crossPro);
		return instance;
	}
}
