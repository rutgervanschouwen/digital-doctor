package com.example.digitaldoctor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.digitaldoctor.R;
import com.example.digitaldoctor.SessionDao;
import com.example.digitaldoctor.models.Evidence;
import com.example.digitaldoctor.models.Session;

import java.util.List;

public class EvidenceAdapter extends BaseAdapter {

    List<Evidence> evidence;
    LayoutInflater mInflater;
    SessionDao dao;

    public EvidenceAdapter(Context c, List<Evidence> evidence, SessionDao dao) {
        this.evidence = evidence;
        this.mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.dao = dao;
    }

    @Override
    public int getCount() {
        return evidence.size();
    }

    @Override
    public Object getItem(int position) {
        return evidence.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = mInflater.inflate(R.layout.search_result_selected_listview, null);
        Evidence result = evidence.get(position);
        TextView searchResultTextview = (TextView) v.findViewById(R.id.evidenceTextView);
        ImageView deleteImageView = (ImageView) v.findViewById(R.id.deleteImageView);
        searchResultTextview.setText(result.getLabel());

        deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v_) {
                Evidence evidenceToDelete = evidence.get(position);
                evidence.remove(evidenceToDelete);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Session session = dao.getSession(evidenceToDelete.sessionId);
                        session.evidenceList.removeIf(obj -> obj.id.equals(evidenceToDelete.id));
                        dao.updateSession(session);
                    }
                });
                thread.start();
                EvidenceAdapter.this.notifyDataSetChanged();
            }
        });

        return v;
    }
}
