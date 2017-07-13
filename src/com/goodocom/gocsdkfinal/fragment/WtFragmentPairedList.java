package com.goodocom.gocsdkfinal.fragment;

import java.util.ArrayList;
import java.util.List;

import com.goodocom.gocsdk.IGocsdkServiceSimple;
import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.activity.MainActivity;
import com.goodocom.gocsdkfinal.domain.BlueToothPairedInfo;
import com.goodocom.gocsdkfinal.service.GocsdkCallbackImp;
import com.goodocom.gocsdkfinal.service.GocsdkServiceHelper;

import android.app.Fragment;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class WtFragmentPairedList extends Fragment{
	private GocsdkServiceHelper mGocsdkServiceHelper;
	private DeviceAdapter mDeviceAdapter;
	private List<BlueToothPairedInfo> mPairedInfoList = new ArrayList<BlueToothPairedInfo>();
	private String mAddress;
	//private boolean mConnecting = false;
	private TextView mStateView;
	private TextView mSearchBtn;
	private View mSearchProgress;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = View.inflate(getActivity(), R.layout.wt_fragment_bt_list, null);
		ListView listView = (ListView) view.findViewById(R.id.lv_paired_list);
		mDeviceAdapter = new DeviceAdapter();
		listView.setAdapter(mDeviceAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				BlueToothPairedInfo info = mDeviceAdapter.getItem(position);
				if(info.address.equals(mAddress)){
					mGocsdkServiceHelper.disconnect();
					mAddress = null;
					mDeviceAdapter.notifyDataSetChanged();
				}else{
					mGocsdkServiceHelper.connectDevice(info.address);
				}
			}
		});
		
		mSearchProgress = view.findViewById(R.id.scanning_progress);
		
		mStateView = (TextView)view.findViewById(R.id.state);
		mSearchBtn = (TextView)view.findViewById(R.id.search);
		mSearchBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				loadData();
			}
		});
		showNormal();
		
		loadData();
		
		return view;
	}
	
	public void setGocsdkServiceHelper(GocsdkServiceHelper helper, GocsdkCallbackImp callbcack){
		mGocsdkServiceHelper = helper;	
		/*
		mGocsdkServiceHelper.registerListener(new GocsdkServiceHelper.OnServiceConnectListener() {			
			@Override
			public void onServiceDisconnected() {
			}
			
			@Override
			public void onServiceConnected(IGocsdkServiceSimple service) {
				loadData();
			}
		});*/
		callbcack.setOnPairedListener(new GocsdkCallbackImp.OnPairedListener() {			
			@Override
			public void onPairedDeviceAdd(BlueToothPairedInfo info) {
				Log.i("hcj.cb", "onPairedDeviceAdd");
				if(info.index>0){
					boolean added = false;
					for(int i=0;i<mPairedInfoList.size();i++){
						BlueToothPairedInfo pairedInfo = mPairedInfoList.get(i);
						if(pairedInfo.address.equals(info.address)){
							added = true;
						}
					}
					if(!added){
						mPairedInfoList.add(info);
						mDeviceAdapter.notifyDataSetChanged();
					}
				}				
			}
			
			@Override
			public void onHfpStateChange(boolean connected) {
				Log.i("hcj.cb", "onHfpStateChange connected="+connected);
				if(!connected){
					mAddress = null;
				}
				loadData();
			}
			
			@Override
			public void onCurrentAddr(String addr) {
				Log.i("hcj.cb", "onCurrentAddr addr="+addr);
				mAddress = addr;
				mDeviceAdapter.notifyDataSetChanged();
			}
		});
		
		//loadData();
	}
	
	private void showNormal(){
		mStateView.setText(R.string.paired_device);
		//mStateView.setVisibility(View.VISIBLE);
		mSearchBtn.setVisibility(View.GONE);
		mSearchProgress.setVisibility(View.GONE);
	}
	
	private void showSearching(){
		mStateView.setText(R.string.state_searching);
		//mStateView.setVisibility(View.VISIBLE);
		mSearchBtn.setVisibility(View.GONE);
		mSearchProgress.setVisibility(View.VISIBLE);
	}
	
	private void loadData(){
		Log.i("hcj.cb", "loadData");
		mPairedInfoList.clear();
		
		mGocsdkServiceHelper.getPairList();
		mGocsdkServiceHelper.getCurrentDeviceAddr();
		//showSearching();
	}
	
	private class DeviceAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mPairedInfoList.size();
		}

		@Override
		public BlueToothPairedInfo getItem(int position) {
			return mPairedInfoList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(WtFragmentPairedList.this.getActivity(),
						R.layout.wt_btpair_list_item_layout, null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) convertView.findViewById(R.id.bt_name);
				holder.iv_remove = (ImageView) convertView.findViewById(R.id.iv_remove);
				holder.mConnectState = (TextView) convertView.findViewById(R.id.connect_state);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			/*
			holder.tv_name = (TextView) convertView.findViewById(R.id.bt_name);
			holder.iv_remove = (ImageView) convertView
					.findViewById(R.id.iv_remove);
			holder.iv_isconnect = (ImageView) convertView
					.findViewById(R.id.iv_isconnect);
			*/
			final BlueToothPairedInfo blueToothInfo = mPairedInfoList.get(position);
			String name = null;
			if (TextUtils.isEmpty(blueToothInfo.name)) {
				name = "该设备无名称";
			} else {
				name = blueToothInfo.name;
			}
			if (!TextUtils.isEmpty(mAddress)
					&& blueToothInfo.address.equals(mAddress)) {
				//holder.iv_isconnect.setImageResource(R.drawable.bt_item_connected);
				holder.mConnectState.setText(R.string.state_connected);
				holder.mConnectState.setVisibility(View.VISIBLE);
			} else {
				//holder.iv_isconnect.setImageResource(R.drawable.bt_item_disconnected);
				holder.mConnectState.setVisibility(View.GONE);
			}
			holder.tv_name.setText(name);
			holder.iv_remove.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mPairedInfoList.remove(blueToothInfo);
					mDeviceAdapter.notifyDataSetChanged();
				}
			});
			return convertView;
		}

	}

	private static class ViewHolder {
		public TextView tv_name;
		public ImageView iv_remove;
		public ImageView iv_isconnect;
		public TextView mConnectState;
	}

}
