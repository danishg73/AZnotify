package com.it.aznotifybbc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity
{
    EditText email,name,password;
    Button register;
    String getemail,getname,getpassword,Response;
    ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
    ProgressDialog progressDialog;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        changeStatusBarColor();
        sp = this.getSharedPreferences("login", MODE_PRIVATE);


        if( sp.contains("email"))
        {

                Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

        }





        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        name = findViewById(R.id.name);
        register = findViewById(R.id.register);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.Loading));
        progressDialog.setCancelable(false);



        register.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                getemail = email.getText().toString();
                getpassword = password.getText().toString();
                getname = name.getText().toString();
                if (TextUtils.isEmpty(getemail) || TextUtils.isEmpty(getpassword)   || TextUtils.isEmpty(getname))
                {
                    Toast.makeText(RegisterActivity.this,R.string.All_Fields_are_required, Toast.LENGTH_SHORT).show();
                }
                else  if(isEmailValid(getemail)==false)
                {
                    Toast.makeText(RegisterActivity.this, R.string.enter_valid_email, Toast.LENGTH_SHORT).show();
                }

                else
                {

                    int length = password.getText().length();
                    if( length<6)
                    {
                        Toast.makeText(RegisterActivity.this, R.string.Weak_password, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                        if(isNetworkConnected())
                        {

                            register();

                        }
                        else
                        {
                            Toast.makeText(RegisterActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            }
        });




    }
    public void register()
    {
        list.add(new BasicNameValuePair("email",getemail));
        list.add(new BasicNameValuePair("password",getpassword));
        list.add(new BasicNameValuePair("name",getname));
        progressDialog.show();
        uploadToServer readService= new  uploadToServer(new ReadServiceResponse() {

            @Override
            public void processFinish(String output)
            {

                list.clear();
                progressDialog.dismiss();
                email.setText("");
                password.setText("");
                name.setText("");
                if(output.contains("Register Successfully"))
                {
                    email.setEnabled(false);
                    password.setEnabled(false);
                    name.setEnabled(false);

                   Toast.makeText(RegisterActivity.this, R.string.Register_Successfully, Toast.LENGTH_SHORT).show();

                }
                else
                {
                    email.setEnabled(true);
                    password.setEnabled(true);
                    name.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, output+"", Toast.LENGTH_SHORT).show();
                }


            }
        });readService.execute();



    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }




    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }

    public void onLoginClick(View view){
//        startActivity(new Intent(this,LoginActivity.class));
        RegisterActivity.super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);

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

            String link=getResources().getString(R.string.link)+"signup.php";


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

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }



}
