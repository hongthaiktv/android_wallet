package app.web.onepagevn;

import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.text.*;
import android.view.View.*;
import android.widget.Toolbar.*;

public class DateTimePickerAdapter extends ArrayAdapter<String>
{
	private Context context;
	
	public DateTimePickerAdapter(Context context, String[] items) {
		super(context, R.layout.datetime_picker, items);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(R.layout.datetime_picker, parent, false);
		Button btnDate = view.findViewById(R.id.dtPickerBtnDate);
		SimpleDateFormat formater = new SimpleDateFormat("dd-MMM-yyyy");
		Date now = new Date();
		btnDate.setText(formater.format(now));
		btnDate.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v)
				{
					DatePicker dPicker = new DatePicker(context);
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					dPicker.setLayoutParams(params);
					LinearLayout layout = (LinearLayout) view;
					layout.removeAllViews();
					layout.addView(dPicker);
				}
			});
		
		Button btnTime = view.findViewById(R.id.dtPickerBtnTime);
		formater.applyPattern("HH:mm");
		btnTime.setText(formater.format(now));
		return view;
	}
}
