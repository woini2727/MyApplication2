package com.example.myapplication;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import java.util.*;
import android.app.NotificationManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.content.ContentValues.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);
        if(remoteMessage.getData() !=null){
            enviarNotificacion(remoteMessage);
        }
        if(remoteMessage.getNotification() !=null){
            Log.d(TAG, "Body notificaction: "+ remoteMessage.getNotification().getBody());
            enviarNotificacion(remoteMessage);
        }
    }
    private void enviarNotificacion(RemoteMessage remoteMessage){
        Map<String,String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("body");

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANEL_ID = "salvi";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
           //aNDROID OREO O superior
            @SuppressLint("WrongConstant") NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANEL_ID, "Mi notificacion",NotificationManager.IMPORTANCE_MAX);
            //CONF DEL CANAL de notif
            channel.setDescription("salvi chanel ");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.setVibrationPattern(new long[]{0,1000,500,1000});
            channel.enableVibration(true);

            manager.createNotificationChannel(channel);

        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder =  new NotificationCompat.Builder(this,NOTIFICATION_CHANEL_ID);
        builder.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_stat_ic_notification).setTicker("Hearty465").setContentTitle(title)
                .setContentText(body).setVibrate(new long[]{0,1000,500,1000}).setContentIntent(pendingIntent).setContentInfo("info");
        manager.notify(1,builder.build());


    }
    @Override
    public void onNewToken(String token){
        Log.d(TAG,"Refreshed Token"+token);
        FirebaseMessaging.getInstance().subscribeToTopic("dispositivos");
        enviarTokenToServer(token);

    }

    private void enviarTokenToServer(final String token) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "http://192.168.1.13/notificaciones/registrarToken.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), "Se registro exitosamente", Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "ERROR EN LA CONEXION", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new Hashtable<String, String>();
                parametros.put("Token", token);

                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
