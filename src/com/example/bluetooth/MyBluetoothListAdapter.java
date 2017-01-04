package com.example.bluetooth;

import java.util.ArrayList;
import java.util.List;

import com.example.entity.Bluetooth;

import android.R.bool;
import android.R.string;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyBluetoothListAdapter extends BaseAdapter {

	private List<Bluetooth> bluetoothList ;
	private LayoutInflater mInflater;
	Context context;
	private Bluetooth bluetooth;
	

	public MyBluetoothListAdapter(List<Bluetooth> bluetoothLis, android.content.Context context) {
		super();
		this.bluetoothList = bluetoothLis;
		mInflater = LayoutInflater.from(context);
		this.context = context;
		
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return bluetoothList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return bluetoothList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
//		View view = null;
//		if (convertView == null) {
//			view = mInflater.inflate(R.layout.bluetooth_list_item, null);
//			
//		} else {
//			view = convertView;
//		}
//		bluetooth = (Bluetooth) bluetoothList.get(position);
//		TextView tvName = (TextView) view.findViewById(R.id.big_text);
//		TextView tvaddr = (TextView) view.findViewById(R.id.tiny_text);
//		ImageView listImag = (ImageView) view.findViewById(R.id.list_imag);
//		tvName.setText(bluetooth.getName());
//		tvaddr.setText(bluetooth.getAddr());
//		listImag.setImageResource(bluetooth.getImage());
//		
//		
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.bluetooth_list_item, null);
			holder = new ViewHolder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.list_imag);
			holder.textName = (TextView) convertView.findViewById(R.id.big_text);
			holder.textView = (TextView) convertView.findViewById(R.id.tiny_text);
			convertView.setTag(holder);
			
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.imageView.setImageResource(bluetoothList.get(position).getImage());
		holder.textName.setText(bluetoothList.get(position).getName());
		holder.textView.setText(bluetoothList.get(position).getAddr());
		
		return convertView;
	}
	
	private class ViewHolder{
		TextView textView,textName;
		ImageView imageView;
	}

	
}
