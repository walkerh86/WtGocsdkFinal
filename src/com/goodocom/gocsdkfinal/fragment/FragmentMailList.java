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
import android.util.Log;
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
	public final static int MSG_PHONE_BOOK = 1;// 鏇存柊鑱旂郴浜�
	public final static int MSG_PHONE_BOOK_DONE = 2;// 鏇存柊鑱旂郴浜虹粨鏉�
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

	// 闈欐�佸唴閮ㄧ被
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
					// 鏇存敼鑱旂郴浜轰釜鏁�
					simpleAdapter.notifyDataSetChanged();

					// 濡傛灉璇ユ暟鎹簱涓嶄负绌猴紝灏辩粰璇ユ暟鎹簱鐨�"phonebook"杩欏紶琛紝鎻掑叆涓�鏉℃暟鎹紙濮撳悕锛屽彿鐮侊級
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
				// 濡傛灉璇ユ暟鎹簱涓嶄负绌猴紝鍏抽棴璇ユ暟鎹簱锛屽苟璧嬪�间负null
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
		//load from db
		if(contacts.size() > 0){
			rl_downloading.setVisibility(View.GONE);
			lv_content.setVisibility(View.VISIBLE);
		}
		return view;
	}
	private void clickItemCallPhone(int position) {
		// 鑾峰緱鐐瑰嚮鑱旂郴浜虹殑淇℃伅
		HashMap<String, String> People = (HashMap<String, String>) simpleAdapter
				.getItem(position);
		callOut(People.get("itemNum"));
		/*String Name = People.get("itemName");
		String Num = People.get("itemNum");
		System.out.println("Name=" + Name + "----Num" + Num);
		// 骞跺皢璇ヨ仈绯讳汉淇℃伅瀛樺埌褰撳墠鍐呴儴绫诲璞′腑
		phoneBook phobook = new phoneBook();
		phobook.name = Name;
		phobook.num = Num;

		// 鑾峰緱涓荤晫闈㈢殑handler锛屽苟缁欏畠鍙戦�佹秷鎭紝璁╀富鐣岄潰寮瑰嚭鎻愮ず鏄惁鎷ㄦ墦鐢佃瘽鐨勫璇濇
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
		Log.i("hcj.serial", "contactList hfpStatus="+GocsdkCallbackImp.hfpStatus);
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
		//try {
			Handler mainActivityHandler = MainActivity.getHandler();
			if (mainActivityHandler == null) {
				return;
			}
			mainActivityHandler
					.sendEmptyMessage(MainActivity.MSG_UPDATE_PHONEBOOK);
			// 鍒ゆ柇鑱旂郴浜哄垪琛ㄦ槸鍚︿负绌猴紝涓嶄负绌烘椂娓呯┖瀹冦��
			if (contacts.isEmpty() == false) {
				contacts.clear();
			}
			// 鑱旂郴浜哄垪琛ㄤ笅杞�
			MainActivity.getService().phoneBookStartUpdate();
		//} catch (RemoteException e) {
			//e.printStackTrace();
		//}
	}
	
	private void callOut(String phoneNumber2) {
		placeCall(phoneNumber2);
	}

	// 鎷ㄦ墦姝ｇ‘鐨勭數璇�
	private static void placeCall(String mLastNumber) {
		if (mLastNumber.length() == 0)
			return;
		if (PhoneNumberUtils.isGlobalPhoneNumber(mLastNumber)) {
			if (mLastNumber == null || !TextUtils.isGraphic(mLastNumber)) {
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
