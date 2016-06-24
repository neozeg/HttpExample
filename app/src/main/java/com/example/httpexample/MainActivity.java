package com.example.httpexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.logging.LogRecord;

public class MainActivity extends Activity implements View.OnClickListener{
    private final static String TAG = "MainActivity";

    private final static int MSG_LIST_UPDATE = 111;


    private ListView mListView;
    private MyListAdapter mListAdapter;
    private ArrayList<String> mListContent;
    private EditText mEtUrl;
    private Button mBtn1,mBtn2,mBtn3,mBtn4,mBtnGo;

    private boolean mIsEtUrlUsed = true;

    private HttpHelper mHttpHelper;
    private ClipboardManager mClipboardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHttpHelper = new HttpHelper(getApplicationContext());
        mClipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        setupViewComponents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(HttpHelper.ACTION_RESPONSE_MSG)){
                String msgStr = intent.getStringExtra(HttpHelper.EXTRA_RESPONSE_MSG);
                mListContent.add(msgStr);
                mListAdapter.notifyDataSetChanged();
            }
        }
    };
    private void registerReceivers(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(HttpHelper.ACTION_RESPONSE_MSG);
        registerReceiver(mReceiver,filter);
    }

    private void unregisterReceivers(){
        unregisterReceiver(mReceiver);
    }

    private void setupViewComponents(){
        /*
        */
        mListView = (ListView) findViewById( R.id.listView);
        mListContent = new ArrayList<String>();
        mListAdapter = new MyListAdapter(getApplicationContext(),mListContent);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemLongClickListener(mOILC);

        mBtn1 = (Button) findViewById(R.id.button1);
        mBtn2 = (Button) findViewById(R.id.button2);
        mBtn3 = (Button) findViewById(R.id.button3);
        mBtn4 = (Button) findViewById(R.id.button4);
        mBtn1.setOnClickListener(this);
        mBtn2.setOnClickListener(this);
        mBtn3.setOnClickListener(this);
        mBtn4.setOnClickListener(this);
        mBtnGo = (Button) findViewById(R.id.buttonGo);
        mBtnGo.setOnClickListener(this);

        mEtUrl = (EditText) findViewById(R.id.editText);
        mIsEtUrlUsed = true;
        mEtUrl.setOnClickListener(this);

    }


    private AdapterView.OnItemLongClickListener mOILC = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            //return false;
            final String str = (String)mListAdapter.getItem(position);
            //mEtUrl.setText(str);
            //ClipData clipData =  ClipData.newPlainText("simpletest",str);
            //mClipboardManager.setPrimaryClip(clipData);
            View popView = LayoutInflater.from(MainActivity.this).inflate(R.layout.popwindow_layout,null);
            Button btn1 = (Button) popView.findViewById(R.id.buttonPop1);
            btn1.setText("Copy");
            final PopupWindow popupWindow = new PopupWindow(popView, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,true);
            popupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.alert_dark_frame));


            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipData clipData =  ClipData.newPlainText("simpletest",str);
                    mClipboardManager.setPrimaryClip(clipData);
                    popupWindow.dismiss();
                }
            });
            popupWindow.showAsDropDown(view,0,0,Gravity.LEFT|Gravity.BOTTOM);

            return true;
        }
    };
    @Override
    public void onClick(View v) {
        final Message msg = new Message();
        if(v.getId() == mBtn1.getId()){
            useHttpClientGet("http://www.baidu.com");
        }else if(v.getId() == mBtn2.getId()){
            useHttpClientPost("http://www.163.com");
        }else if(v.getId() == mBtn3.getId()){
            useHttpClientGet("www.baidu.com");
        }else if(v.getId() == mBtn4.getId()){
            mListContent.clear();
            mListAdapter.notifyDataSetChanged();
        }else if(v.getId() == mBtnGo.getId()){
           useHttpClientGet(mEtUrl.getText().toString());
            mIsEtUrlUsed = true;
            mEtUrl.clearFocus();
        }else if(v.getId() == mEtUrl.getId()){
            if(mIsEtUrlUsed)
                mEtUrl.setText("");
            mIsEtUrlUsed = false;
        }
        if( !(v instanceof EditText)){
            InputMethodManager manager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
    }

    private String useHttpClientGet(String link){
        final String url = HttpHelper.getUrl(link);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHttpHelper.useHttpClientGet(url);
            }
        }).start();
        mEtUrl.setText(url);
        return url;
    }


    private String useHttpClientPost(String link){
        final String url = HttpHelper.getUrl(link);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHttpHelper.useHttpClientPost(url);
            }
        }).start();
        mEtUrl.setText(url);
        return url;
    }

    private Handler listHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_LIST_UPDATE:
                    Bundle data = msg.getData();
                    mListContent.add(data.getString(HttpHelper.EXTRA_RESPONSE_MSG));
                    mListAdapter.notifyDataSetChanged();
                    //mListView.setSelection(mListView.getCount()-1);
            }
        }
    };
}
