/**
 * 
 */
package com.halong.macauctshotel.book;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.halong.macauctshotel.LoginActivity;
import com.halong.macauctshotel.ProvisionActivity;
import com.halong.macauctshotel.R;
import com.halong.macauctshotel.common.BackActivity;
import com.halong.macauctshotel.common.SaveInfoUtil;
import com.halong.macauctshotel.entity.soap.Gres;
import com.halong.macauctshotel.entity.soap.rmtype.RateCode;
import com.halong.macauctshotel.entity.soap.rmtype.RmType;
import com.halong.macauctshotel.util.AppStackManager;
import com.halong.macauctshotel.util.SharedPreferencesHelper;
import com.halong.macauctshotel.wcf.AsyncAPIClient;
import com.halong.macauctshotel.wcf.AsyncAPIClient;
import com.halong.macauctshotel.widget.HtmlImageGetter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 
 */
public class HotelBookActivity extends BackActivity {
	private Gres mGres;

	private CheckBox checkBox;
	private Button button;

	private TextView scrollTextView;
	private TextView textView, textView2, textView3, textView4, textView5;
	private ImageView imageView;
	private RadioGroup radioGroup;
	private LinearLayout linearLayout;

	private ImageLoader mImageLoader;
	private DisplayImageOptions mOptions;

	private int currentInt = -1;
	private RateCode currentRateCode;
	private RmType rmType;
	private String hotelName;

	private String hotelPhoto;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		AppStackManager.getMyAppStackManager().addActivity(this);

