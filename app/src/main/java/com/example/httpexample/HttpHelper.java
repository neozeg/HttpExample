package com.example.httpexample;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/6/23.
 */
public class HttpHelper {
    public final static String ACTION_RESPONSE_MSG = "action.httphelper.response.message";
    public final static String EXTRA_RESPONSE_MSG = "extra.HttpHelper.response.message";
    public final static String HTTP_URL_PATTERN = "((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?|(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?";
    private final static String HTTP_URL_PATTERN_SHORT = "(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?|(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?";
    private final static String HTTP_INIT_PATTERN = "((http|ftp|https)://)";
    private final static String SEARCH_URL = "https://www.baidu.com/s?ie=UTF-8&wd=";
    private final static String DEFAULT_USERNAME = "test";
    private final static String DEFAULT_PASSWORD =  "123";

    private final static String TAG = "HttpHelper";

    private Context mContext;

    public HttpHelper(Context context){
        mContext = context;
    }

    private HttpClient createHttpClient(){
        HttpParams mDefaultHttpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(mDefaultHttpParams,15000);
        HttpConnectionParams.setSoTimeout(mDefaultHttpParams,15000);
        HttpConnectionParams.setTcpNoDelay(mDefaultHttpParams,true);
        HttpProtocolParams.setVersion(mDefaultHttpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(mDefaultHttpParams, HTTP.UTF_8);
        HttpProtocolParams.setUseExpectContinue(mDefaultHttpParams,true);
        HttpClient mHttpClient = new DefaultHttpClient(mDefaultHttpParams);
        return  mHttpClient;
    }

    public String useHttpClientGet(String url){
        HttpGet mHttpGet = new HttpGet(url);
        mHttpGet.addHeader("Connection","Keep-Alive");
        String resposeString = "";
        try {
            HttpClient mHttpClient = createHttpClient();
            HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
            HttpEntity mHttpEntity = mHttpResponse.getEntity();
            int code = mHttpResponse.getStatusLine().getStatusCode();
            if(mHttpEntity != null){
                InputStream mInputStream = mHttpEntity.getContent();
                String reponse = convertStreamToString(mInputStream);
                //Log.i(TAG,"Request code: " + code + "\nRequest response:\n" + reponse);
                resposeString += "Request code: " + code + "\nRequest response:\n" + reponse;
                mInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  resposeString;
    }

    private String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        String line = null;
        while ((line = reader.readLine()) != null){
            sb.append(line + "\n");
            broadCastResponse(ACTION_RESPONSE_MSG,line);
        }
        String response = sb.toString();
        return response;
    }

    public String useHttpClientPost(String url){
        HttpPost mHttpPost = new HttpPost(url);
        mHttpPost.addHeader("Connection","Keep-Alive");
        HttpClient mHttpClient = createHttpClient();
        String resposeString = "";
        try {
            List<NameValuePair> postParams = new ArrayList<NameValuePair>();
            postParams.add(new BasicNameValuePair("username",DEFAULT_USERNAME));
            postParams.add(new BasicNameValuePair("password",DEFAULT_PASSWORD));
            mHttpPost.setEntity(new UrlEncodedFormEntity(postParams));
            HttpResponse mHttpResponse = mHttpClient.execute(mHttpPost);
            HttpEntity mHttpEntity = mHttpResponse.getEntity();
            int code = mHttpResponse.getStatusLine().getStatusCode();
            if(mHttpEntity != null){
                InputStream mInputStream = mHttpEntity.getContent();
                String reponse = convertStreamToString(mInputStream);
                //Log.i(TAG,"Request code: " + code + "\nRequest response:\n" + reponse);
                resposeString += "Request code: " + code + "\nRequest response:\n" + reponse;
                mInputStream.close();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return resposeString;
    }

    private void broadCastResponse(String action,String responseMsg){
        //Log.i(TAG,action + "  " +responseMsg);
        Intent intent = new Intent();
        intent.setAction(ACTION_RESPONSE_MSG);
        intent.putExtra(EXTRA_RESPONSE_MSG,responseMsg);
        mContext.sendBroadcast(intent);
    }

    public static String getUrl(String input){
        String url = "";
        String url_init = "";

        Pattern pattenLink = Pattern.compile(HTTP_URL_PATTERN_SHORT);
        Pattern pattenInit = Pattern.compile(HTTP_INIT_PATTERN);
        Matcher matcherLink = pattenLink.matcher(input);
        Matcher matcherInit = pattenInit.matcher(input);
        if(matcherInit.find()){
            url_init = input.substring(matcherInit.start(),matcherInit.end()).toString();
        }else{
            url_init = "http://";
        }
        if(matcherLink.find()){
            url = input.substring(matcherLink.start(),matcherLink.end()).toString();
            url = url_init + url;
        }else{
            url = input;
            url = url.replace(" ","%20");
            url = SEARCH_URL + url;
        }
        Pattern patternCRLF = Pattern.compile("\n|\r|\t");
        Matcher matcherCRLF = patternCRLF.matcher(url);
        url = matcherCRLF.replaceAll("");
        return url;
    }
}
