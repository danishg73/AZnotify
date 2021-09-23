package com.it.aznotifybbc;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class Comment_adapter  extends BaseAdapter {
    Context context;
    List<alert_list> valueList;
    static int position = 0;
    MainActivity mainActivity2;
    ViewEM finalViewItem1;








    public Comment_adapter(List<alert_list> listValue, Context context, MainActivity mainActivity2) {
        this.context = context;
        this.valueList = listValue;
        this.mainActivity2 = mainActivity2;
    }




    @Override
    public int getCount() {
        return this.valueList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.valueList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public int getViewTypeCount() {

        if (getCount() > 0) {
            return getCount();
        } else {
            return super.getViewTypeCount();
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewEM viewItem = null;
        Comment_adapter.position=position;


        if(convertView == null)
        {
            viewItem = new ViewEM();

            LayoutInflater layoutInfiater = (LayoutInflater)this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            convertView = layoutInfiater.inflate(R.layout.custom_comment, null);
            viewItem.title = convertView.findViewById(R.id.title);
            viewItem.description = convertView.findViewById(R.id.comment);
            viewItem.timestamp = convertView.findViewById(R.id.timestamp);
            viewItem.delete = convertView.findViewById(R.id.del);

            convertView.setTag(viewItem);

            viewItem.title.setText(valueList.get(position).title);
            viewItem.description.setText(valueList.get(position).description);
            viewItem.timestamp.setText(valueList.get(position).timestamp);






            finalViewItem1 = viewItem;


            ViewEM finalViewItem = viewItem;







        }
        else
        {
            viewItem = (ViewEM) convertView.getTag();
        }

        return convertView;
    }








}

class ViewEM {
    TextView title;
    TextView description;
    TextView timestamp;
    ImageView delete;

}