		mGres = (Gres) getIntent().getSerializableExtra("mGres");
		rmType = (RmType) getIntent().getSerializableExtra("rmType");
		hotelName = getIntent().getStringExtra("name");
		hotelPhoto = getIntent().getStringExtra("photo");
		mImageLoader = ImageLoader.getInstance();
		this.mOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true).build();

		setContentView(R.layout.activity_hotel_book);
		button = (Button) findViewById(R.id.OK);
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		textView = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);
		textView5 = (TextView) findViewById(R.id.textView5);
		imageView = (ImageView) findViewById(R.id.imageView);
		linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

		mImageLoader.displayImage(AsyncAPIClient.PICTURE_URL + rmType.getPhoto1(), imageView, mOptions);

		textView4.setText(rmType.getName());

		if ("".equals(rmType.getDescript())) {
			textView.setVisibility(View.GONE);
		} else {
			Drawable defaultDrawable = getResources().getDrawable(R.drawable.image_loading_03);
			HtmlImageGetter htmlImageGetter = new HtmlImageGetter(HotelBookActivity.this, textView, defaultDrawable, AsyncAPIClient.PICTURE_URL_HTML);
			textView.setText(Html.fromHtml(rmType.getDescript(), htmlImageGetter, null));
		}

		initRadioGroup();
		checkBox = (CheckBox) findViewById(R.id.rtp);

		textView5.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(HotelBookActivity.this, ProvisionActivity.class);
				intent.putExtra("num", ProvisionActivity.PAY);
				startActivity(intent);
			}
		});
		drawableTofile();
	}

	private void initRadioGroup() {
		// TODO Auto-generated method stub
		//???????????????????????????radiobutton
		final List<RateCode> rateCodeList = rmType.getRateCodeList();
		for (int i = 0; i < rateCodeList.size(); i++) {
			RadioButton radiobutton = (RadioButton) getLayoutInflater().inflate(R.layout.radiobutton_hotel_book, null);
			radiobutton.setPadding(0, 0, 0, 0); // ???????????????????????????????????????
			radiobutton.setText(rateCodeList.get(i).getName());
			radiobutton.setId(i);
			radioGroup.addView(radiobutton);
		}

		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				linearLayout.setVisibility(View.VISIBLE);
				currentInt = checkedId;
				currentRateCode = rateCodeList.get(checkedId);
				textView2.setText(getResources().getString(R.string.total) + currentRateCode.getTotalRate() + currentRateCode.getCurrencyName());
				if (!"".equals(currentRateCode.getRemarks().trim())) {
					textView3.setVisibility(View.VISIBLE);
					textView3.setText(getResources().getString(R.string.remark2) + currentRateCode.getRemarks());
				} else {
					textView3.setVisibility(View.GONE);
				}
			}
		});
	}

	public void toNextActivity(View view) {
		if (checkBox.isChecked()) {
			Toast.makeText(this, getResources().getString(R.string.clause_toast), Toast.LENGTH_SHORT).show();
			return;
		}
		if (currentInt == -1) {
			Toast.makeText(this, "?????????", Toast.LENGTH_SHORT).show();
			return;
		}

		Intent intent = null;
		if (SaveInfoUtil.isLogin(this)) {
			intent = new Intent(this, HotelBookFragmentActivity.class);
			Bundle bundle = new Bundle();
			mGres.setRateCode(currentRateCode.getRateCode());
			mGres.setPrice(currentRateCode.getTotalRate());
			mGres.setCurrencyname(currentRateCode.getCurrencyName());
			mGres.setCustId(SharedPreferencesHelper.getLongValue(HotelBookActivity.this, SaveInfoUtil.CUSTID_KEY_STRING));
			mGres.setCardNo(SharedPreferencesHelper.getStringValue(HotelBookActivity.this, SaveInfoUtil.CARDNO_KEY_STRING));
			bundle.putSerializable("mGres", mGres);
			intent.putExtras(bundle);
			startActivity(intent);
		} else {
			intent = new Intent(this, LoginActivity.class);
			startActivityForResult(intent, LoginActivity.LOGINSUCCESS);
		}
	}

	// public void scrollTextView() {
	// scrollTextView = (TextView) findViewById(R.id.scrollTextView);
	// scrollTextView.requestFocus();
	// }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == LoginActivity.LOGINSUCCESS) {
			if (resultCode == LoginActivity.LOGINSUCCESS) {
				toNextActivity(null);
			}
		}
	}

	/**
	 * QQ????????????????????????????????????????????????????????????
	 * ?????????????????????????????????????????????
	 */
	public void drawableTofile() {
		// Log.i(TAG, "drawableToFile:"+path);
		String path = Environment.getExternalStorageDirectory() + "/CtsHotel/share.jpg";
		Drawable drawable = getResources().getDrawable(R.drawable.share);
		File file = new File(path);
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 100 /* ignored for PNG */, bos);
		byte[] bitmapdata = bos.toByteArray();

		// write the bytes in file
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			fos.write(bitmapdata);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	public void shareOthers(View view) {
		ShareSDK.initSDK(this);
		OnekeyShare oks = new OnekeyShare();
		// ??????sso??????
		oks.disableSSOWhenAuthorize();

		// ?????????Notification??????????????????
		oks.setNotification(R.drawable.icon_app, getString(R.string.app_name));
		// title???????????????????????????????????????????????????????????????QQ????????????
		oks.setTitle(getString(R.string.app_name));
		// titleUrl?????????????????????????????????????????????QQ????????????
		oks.setTitleUrl("http://www.macauctshotel.com");
		// text???????????????????????????????????????????????????
		oks.setText("????????????" + hotelName + "?????????" + rmType.getName() + "???????????????????????????");
		// imagePath???????????????????????????Linked-In?????????????????????????????????
		oks.setImagePath(Environment.getExternalStorageDirectory() + "/CtsHotel/share.jpg");
		// url???????????????????????????????????????????????????
		oks.setUrl("http://www.macauctshotel.com");
		// comment???????????????????????????????????????????????????QQ????????????
		oks.setComment("");
		// site??????????????????????????????????????????QQ????????????
		oks.setSite(getString(R.string.app_name));
		// siteUrl??????????????????????????????????????????QQ????????????
		oks.setSiteUrl("http://www.macauctshotel.com");

		// ????????????GUI
		oks.show(this);
	}

}
