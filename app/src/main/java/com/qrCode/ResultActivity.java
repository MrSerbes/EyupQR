package com.qrCode;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {
    private static String TAG = qrActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private  static  String id,username,password,content2;
    private Bundle extras = null;
    private ScrollView lytResult;
    private LinearLayout lytInformation;
    private ImageView imgAlertIcon;
    private View viewRectangle;
    private SpannableStringBuilder sb;
    private StyleSpan bss;
    private TextView txtName,txtIdentity,txtEvent,txtTicket,txtSeat,txtStatus;
    // temporary string to show the parsed response
    private String urlEvent ="http://demo.beework.com.tr/ebilet/event/api/checkTicket";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        txtName = (TextView) findViewById(R.id.txtName);
        txtIdentity = (TextView) findViewById(R.id.txtIdentity);
        txtEvent = (TextView) findViewById(R.id.txtEvent);
        txtTicket = (TextView) findViewById(R.id.txtTicket);
        txtSeat = (TextView) findViewById(R.id.txtSeat);
        txtStatus= (TextView)findViewById(R.id.txtStatus);

        lytResult = (ScrollView) findViewById(R.id.lytResult);
        lytInformation=(LinearLayout)findViewById(R.id.lytInformation);
        imgAlertIcon =(ImageView) findViewById(R.id.imgAlertIcon);
        viewRectangle= findViewById(R.id.viewRectangle);

        someCode();
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private  void someCode(){
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Lütfen Bekleyin...");
        pDialog.setCancelable(false);
        showpDialog();

        makeJsonObjectRequest();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void makeJsonObjectRequest() {

        extras = getIntent().getExtras();

        username=extras.getString("username");
        password=extras.getString("password");
        id=extras.getString("id");
        content2=extras.getString("content");


        RequestQueue queue = Volley.newRequestQueue(this);

        Map<String, String> params = new HashMap<String, String>();
        params.put("username", username);
        params.put("password", password);
        params.put("event_id", id);
        params.put("content", content2);
        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, urlEvent, params, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(lytInformation.getVisibility()==View.INVISIBLE){
                                lytInformation.setVisibility(View.VISIBLE);
                            }
                            MediaPlayer mp;
                            int status = Integer.parseInt(response.getString("status"));

                            if (status == 0) {
                                mp = MediaPlayer.create(getApplicationContext(),R.raw.user_not_found);
                                mp.start();
                                lytResult.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.bilet_bulunamadi_back_color));
                                viewRectangle.setBackgroundResource(R.drawable.redborder);
                                lytInformation.setVisibility(View.INVISIBLE);
                                imgAlertIcon.setImageResource(R.drawable.icon0);
                                txtStatus.setText("Bilet Bulunamadı!");
                            } else {
                                mp = MediaPlayer.create(getApplicationContext(),R.raw.user_found);
                                mp.start();

                                if (status == 1) { //Kayıt bulundu
                                    lytResult.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.bilet_bulundu_back_color));
                                    viewRectangle.setBackgroundResource(R.drawable.greenborder);
                                    imgAlertIcon.setImageResource(R.drawable.icon1);
                                    txtStatus.setText("Bilet Onaylandı");
                                } else if (status == 2) {//Aynı giriş tekrar
                                    txtStatus.setText("Daha Önce Giriş Yapıldı");
                                    imgAlertIcon.setImageResource(R.drawable.icon2);
                                    lytResult.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.tekrar_giris_back_color));
                                    viewRectangle.setBackgroundResource(R.drawable.yellowborder);
                                }

                                String fullname = response.getString("fullname");
                                String identity_number = response.getString("identity_number");
                                String ticket_number = response.getString("ticket_number");
                                String seat_number = response.getString("seat_number");
                                String date = response.getString("date");
                                String time = response.getString("time");
                                String birth_year = response.getString("birth_year");
                                String event_title = response.getString("event_title");
                                String gender = response.getString("gender");

                                sb= new SpannableStringBuilder(fullname+ "\n"+birth_year+", "+gender);
                                bss = new StyleSpan(android.graphics.Typeface.BOLD);
                                sb.setSpan(bss, 0, fullname.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                txtName.setText(sb);

                                String tmp=identity_number;
                                sb= new SpannableStringBuilder("TC: "+ tmp);
                                sb.setSpan(bss, 4, tmp.length() + 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                txtIdentity.setText(sb);

                                tmp="\n"+event_title+"\n"+date+" - "+time;
                                sb= new SpannableStringBuilder("Etkinlik Adı"+ tmp);
                                sb.setSpan(bss, 12, tmp.length() + 12, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                txtEvent.setText(sb);

                                tmp=ticket_number;
                                sb= new SpannableStringBuilder("Bilet No: "+ tmp);
                                sb.setSpan(bss, 10, tmp.length() + 10, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                txtTicket.setText(sb);

                                tmp=seat_number;
                                sb= new SpannableStringBuilder("Koltuk No: "+ tmp);
                                sb.setSpan(bss, 11, tmp.length() + 11, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                txtSeat.setText(sb);
                            }

                        } catch (JSONException e) {
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
        // Adding request to request queue

        queue.add(jsObjRequest);
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
                i.putExtra("id",id);
                finish();
                startActivity(i);
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    public void onClick(View view) {
        new IntentIntegrator(this).initiateScan();
    }

    public void onClickBack(View view) {
        finish();
        onBackPressed();
    }

    public void onExit(View view) {
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
}
