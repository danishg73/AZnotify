package com.it.aznotifybbc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Info extends AppCompatActivity {

    TextView testapi,sendemail,mymail,fiverr,linkinfo;
    SharedPreferences sp;
    String email,userid,name,accountkey;
    LinearLayout account, alert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
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
            Intent intent = new Intent(Info.this,Splashscreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        testapi = findViewById(R.id.testapi);


        alert = findViewById(R.id.alert);
        account = findViewById(R.id.account);
        linkinfo = findViewById(R.id.linkinfo);


        fiverr = findViewById(R.id.fiverr);
        mymail = findViewById(R.id.mymail);
        linkinfo.setText(getString(R.string.link)+"info");
        String ta = "You can test AZ Notify by the link below and pasting it into any web browser (feel free to change the title and message)."+
        "\n\n\n"+ getString(R.string.link)+"send.php?accountkey="+accountkey+"&title=text1&message=text2 \n\n\n"+
                "Please Note, we always recommend POST Method. GET Method should only be used for testing purpose.\n\n"+
                "See integration info by clicking url below \n\n"
                +"https://www.google.com.pk";
        testapi.setText(ta);
        fiverr.setText("https://www.fiverr.com/danishg73");

        mymail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"danishg73@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Enter Subject");
                intent.putExtra(Intent.EXTRA_TEXT, "Enter Message");
                try
                {
                    startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_mail)));
                }
                catch (ActivityNotFoundException ex)
                {
                    Toast.makeText(Info.this, getResources().getString(R.string.no_email_app), Toast.LENGTH_SHORT).show();
                }
            }
        });


        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Info.this, Account.class);
                startActivity(intent);
            }
        });

        alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Info.this, MainActivity.class);
                startActivity(intent);
            }
        });



    }
}