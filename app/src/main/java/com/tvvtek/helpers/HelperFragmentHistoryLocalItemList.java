package com.tvvtek.helpers;


import android.app.Activity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.tvvtek.keepstring.R;
import com.tvvtek.keepstring.StaticSettings;

import java.util.ArrayList;
import java.util.List;

public class HelperFragmentHistoryLocalItemList extends ArrayAdapter<String> implements Filterable {

    StaticSettings staticSettings;
    private List<String> allModelItemsArray;
    private List<String> filteredModelItemsArray;
    private Activity context;
    private ModelFilter my_filter;
    private LayoutInflater inflater;

    public HelperFragmentHistoryLocalItemList(Activity context, ArrayList<String> list) {
        super(context, R.layout.fragment_history_local_item_list, list);
        this.context = context;
        this.allModelItemsArray = new ArrayList<>();
        allModelItemsArray.addAll(list);
        this.filteredModelItemsArray = new ArrayList<>();
        filteredModelItemsArray.addAll(allModelItemsArray);
        inflater = context.getLayoutInflater();
        getFilter();
    }
    @Override
    public Filter getFilter() {
        if (my_filter == null){
            my_filter = new ModelFilter();
        }
        return my_filter;
    }
    static class ViewHolder {
        protected TextView text;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        String timestamp_via_html = filteredModelItemsArray.get(position).substring(0,59);
        String data_via_string = filteredModelItemsArray.get(position).substring(59);
        String data_for_item;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = inflater.inflate(R.layout.fragment_history_local_item_list, null);
            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) view.findViewById(R.id.list_item_Content);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = ((ViewHolder) view.getTag());
        }
        // вставляем дату в виде html а сами данные в виде обычной строки.
        data_for_item = Html.fromHtml(timestamp_via_html) + data_via_string;
        try{
            viewHolder.text.setText(data_for_item.substring(0, 150)); //пытаемся порезать строку.
        }catch (IndexOutOfBoundsException substring_error){
            Log.d(staticSettings.getLogTag(), substring_error.toString());
            viewHolder.text.setText(data_for_item);
        }
        return view;
    }

    private class ModelFilter extends Filter
    {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if(constraint.toString().length() > 0)
            {
                ArrayList<String> filteredItems = new ArrayList<String>();
                for(int i = 0, l = allModelItemsArray.size(); i < l; i++)
                {
                    String m = allModelItemsArray.get(i);
                 //   Log.d(staticSettings.getLogTag(), "item=" + m);
                    if(m.toLowerCase().contains(constraint))
                        filteredItems.add(m);
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            }
            else
            {
                synchronized(this)
                {
                    result.values = allModelItemsArray;
                    result.count = allModelItemsArray.size();
                }
            }
            return result;
        }
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredModelItemsArray = (ArrayList<String>)results.values;
            notifyDataSetChanged();
            setNotifyOnChange(true);
            clear();
            for(int i = 0, l = filteredModelItemsArray.size(); i < l; i++)
                add(filteredModelItemsArray.get(i));
            notifyDataSetInvalidated();
          //  setNotifyOnChange(true);
        }
    }
}