package ifl.queues;

/*必需引用apache.http相关类别来建立HTTP联机*/
import org.apache.http.HttpResponse; 
import org.apache.http.NameValuePair; 
import org.apache.http.client.ClientProtocolException; 
import org.apache.http.client.entity.UrlEncodedFormEntity; 
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost; 
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient; 
import org.apache.http.message.BasicNameValuePair; 
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;


/*必需引用java.io 与java.util相关类来读写文件*/
import irdc.EX08_01.R;

import java.io.BufferedReader;
import java.io.IOException; 
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList; 
import java.util.List; 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.R.string;
import android.app.Activity; 
import android.os.Bundle; 
import android.view.View; 
import android.widget.Button; 
import android.widget.TextView; 

/*ActiveMQ*/
import pk.aamir.stompj.*;
import pk.aamir.stompj.internal.*;

/*Luajava*/
import org.keplerproject.luajava.*;


public class coqueues extends Activity 
{ 
  /*创建一个handler类的实例*/
  //Handler h = null;
  
  /*声明一个Button对象,与一个TextView对象*/
  private Button mButton1;
  private Button mButton2;
  private TextView mTextView1;
   
  /** Called when the activity is first created. */ 
  @Override 
  public void onCreate(final Bundle savedInstanceState) 
  { 
    super.onCreate(savedInstanceState); 
    setContentView(R.layout.main); 

    /*透过findViewById建构子建立TextView与Button对象*/ 
    mButton1 =(Button) findViewById(R.id.myButton1);
    mButton2 =(Button) findViewById(R.id.myButton2);
    mTextView1 = (TextView) findViewById(R.id.myTextView1);
    final String strlua = "a = 'campo a';" + 
    "b = 'campo b';" +
    "c = 'campo c';" +
    "tab= { a='tab a'; b='tab b'; c='tab c', d={ e='tab d e'} } ;"+
    "function imprime (str) print(str); return 'joao', 1  end;" + 
    "luaPrint={implements='org.keplerproject.luajava.test.Printable', print=function(str)print('Printing from lua :'..str)end  }";
    
    //final TextView mTextView1 = (TextView)findViewById(R.id.myTextView1);

    /*设定OnClickListener来聆听OnClick事件*/ 
    mButton2.setOnClickListener(new Button.OnClickListener() 
    { 
      @Override 
      public void onClick(View v) 
      { 
        
        LuaState L = LuaStateFactory.newLuaState();
        L.openBase();
        
        L.LdoString(strlua);

        L.close();
        /*
        L.openLibs();
        L.LdoString("text = 'Hello Android, I am Lua.'");
        L.getGlobal("text");
        String text = L.toString(-1);
        */
        mTextView1.setText(strlua);

      }
    }); 
    
    mButton1.setOnClickListener(new Button.OnClickListener() 
    { 
    @Override 
    public void onClick(View v) { 
        // TODO Auto-generated method stub 
        /*声明网址字符串*/
        Connection con = new Connection("10.128.34.75", 61615);
        try
        {
          con.connect();

          con.subscribe("/queue/ifl_test", true);
          con.addMessageHandler("/queue/ifl_test", new MessageHandler() {
            public void onMessage(Message msg) {
              //System.out.println(msg.getContentAsString());
              String strResult = msg.getContentAsString();
              strResult = eregi_replace("(\r\n|\r|\n|\n\r)","",strResult);
              try
              {
                strResult = new String(strResult.getBytes("ISO-8859-1"), "UTF-8");
              } catch (UnsupportedEncodingException e1)
              {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }
              
              //mTextView1.setText(strResult);              
              
              /*声明网址字符串*/
              String uriAPI = "http://10.124.20.136/data-base";
              try
              {
                
                String res = postfareasraw(uriAPI,strResult);
                //String res = simplepostfare(uriAPI,strResult);
                System.out.println("++++++++++++++++++:\n"+res);
                
                
                /*
                h = new Handler(){  
                  public void handleMessage(Message strResult){  
                      // call update gui method. 
                      mTextView1.setText((CharSequence) strResult);
                  }  
              };*/
              
              } catch (Exception e)
              {
                // TODO Auto-generated catch block
                //mTextView1.setText(e.getMessage().toString());
                e.printStackTrace();
              }             
             
            }   
          });

          //con.disconnect();
          
        } catch (StompJException e)
        {
          // TODO Auto-generated catch block
          //mTextView1.setText(e.getMessage().toString());
          e.printStackTrace();
        }
      }
    }); 
  }
    /* 自定义字符串取代函数 */
    public String eregi_replace(String strFrom, String strTo, String strTarget)
    {
      String strPattern = "(?i)"+strFrom;
      Pattern p = Pattern.compile(strPattern);
      Matcher m = p.matcher(strTarget);
      if(m.find())
      {
        return strTarget.replaceAll(strFrom, strTo);
      }
      else
      {
        return strTarget;
      }
    }    
    
    public String postfareasraw(String purl, String faredata) throws Exception{
      HttpURLConnection con = (HttpURLConnection)new URL(purl).openConnection();
      con.setRequestMethod("POST");
      con.setDoOutput(true);
      con.setDoInput(true);
      con.setUseCaches(false);
      con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(),"UTF-8");
      
      osw.write(faredata);
      osw.flush();
      osw.close();
      
      //读取返回信息
      StringBuffer buffer =  new StringBuffer();
      BufferedReader br = null;
      br = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
      String temp;
      while ((temp = br.readLine()) != null) {
       buffer.append(temp);
       buffer.append("\n");
      }
      //System.out.println("++++++++++++++++++:"+buffer.toString());

      return buffer.toString();
    }
    
    public String simplepostfare(String purl, String faredata){
        HttpPost httpRequest = new HttpPost(purl); 
        /*  
        * Post运作传送变量必须用NameValuePair[]数组储存
        * 2013/01/19 19:27:48 [debug] 18229#0: *14 http header: "Content-Type: application/x-www-form-urlencoded"
        * 2013/01/19 19:27:48 [debug] 18229#0: *14 http header: "Host: labs.rhomobi.com"
        * 2013/01/19 19:27:48 [debug] 18229#0: *14 http header: "Connection: Keep-Alive"
        * 2013/01/19 19:27:48 [debug] 18229#0: *14 http header: "User-Agent: Apache-HttpClient/UNAVAILABLE (java 1.4)"
        */
       List <NameValuePair> params = new ArrayList <NameValuePair>(); 
       //params.add(new BasicNameValuePair("str", "I am Post String"));
       params.add(new BasicNameValuePair("body", faredata));
       try 
       { 
       httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8)); 
  
       HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest); 
  
       if(httpResponse.getStatusLine().getStatusCode() == 200)
         { 
           String strResultPost = EntityUtils.toString(httpResponse.getEntity());
           //mTextView1.setText(strResultPost); 
           return strResultPost;
         } 
       else 
         { 
           //mTextView1.setText("Error Response: "+httpResponse.getStatusLine().toString()); 
           return "Error Response: "+httpResponse.getStatusLine().toString();
         } 
       } 
       catch (ClientProtocolException e) 
       {  
         //mTextView1.setText(e.getMessage().toString()); 
         e.printStackTrace(); 
       } 
       catch (IOException e) 
       {  
         //mTextView1.setText(e.getMessage().toString()); 
         e.printStackTrace(); 
       } 
       return "";
    }
    
}
