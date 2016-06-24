package com.example.httpexample;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

/**
 * Created by Administrator on 2016/6/23.
 */
public class MyListAdapter extends BaseAdapter {

    private final  static  String TAG = "MyListAdapter";


    private Context mContext;
    private List<String> mList;
    private LayoutInflater mInflater;

    public MyListAdapter(Context context,List<String> list){
        mContext = context;
        mList = list;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String str = mList.get(position);
        ViewHolder holder = null;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.simplelistlayout,null);
            holder.textView = (TextView) convertView.findViewById(R.id.textView1);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        SpannableStringBuilder builder = new SpannableStringBuilder(str);
        CharacterStyle span = null;
        Pattern p = Pattern.compile(HttpHelper.HTTP_URL_PATTERN);
        Matcher m = p.matcher(str);
        while (m.find()){
            span = new ForegroundColorSpan(Color.RED);

            builder.setSpan(span,m.start(),m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        holder.textView.setText(builder);

        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    private class ViewHolder{
        TextView textView;
    }
}
