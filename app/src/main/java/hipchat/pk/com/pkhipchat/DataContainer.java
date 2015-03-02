package hipchat.pk.com.pkhipchat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by pavan on 3/1/15.
 *
 * Container object to hold processed data
 *
 */
public class DataContainer{

    //members
    private List<HashMap> links;
    private List<String> emoticons = new ArrayList<String>();
    private List<String> mentions = new ArrayList<String>();

    // Constructor
    public  DataContainer(){
        links = new ArrayList<HashMap>();
    }

    // To add emoticons after processing input data
    public void addEmoticons(String input){
        emoticons.add(input);
    }

    // To add mentions after processing input data
    public void addMentions(String input){
        mentions.add(input);
    }

    // To add url links and their respective title string
    public void addElement(String link,String title){
        HashMap<String, String> map = new HashMap<String,String>();
        map.put("url",link);
        map.put("title",title);
        links.add(map);
    }
}
