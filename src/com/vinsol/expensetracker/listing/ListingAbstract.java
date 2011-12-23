package com.vinsol.expensetracker.listing;

import java.util.List;

import com.vinsol.expensetracker.DatabaseAdapter;
import com.vinsol.expensetracker.R;
import com.vinsol.expensetracker.edit.CameraActivity;
import com.vinsol.expensetracker.edit.TextEntry;
import com.vinsol.expensetracker.edit.Voice;
import com.vinsol.expensetracker.show.ShowCameraActivity;
import com.vinsol.expensetracker.show.ShowTextActivity;
import com.vinsol.expensetracker.show.ShowVoiceActivity;
import com.vinsol.expensetracker.helpers.CheckEntryComplete;
import com.vinsol.expensetracker.helpers.ConvertCursorToListString;
import com.vinsol.expensetracker.helpers.DisplayDate;
import com.vinsol.expensetracker.helpers.StringProcessing;
import com.vinsol.expensetracker.models.DisplayList;
import com.vinsol.expensetracker.models.ListDatetimeAmount;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

abstract class ListingAbstract extends Activity implements OnItemClickListener{

	protected List<ListDatetimeAmount> mDataDateList;
	protected SeparatedListAdapter mSeparatedListAdapter;
	protected List<DisplayList> mSubList;
	protected ConvertCursorToListString mConvertCursorToListString;
	protected StringProcessing mStringProcessing;
	protected ListView mListView;
	protected String highlightID = null;
	protected static int firstVisiblePosition;
	protected UnknownEntryDialog unknownDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.expense_listing);
		mConvertCursorToListString = new ConvertCursorToListString(this);
		mStringProcessing = new StringProcessing();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mSeparatedListAdapter = new SeparatedListAdapter(this);
	}
	
	protected DisplayList getListCurrentWeek(int j) {
		DisplayList templist = new DisplayList();
		templist.userId = mSubList.get(j).userId;
		if (mSubList.get(j).description != null && !mSubList.get(j).description.equals("")) {
			templist.description = mSubList.get(j).description;
		} else {
			CheckEntryComplete mCheckEntryComplete = new CheckEntryComplete();
			if (mSubList.get(j).type.equals(getString(R.string.camera))) {
				if(mCheckEntryComplete.isEntryComplete(mSubList.get(j),this)){
					templist.description = getString(R.string.finished_cameraentry);
				} else {
					templist.description = getString(R.string.unfinished_cameraentry);
				}
			} else if (mSubList.get(j).type.equals(getString(R.string.voice))) {
				if(mCheckEntryComplete.isEntryComplete(mSubList.get(j),this)){
					templist.description = getString(R.string.finished_voiceentry);
				} else {
					templist.description = getString(R.string.unfinished_voiceentry);
				}
			} else if (mSubList.get(j).type.equals(getString(R.string.text))) {
				if(mCheckEntryComplete.isEntryComplete(mSubList.get(j),this)){
					templist.description = getString(R.string.finished_textentry);
				} else {
					templist.description = getString(R.string.unfinished_textentry);
				}
			} else if (mSubList.get(j).type.equals(getString(R.string.favorite_entry))) {
				templist.description = "Unfinished Favorite Entry";
			} else if (mSubList.get(j).type.equals(getString(R.string.unknown))) {
				templist.description = getString(R.string.unknown_entry);
			}
		}

		if (mSubList.get(j).amount != null&& !mSubList.get(j).amount.equals("")) {
			templist.amount = mStringProcessing.getStringDoubleDecimal(mSubList.get(j).amount);
		} else {
			templist.amount = "?";
		}

		// ///// ******* Adding location date data to list ******* //////////

		if (mSubList.get(j).timeInMillis != null  && !mSubList.get(j).timeInMillis.equals("")) {
			templist.timeLocation = new DisplayDate().getLocationDate(mSubList.get(j).timeInMillis, mSubList.get(j).location);
		} else if ((mSubList.get(j).timeInMillis == null || mSubList.get(j).timeInMillis.equals(""))&& mSubList.get(j).location != null&& !mSubList.get(j).location.equals("")) {
			templist.timeLocation = "Unknown time at "+ mSubList.get(j).location;
		} else {
			templist.timeLocation = "Unknown time at Unknown Location";
		}

		if (mSubList.get(j).favorite != null && !mSubList.get(j).favorite.equals("")) {
			templist.favorite = mSubList.get(j).favorite;
		} else {
			templist.favorite = "";
		}

		if (mSubList.get(j).type != null && !mSubList.get(j).type.equals("")) {
			templist.type = mSubList.get(j).type;
		} else {
			templist.type = "";
		}

		templist.timeInMillis = mSubList.get(j).timeInMillis;
		templist.location = mSubList.get(j).location;
		return templist;
	}
	
	@Override
	public void onItemClick(final AdapterView<?> adapter, View v,final int position, long arg3) {
		final DisplayList mTempClickedList = (DisplayList) adapter.getItemAtPosition(position);
		String userId = mTempClickedList.userId;
		if (!userId.contains(",")) {
			Bundle bundle = new Bundle();
			bundle.putParcelable("mDisplayList", mTempClickedList);
			CheckEntryComplete mCheckEntryComplete = new CheckEntryComplete();
			if (mTempClickedList.type.equals(getString(R.string.camera))) {
				if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
					if(!mCheckEntryComplete.isEntryComplete(mTempClickedList,this)){
						Intent intentCamera = new Intent(this,CameraActivity.class);
						intentCamera.putExtra("cameraBundle", bundle);
						startActivity(intentCamera);
					} else {
						Intent intentCamera = new Intent(this,ShowCameraActivity.class);
						intentCamera.putExtra("cameraShowBundle", bundle);
						startActivity(intentCamera);
					}
				} else {
					Toast.makeText(this, "sdcard not available",Toast.LENGTH_SHORT).show();
				}
			} else if (mTempClickedList.type.equals(getString(R.string.text))) {
				if(!mCheckEntryComplete.isEntryComplete(mTempClickedList,this)){
					Intent intentTextEntry = new Intent(this, TextEntry.class);
					intentTextEntry.putExtra("textEntryBundle", bundle);
					startActivity(intentTextEntry);
				} else {
					Intent intentTextShow = new Intent(this,ShowTextActivity.class);
					intentTextShow.putExtra("textShowBundle", bundle);
					Log.v("mShowList pass", mTempClickedList.favorite+" "+mTempClickedList.userId+" "+mTempClickedList.timeInMillis+" "+mTempClickedList.location);
					startActivity(intentTextShow);
				}
			} else if (mTempClickedList.type.equals(getString(R.string.voice))) {
				if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
					if(!mCheckEntryComplete.isEntryComplete(mTempClickedList,this)){
						Intent intentVoice = new Intent(this, Voice.class);
						intentVoice.putExtra("voiceBundle", bundle);
						startActivity(intentVoice);
					} else {
						Intent intentVoiceShow = new Intent(this,ShowVoiceActivity.class);
						intentVoiceShow.putExtra("voiceShowBundle", bundle);
						startActivity(intentVoiceShow);
					}
				} else {
					Toast.makeText(this, "sdcard not available", Toast.LENGTH_SHORT).show();
				}
			} else if (mTempClickedList.type.equals(getString(R.string.unknown))) {
				unknownDialog = new UnknownEntryDialog(this,mTempClickedList,new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						DatabaseAdapter mDatabaseAdapter = new DatabaseAdapter(ListingAbstract.this);
						mDatabaseAdapter.open();
						mDatabaseAdapter.deleteDatabaseEntryID(mTempClickedList.userId);
						mDatabaseAdapter.close();
						unknownDialog.dismiss();
						unknownDialogAction(mTempClickedList.userId);
						Toast.makeText(ListingAbstract.this, "Deleted", Toast.LENGTH_SHORT).show();
					}
				});
			}
		} else {
			onClickElse(mTempClickedList.userId);
		}
	}
	
	protected void unknownDialogAction(String userId){}
	
	protected void onClickElse(String userId) {}
	
	protected void noItemButtonAction(Button noItemButton){}
	
	protected void getListToAdd(){}
	
	protected void doOperationsOnListview(){
		mListView = (ListView) findViewById(R.id.expense_listing_listview);
		mListView.setOnItemClickListener(this);
		mListView.setAdapter(mSeparatedListAdapter);
		if (mDataDateList.size() < 1) {
			mListView.setVisibility(View.GONE);
			RelativeLayout mRelativeLayout = (RelativeLayout) findViewById(R.id.expense_listing_listview_no_item);
			mRelativeLayout.setVisibility(View.VISIBLE);
			Button noItemButton = (Button) findViewById(R.id.expense_listing_listview_no_item_button);
			noItemButtonAction(noItemButton);
		}
		mListView.setSelection(firstVisiblePosition);
	}
}
