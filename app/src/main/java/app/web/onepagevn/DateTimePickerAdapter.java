package app.web.onepagevn;

import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;
import android.widget.DatePicker.*;
import java.text.*;
import java.util.*;

public class DateTimePickerAdapter extends ArrayAdapter<String>
{
	private Context context;
	private int resource;
	private StatusBar stt;
	private AlertDialog dialog;
	private DatePicker dPicker;
	private TimePicker tPicker;
	private Date pickedDate;
	private Date pickedTime;
	
	public DateTimePickerAdapter(Context context, int resource) {
		super(context, resource, new String[1]);
		init(context, resource, null);
	}
	
	public DateTimePickerAdapter(Context context, int resource, Date datetime) {
		super(context, resource, new String[1]);
		init(context, resource, datetime);
	}
	
	private void init(Context context, int resource, Date datetime) {
		this.context = context;
		this.resource = resource;
		this.pickedDate = datetime;
		this.pickedTime = datetime;
		stt = new StatusBar("DateTimePickerAdapter");
	}
	
	public DateTimePickerAdapter setPickedDate(Date date)
	{
		pickedDate = date;
		return this;
	}
	
	public DateTimePickerAdapter setPickedTime(Date time)
	{
		pickedTime = time;
		return this;
	}

	public Date getPickedDate()
	{
		return pickedDate;
	}
	
	public Date getPickedTime()
	{
		return pickedTime;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(resource, parent, false);
		View picker = view.findViewById(R.id.picker);
		String name = picker.getClass().getSimpleName();
		switch (name) {
			case "DatePicker":
				dPicker = (DatePicker) picker;
				if (pickedDate != null) dPicker.updateDate(pickedDate.getYear(), pickedDate.getMonth(), pickedDate.getDate());
				dPicker.setOnDateChangedListener(new OnDateChangedListener() {

					@Override
					public void onDateChanged(DatePicker p, int year, int month, int day)
					{
						SimpleDateFormat formater = new SimpleDateFormat("d-M-yyyy");
						String sDate = day + "-" + ++month + "-" + year;
						try
						{
							pickedDate = formater.parse(sDate);
						}
						catch (ParseException e)
						{stt.error(e.getMessage());}
						if (dialog != null) dialog.dismiss();
					}
				});
				break;
			case "TimePicker":
				tPicker = (TimePicker) picker;
				if (pickedTime != null) {
					tPicker.setHour(pickedTime.getHours());
					tPicker.setMinute(pickedTime.getMinutes());
				}
				tPicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
					
						@Override
						public void onTimeChanged(TimePicker p, int hour, int minute)
						{
							pickedTime = new Date();
							pickedTime.setHours(hour);
							pickedTime.setMinutes(minute);
							pickedTime.setSeconds(0);
						}
					});
				break;
		}
		return view;
	}
	
	public void setDialog(AlertDialog dialog) {
		this.dialog = dialog;
	}
}
