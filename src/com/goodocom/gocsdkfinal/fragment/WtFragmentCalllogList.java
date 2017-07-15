package com.goodocom.gocsdkfinal.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.activity.WtContactsActivity;
import com.goodocom.gocsdkfinal.domain.BlueToothInfo;
import com.goodocom.gocsdkfinal.domain.BlueToothPairedInfo;
import com.goodocom.gocsdkfinal.domain.CallLogInfo;
import com.goodocom.gocsdkfinal.service.GocsdkCallbackImp;
import com.goodocom.gocsdkfinal.service.GocsdkServiceHelper;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
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
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

public class WtFragmentCalllogList extends Fragment{
	private static final String TAG = "hcj.WtFragmentCalllogList";
	private GocsdkServiceHelper mGocsdkServiceHelper;
	private GocsdkCallbackImp mCallBack;
	
	private boolean mSearching = false;
	private TextView mStateView;
	private ImageView mPageIndicator;
	private TextView mSyncBtn;
	private View mSearchProgress;
	private ViewPager mViewPager;
	public List<Map<String, String>> mDatasIn = new ArrayList<Map<String, String>>();
	public List<Map<String, String>> mDatasOut = new ArrayList<Map<String, String>>();
	public List<Map<String, String>> mDatasMiss = new ArrayList<Map<String, String>>();
	private List<ListView> mListViews = new ArrayList<ListView>();
	private List<PageItem> mPageItems = new ArrayList<PageItem>();
	
	private static final int CALLLOG_IN = 3;
	private static final int CALLLOG_OUT = 1;
	private static final int CALLLOG_MISS = 2;
	
