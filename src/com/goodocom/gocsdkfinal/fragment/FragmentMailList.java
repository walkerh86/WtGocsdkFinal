package com.goodocom.gocsdkfinal.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.activity.CallActivity;
import com.goodocom.gocsdkfinal.activity.MainActivity;
import com.goodocom.gocsdkfinal.db.Database;
import com.goodocom.gocsdkfinal.domain.ContactInfo;
import com.goodocom.gocsdkfinal.service.GocsdkCallbackImp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract.Contacts.Data;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class FragmentMailList extends Fragment {
	public final static int MSG_PHONE_BOOK = 1;// 更新联系人
	public final static int MSG_PHONE_BOOK_DONE = 2;// 更新联系人结束
	public final static int MSG_CURRENT_DEVICE_ADDRESS = 3;
	private MainActivity activity;
	private ListView lv_content;
	private SimpleAdapter simpleAdapter;
	private List<Map<String, String>> contacts = new ArrayList<Map<String, String>>();
	private String address;

	private void saveDeviceAddress(String address) {
		SharedPreferences sp = activity.getSharedPreferences("config",
				activity.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("address", address);
		editor.commit();
	};

	private String getDeviceAddress() {
		SharedPreferences sp = activity.getSharedPreferences("config",
				activity.MODE_PRIVATE);
		String string = sp.getString("address", "");
		return string;
	}

	private SQLiteDatabase systemDb;

	// 静态内部类
	public static class phoneBook {
		public String name = null;
		public String num = null;
	}

	public static Handler hand = null;

	public static Handler getHandler() {
		return hand;
	}

	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_PHONE_BOOK:
				if (contacts.size() <= 4000) {
					phoneBook phoBooks = (phoneBook) msg.obj;
					Map<String, String> phoBook = new HashMap<String, String>();
					phoBook.put("itemName", phoBooks.name);
					phoBook.put("itemNum", phoBooks.num);
					contacts.add(phoBook);
					// 更改联系人个数
					simpleAdapter.notifyDataSetChanged();

					// 如果该数据库不为空，就给该数据库的"phonebook"这张表，插入一条数据（姓名，号码）
					// System.out.println("handler" + phoBooks.name +
					// phoBooks.num);
					if (systemDb != null) {
						Database.createTable(systemDb, Database.Sql_create_phonebook_tab);
						Database.insert_phonebook(systemDb,
								Database.PhoneBookTable, phoBooks.name,
								phoBooks.num);
					}
				}
				break;
			case MSG_PHONE_BOOK_DONE: {
				// 如果该数据库不为空，关闭该数据库，并赋值为null
				rl_downloading.setVisibility(View.GONE);
				lv_content.setVisibility(View.VISIBLE);
				if (systemDb != null) {
					systemDb.close(); // close database
					systemDb = null;
				}
			}
				break;
			case MSG_CURRENT_DEVICE_ADDRESS:
				address = (String) msg.obj;
				String deviceAddress = getDeviceAddress();
				if (!deviceAddress.equals(address) && contacts.size() > 0
						&& simpleAdapter != null) {
					contacts.clear();
					simpleAdapter.notifyDataSetChanged();
				}
				saveDeviceAddress(address);
				break;
			}
		}
	};
	private ImageView image_animation;
	private RelativeLayout rl_downloading;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		InitData();
		View view = View.inflate(activity, R.layout.fragmentmaillist, null);
		lv_content = (ListView) view.findViewById(R.id.lv_content);
		lv_content.setSelector(R.drawable.contact_list_item_selector);
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

		simpleAdapter = new SimpleAdapter(activity, contacts,
				R.layout.contacts_listview_item, new String[] { "itemName",
						"itemNum" }, new int[] { R.id.tv_name, R.id.tv_number });
		lv_content.setAdapter(simpleAdapter);
		lv_content.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (GocsdkCallbackImp.hfpStatus > 0) {
					clickItemCallPhone(position);
				} else {
					Toast.makeText(activity, activity.getString(R.string.warning_connect), Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
		hand = handler;
		return view;
	}
	private void clickItemCallPhone(int position) {
		// 获得点击联系人的信息
		HashMap<String, String> People = (HashMap<String, String>) simpleAdapter
				.getItem(position);
		callOut(People.get("itemNum"));
		/*String Name = People.get("itemName");
		String Num = People.get("itemNum");
		System.out.println("Name=" + Name + "----Num" + Num);
		// 并将该联系人信息存到当前内部类对象中
		phoneBook phobook = new phoneBook();
		phobook.name = Name;
		phobook.num = Num;

		// 获得主界面的handler，并给它发送消息，让主界面弹出提示是否拨打电话的对话框
		Handler handler = MainActivity.getHandler();
		if (handler != null) {
			handler.sendMessage(handler.obtainMessage(
					MainActivity.MSG_DIAL_DIALOG, phobook));
		}*/
	}
	private void InitData() {
		if(DBG){
			dbgLoadData();
			return;
		}
		systemDb = Database.getSystemDb();
		if (GocsdkCallbackImp.hfpStatus > 0) {
			reflashContactsData();
		} else {
			List<ContactInfo> contactInfos = Database.queryAllContact(systemDb);
			Map<String, String> map = null;
			for (int i = 0; i < contactInfos.size(); i++) {
				ContactInfo contactInfo = contactInfos.get(i);
				map = new HashMap<String, String>();
				map.put("itemName", contactInfo.name);
				map.put("itemNum", contactInfo.number);
				contacts.add(map);
			}
		}
	}

	private void reflashContactsData() {
		try {
			Handler mainActivityHandler = MainActivity.getHandler();
			if (mainActivityHandler == null) {
				return;
			}
			mainActivityHandler
					.sendEmptyMessage(MainActivity.MSG_UPDATE_PHONEBOOK);
			// 判断联系人列表是否为空，不为空时清空它。
			if (contacts.isEmpty() == false) {
				contacts.clear();
			}
			// 联系人列表下载
			MainActivity.getService().phoneBookStartUpdate();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void callOut(String phoneNumber2) {
		placeCall(phoneNumber2);
	}

	// 拨打正确的电话
	private static void placeCall(String mLastNumber) {
		if (mLastNumber.length() == 0)
			return;
		if (PhoneNumberUtils.isGlobalPhoneNumber(mLastNumber)) {
			if (mLastNumber == null || !TextUtils.isGraphic(mLastNumber)) {
				return;
			}
			try {
				MainActivity.getService().phoneDail(mLastNumber);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static final boolean DBG = false;
	private void dbgLoadData(){
		HashMap<String, String> map = new HashMap<String, String>();
		for(int i=0;i<10;i++){
			map.put("itemName", "name"+i);
			map.put("itemNum", "12345678"+i);
			contacts.add(map);
		}	
		handler.postDelayed(new Runnable(){
			@Override
			public void run(){
				simpleAdapter.notifyDataSetChanged();
				handler.sendEmptyMessage(MSG_PHONE_BOOK_DONE);
			}
		},2000);
	}
}
