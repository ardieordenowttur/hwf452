/**
 * 
 */
package com.halong.macauctshotel.setting;

import java.io.File;

import javax.security.auth.PrivateCredentialPermission;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.halong.macauctshotel.R;
import com.halong.macauctshotel.common.BackActivity;
import com.halong.macauctshotel.ver.UpdateService;
import com.halong.macauctshotel.wcf.AsyncAPIClient;
import com.halong.macauctshotel.wcf.AsyncAPIClient;
import com.halong.macauctshotel.wcf.OnReturnListener;
import com.halong.macauctshotel.wcf.ParseJson;

public class AboutActivity extends BackActivity implements OnClickListener {

	private LinearLayout nameLayout;
	private TextView mVerTextView;

	private String verdata;
	private String content;
	private String ver;
	private String appVer;
	private File file;
	private String savePath;
	private String downPath;
	private Dialog mDialog;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		nameLayout = (LinearLayout) findViewById(R.id.nameLayout);
		mVerTextView = (TextView) findViewById(R.id.tv_ver);
		mVerTextView.setText(getVersionName());
		nameLayout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.nameLayout:
			Update();
			break;

		default:
			break;
		}
	}

	private void Update() {
		AsyncAPIClient asyncAPIClient = new AsyncAPIClient(this);
		asyncAPIClient.setOnReturnListener(new OnReturnListener() {
			@Override
			public void onSuccess(String content) {
				// TODO Auto-generated method stub
				super.onSuccess(content);
				boolean result = ParseJson.getInstance().updateVer(AboutActivity.this, content);
				if (result) {
					try {
						JSONObject jsonObject = new JSONObject(content);
						JSONObject object = jsonObject.getJSONObject("result");
						verdata = object.getString("img");
						AboutActivity.this.content = object.getString("info");
						ver = object.getString("abst");
						downPath = AsyncAPIClient.UPDATEVER_URL_HALONG + verdata;
						appVer = getVersionName();
						checkVersion();
						return;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		});
		asyncAPIClient.updateVer();
	}

	/*
	 * ??????????????????????????????
	 */
	private String getVersionName() {
		try {
			// ??????packagemanager?????????
			PackageManager packageManager = getPackageManager();
			// getPackageName()???????????????????????????0???????????????????????????
			PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			return packInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	/***
	 * ????????????????????????
	 */
	public void checkVersion() {
		Log.v("checkVersion", ver + "-" + appVer);
		if (!ver.equals(appVer)) {
			// ????????????????????????????????????
			mDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
			mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			mDialog.setContentView(R.layout.alert_dialog_hint);

			((TextView) mDialog.findViewById(R.id.textView1)).setText(getResources().getString(R.string.software_update));
			((TextView) mDialog.findViewById(R.id.textView2)).setText(Html.fromHtml(content));
			((Button) mDialog.findViewById(R.id.button)).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					// ??????????????????UpdateService
					// ???????????????update?????????????????????????????????updateService????????????
					// ?????????ID?????????ID????????????????????????,?????????app_name??????
					Intent updateIntent = new Intent(AboutActivity.this, UpdateService.class);
					updateIntent.putExtra("app_name", verdata);
					updateIntent.putExtra("down_url", AsyncAPIClient.UPDATEVER_URL_HALONG + verdata);
					startService(updateIntent);
					mDialog.dismiss();
				}
			});
			((Button) mDialog.findViewById(R.id.button1)).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mDialog.dismiss();
				}
			});
			mDialog.show();
		} else {
			Toast.makeText(AboutActivity.this, getResources().getString(R.string.new_version), Toast.LENGTH_SHORT).show();
		}
	}
}
