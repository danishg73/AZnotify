package com.it.aznotifybbc;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    static int k = 0;
    String a[] = null;
    String receive = "";
    PendingIntent pendingIntent;
    NotificationManager notificationManager;
    Intent intent;
    String userid,Response,accountkey;
    ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
    String print_text="";
    EscPosPrinter printer;

    SharedPreferences sp;



    /**
     * this function will be called whenever fcm will push data to the application
     *
     * @param remoteMessage
     */


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null) {
            sendNotificacion(remoteMessage);
        } else if (remoteMessage.getNotification() != null) {
            sendNotificacion(remoteMessage);
        }
    }

    /**
     * this function will generate the notification
     *
     * @param remoteMessage
     */
    @SuppressLint("WrongConstant")
    private void sendNotificacion(RemoteMessage remoteMessage) {

        /**
         * Map data type is type of data structure for fast indexing, every value will be composed of (key, value)
         * to access the data we will give call to the key, key for every value will be different
         */
        Map<String, String> data = remoteMessage.getData();

        /**
         * data.get("title"), in this statement title is the key of the value of title, on calling this key the
         * respective value, will saved to the string title, and same case for body
         */
        String title = remoteMessage.getNotification().getTitle()+"";
        String body = remoteMessage.getNotification().getBody()+"";
        print_text="";



        //---------------------------------------------------

        BluetoothConnection connection = BluetoothPrintersConnections.selectFirstPaired();

        try {

            printer = new EscPosPrinter(connection, 203, 48f, 32);

        } catch (EscPosConnectionException e) {
            e.printStackTrace();
        }

        try {
            print_text =

                            "[C]================================\n" +
                            "[L]\n" +
                            "[L]<font size='tall'><b>"+title+"</b></font>\n" +
                            "[L]<font size='tall'>"+body+"</font>\n\n"+
                            "[C]================================\n";
            printer.printFormattedText(print_text,70);
            printer.printFormattedTextAndCut("\n");
            printer.disconnectPrinter();
        } catch (EscPosConnectionException e) {
            e.printStackTrace();
        } catch (EscPosParserException e) {
            e.printStackTrace();
        } catch (EscPosEncodingException e) {
            e.printStackTrace();
        } catch (EscPosBarcodeException e) {
            e.printStackTrace();
        }
        printer.disconnectPrinter();
        print_text="";











        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = getResources().getString(R.string.app_name);


//        intent = new Intent(this,Dashboard.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Solo para android Oreo o superior
            @SuppressLint("WrongConstant")
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    getResources().getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_MAX
            );
            //Notification channel configuration
            channel.setDescription(getResources().getString(R.string.app_name));
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.setVibrationPattern(new long[]{0, 500});
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);

        }




        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.ic_app_icon);
            builder.setColor(getResources().getColor(R.color.skyblue));
        } else {
            builder.setSmallIcon(R.drawable.ic_account);
        }


        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_app_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500})
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))

                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentInfo(getResources().getString(R.string.app_name));

        manager.notify(++k, builder.build());






    }

    @SuppressLint("WrongThread")
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        sp = this.getSharedPreferences("login", MODE_PRIVATE);

        if( sp.contains("email"))
        {

            userid = sp.getString("userid", "").trim();
            accountkey = sp.getString("accountkey", "").trim();
            String deviceName = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;



            list.add(new BasicNameValuePair("accountkey",accountkey));
            list.add(new BasicNameValuePair("token",s));
            list.add(new BasicNameValuePair("deviceName",deviceName));

            uploadToServer readService= new  uploadToServer(new ReadServiceResponse() {

                @Override
                public void processFinish(String output)
                {
                    list.clear();

                }
            });readService.execute();








        }

        Log.d("NEW_TOKEN", s);
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

            String link=getResources().getString(R.string.link)+"token.php";


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