package com.goodocom.gocsdkfinal.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.activity.WtContactsActivity;
import com.goodocom.gocsdkfinal.fragment.FragmentMailList.phoneBook;
import com.goodocom.gocsdkfinal.service.GocsdkCallbackImp;
import com.goodocom.gocsdkfinal.service.GocsdkServiceHelper;

import android.app.Fragment;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
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
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class WtFragmentContactsList extends Fragment{
	private static final String TAG = "hcj.WtFragmentContactsList";
	private GocsdkServiceHelper mGocsdkServiceHelper;
	private GocsdkCallbackImp mCallBack;
	
	private SimpleAdapter mAdapter;
	private List<Map<String, String>> mDatas = new ArrayList<Map<String, String>>();
	private boolean mSearching = false;
	private TextView mStateView;
	private TextView mSyncBtn;
	private View mSearchProgress;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = View.inflate(getActivity(), R.layout.wt_fragment_contact_list, null);
		ListView listView = (ListView) view.findViewById(R.id.lv_paired_list);
		mAdapter = new SimpleAdapter(WtFragmentContactsList.this.getActivity(),mDatas,
				R.layout.contacts_listview_item, new String[] { "itemName","itemNum" },
				new int[] { R.id.tv_name, R.id.tv_number });
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {					
				if(mGocsdkServiceHelper.isBtConnected()){
					HashMap<String, String> contact = (HashMap<String, String>)mAdapter.getItem(position);	
					placeCall(contact.get("itemNum"));
				}else{
					WtContactsActivity.showBtDisconnected(getActivity());
				}
			}
		});
		
		mSearchProgress = view.findViewById(R.id.scanning_progress);
		
		mStateView = (TextView)view.findViewById(R.id.state);
		mSyncBtn = (TextView)view.findViewById(R.id.sync);
		mSyncBtn.setOnClickListener(new View.OnClickListener() {			
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
		mCallBack.setOnPhbListener(null);
	}
	
	public void setGocsdkServiceHelper(GocsdkServiceHelper helper, GocsdkCallbackImp callback){
		mGocsdkServiceHelper = helper;
		mCallBack = callback;
		mCallBack.setOnPhbListener(new GocsdkCallbackImp.OnPhbListener() {			
			@Override
			public void onPhoneBookDone() {
				Log.i(TAG, "onPhoneBookDone");
				showNormal();
			}
			
			@Override
			public void onPhoneBook(phoneBook phb) {
				Log.i(TAG, "onPhoneBook num="+phb.num);
				Map<String, String> phoBook = new HashMap<String, String>();
				phoBook.put("itemName", phb.name);
				phoBook.put("itemNum", phb.num);
				mDatas.add(phoBook);
				mAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void onCurrentAddr(String addr) {
				
			}
		});
		
		//loadData();
	}
	
	private void showNormal(){
		mStateView.setText(R.string.phonebook);
		mSyncBtn.setVisibility(View.VISIBLE);
		mSearchProgress.setVisibility(View.GONE);
	}
	
	private void showSearching(){
		mStateView.setText(R.string.state_loading);
		mSyncBtn.setVisibility(View.GONE);
		mSearchProgress.setVisibility(View.VISIBLE);
	}
	
	private void loadData(){
		if(!mGocsdkServiceHelper.isBtConnected()){
			Log.i(TAG, "loadData return");
			return;
		}
		Log.i(TAG, "loadData");
		mDatas.clear();
		
		mGocsdkServiceHelper.phoneBookStartUpdate();
		showSearching();
	}
	
	private void placeCall(String number) {
		if (number.length() == 0)
			return;
		if (PhoneNumberUtils.isGlobalPhoneNumber(number)) {
			if (number == null || !TextUtils.isGraphic(number)) {
				return;
			}

			mGocsdkServiceHelper.phoneDail(number);
		}
	}
}
