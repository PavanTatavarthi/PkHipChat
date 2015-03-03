package hipchat.pk.com.pkhipchat;

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

    }

    /* set button click listener and start processing input string on click event*/
    public void handleButtonEvent() {
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String inputStr = input.getText().toString().trim();
                new LongOperation(mHandler,inputStr).execute("");
            }
    });}

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
    public class DataHandler extends Handler{
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case HipChatUtil.FULL_DATA:
                {
                    Gson gson = new GsonBuilder()
                            .setExclusionStrategies(new DataStrategy())
                            .create();
                    DataContainer data = util.getData();
                    String json = gson.toJson(data);
                    output.setText(json);
                    HipChatUtil.debugLog(json);
                    progressBar.setVisibility(View.GONE);
                }
            }
        }
    }

    /*Strategy to control output json string based on input string matches*/
    public class DataStrategy implements ExclusionStrategy {

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