	private int mPageIdx = 0;
	private int mLoadingType = -1;
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			if(!mGocsdkServiceHelper.isBtConnected()){
				WtContactsActivity.showBtDisconnected(getActivity());
				return;
			}
			SimpleAdapter adapter = (SimpleAdapter)arg0.getAdapter();
			Log.i(TAG, "onItemClick adapter="+adapter);
			if(adapter == null){
				return;
			}
			HashMap<String, String> contact = (HashMap<String, String>)adapter.getItem(position);
			if(contact == null){
				return;
			}
			WtContactsActivity.placeCall(contact.get("itemNum"),mGocsdkServiceHelper);
		}		
	};
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = View.inflate(getActivity(), R.layout.wt_fragment_calllog_list, null);			
		
		ListView inList = new ListView(getActivity());
		inList.setOnItemClickListener(mOnItemClickListener);
		SimpleAdapter inAdapter = new SimpleAdapter(getActivity(), mDatasIn,
				R.layout.wt_calllog_in_list_item, new String[]{"itemName", "itemNum", "itemTime" },
				new int[] { R.id.tv_name, R.id.tv_number,R.id.tv_time });
		inList.setAdapter(inAdapter);
		mListViews.add(inList);
		
		ListView outList = new ListView(getActivity());
		outList.setOnItemClickListener(mOnItemClickListener);
		SimpleAdapter outAdapter = new SimpleAdapter(getActivity(), mDatasOut,
				R.layout.wt_calllog_out_list_item, new String[]{"itemName", "itemNum", "itemTime" },
				new int[] { R.id.tv_name, R.id.tv_number,R.id.tv_time });
		outList.setAdapter(outAdapter);
		mListViews.add(outList);
		
		ListView missList = new ListView(getActivity());
		missList.setOnItemClickListener(mOnItemClickListener);
		SimpleAdapter missAdapter = new SimpleAdapter(getActivity(), mDatasOut,
				R.layout.wt_calllog_miss_list_item, new String[]{"itemName", "itemNum", "itemTime" },
				new int[] { R.id.tv_name, R.id.tv_number,R.id.tv_time });
		missList.setAdapter(missAdapter);
		mListViews.add(missList);
		
		mViewPager = (ViewPager)view.findViewById(R.id.view_pager);
		mViewPager.setAdapter(new CalllogPageAdapter());
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {			
			@Override
			public void onPageSelected(int arg0) {
				mPageIdx = arg0;
				showPage();
				loadData(mPageIdx);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		mPageItems.add(new PageItem(CALLLOG_IN,getString(R.string.calllog_in),
				getActivity().getDrawable(R.drawable.wt_settings_page4_indicator2),mDatasIn,inAdapter));
		mPageItems.add(new PageItem(CALLLOG_OUT,getString(R.string.calllog_out),
				getActivity().getDrawable(R.drawable.wt_settings_page4_indicator3),mDatasOut,outAdapter));
		mPageItems.add(new PageItem(CALLLOG_MISS,getString(R.string.calllog_miss),
				getActivity().getDrawable(R.drawable.wt_settings_page4_indicator4),mDatasMiss,missAdapter));

		mSearchProgress = view.findViewById(R.id.scanning_progress);
		mPageIndicator = (ImageView)view.findViewById(R.id.page_indicator);
		
		mStateView = (TextView)view.findViewById(R.id.state);
		mSyncBtn = (TextView)view.findViewById(R.id.sync);
		mSyncBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				loadData(mPageIdx);
			}
		});
		showPage();
		showNormal();
		//loadData(mPageIdx);
		
		return view;
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if(isVisibleToUser){
			loadData(mPageIdx);
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mCallBack.setOnCalllogListener(null);
	}
	
	public void setGocsdkServiceHelper(GocsdkServiceHelper helper, GocsdkCallbackImp callback){
		mGocsdkServiceHelper = helper;
		mCallBack = callback;
		mCallBack.setOnCalllogListener(new GocsdkCallbackImp.OnCalllogListener() {
			@Override
			public void onCalllog(CallLogInfo calllog) {
				Log.i(TAG, String.format("onCalllog type %d",calllog.calltype));
				PageItem item = null;
				if(calllog.calltype == 4){
					item = mPageItems.get(1);
				}else if(calllog.calltype == 5){
					item = mPageItems.get(0);
				}else if(calllog.calltype == 6){
					item = mPageItems.get(2);
				}
				if(item == null || item.mDatas.size() > 100){
					showPage();
					return;
				}
				HashMap map = new HashMap<String, String>();
				String phonenameTmp = null;
				if (TextUtils.isEmpty(calllog.phonename)) {
					phonenameTmp = getString(R.string.unkown_number);
				} else {
					phonenameTmp = calllog.phonename;
				}
				map.put("itemName", phonenameTmp);
				map.put("itemNum", calllog.phonenumber);
				item.mDatas.add(map);
				Log.i(TAG, String.format("onCalllog add %s",calllog.phonenumber));
				if(WtFragmentCalllogList.this.isResumed()){
					item.mAdapter.notifyDataSetChanged();
				}else{
					Log.i(TAG, "onCalllog not resumed");
				}
			}

			@Override
			public void onCalllogDone() {
				Log.i(TAG, "onCalllogDone mPageIdx="+mPageIdx+",mLoadingType="+mLoadingType);
				PageItem item = null;
				if(mLoadingType == CALLLOG_IN){
					item = mPageItems.get(0);
				}else if(mLoadingType == CALLLOG_OUT){
					item = mPageItems.get(1);
				}else if(mLoadingType == CALLLOG_MISS){
					item = mPageItems.get(2);
				}
				if(item != null){
					item.mIsLoaded = true;
					Log.i(TAG, "onCalllogDone item set loaded "+mLoadingType);
				}
				
				showPage();
				
				item = mPageItems.get(mPageIdx);
				//Log.i(TAG, String.format("onCalllogDone curr page mIsLoaded=b,mCallTyp%d",item.mIsLoaded,item.mCallType));
				if(!item.mIsLoaded && item.mCallType != mLoadingType){
					loadData(mPageIdx);
				}
				
				mLoadingType = -1;
			}

			@Override
			public void onCurrentAddr(String addr) {
				
			}
		});
		
		//loadData();
	}
	
	private void showPage(){
		PageItem item = mPageItems.get(mPageIdx);
		mStateView.setText(item.mTitle);
		mPageIndicator.setImageDrawable(item.mPageIndicator);
		mSyncBtn.setVisibility(View.VISIBLE);
		mSearchProgress.setVisibility(View.GONE);
	}
	
	private void showNormal(){
		//mStateView.setText(R.string.calllog);
		mSyncBtn.setVisibility(View.VISIBLE);
		mSearchProgress.setVisibility(View.GONE);
	}
	
	private void showSearching(){
		mStateView.setText(R.string.state_loading);
		mSyncBtn.setVisibility(View.GONE);
		mSearchProgress.setVisibility(View.VISIBLE);
	}
	
	private boolean isLoading(){
		return (mLoadingType == CALLLOG_IN) 
				|| (mLoadingType == CALLLOG_OUT) 
				|| (mLoadingType == CALLLOG_MISS);
	}
	
	private void loadData(int pageIdx){
		if(!mGocsdkServiceHelper.isBtOpen()){
			Log.i(TAG, String.format("loadData return isBtOpen"));
			return;
		}
		if(isLoading()){
			Log.i(TAG, String.format("loadData return isLoading"));
			return;
		}
		
		PageItem item = mPageItems.get(pageIdx);
		if(item.mIsLoaded){
			Log.i(TAG, String.format("loadData page %d loaded", pageIdx));
			return;
		}
		item.mDatas.clear();
		
		Log.i(TAG, String.format("loadData page %d",pageIdx));
		mGocsdkServiceHelper.callLogstartUpdate(item.mCallType);
		mLoadingType = item.mCallType;
		showSearching();
	}
	
	private class CalllogPageAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
		
		@Override  
	    public Object instantiateItem(View view, int position){ 
	    
	        ((ViewPager) view).addView(mListViews.get(position), 0);  
	          
	        return mListViews.get(position);  
	    }  
		
		@Override  
        public void destroyItem(ViewGroup view, int position, Object object) {  
            view.removeView(mListViews.get(position));  
        } 
	}
	
	private class PageItem{		
		public int mCallType;
		public String mTitle;
		public Drawable mPageIndicator;
		public List<Map<String, String>> mDatas;
		public SimpleAdapter mAdapter;
		public boolean mIsLoaded;
		
		public PageItem(int type, String title , Drawable pageIndicator,
				List<Map<String, String>> datas, SimpleAdapter adapter){
			mCallType = type;
			mTitle = title;
			mPageIndicator = pageIndicator;
			mDatas = datas;
			mAdapter = adapter;
		}
	}
}
