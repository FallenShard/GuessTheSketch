package nvnteam.guessthesketch.bluetooth;

import java.util.List;

import nvnteam.guessthesketch.R;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DeviceListAdapter extends ArrayAdapter<String>
{
    public DeviceListAdapter(Context context, int textViewResourceId)
    {
        super(context, textViewResourceId);
    }

    public DeviceListAdapter(Context context, int resource, List<String> items)
    {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {

        View v = convertView;

        if (v == null)
        {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.device_name, null);
        }

        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Segoe.ttf");

        String p = getItem(position);
        if (p != null)
        {

            TextView deviceName = (TextView) v.findViewById(R.id.device_name_element);
            if (deviceName != null)
            {
                deviceName.setTypeface(tf);
                deviceName.setText(p);
            }
        }

        return v;
    }
}
