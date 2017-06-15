package com.goodocom.gocsdkfinal.fragment;

import java.util.ArrayList;
import java.util.List;

import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.activity.MainActivity;
import com.goodocom.gocsdkfinal.domain.BlueToothPairedInfo;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentBlueToothList extends Fragment {

	public static final int MSG_PAIRED_DEVICE = 0;
	public static final int MSG_CONNECT_ADDRESS = 1;
	public static final int MSG_CONNECT_SUCCESS = 2;
	public static final int MSG_CONNECT_FAILE = 3;

	private MainActivity activity;
	private ListView lv_paired_list;
	private DeviceAdapter deviceAdapter;
	private String address;
	private boolean isConnecting = false;

	private List<BlueToothPairedInfo> btpi = new ArrayList<BlueToothPairedInfo>();
	private static Handler hand = null;
	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_PAIRED_DEVICE:
				BlueToothPairedInfo pairedInfo = (BlueToothPairedInfo) msg.obj;
				System.out.println("handler中pairedInfo.index="
						+ pairedInfo.index + "pairedInfo.name="
						+ pairedInfo.name + "pairedInfo.address="
						+ pairedInfo.address);
				if(pairedInfo.index>0){
					btpi.add(pairedInfo);
				}
				deviceAdapter.notifyDataSetChanged();
				System.out.println("btpi的大小："+btpi.size());
				break;
			case MSG_CONNECT_ADDRESS:
				address = (String) msg.obj;
				break;
			case MSG_CONNECT_SUCCESS:
				initData();
				break;
			case MSG_CONNECT_FAILE:
				initData();
				break;
			}
		};
	};

	public static Handler getHandler() {
		return hand;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = View
				.inflate(activity, R.layout.fragmentbluetoothlist, null);
		
		lv_paired_list = (ListView) view.findViewById(R.id.lv_paired_list);
		lv_paired_list.setSelector(R.drawable.contact_list_item_selector);
		deviceAdapter = new DeviceAdapter();
		lv_paired_list.setAdapter(deviceAdapter);
		initData();
		lv_paired_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				isConnecting = !isConnecting;
				if(isConnecting){
					address = null;
					try {
						MainActivity.getService().disconnect();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}else{
					BlueToothPairedInfo blueToothPairedInfo = deviceAdapter.getItem(position);
					address = blueToothPairedInfo.address;
					try {
						MainActivity.getService().connectDevice(blueToothPairedInfo.address);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		});
		hand = handler;
		return view;
	}

	private void initData() {
		btpi.clear();
		deviceAdapter.notifyDataSetChanged();
		try {
			MainActivity.getService().getPairList();
			MainActivity.getService().getCurrentDeviceAddr();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private class DeviceAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return btpi.size();
		}

		@Override
		public BlueToothPairedInfo getItem(int position) {
			return btpi.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(activity,
						R.layout.btpair_list_item_layout, null);
				holder = new ViewHolder();
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_name = (TextView) convertView.findViewById(R.id.bt_name);
			holder.iv_remove = (ImageView) convertView
					.findViewById(R.id.iv_remove);
			holder.iv_isconnect = (ImageView) convertView
					.findViewById(R.id.iv_isconnect);
			final BlueToothPairedInfo blueToothInfo = btpi.get(position);
			String name = null;
			if (TextUtils.isEmpty(blueToothInfo.name)) {
				name = "该设备无名称";
			} else {
				name = blueToothInfo.name;
			}
			if (!TextUtils.isEmpty(address)
					&& blueToothInfo.address.equals(address)) {
				holder.iv_isconnect.setImageResource(R.drawable.bt_item_connected);
			} else {
				holder.iv_isconnect.setImageResource(R.drawable.bt_item_disconnected);
			}
			holder.tv_name.setText(name);
			holder.iv_remove.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					btpi.remove(blueToothInfo);
					deviceAdapter.notifyDataSetChanged();
				}
			});
			return convertView;
		}

	}

	private static class ViewHolder {
		public TextView tv_name;
		public ImageView iv_remove;
		public ImageView iv_isconnect;
	}

}
