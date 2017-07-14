package com.goodocom.gocsdkfinal.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.activity.CallActivity;
import com.goodocom.gocsdkfinal.activity.MainActivity;
import com.goodocom.gocsdkfinal.db.Database;
import com.goodocom.gocsdkfinal.domain.CallLogInfo;
import com.goodocom.gocsdkfinal.service.GocsdkCallbackImp;
import com.goodocom.gocsdkfinal.service.GocsdkServiceHelper;
import com.goodocom.gocsdkfinal.view.MyFragmentTabHost;
import com.goodocom.gocsdkfinal.view.NoScrollViewPager;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentCallog extends Fragment implements OnClickListener {
	private MainActivity activity;
	private ImageView ib_call_in;
	private ImageView ib_call_out;
	private ImageView ib_call_missed;
	private NoScrollViewPager vp_content;
	private String[] callLogString = { "鎷ㄥ嚭", "鎵撹繘", "鏈帴" };
	private static final int CALLLOG_IN = 3;
	private static final int CALLLOG_OUT = 1;
	private static final int CALLLOG_MISS = 2;

	// 妯℃嫙鎺т欢
	private List<TextView> textViews = new ArrayList<TextView>();
	// 瀹為檯鎺т欢
	private List<ListView> listViews = new ArrayList<ListView>();
	private View view;

	public List<Map<String, String>> call_log_in = new ArrayList<Map<String, String>>();
	public List<Map<String, String>> call_log_out = new ArrayList<Map<String, String>>();
	public List<Map<String, String>> call_log_miss = new ArrayList<Map<String, String>>();
	private SimpleAdapter mSimpleAdapterIn;
	private SimpleAdapter mSimpleAdapterout;
	private SimpleAdapter mSimpleAdapterMiss;
	private MyFragmentTabHost tabHost;
	private SQLiteDatabase systemDb;
	public final static int MSG_CALLLOG = 1;// 閫氳瘽璁板綍涓嬭浇
	public final static int MSG_CALLLOG_DONE = 2;// 閫氳瘽璁板綍涓嬭浇缁撴潫
	private static Handler hand = null;
	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CALLLOG: {
				CallLogInfo info = (CallLogInfo) msg.obj;
				Map<String, String> map = null;
				switch (info.calltype) {
				case 4: {
					if (call_log_out.size() <= 100) {
						map = new HashMap<String, String>();
						String phonenameTmp = null;
						if (TextUtils.isEmpty(info.phonename)) {
							phonenameTmp = activity.getString(R.string.unkown_number);
						} else {
							phonenameTmp = info.phonename;
						}
						map.put("itemName", phonenameTmp);
						map.put("itemNum", info.phonenumber);
						call_log_out.add(map);
						mSimpleAdapterout.notifyDataSetChanged();
					}
					break;
				}
				case 6: {
					if (call_log_miss.size() <= 100) {
						map = new HashMap<String, String>();
						String phonenameTmp = null;
						if (TextUtils.isEmpty(info.phonename)) {
							phonenameTmp = activity.getString(R.string.unkown_number);
						} else {
							phonenameTmp = info.phonename;
						}
						map.put("itemName", phonenameTmp);
						map.put("itemNum", info.phonenumber);
						call_log_miss.add(map);
						mSimpleAdapterMiss.notifyDataSetChanged();
					}
					break;
				}
				case 5: {
					if (call_log_in.size() <= 100) {
						map = new HashMap<String, String>();
						String phonenameTmp = null;
						if (TextUtils.isEmpty(info.phonename)) {
							phonenameTmp = activity.getString(R.string.unkown_number);
						} else {
							phonenameTmp = info.phonename;
						}
						map.put("itemName", phonenameTmp);
						map.put("itemNum", info.phonenumber);
						call_log_in.add(map);
						mSimpleAdapterIn.notifyDataSetChanged();
					}
					break;
				}
				}
			}
			case MSG_CALLLOG_DONE:
				//Toast.makeText(activity, "褰撳墠閫氳瘽璁板綍涓嬭浇瀹屾瘯锛�", 0).show();
				rl_downloading.setVisibility(View.GONE);
				vp_content.setVisibility(View.VISIBLE);
				break;
			}
		};
	};
	private Handler mainHandler;
	private ImageView image_animation;
	private RelativeLayout rl_downloading;
	
	public static Handler getHandler() {
		return hand;
	}

	// 鍒涘缓璇ュ璞℃椂锛岃皟鐢ㄨ鏂规硶
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
	}

	// 鍔犺浇椤甸潰
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		System.out.println("浣犲埌浜嗘潵娌℃潵");
		view = View.inflate(activity, R.layout.fragmentcalllog, null);
		
		ib_call_in = (ImageView) view.findViewById(R.id.ib_call_in);
		ib_call_out = (ImageView) view.findViewById(R.id.ib_call_out);
		ib_call_missed = (ImageView) view.findViewById(R.id.ib_call_missed);
		vp_content = (NoScrollViewPager) view.findViewById(R.id.vp_content);
		rl_downloading = (RelativeLayout) view
				.findViewById(R.id.rl_downloading);

		image_animation = (ImageView) view.findViewById(R.id.image_animation);
		TranslateAnimation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.8f);
		animation.setDuration(1000);
		animation.setFillAfter(false);
		animation.setRepeatCount(-1);
		animation.setRepeatMode(Animation.RESTART);
		image_animation.startAnimation(animation);
		InitData();
		vp_content.setAdapter(new MyPagerAdapter());

		for (int i = 0; i < callLogString.length; i++) {
			ListView lv_item = new ListView(activity);
			lv_item.setSelector(R.drawable.contact_list_item_selector);
			paddingData(lv_item, i);
			listViews.add(lv_item);
		}

		ib_call_in.setOnClickListener(this);
		ib_call_out.setOnClickListener(this);
		ib_call_missed.setOnClickListener(this);

		tabHost = MainActivity.getTabHost();
		
		hand = handler;
		return view;
	}

	@Override
	public void onStop() {
		super.onStop();
		// 濡傛灉璇ユ暟鎹簱涓嶄负绌猴紝鍏抽棴璇ユ暟鎹簱锛屽苟璧嬪�间负null
		if (systemDb != null) {
			systemDb.close(); // close database
			systemDb = null;
		}
	}

	private void InitData() {
		systemDb = Database.getSystemDb();
		Database.createTable(systemDb, Database.Sql_create_calllog_tab);
		LoadIncomingData();
	}

	// 鍒囨崲椤甸潰
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ib_call_in:
			LoadIncomingData();
			vp_content.setCurrentItem(0, false);
			break;
		case R.id.ib_call_out:
			LoadCalloutData();
			ListView lv_callout = listViews.get(1);
			paddingData(lv_callout, 1);
			vp_content.setCurrentItem(1, false);
			break;
		case R.id.ib_call_missed:
			
			LoadMissedData();
			ListView lv_missed = listViews.get(2);
			paddingData(lv_missed, 2);
			vp_content.setCurrentItem(2, false);
			break;
		}
	}

	private void LoadMissedData() {
		if (GocsdkCallbackImp.hfpStatus > 0) {
			if (call_log_miss.size() == 0) {
				rl_downloading.setVisibility(View.VISIBLE);
				vp_content.setVisibility(View.GONE);
				if (mainHandler != null) {
					mainHandler
							.sendEmptyMessage(MainActivity.MSG_UPDATE_MISSED_CALLLOG);
				}
				call_log_miss.clear();
				//try {
					MainActivity.getService().callLogstartUpdate(CALLLOG_MISS);
				//} catch (RemoteException e) {
					//e.printStackTrace();
				//}
			}
		} else {/*
				 * if (systemDb != null) { List<CallLogInfo> InCallLog =
				 * Database .queryCallLog(systemDb, 6); Map<String, String> map
				 * = null; for (int i = 0; i < InCallLog.size(); i++) {
				 * CallLogInfo callLogInfo = InCallLog.get(i); map = new
				 * HashMap<String, String>(); String phonenameTmp = null; if
				 * (TextUtils.isEmpty(callLogInfo.phonename)) { phonenameTmp =
				 * "鏈煡鍙风爜"; } else { phonenameTmp = callLogInfo.phonename; }
				 * map.put("itemName", phonenameTmp); map.put("itemNum",
				 * callLogInfo.phonenumber); call_log_miss.add(map);
				 * //mSimpleAdapterMiss.notifyDataSetChanged(); } }
				 */
		}

	}

	private void LoadCalloutData() {
		if (GocsdkCallbackImp.hfpStatus > 0) {
			if (call_log_out.size() == 0) {
				rl_downloading.setVisibility(View.VISIBLE);
				vp_content.setVisibility(View.GONE);
				if (mainHandler != null) {
					mainHandler
							.sendEmptyMessage(MainActivity.MSG_UPDATE_CALLOUT_CALLLOG);
				}
				call_log_out.clear();
				//try {
					MainActivity.getService().callLogstartUpdate(CALLLOG_OUT);
				//} catch (RemoteException e) {
					//e.printStackTrace();
				//}
			}
		} else {
			
		}
	}

	private void LoadIncomingData() {
		if(DBG){
			dbgLoadData();
			return;
		}
		if (GocsdkCallbackImp.hfpStatus > 0) {// 鏈夎澶囪繛鎺�
			if (call_log_in.size() == 0) {
				rl_downloading.setVisibility(View.VISIBLE);
				vp_content.setVisibility(View.GONE);
				mainHandler = MainActivity.getHandler();
				mainHandler
						.sendEmptyMessage(MainActivity.MSG_UPDATE_INCOMING_CALLLOG);
				call_log_in.clear();
				//try {
					MainActivity.getService().callLogstartUpdate(CALLLOG_IN);
				//} catch (RemoteException e) {
					//e.printStackTrace();
				//}
			}
		} else {

		}
	}

	private class MyPagerAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ListView listView = listViews.get(position);
			ViewGroup parent = (ViewGroup) listView.getParent();
			if (parent != null) {
				parent.removeView(listView);
			}
			container.addView(listView);
			return listView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}

	private void clickItemCall(SimpleAdapter mSimpleAdapter, int position) {

		if (mSimpleAdapter != null) {
			Map<String, String> map = (Map<String, String>) mSimpleAdapter
					.getItem(position);
			callOut(map.get("itemNum"));
		}

	}

	private void callOut(String phoneNumber2) {
		placeCall(phoneNumber2);
	}

	// 缁欐帶浠跺～鍏呮暟鎹�
	public void paddingData(ListView listView, int position) {

		switch (position) {
		case 0:
			mSimpleAdapterIn = new SimpleAdapter(activity, call_log_in,
					R.layout.call_log_in_listview_item_view, new String[] {
							"itemName", "itemNum", "itemTime" },
					new int[] { R.id.tv_in_name, R.id.tv_in_number,
							R.id.tv_in_time });
			listView.setAdapter(mSimpleAdapterIn);

			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (GocsdkCallbackImp.hfpStatus > 0) {
						clickItemCall(mSimpleAdapterIn, position);
					} else {
						Toast.makeText(activity, activity.getString(R.string.warning_connect), Toast.LENGTH_SHORT)
								.show();
					}
				}

			});
			break;
		case 1:
			mSimpleAdapterout = new SimpleAdapter(activity, call_log_out,
					R.layout.call_log_out_listview_item_view, new String[] {
							"itemName", "itemNum", "itemTime" }, new int[] {
							R.id.tv_out_name, R.id.tv_out_number,
							R.id.tv_out_time });
			listView.setAdapter(mSimpleAdapterout);
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (GocsdkCallbackImp.hfpStatus > 0) {
						clickItemCall(mSimpleAdapterout, position);
					} else {
						Toast.makeText(activity,  activity.getString(R.string.warning_connect), Toast.LENGTH_SHORT)
								.show();
					}

				}
			});
			break;
		case 2:
			mSimpleAdapterMiss = new SimpleAdapter(activity, call_log_miss,
					R.layout.call_log_miss_listview_item_view, new String[] {
							"itemName", "itemNum", "itemTime" }, new int[] {
							R.id.tv_miss_name, R.id.tv_miss_number,
							R.id.tv_miss_time });
			listView.setAdapter(mSimpleAdapterMiss);
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (GocsdkCallbackImp.hfpStatus > 0) {
						clickItemCall(mSimpleAdapterMiss, position);
					} else {
						Toast.makeText(activity, activity.getString(R.string.warning_connect), Toast.LENGTH_SHORT)
								.show();
					}

				}
			});
			break;
		}

	}

	// 鎷ㄦ墦姝ｇ‘鐨勭數璇�
	private static void placeCall(String mLastNumber) {
		if (mLastNumber.length() == 0)
			return;
		if (PhoneNumberUtils.isGlobalPhoneNumber(mLastNumber)) {
			// place the call if it is a valid number
			if (mLastNumber == null || !TextUtils.isGraphic(mLastNumber)) {
				// There is no number entered.
				return;
			}
			//try {
				MainActivity.getService().phoneDail(mLastNumber);
			//} catch (RemoteException e) {
				//e.printStackTrace();
			//}
		}
	}
	
	private static final boolean DBG = false;
	private void dbgLoadData(){
		HashMap<String, String> map = new HashMap<String, String>();
		for(int i=0;i<10;i++){
			map.put("itemName", "name"+i);
			map.put("itemNum", "12345678"+i);
			call_log_in.add(map);
		}	
		handler.postDelayed(new Runnable(){
			@Override
			public void run(){
				mSimpleAdapterIn.notifyDataSetChanged();
				handler.sendEmptyMessage(MSG_CALLLOG_DONE);
			}
		},2000);
	}
}
