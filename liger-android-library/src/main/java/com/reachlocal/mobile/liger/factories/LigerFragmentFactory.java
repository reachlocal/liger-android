package com.reachlocal.mobile.liger.factories;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.reachlocal.mobile.liger.ui.CordovaPageFragment;
import com.reachlocal.mobile.liger.ui.DefaultMainActivity;
import com.reachlocal.mobile.liger.ui.LigerAppMenuFragment;
import com.reachlocal.mobile.liger.ui.LigerDrawerFragment;
import com.reachlocal.mobile.liger.ui.LigerNavigatorFragment;
import com.reachlocal.mobile.liger.ui.PageFragment;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mark Wagner on 10/22/14.
 */
public class LigerFragmentFactory {

    static final int REQUEST_IMAGE_GET = 1;

    private DefaultMainActivity mDefaultMainActivity;

    public static Context mContext = null;

    // Intents to support: browser, email, message (SMS), image, twitter, facebook, sina weibo, tencent weibo
    private final static String[] SUPPORTED_INTENTS = {"email", "browser", "message", "image", "twitter", "facebook", "sinaweibo", "tencentweibo"};

    public static PageFragment openPage(String pageName, String title, JSONObject pageArgs, JSONObject pageOptions) {
        PageFragment returnFragment = null;

        if (ArrayUtils.contains(SUPPORTED_INTENTS, pageName) && mContext != null) {
            Intent intent = null;
            if (pageName.equalsIgnoreCase("email")) {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("mailto:"));
                mContext.startActivity(Intent.createChooser(intent, "Send email..."));
            } else if (pageName.equalsIgnoreCase("browser")) {
                String url = "http://www.google.com";
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                mContext.startActivity(Intent.createChooser(intent, "Open Browser..."));
            } else if (pageName.equalsIgnoreCase("message")) {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("sms:"));
                mContext.startActivity(Intent.createChooser(intent, "Send message..."));
            } else if (pageName.equalsIgnoreCase("image")) {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                    ((Activity) mContext).startActivityForResult(intent, REQUEST_IMAGE_GET);
                }
            } else if (pageName.equalsIgnoreCase("twitter")) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com"));
                mContext.startActivity(Intent.createChooser(intent, "Open Twitter..."));
            } else if (pageName.equalsIgnoreCase("facebook")) {
                try {
                    mContext.getPackageManager().getPackageInfo("com.facebook.katana", 0);
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://root"));
                } catch (Exception e) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/"));
                }
                mContext.startActivity(Intent.createChooser(intent, "Open Facebook..."));
            } else if (pageName.equalsIgnoreCase("sinaweibo")) {
                try {
                    mContext.getPackageManager().getPackageInfo("com.sina.weibo", 0);
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://root"));
                } catch (Exception e) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://weibo.com/"));
                }
                mContext.startActivity(Intent.createChooser(intent, "Open Sina Weibo..."));
            } else if (pageName.equalsIgnoreCase("tencentweibo")) {
                try {
                    mContext.getPackageManager().getPackageInfo("com.tencent.weibo", 0);
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://root"));
                } catch (Exception e) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://t.qq.com/"));
                }
                mContext.startActivity(Intent.createChooser(intent, "Open Tencent Weibo..."));
            }
        } else {

            if (pageName.equalsIgnoreCase("drawer")) {
                returnFragment = LigerDrawerFragment.build(pageName, title, pageArgs, pageOptions);
            } else if (pageName.equalsIgnoreCase("navigator")) {
                returnFragment = LigerNavigatorFragment.build(pageName, title, pageArgs, pageOptions);
            } else if (pageName.equalsIgnoreCase("appMenu")) {
                returnFragment = LigerAppMenuFragment.build(pageName, title, pageArgs, pageOptions);
            } else {
                try {
                    String packageName =  mContext.getApplicationContext().getPackageName();
                    packageName = packageName.replace(".debug", "").replace(".alpha", "").replace(".beta", "");
                    returnFragment = (PageFragment) Class.forName(packageName + "." + pageName).newInstance();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                } catch (InstantiationException e) {
                } catch (IllegalAccessException e) {
                }
                if (returnFragment == null)
                    returnFragment = CordovaPageFragment.build(pageName, title, pageArgs, pageOptions);
            }
        }

        return returnFragment;
    }

    public static PageFragment openPage(JSONObject pageObject) {
        PageFragment returnFragment = null;
        try {
            String name = pageObject.getString("page");
            String title = pageObject.optString("title");
            JSONObject args = pageObject.optJSONObject("args");
            JSONObject options = pageObject.optJSONObject("options");
            returnFragment = openPage(name, title, args, options);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return returnFragment;
    }
}
