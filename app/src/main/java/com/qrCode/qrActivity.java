package com.qrCode;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tutsplus.code.materiallogin.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class qrActivity extends Activity implements View.OnClickListener {

    private String btnId="";
    private static final int ORANGE=Color.parseColor("#FF6D37");
    private String username,password;
    private SpannableStringBuilder sb;
    private StyleSpan bss;
    private TextView txtGelen,txtGelmeyen;
    private ViewGroup parent;
    private ProgressDialog pDialog;
    private String urlEvent = "http://demo.beework.com.tr/ebilet/event/api/events";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        Bundle extras = getIntent().getExtras();

        parent = (ViewGroup) findViewById(R.id.lytEvents);

        if (extras != null) try {
            username = extras.getString("username");
            password = extras.getString("password");
            String strEvents = extras.getString("events");
            JSONArray events = new JSONArray(strEvents);

            loadEvents(events);

            pDialog = new ProgressDialog(this);
            pDialog.setMessage("Yükleniyor...");
            pDialog.setCancelable(false);

        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadEvents(JSONArray events){
        try {
            parent.removeAllViews();
            LinearLayout layout;
        Button btnTag;
        ImageView imgIcon;
        JSONObject event;
        String urlIcon,title,date,time,color,gelen,gelmeyen;

        for (int i = 0; i < events.length(); i++) {

            event = (JSONObject) events.get(i);
            String id = event.getString("id");
            title = event.getString("title");
            date = event.getString("date");
            time = event.getString("time");
            urlIcon = event.getString("icon");
            color=event.getString("color");
            gelmeyen=event.getString("coming_people");
            gelen=event.getString("entering_people");

            layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.activity_etkinlik, null);
            btnTag = (Button) layout.findViewById(R.id.btnEtkinlik);
            imgIcon=(ImageView)layout.findViewById(R.id.imgIcon);
            txtGelen=(TextView)layout.findViewById(R.id.txtGelen);
            txtGelmeyen=(TextView)layout.findViewById(R.id.txtGelmeyen);

            txtGelen.setText("Gelen : "+gelen);
            txtGelmeyen.setText("Gelmeyen : "+gelmeyen);

            sb = new SpannableStringBuilder(title + "\n" + date + " / " + time);
            bss = new StyleSpan(android.graphics.Typeface.BOLD);
            sb.setSpan(bss, 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            sb.setSpan(new RelativeSizeSpan(1.4f), 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            try{
                btnTag.setBackgroundColor(Color.parseColor(color));
                imgIcon.setBackgroundColor(Color.parseColor(color));
            }catch(IllegalArgumentException ex){
                btnTag.setBackgroundColor(ORANGE);
                imgIcon.setBackgroundColor(ORANGE);
            }
            btnTag.setText(sb);
            btnTag.setId(Integer.parseInt(id));
            btnTag.setOnClickListener(this);

            new DownloadImageWithURLTask(imgIcon).execute(urlIcon);

            parent.addView(layout);
        }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            String content=result.getContents();
            if(result.getContents() == null) {
                Toast.makeText(this, "İptal Edildi!", Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(this, com.qrCode.ResultActivity.class);
                i.putExtra("content", content);
                i.putExtra("username", username);
                i.putExtra("password",password);
                i.putExtra("id",btnId);
                startActivity(i);
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View view) {
        btnId=String.valueOf(view.getId());
        new IntentIntegrator(this).initiateScan();
    }

    class DownloadImageWithURLTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageWithURLTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }
        protected Bitmap doInBackground(String... urls) {
            String pathToFile = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(pathToFile).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
    public void onExit(View v){
        onBackPressed();
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
    private void doLogin( final String username, final String password) {
        // TODO: login procedure; not within the scope of this tutorial.
        RequestQueue queue = Volley.newRequestQueue(this);

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, urlEvent, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    int  status=Integer.parseInt(response.getString("status"));
                    if(status==400){
                        Toast.makeText(getApplicationContext(),"Kullanıcı Adı veya Şifre yanlış  ",Toast.LENGTH_LONG).show();

                    }else if(status==200) {
                        JSONArray articles = response.getJSONArray("events"); // get articles array

                        loadEvents(articles);

                    }

                }catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
                hidepDialog();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),error.getMessage(), Toast.LENGTH_LONG).show();
                hidepDialog();
            }
        });

        queue.add(jsObjRequest);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Uygulama Kapatılıyor")
                .setMessage("Çıkmak istediğinizden emin misiniz?")
                .setPositiveButton("Evet", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("Hayır", null)
                .show();
    }
    public void onRefresh(View v){

        showpDialog();
        //parent.removeAllViews();
        doLogin(username,password);
    }

}

