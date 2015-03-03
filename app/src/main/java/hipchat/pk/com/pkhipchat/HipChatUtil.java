package hipchat.pk.com.pkhipchat;

import android.os.Message;
import android.util.Log;
import android.content.Context;
import android.os.Handler;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created by pavan on 2/27/15.
 */
public class HipChatUtil {

    /*
      private constants
     */
    private final String MENTIONS_REGX = "\\B@[A-Za-z0-9_-]+";
    private final String EMOTICONS_REGX = "[(]+[a-zA-Z\\\\.@\\\\-\\\\']*[)]{1,15}";
    private final String URL_REGX = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?";
    private final String TITLE_REGX = "<title>(.*?)</title>";
    private static final String LOG_TAG = "HipChatUtil";

    /*
      private data
     */
    private String mInput;
    private Handler mHandler;
    private static boolean isDebugEnable;

    /* public variables and constant */
    public DataContainer mData;
    public static boolean isMentions, isEmoticons,isLinks;
    public static final int FULL_DATA = 1;

    /* constructor*/
    public HipChatUtil(Handler handler,String input)
    {
        mInput = input;
        mHandler = handler;
        mData  = new DataContainer();
        isMentions = false;
        isEmoticons = false;
        isLinks = false;
        isDebugEnable = false;
    }


    /* print logs if debug mode is enabled */
    public static void debugLog(String logstr){
        if(isDebugEnable)
        {
            Log.d(LOG_TAG,logstr);
        }
    }

    /*get method to data container object from util instance*/
    public DataContainer getData(){
        return mData;
    }

    /* Util method to invoke from main thread*/
    public void processInput(){
        getMentions();
        getEmoticons();
        getLinks();
    }

    /* To fetch Mentions from input string and
     add it to data container */
    private void getMentions()
    {
        debugLog("Pattern Match in Mentions");
            Pattern p = Pattern.compile(MENTIONS_REGX);
            Matcher m = p.matcher(mInput);
            while (m.find()) {
                isMentions = true;
                String ret = m.group();
                ret = ret.substring(1,ret.length());
                mData.addMentions(ret.trim());
            }

    }

     /*To fetch emoticons from input string
     and add it to data container*/
    private void getEmoticons()
    {
        debugLog("Pattern Match Emoticons");
        Pattern p = Pattern.compile(EMOTICONS_REGX);
        Matcher m = p.matcher(mInput);
        while (m.find()) {
            isEmoticons = true;
            String ret = m.group();
            ret = ret.substring(1, ret.length() - 1);
            mData.addEmoticons(ret);
        }
    }

    /*To fetch url and title from input string
      and add it to data container*/
    private String getLinks()
    {
        debugLog("Pattern Match Links");
        Pattern p = Pattern.compile(URL_REGX);
        Matcher m = p.matcher(mInput);
        while (m.find()) {
            isLinks = true;
            String url = m.group();
            String title = getTitleFromPage(url);
            mData.addElement(url,title);
        }
        Message msg = new Message();
        msg.what = FULL_DATA;
        mHandler.sendMessage(msg);
        return null;
    }

    /* Fetch title string from input param*/
    public String getTitle(String response)
    {
        debugLog("Pattern Match title");
        String title= "";
        Pattern p = Pattern.compile(TITLE_REGX, Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
        Matcher m = p.matcher(response);
        while (m.find()) {
            title = m.group(1);
        }
        return title;
    }

    /*Extract web page and feed in page data for title match to get title string*/
    private String getTitleFromPage(String link) {
        String str = "";
        String title = "";
        try
        {
            HttpClient hc = new DefaultHttpClient();
            HttpPost post = new HttpPost(link);
            HttpResponse rp = hc.execute(post);

            if(rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                str = EntityUtils.toString(rp.getEntity());
                title = getTitle(str);
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        return title;
    }

}
