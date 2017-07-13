package com.goodocom.gocsdkfinal.fragment;

import java.util.ArrayList;
import java.util.List;

import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.domain.BlueToothInfo;
import com.goodocom.gocsdkfinal.domain.BlueToothPairedInfo;
import com.goodocom.gocsdkfinal.service.GocsdkCallbackImp;
import com.goodocom.gocsdkfinal.service.GocsdkServiceHelper;

import android.app.Fragment;
import android.os.Bundle;
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

public class WtFragmentAvailBtList extends Fragment{
	private GocsdkServiceHelper mGocsdkServiceHelper;
	private DeviceAdapter mDeviceAdapter;
	private List<BlueToothInfo> mAvailInfoList = new ArrayList<BlueToothInfo>();
	private boolean mSearching = false;
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
				BlueToothInfo info = mDeviceAdapter.getItem(position);				
				mGocsdkServiceHelper.connectDevice(info.address);
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
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(mSearching){
			mGocsdkServiceHelper.stopDiscovery();
		}
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
		callbcack.setOnAvailListener(new GocsdkCallbackImp.OnAvailListener() {	
			@Override
			public void onHfpStateChange(boolean connected) {
				Log.i("hcj.cb", "onHfpStateChange connected="+connected);
				//loadData();
			}
			
			@Override
			public void onDiscovery(BlueToothInfo info) {
				mSearching = true;
				mAvailInfoList.add(info);
				mDeviceAdapter.notifyDataSetChanged();
			}

			@Override
			public void onDiscoveryDone() {
				mSearching = false;
				showNormal();
			}
		});
		
		//loadData();
	}
	
	private void showNormal(){
		mStateView.setText(R.string.avail_device);
		//mStateView.setVisibility(View.VISIBLE);
		mSearchBtn.setVisibility(View.VISIBLE);
		mSearchProgress.setVisibility(View.GONE);
	}
	
	private void showSearching(){
		mStateView.setText(R.string.state_searching);
		//mStateView.setVisibility(View.VISIBLE);
		mSearchBtn.setVisibility(View.GONE);
		mSearchProgress.setVisibility(View.VISIBLE);
	}
	
	private void loadData(){
		if(!mGocsdkServiceHelper.isBtOpen()){
			return;
		}
		Log.i("hcj.cb", "loadData");
		mAvailInfoList.clear();
		
		mGocsdkServiceHelper.startDiscovery();
		mSearching = true;
		showSearching();
	}
	
	private class DeviceAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mAvailInfoList.size();
		}

		@Override
		public BlueToothInfo getItem(int position) {
			return mAvailInfoList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(WtFragmentAvailBtList.this.getActivity(),
						R.layout.wt_btavail_list_item_layout, null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tv_address = (TextView) convertView.findViewById(R.id.tv_address);						
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
			final BlueToothInfo blueToothInfo = mAvailInfoList.get(position);
			String name = null;
			if (TextUtils.isEmpty(blueToothInfo.name)) {
				name = "该设备无名称";
			} else {
				name = blueToothInfo.name;
			}
			holder.tv_name.setText(name);
			holder.tv_address.setText(blueToothInfo.address);
			return convertView;
		}

	}

	private static class ViewHolder {
		public TextView tv_name;
		public TextView tv_address;
	}
}
