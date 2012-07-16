package avk.viv.abs;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BeaconArrayAdapter extends ArrayAdapter<BeaconObj> {

	private Context context;
	private BeaconObj[] beacons;
	private int		textColor;

	public BeaconArrayAdapter(Context context, int textViewResourceId,
			BeaconObj[] objects,int textColor) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.beacons = objects;
		this.textColor = textColor;
	}
	

	public int getCount() {
		return beacons.length;
	}
	
	public BeaconObj getItem(int pos) {
		return beacons[pos];	
	}
	
	public long getItemId(int pos) {
		return pos;
	}
	
	@Override
	public View getView(int position,View convertView,ViewGroup parent) {
		TextView label = new TextView(this.context);
		label.setTextColor(textColor);
		label.setText(beacons[position].name);
		label.setTextSize(15);
		return label;
	}

	@Override
	public View getDropDownView(int position,View convertView,ViewGroup parent) {
		TextView label = new TextView(context);
        label.setTextColor(textColor);
        label.setText(beacons[position].name);
		label.setTextSize(20);
        return label;
	}
}
