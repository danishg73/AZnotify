package com.it.aznotifybbc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    LinearLayout account, info;
    String Response;
    Comment_adapter adapter_main;
    static String setlink;
    //ListView listView;
    RecyclerView rv;
    String email,userid,name,accountkey;
    List<alert_list> array_alert_list = new ArrayList<>();
    ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
    SharedPreferences sp;
    Handler myHandler;
    ProgressDialog progressDialog;
    SwipeRefreshLayout swipeRefreshLayout;
    MyRecyclerViewAdapter recyclerViewAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        sp = this.getSharedPreferences("login", MODE_PRIVATE);
        if( sp.contains("email"))
        {

            userid = sp.getString("userid", "").trim();
            accountkey = sp.getString("accountkey", "").trim();
            name = sp.getString("name", "").trim();
            email = sp.getString("email", "").trim();
        }
        else
        {
            Intent intent = new Intent(MainActivity.this,Splashscreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        setlink = getResources().getString(R.string.link);


        myHandler = new Handler();

        account = findViewById(R.id.account);
        info = findViewById(R.id.info);
       // listView = findViewById(R.id.listview);

        rv = findViewById(R.id.listview);
        rv.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.Loading));
        progressDialog.setCancelable(false);


        if(isNetworkConnected())
        {
            getdata();
        }
        else
        {
            Toast.makeText(MainActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
        }


        String deviceName = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();


        list.add(new BasicNameValuePair("accountkey",accountkey));
        list.add(new BasicNameValuePair("token",refreshedToken));
        list.add(new BasicNameValuePair("deviceName",deviceName));
        uploadToServer readService= new  uploadToServer(new ReadServiceResponse() {

            @Override
            public void processFinish(String output)
            {
                list.clear();


            }
        });readService.execute("token.php");


        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Account.class);
                startActivity(intent);
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Info.class);
                startActivity(intent);
            }
        });



        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh()
            {
                if(isNetworkConnected())
                {


                    myHandler.removeCallbacks(null);
                    myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if(isNetworkConnected())
                            {

                                getdata();
                            }
                            else
                            {
                                Toast.makeText(MainActivity.this, "Connection Lost", Toast.LENGTH_SHORT).show();
                            }

                            myHandler.postDelayed(this,1000*30);
                            //Do something after 20 seconds
                            //call the method which is schedule to call after 20 sec
                        }
                    }, 10);



                }
                else
                {
                    Toast.makeText(MainActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setEnabled(false);
                }



            }
        });




    }



    public void getdata()
    {

        progressDialog.setMessage("Loading");
        progressDialog.show();
        list.clear();
        list.add(new BasicNameValuePair("accountkey",accountkey));
        uploadToServer readService= new uploadToServer(new ReadServiceResponse() {

            @Override
            public void processFinish(String output)
            {
                progressDialog.dismiss();
                swipeRefreshLayout.setRefreshing(false);
                if(output.contains("No alert found"))
                {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this, "No alert found", Toast.LENGTH_SHORT).show();
                }
                else
                {

                    insertdata(output);
                    myHandler.postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            myHandler.postDelayed(this,30*1000);
                            progressDialog.setMessage("Updating");
                            update_data();
                        }
                    },30*1000);



                }
            }
        });readService.execute("getalert.php");

    }
    public void update_data()
    {

        progressDialog.show();
        list.clear();
        list.add(new BasicNameValuePair("accountkey",accountkey));
        uploadToServer readService= new uploadToServer(new ReadServiceResponse() {

            @Override
            public void processFinish(String output)
            {
                progressDialog.dismiss();
                swipeRefreshLayout.setRefreshing(false);
                if(output.contains("No alert found"))
                {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this, "No alert found", Toast.LENGTH_SHORT).show();
                }
                else
                {

                    insertdata(output);
                }
            }
        });readService.execute("getalert.php");







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
            String link=getResources().getString(R.string.link)+z;



            try {
                HttpPost httppost = new HttpPost(link);
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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }






    public void insertdata(String output)
    {
        array_alert_list.clear();
        try {
            JSONArray jsonObject = new JSONArray(output);
            for (int i=0; i<jsonObject.length(); i++)
            {
                alert_list alertList = new alert_list();
                JSONObject obj = jsonObject.getJSONObject(i);
                alertList.title = obj.getString("title").trim();
                alertList.id = obj.getString("id").trim();
                alertList.description =obj.getString("description").trim();
                alertList.timestamp = obj.getString("creation_date").trim();
                alertList.seen = obj.getString("seen").trim();

                array_alert_list.add(alertList);
                alertList = null;
            }
        }

        catch (JSONException e) {
            e.printStackTrace();
        }


        recyclerViewAdapter = new MyRecyclerViewAdapter(MainActivity.this,array_alert_list);
        rv.setAdapter(recyclerViewAdapter);
        progressDialog.dismiss();
//        listView.setAdapter(null);
//        adapter_main = new Comment_adapter(array_alert_list, this, MainActivity.this);
//        listView.setAdapter(adapter_main);
//        adapter_main.notifyDataSetChanged();
    }


}