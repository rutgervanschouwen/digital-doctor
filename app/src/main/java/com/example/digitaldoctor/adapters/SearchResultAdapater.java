package com.example.digitaldoctor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.digitaldoctor.R;
import com.example.digitaldoctor.models.Evidence;

import java.util.List;

public class SearchResultAdapater extends BaseAdapter {

    List<Evidence> results;
    LayoutInflater mInflater;

    public SearchResultAdapater(Context c, List<Evidence> results) {
        this.results = results;
        this.mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public Object getItem(int position) {
        return results.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = mInflater.inflate(R.layout.search_result_listview, null);
        Evidence result = results.get(position);
        TextView searchResultTextview = (TextView) v.findViewById(R.id.evidenceTextView);
        searchResultTextview.setText(result.getLabel());
        return v;
    }
}
