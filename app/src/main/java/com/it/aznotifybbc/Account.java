package com.it.aznotifybbc;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;

public class Account extends AppCompatActivity {
    TextView logout,account_key,copy,textView_email,devicename,changepassword;
    SharedPreferences sp;
    String email,userid,name,oldpass,newpass,confirmpass,Response,accountkey;
    LinearLayout alert, info;
    ProgressDialog progressDialog;

    ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
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
            Intent intent = new Intent(Account.this,Splashscreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        logout= findViewById(R.id.logout);
        account_key= findViewById(R.id.account_key);


        alert = findViewById(R.id.alert);
        info = findViewById(R.id.info);

        copy= findViewById(R.id.copy);
        changepassword= findViewById(R.id.changepassword);
        textView_email= findViewById(R.id.email);
        devicename= findViewById(R.id.devicename);
        account_key.setText(accountkey);
        textView_email.setText(email);
        String dn = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
        devicename.setText(dn.toUpperCase());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.Loading));
        progressDialog.setCancelable(false);

        logout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                SharedPreferences.Editor e = sp.edit();
                e.clear();
                e.commit();
                Intent intent = new Intent(Account.this,Splashscreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        copy.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", userid);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(Account.this, "Copied", Toast.LENGTH_SHORT).show();
            }
        });
        changepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                if(isNetworkConnected())
                {

                    changepass();

                }
                else
                {
                    Toast.makeText(Account.this, "No Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });



        alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Account.this, MainActivity.class);
                startActivity(intent);
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Account.this, Info.class);
                startActivity(intent);
            }
        });




    }


    public void changepass()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(Account.this);
        LayoutInflater inflater = ((Account) Account.this).getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.custom_changepassword,
                null);

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.setView(dialogLayout, 0, 0, 0, 0);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        WindowManager.LayoutParams wlmp = dialog.getWindow()
                .getAttributes();
        wlmp.gravity = Gravity.CENTER_VERTICAL;


        Button cancel =   dialogLayout.findViewById(R.id.cancel);
        Button submit =   dialogLayout.findViewById(R.id.submit);
        EditText oldpasword =dialogLayout.findViewById(R.id.old_password);
        EditText newpassword =dialogLayout.findViewById(R.id.new_password);
        EditText confirmpassword =dialogLayout.findViewById(R.id.confrim_password);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                oldpass = oldpasword.getText().toString().trim();
                newpass = newpassword.getText().toString().trim();
                confirmpass = confirmpassword.getText().toString().trim();
                if (TextUtils.isEmpty(oldpass) || TextUtils.isEmpty(newpass) || TextUtils.isEmpty(confirmpass))
                {
                    Toast.makeText( Account.this,R.string.All_Fields_are_required, Toast.LENGTH_SHORT).show();
                }
                else if ( !newpass.equals(confirmpass))
                {
                    Toast.makeText(Account.this, R.string.Passwords_are_not_same, Toast.LENGTH_SHORT).show();

                }
                else
                {
                    list.clear();
                    list.add(new BasicNameValuePair("email",email));
                    list.add(new BasicNameValuePair("oldpassword",oldpass));
                    list.add(new BasicNameValuePair("newpassword",newpass));
                    progressDialog.show();

                    uploadToServer readService= new  uploadToServer(new ReadServiceResponse() {

                        @Override
                        public void processFinish(String output)
                        {
                            progressDialog.dismiss();
                            list.clear();

                            if(output.contains("Password changed successfully"))
                            {
                                Toast.makeText(Account.this, R.string.Password_changed_successfully, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();

                            }
                            else
                            {
                                Toast.makeText(Account.this, output+"", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                        }
                    });readService.execute("change_password.php");




                }

            }
        });



        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });


        builder.setView(dialogLayout);

        dialog.show();

    }



    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public class uploadToServer extends AsyncTask<String, Void, String> {

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








}