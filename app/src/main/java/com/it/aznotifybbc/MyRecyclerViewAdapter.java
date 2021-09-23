package com.it.aznotifybbc;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<alert_list> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    MainActivity m = new MainActivity();
    String s;
    ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
    String Response;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, List<alert_list> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.custom_comment, parent, false);

        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {


        holder.title.setText(mData.get(position).title);
        holder.timestamp.setText(mData.get(position).timestamp);
        holder.description.setText(mData.get(position).description);
        if(mData.get(position).seen.equals("0"))
        {
            holder.newalert.setVisibility(View.VISIBLE);
        }
        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                String i =mData.get(position).id.toString();
                mData.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,mData.size());
                list.clear();
                list.add(new BasicNameValuePair("id",i));
                uploadToServer readService= new  uploadToServer(new ReadServiceResponse() {

                    @Override
                    public void processFinish(String output)
                    {
                        Log.e("delete",""+output);

                    }
                });readService.execute("delete_alert.php");







            }
        });

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title,description,timestamp,newalert;
        ImageView cancel;

        ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.header_title);
            description = itemView.findViewById(R.id.comment);
            timestamp = itemView.findViewById(R.id.timestamp);
            newalert = itemView.findViewById(R.id.newalert);
            cancel = itemView.findViewById(R.id.del);
            s = m.setlink;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    alert_list getItem(int id) {
        return mData.get(id);
    }


    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }





    public  class uploadToServer extends AsyncTask<String, Void, String> {

        ReadServiceResponse delegate =null;

        public uploadToServer(ReadServiceResponse res) {
            delegate = res;
        }
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... arg0) {


            String z = arg0[0];
           // String link=   context.getResources().getString(R.string.link)+z;
         //   String link= String.format("%s%s", Resources.getSystem(android.R.string.link))+z;



            try {
                HttpPost httppost = new HttpPost(s+z);
                HttpClient httpclient = new DefaultHttpClient();
                httppost.setEntity(new UrlEncodedFormEntity(list,"UTF-8"));
                HttpResponse response = httpclient.execute(httppost);
                Response = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                Log.v("log_tag", "Error in http connection " + e.toString());
            }
            return Response;

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            delegate.processFinish(result);

        }
    }




}