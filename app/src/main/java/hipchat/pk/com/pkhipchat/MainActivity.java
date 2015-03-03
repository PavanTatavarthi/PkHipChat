package hipchat.pk.com.pkhipchat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.ProgressBar;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/*
  Main Activity hold all reference to UI and responsible for its lifecycle
 */
public class MainActivity extends ActionBarActivity {

    /* private variables*/
    private ProgressBar progressBar;
    private DataHandler mHandler;
    private HipChatUtil util;
    private EditText input;
    private EditText output;
    private Context  mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hipchat_main);
        handleButtonEvent();
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);
        input = (EditText) findViewById(R.id.editText3);
        output = (EditText) findViewById(R.id.editText);
        mHandler = new DataHandler();
        mContext = this;

    }

    /******************* Private Methods **********/

    /* set button click listener and start processing input string on click event*/
    private void handleButtonEvent() {
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String inputStr = input.getText().toString().trim();
                if(inputStr != null && inputStr.length() > 0) {
                    new LongOperation(mHandler, inputStr).execute("");
                } else{
                    AlertDialog.Builder alertBuilder = createAlertDialog("Invalid input String",
                            "Please verify you input String!");
                    AlertDialog dialog = alertBuilder.create();
                    dialog.show();
                }
            }
    });}


    private AlertDialog.Builder createAlertDialog(String title, String msg){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
        builder1.setMessage(msg);
        builder1.setTitle(title);
        builder1.setCancelable(false);
        builder1.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder1;
    }

    /*********** Public override Methods **********/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present.*/
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Handle action bar item clicks here. The action bar will
         automatically handle clicks on the Home/Up button, so long
         as you specify a parent activity in AndroidManifest.xml.*/
        int id = item.getItemId();

        /*noinspection SimplifiableIfStatement*/
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* AyncTask to perform blocking operations
     Processing of String, long running network operations*/
    private class LongOperation extends AsyncTask<String, Void, String>
    {

        public LongOperation(Handler handler,String inputStr){
            util = new HipChatUtil(handler,inputStr);
        }

        protected String doInBackground(String... params) {
            util.processInput();
            return null;
        }

        protected void onPostExecute(String result) {
        }

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        protected void onProgressUpdate(Void... values) {
        }
    }

    /* Used to update UI components after finishing blocking operation
     Runs in main thread*/
    private class DataHandler extends Handler{
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case HipChatUtil.FULL_DATA:
                    Gson gson = new GsonBuilder()
                            .setExclusionStrategies(new DataStrategy())
                            .create();
                    DataContainer data = util.getData();
                    String json = gson.toJson(data);
                    output.setText(json);
                    HipChatUtil.debugLog(json);
                    progressBar.setVisibility(View.GONE);
                    break;
                case HipChatUtil.NO_MATCH_FOUND:
                    AlertDialog.Builder alertBuilder =  createAlertDialog("No Match","No " +
                            "match emoticons, mentions or url that meets requirement found!");
                    alertBuilder.create().show();
                    break;
            }
        }
    }

    /*Strategy to control output json string based on input string matches*/
    private class DataStrategy implements ExclusionStrategy {

        public boolean shouldSkipClass(Class<?> arg0) {
            return false;
        }
        public boolean shouldSkipField(FieldAttributes f) {
            return ((HipChatUtil.isEmoticons == false && f.getName().equals("emoticons")) ||
                    (HipChatUtil.isMentions  == false && f.getName().equals("mentions"))  ||
                    (HipChatUtil.isLinks  == false    && f.getName().equals("links")) );
        }
    }
}
