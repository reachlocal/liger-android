package com.reachlocal.mobile.liger;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Created by brad.marsh on 8/21/14.
 */
public class PageFactory {

    PageStackHelper mStackHelper;

    public PageFactory(PageStackHelper stackHelper) {
        mStackHelper = stackHelper;
    }

    public PageFragment openPage(String pageName, String title, JSONObject pageArgs, JSONObject pageOptions){
        PageFragment pageFrag = null;

        if (StringUtils.equalsIgnoreCase("email", pageName)) {
            final String[] recipients = StringUtils.split(pageArgs.optString("toRecipients"));
            final String[] ccRecipients = StringUtils.split(pageArgs.optString("ccRecipients"));
            final String[] bccRecipients = StringUtils.split(pageArgs.optString("bccRecipients"));
            final String subject = pageArgs.optString("subject");
            final DefaultMainActivity activity = mStackHelper.getActivity();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, recipients);
            intent.putExtra(Intent.EXTRA_CC, ccRecipients);
            intent.putExtra(Intent.EXTRA_BCC, bccRecipients);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);

            mStackHelper.getActivity().startActivity(Intent.createChooser(intent, "Send Email"));
        } else if (StringUtils.equalsIgnoreCase("maps", pageName)){
            final DefaultMainActivity activity = mStackHelper.getActivity();
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
            Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(pageArgs.optString("address")));
            intent.setData(uri);

            if(intent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivity(intent);
            }
        } else if (StringUtils.equalsIgnoreCase("browser", pageName)) {
            final DefaultMainActivity activity = mStackHelper.getActivity();
            String link = pageArgs.optString("link");
            if(link != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(link);
                intent.setData(uri);
                activity.startActivity(intent);
            }
        } else {

            pageFrag = CordovaPageFragment.build(pageName, title, pageArgs, pageOptions);
        }

        Log.d("PageFactory","Created page");

        return pageFrag;
    }
}
