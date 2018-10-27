package com.tutsplus.code.materiallogin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.qrCode.CustomRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginActivity extends Activity {
    private static String TAG = LoginActivity.class.getSimpleName();

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;
    private String urlEvent = "http://demo.beework.com.tr/ebilet/event/api/events";

    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean remember;
    private CheckBox chkRemember;
    private ProgressDialog pDialog;

    // private int status;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final TextInputLayout usernameWrapper = (TextInputLayout) findViewById(R.id.usernameWrapper);
        final TextInputLayout passwordWrapper = (TextInputLayout) findViewById(R.id.passwordWrapper);
        final Button btn = (Button) findViewById(R.id.btn);
        chkRemember=(CheckBox)findViewById(R.id.chkRemember);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Lütfen Bekleyin...");
        pDialog.setCancelable(false);

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
        remember = loginPreferences.getBoolean("remember", false);

        if (remember) {
            usernameWrapper.getEditText().setText(loginPreferences.getString("username", ""));
            passwordWrapper.getEditText().setText(loginPreferences.getString("password", ""));
            chkRemember.setChecked(true);
        }
        
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();

                String username = usernameWrapper.getEditText().getText().toString();
                String password = passwordWrapper.getEditText().getText().toString();

                if (!validatePassword(password)) {
                    passwordWrapper.setError("Geçerli bir şifre girin!");
                } else {
                    usernameWrapper.setErrorEnabled(false);
                    passwordWrapper.setErrorEnabled(false);
                    showpDialog();
                    doLogin(username,password);
                }
            }
        });
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

                        openActivity(username,password,articles);

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

    private void openActivity(String username, String password, JSONArray events){
        if (chkRemember.isChecked())	{
            loginPrefsEditor.putBoolean("remember", true);
            loginPrefsEditor.putString("username", username);
            loginPrefsEditor.putString("password", password);
            loginPrefsEditor.commit();
        } else {
            loginPrefsEditor.clear();
            loginPrefsEditor.commit();
        }
        Intent i = new Intent(getApplicationContext(), com.qrCode.qrActivity.class);
        i.putExtra("username",username);
        i.putExtra("password",password);
        i.putExtra("events",events.toString());
        startActivity(i);
    }
    public boolean validatePassword(String password) {
        return password.length() > 3;
    }

    public boolean validateEmail(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
