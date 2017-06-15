package com.goodocom.gocsdkfinal.db;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import com.goodocom.gocsdkfinal.domain.CallLogInfo;
import com.goodocom.gocsdkfinal.domain.ContactInfo;



import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Database {

	private static String mDbPath = "/data/data/com.goodocom.gocsdkfinal/BtPhone.db";
	public static String Sql_create_phonebook_tab = "create table if not exists phonebook(_id integer primary key autoincrement,phonename text,phonenumber text)";
	public static String Sql_create_calllog_tab = "create table if not exists calllog(_id integer primary key autoincrement,phonename text,phonenumber text,calltype integer)";
	public static String PhoneBookTable = "phonebook";
	public static String CallLogTable = "calllog";
	


	// get the database
	public static SQLiteDatabase getSystemDb() {

		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(mDbPath, null);
		return db;
	}

	// create table
	public static void createTable(SQLiteDatabase db, String Sql_create_tab_String) {
		// 执行SQL语句
		db.execSQL(Sql_create_tab_String);
	}
	/**
	 * 联系人表
	 * @param db
	 * @param Table_name
	 * @param Phone_name
	 * @param Phone_num
	 */
	// 向联系人表中插入一条数据
	public static void insert_phonebook(SQLiteDatabase db, String Table_name,
			String Phone_name, String Phone_num) {
		// 实例化常量值
		ContentValues cValue = new ContentValues();
		cValue.put("phonename", Phone_name);
		cValue.put("phonenumber", Phone_num);
		db.insert(Table_name, null, cValue);
	}
	
	
	public static List<ContactInfo> queryAllContact(SQLiteDatabase db){
		createTable(db,Sql_create_phonebook_tab);
		Cursor cursor = db.query(PhoneBookTable, new String[] { "phonename",
				"phonenumber" }, null, null, null, null, null);
		List<ContactInfo> contacts = new ArrayList<ContactInfo>();
		ContactInfo contactInfo = null;
		while(cursor.moveToNext()){
			contactInfo = new ContactInfo();
			contactInfo.name = cursor.getString(cursor.getColumnIndex("phonenumber"));
			contactInfo.number = cursor.getString(cursor.getColumnIndex("phonename"));
			contacts.add(contactInfo);
		}
		cursor.close();
		return contacts;
	}
	// 通过号码，找联系人姓名
	public static String queryPhoneName(SQLiteDatabase db, String Table_name,
			String Phone_num) {
		// 查询获得游标
		// 参数1：表名
		// 参数2：要想显示的列
		// 参数3：where子句
		// 参数4：where子句对应的条件值
		// 参数5：分组方式
		// 参数6：having条件
		// 参数7：排序方式
		String Phonename;
		String Phonenum;
		Cursor cursor = db.query(Table_name, new String[] { "phonename",
				"phonenumber" }, "phonenumber=?", new String[] { Phone_num },
				null, null, null); // "ORDEY BY ASC"

		//int count = cursor.getCount();

		while (cursor.moveToNext()) {
			Phonenum = cursor.getString(cursor.getColumnIndex("phonenumber"));

			Phonename = cursor.getString(cursor.getColumnIndex("phonename"));
			return Phonename;
		}
		cursor.close();
		return null;
	}

	
	//删除表中的所有数据
	public static void delete_table_data(SQLiteDatabase db, String Table_name) {
		// 删除SQL语句
		String sql = "delete from " + Table_name;
		// 执行SQL语句
		db.execSQL(sql);
	}

	// 删除一张表
	static void drop(SQLiteDatabase db, String Table_name) {
		// 删除表的SQL语句
		String sql = "DROP TABLE " + Table_name;
		// 执行SQL
		db.execSQL(sql);
	}
	
	
	
	/**
	 * 通话记录表
	 * @param db
	 * @param Table_name
	 * @param Phone_name
	 * @param Phone_num
	 * @param calltype
	 * @param time
	 */
	// 向通话记录表中插入一条数据
		public static void insert_calllog(SQLiteDatabase db, String Table_name,
				String Phone_name, String Phone_num, int calltype) {
			ContentValues cValue = new ContentValues();
			cValue.put("phonename", Phone_name);
			cValue.put("phonenumber", Phone_num);
			cValue.put("calltype", calltype);
			db.insert(Table_name, null, cValue);
		}
		
		//根据通话类型，查询通话记录中的数据,返回回来
		public static List<CallLogInfo> queryCallLog(SQLiteDatabase db, int CallType) {
			// 查询获得游标
			// 参数1：表名
			// 参数2：要想显示的列
			// 参数3：where子句
			// 参数4：where子句对应的条件值
			// 参数5：分组方式
			// 参数6：having条件
			// 参数7：排序方式
			List<CallLogInfo> calllogs = new ArrayList<CallLogInfo>();
			CallLogInfo callloginfo =null;
			String Phonename;
			String Phonenum;
			String time;
			Cursor cursor = db.query(CallLogTable, new String[] { "phonename",
					"phonenumber"}, "calltype=?",
					new String[] { CallType+"" }, null, null, null); // "ORDEY BY ASC"

			int count = cursor.getCount();

			while (cursor.moveToNext()) {
				Phonenum = cursor.getString(cursor.getColumnIndex("phonenumber"));
				Phonename = cursor.getString(cursor.getColumnIndex("phonename"));
				//time = cursor.getString(cursor.getColumnIndex("time"));
				
				/*//时间转化
				SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex("time"))));
				//呼叫时间
				time = sfd.format(date); */
				
				callloginfo = new CallLogInfo();
				callloginfo.phonename = Phonename;
				callloginfo.phonenumber = Phonenum;
				callloginfo.calltype = CallType;
				
				calllogs.add(callloginfo);
			}
			cursor.close();
			return calllogs;
		}
}
