package xproperty;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XPropertyReader {
    private HashMap<String,String> properties = new HashMap<String,String>();
    private HashMap<String,XFunction> functions = new HashMap<>();

    public XPropertyReader(String path){
        try {
            loadDefaultFunctions();
            load(path);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadDefaultFunctions(){
        addFunction("currentDate",new XFunction(){
            public String execute(){
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                Date date = new Date();
                return(dateFormat.format(date));
            }
        });
    }

    public void addFunction(String key, XFunction f){
        functions.put(key,f);
    }

    private String execute(String key){
        return functions.get(key).execute();
    }

    private void load(String path) throws Exception {
        Scanner scanner = new Scanner(new File(path));
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            int equals = line.indexOf('=');
            String key = line.substring(0,equals);
            String val = line.substring(equals+1);

            setProperty(key,val);
        }
    }

    public void setProperty(String key, String val){
        Pattern p = Pattern.compile("\\{\\$(.*?)}");
        Matcher m = p.matcher(val);
        while(m.find()){
            MatchResult result = m.toMatchResult();
            String var = result.group(1);
            val = val.replace(result.group(), getProperty(var));
            m = p.matcher(val);
        }
        properties.put(key,val);
    }
    public String getProperty(String key){
        String value = properties.get(key);
        Pattern p = Pattern.compile("\\{#(.*?)}");
        Matcher m = p.matcher(value);
        while(m.find()){
            MatchResult result = m.toMatchResult();
            String var = result.group(1);
            value = value.replace(result.group(),execute(var));
            m = p.matcher(value);
        }

        return value;
    }

    public void printProperties(){
        Iterator<String> iter = properties.keySet().iterator();
        while(iter.hasNext()){
            String key = iter.next();
            String value = getProperty(key);

            System.out.println(String.format("%s=%s",key,value));
        }
    }

    public static void main(String[] args){
        XPropertyReader pr = new XPropertyReader("./src/resources/test.properties");
        pr.printProperties();
    }
}
