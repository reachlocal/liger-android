package com.reachlocal.mobile.liger.test;

import com.reachlocal.mobile.liger.utils.JSUtils;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Mark Wagner on 1/21/15.
 */
public class JSUtilsTest extends TestCase {


    public void testCleanJSString() {
        String returnValue = JSUtils.cleanJSString("{\"foo\": \"bar\"}");
        assertNotNull(returnValue);

        returnValue = JSUtils.cleanJSString("undefined");
        assertNull(returnValue);

    }

    public void testStringListToArgString() throws JSONException {
        // Regular Strings
        String output = JSUtils.stringListToArgString("foo", "bar", "baz");
        assertEquals(output, "\"foo\", \"bar\", \"baz\"");

        // JSONObject string and regular string
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("foo", "bar");
        jsonObject.put("baz", 1);
        output = JSUtils.stringListToArgString(jsonObject.toString(), "otherString");
        assertEquals(output, "{\"baz\":1,\"foo\":\"bar\"}, \"otherString\"");

        // JSONArray string and regular string
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObject);
        jsonArray.put("arrayString");
        output = JSUtils.stringListToArgString(jsonArray.toString(), "otherString");
        assertEquals(output, "[{\"baz\":1,\"foo\":\"bar\"},\"arrayString\"], \"otherString\"");

    }


}
