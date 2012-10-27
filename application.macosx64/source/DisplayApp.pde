import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;



import javax.swing.JFrame;

import processing.core.PApplet;
import processing.core.PFont;
import processing.opengl.PGraphicsOpenGL;
import processing.video.Capture;
import processing.video.Movie;

public class DisplayApp extends PApplet implements Runnable {

  /**
   	 * 
   	 */
  private static final long serialVersionUID = 1L;

  String addressOfFeedsList =  ""; // "http://www.ps3nyc.org/display/";

  String addressOfManualUpdateFile = null;

  static String userName = "parent";

  static String password = "hudson";

  static boolean reflect = false;

  static int videoSource = 0;

  // static Font headlineFont;
  static PFont headlineFont;

  static JFrame myFrame;

  String font = "Arial";

  static int proxyPort = 8002;

  static String proxyHost = "proxy";

  static int maxNumberOfContentLines = 3;

  static int headColor = 0x88000000;
  static int bgColor = 0xffffff;

  // static int headColor = 0xFF222222;

  // static int headColor = 0x88FFFFFF;

  static int contentColor = 0xFF222222;

  // static int contentColor = 0x88FFFFFF;

  // static boolean proxyTry = false;

  static FontMetrics headlineMetrics;

  // static Font contentFont;
  static PFont contentFont;

  static FontMetrics contentMetrics;

  static int headLineSize = 60;

  static int descriptionSize = 48;

  // ArrayList screens = new ArrayList();

  ArrayList<Screen> activeScreens = new ArrayList<Screen>();

  // int zDepth = -6000;

  static int width = 1024;

  public static int millisPerLetter = 35;

  public static int numOfTransitionFrames = 40;

  static boolean isFullScreen = false;

  static boolean isApplication = false;

  //public static MovieScreen movieScreen;

  static int scaler = 1;

  static int height = 768;

  static int topMargin = height / 6;

  static int sideMargin = width / 20;

  long lastChecked = 0;

  // BufferedImage offScreen;

  // Graphics2D offg;

  ArrayList<Feed> activeFeeds = new ArrayList<Feed>();

  ArrayList<Feed> masterFeeds = new ArrayList<Feed>();

  TreeMap segmentsOfTheDay = new TreeMap();

  // ArrayList backgroundElements = new ArrayList();

  int whichFeed = 0;

  public static String[] args;

  static DisplayApp myApplet;

  static boolean requireManualUpdate;

  long lastTouchFeedsFile;

  long lastDailyRecheck = System.currentTimeMillis();

  //final int millisInADay = 1000 * 60 * 60 * 24;

  static int updatePeriodMinutes = 60 * 24;

  String[] appletParams = { 
    "w", "h", "numOfTransitionFrames", "addressOfManualUpdateFile", "updatePeriodMinutes", "fullScreen", "topMargin", "sideMargin", "scale", "proxyPort", "proxyHost", "font", "contentColor", "headlineColor", "bgColor", "headlineFontSize", "contentFontSize", "requireManualUpdate", "millisPerLetter", "addressOfFeedsList", "userName", "password", "reflect", "videoSource", "maxNumberOfContentLines"
  };

  Capture video;// regular processing libary

  Tracker myTracker;

  ArrayList previewPoints = new ArrayList();

  int placeInPreviewPoints = 0;


  int placeInPoints = 0;

  public static void  needToCheckAgain() {
    requireManualUpdate = true;
  }
  public void movieEvent(Movie _movie) {
    _movie.read();
    //println("movie event");
  }
  public boolean okayToReadFeeds() {

    boolean ok = false;
    long fileDate = 0;

    if (requireManualUpdate == true && fileDate != lastTouchFeedsFile ) {
      if (addressOfManualUpdateFile  != null) {
        try {
          URL myURL = new URL(addressOfManualUpdateFile );
          HttpURLConnection urlConnection = (HttpURLConnection) myURL.openConnection();
          fileDate = urlConnection.getLastModified();
        } 
        catch (MalformedURLException e) {
          // TODO Auto-generated catch block
          // e.printStackTrace();
          System.out.println("Couldn't get url of addressOfManualUpdateFile " + addressOfManualUpdateFile );
          //return false;
        } 
        catch (IOException e) {
          System.out.println("Couldn't read url of addressOfManualUpdateFile " + addressOfManualUpdateFile );
          //return false;
        }
      }
      readInFeedsList();
      println(addressOfManualUpdateFile + " touched " + fileDate);
      lastTouchFeedsFile = fileDate;
      ok = true;
    } 
    else if (requireManualUpdate == false && System.currentTimeMillis() > lastDailyRecheck + updatePeriodMinutes * 1000 * 60 ) {
      System.out.println("Haven't Checked Feeds.txt for " + updatePeriodMinutes + " minutes");
      readInFeedsList();
      println("periodic check " + updatePeriodMinutes );
      lastDailyRecheck = System.currentTimeMillis();
      ok = true;
    } //else if (requireManualUpdate == true) {
    //requireManualUpdate =false;
    //ok = true;

    //}
    //println("okay?" + ok);
    return ok;
  }

  public void forceFeed() {
    // if (okayToReadFeeds()){
    System.out.println("ForceFeed");
    for (int i = 0; i < activeFeeds.size(); i++) {
      Feed thisFeed = (Feed) activeFeeds.get(i);
      thisFeed.check(true);
    }
    // }
  }

  public void readInConfig() {

    ArrayList thisSegment = new ArrayList();

    BufferedReader in = null;
    if (isApplication) {
      try {
        println("Config.txt");
        FileReader fr = new FileReader("Config.txt");
        in = new BufferedReader(fr);
      } 
      catch (FileNotFoundException e) {
        System.out.println("Couldn't find local file Config.txt, will look on net");
      }
    }
    if (in == null) {
      try {
        URL myURL = new URL(addressOfFeedsList + "Config.txt");
        HttpURLConnection urlConnection = (HttpURLConnection) myURL.openConnection();

        urlConnection.connect();

        InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());
        in = new BufferedReader(reader);
      } 
      catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        // e.printStackTrace();
        System.out.println("Couldn't get url of Config.txt" + addressOfFeedsList);
      } 
      catch (IOException e) {
        System.out.println("Couldn't read url of Config.txt" + addressOfFeedsList);
      }
    }
    if (in != null) 
    try {

      // FileInputStream fileIn = new FileInputStream("Feeds.txt");
      // BufferedReader in = new BufferedReader((new
      // InputStream(fileIn));
      String input = in.readLine();
      ArrayList parameters = new ArrayList();
      while (input != null) {
        String comment = "//";
        if (input.startsWith(comment)) {
          input = in.readLine();
          continue;
        }
        String[] parts = input.split("//");
        input = parts[0];
        parameters.add(input);
        //System.out.println("Added " + input);
        input = in.readLine();
      }
      String[] output = new String[parameters.size()];
      parameters.toArray(output);
      getParams(output);
    } 
    catch (IOException e) {
      System.out.println("Trouble reading " + "Display.config");
    }
  }


  public void readInFeedsList() {

    ArrayList thisSegment = new ArrayList();

    BufferedReader in = null;
    if (isApplication) {
      try {
        FileReader fr = new FileReader(sketchPath("Feeds.txt"));
        in = new BufferedReader(fr);
      } 
      catch (FileNotFoundException e) {
        System.out.println("Couldn't find local file Feeds.txt, will look on net");
      }
    }
    if (in == null) {
      try {
        URL myURL = new URL( addressOfFeedsList + "Feeds.txt");
        HttpURLConnection urlConnection = (HttpURLConnection) myURL.openConnection();

        urlConnection.connect();

        InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());
        in = new BufferedReader(reader);
      } 
      catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        // e.printStackTrace();
        System.out.println("Couldn't get url of feeds.txt" + addressOfFeedsList);
      } 
      catch (IOException e) {
        System.out.println("Couldn't read url of feeds.txt" + addressOfFeedsList);
      }
    }
    if (in == null) {
      Feed defaultFeed = new Feed("http://ps3nyc.org/defaultBlog.xml");
      thisSegment.add(defaultFeed);
      segmentsOfTheDay.put(new Integer(0), thisSegment);
    } 
    else {
      try {

        // FileInputStream fileIn = new FileInputStream("Feeds.txt");
        // BufferedReader in = new BufferedReader((new
        // InputStream(fileIn));
        String input = in.readLine();
        while (input != null   ) {
          String comment = "//";
          if (input.startsWith(comment) || input.trim().length()== 0) {
            input = in.readLine();
            continue;
          }
          if (input.length() < 7 && input.indexOf(":") != -1) {
            String[] time = input.split(":");

            if (time.length > 1) {
              int minutes = Integer.parseInt(time[0]) * 60 + Integer.parseInt(time[1]);
              // maybe if thisSegment is not empty I should add
              // the
              // other one under time 0
              thisSegment = new ArrayList();
              segmentsOfTheDay.put(new Integer(minutes), thisSegment);
            }
            //System.out.println("Added Time" + input);
          } 
          else {

            Feed thisFeed = null;
            for (int i = 0; i < masterFeeds.size(); i++) {
              Feed thisMaster = (Feed) masterFeeds.get(i);
              if (thisMaster.getAddress().equals(input)) {
                thisFeed = thisMaster;
                break;
              }
            }
            if (thisFeed == null) {
              if (input.indexOf("http") != -1) {
                if (input.indexOf("flickr.com") != -1) {
                  thisFeed = new FlikrFeed(input);
                } 
                else {
                  thisFeed = new Feed(input);
                }
              } 
              else {
                thisFeed = new DirectoryFeed(input);
                System.out.println("NEw direcroy feed" + input);
              }
              masterFeeds.add(thisFeed);
            }
            thisSegment.add(thisFeed);
            //System.out.println("Added Feed" + input);
          }
          input = in.readLine();
        }
        // in case they did not specify times
        if (thisSegment.size() > 0 && segmentsOfTheDay.size() == 0) {
          segmentsOfTheDay.put(new Integer(0), thisSegment);
        }
      } 
      catch (IOException e) {
        System.out.println("Trouble reading " + "Feeds.prefs");
      }
    }
  }

  public void draw() {
    // fill(100, 100, 100);
    hint(ENABLE_DEPTH_SORT);
    if (millis()- lastChecked > 60000) {
      lastChecked = millis();
      if (okayToReadFeeds()) {
        for (int i = 0; i < activeFeeds.size(); i++) {
          Feed thisFeed = (Feed) activeFeeds.get(i);
          thisFeed.check(true);
        }
      }
      checkSegmentOfDay();
    }
    animate();
    //movieScreen.jiggle();
  }

  public void getParams(String[] args) {
    System.out.println("--Incoming Parameters");
    for (int i = 0; i < args.length; i++) {
      String name = null;
      String value = null;
      try {

        if (isApplication) {
          String pair = args[i];
          String parts[] = pair.split("=");
          if (parts.length != 2) continue;
          name = parts[0].trim();
          value = parts[1].trim();
        } 
        else {
          name = args[i];
          value = getParameter(name);

          if (value == null) continue;
        }
        value = value.replaceAll("\\+", " ");
        if (name.toLowerCase().equals("headlinecolor")) {
          try {
            headColor = Integer.parseInt(value, 16);
          } 
          catch (Exception e) {
            System.out.println("Didn't like headColor try like this FF00FF for purple");
          }
        } 
        else if (name.toLowerCase().equals("contentcolor")) {
          try {
            contentColor = Integer.parseInt(value, 16);
          } 
          catch (Exception e) {
            System.out.println("Didn't like contentColor try like this FF00FF for purple");
          }
        } 
        else if (name.toLowerCase().equals("bgcolor")) {
          try {
            bgColor = Integer.parseInt(value, 16);
          } 
          catch (Exception e) {
            System.out.println("Didn't like bgColor try like this FF00FF for purple");
          }
        }
        else if (name.toLowerCase().equals("headlinefontsize")) {
          headLineSize = Integer.parseInt(value);
          System.out.println("headlinesize: " + value);
        } 
        else if (name.toLowerCase().equals("contentfontsize")) {
          descriptionSize = Integer.parseInt(value);
          System.out.println("contentFontSize: " + value);
        } 
        else if (name.toLowerCase().equals("font")) {
          font = value;
          System.out.println("font: " + value);
        } 
        else if (name.toLowerCase().equals("w")) {
          width = Integer.parseInt(value);
          System.out.println("Width: " + value);
        }
        if (name.toLowerCase().equals("h")) {
          height = Integer.parseInt(value);
          System.out.println("Height: " + value);
        }
        if (name.toLowerCase().equals("scale")) {
          scaler = Integer.parseInt(value);
          System.out.println("Scaler: " + value);
        }
        if (name.toLowerCase().equals("fullscreen")) {
          isFullScreen = value.toLowerCase().equals("true");
          System.out.println("Full Screen: " + value);
        }
        if (name.toLowerCase().equals("proxyhost")) {
          System.setProperty("http.proxyHost", value);
          System.out.println("Using Proxy Host" + value);
          proxyHost = value;
          // System.setProperty("http.proxyPort", "");
        }
        if (name.toLowerCase().equals("proxyport")) {
          System.setProperty("http.proxyPort", value);
          proxyPort = Integer.parseInt(value);
          System.out.println("Using Proxy Port" + value);
        }
        if (name.toLowerCase().equalsIgnoreCase("requireManualUpdate")) {
          requireManualUpdate = value.toLowerCase().equals("true");
          System.out.println("Requires Manual Update of Feeds File " + requireManualUpdate);
        }
        if (name.toLowerCase().equalsIgnoreCase("millisPerLetter")) {
          millisPerLetter = Integer.parseInt(value);
          System.out.println("Milliseconds Per Letter " + millisPerLetter);
        }
        if (name.toLowerCase().equalsIgnoreCase("topMargin")) {
          topMargin= Integer.parseInt(value);
          System.out.println("Top Margin " + topMargin);
        }
        if (name.toLowerCase().equalsIgnoreCase("updatePeriodMinutes")) {
          updatePeriodMinutes = Integer.parseInt(value);
          System.out.println("updatePeriodMinutes " + updatePeriodMinutes);
        }
        if (name.toLowerCase().equalsIgnoreCase("numOfTransitionFrames")) {
          numOfTransitionFrames = Integer.parseInt(value);
          System.out.println("numOfTransitionFrames " + numOfTransitionFrames);
        }

        if (name.toLowerCase().equalsIgnoreCase("sideMargin")) {
          sideMargin = Integer.parseInt(value);
          System.out.println("Side Margin " + sideMargin);
        }
        if (name.toLowerCase().equalsIgnoreCase("addressOfFeedsList")) {
          addressOfFeedsList = "http://" + value;
          System.out.println("addressOfFeedsList " + addressOfFeedsList);
        }
        if (name.toLowerCase().equalsIgnoreCase("addressOfManualUpdateFile")) {
          addressOfManualUpdateFile = "http://" + value;
          System.out.println("addressOfManualUpdateFile " + addressOfManualUpdateFile);
        }
        if (name.toLowerCase().equalsIgnoreCase("userName")) {
          userName = value;
          System.out.println("userName " + userName);
        }
        if (name.toLowerCase().equalsIgnoreCase("password")) {
          password = value;
          System.out.println("password " + password);
        }
        if (name.toLowerCase().equalsIgnoreCase("reflect")) {
          reflect = value.toLowerCase().equals("true");
          System.out.println("reflect " + password);
        }
        if (name.toLowerCase().equalsIgnoreCase("videoSource")) {
          videoSource = Integer.parseInt(value);
          System.out.println("VideoSource: " + value);
        }
        if (name.toLowerCase().equalsIgnoreCase("maxNumberOfContentLines")) {
          maxNumberOfContentLines = Integer.parseInt(value);
          System.out.println("maxNumberOfContentLines: " + value);
        }


        // if (name.toLowerCase().equals("proxytry")) {
        // proxyTry = value.toLowerCase().equals("true");
        // System.out.println("Tyring proxy url construction");
        // }
      } 
      catch (NumberFormatException e) {
        System.out.println("Problem with number format " + name + " " + value);
      }
    }
  }
static public void main(String _args[]) {
    args = _args;
  isApplication = true;
      boolean madeCache = (new File("cache")).mkdir();
    System.out.println("Made Cache = " + madeCache);
PApplet.main(new String[] { "--present", "DisplayApp" });
}
  /*public static void main(String[] _args) {
    args = _args;

    myApplet = new DisplayApp();

    // myApplet.size(800, 600, myApplet.OPENGL);
    isApplication = true;
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    GraphicsDevice[] devs = ge.getScreenDevices();
    if (devs.length > 1) {
      gd = ge.getScreenDevices()[1];
    }
    myFrame = new JFrame(gd.getDefaultConfiguration());
    myFrame.setBackground(Color.BLACK);
    myFrame.getContentPane().add(myApplet, BorderLayout.CENTER);
    height = myApplet.height ;  //was screen.height...
    width = myApplet.width ;
    //myFrame.setSize(width, height);
    //myFrame.setUndecorated(true);
    //myFrame.setVisible(true);
    // myFrame.getGraphics().setColor(Color.BLACK);
    // myFrame.getGraphics().fillRect(0, 0, width, height);

    myFrame.setTitle("PS3 Display");
    boolean madeCache = (new File("cache")).mkdir();
    System.out.println("Made Cache = " + madeCache);
    myApplet.init();
    myFrame.setSize(width, height);
    myFrame.setUndecorated(true); 
    myFrame.setVisible(true);
    // myApplet.start();

    if (isFullScreen && isApplication) {
      setFullScreen(gd, myFrame, width, height);
    }
    myFrame.setBackground(new Color(bgColor));
  }*/

  static public void setFullScreen() {
    setFullScreen(null, myFrame, width, height);
  }

  static public void setFullScreen(GraphicsDevice _gd, JFrame _frame, int _w, int _h) {
    //System.out.println("FullScreen");
    if (_gd == null) {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      _gd = ge.getDefaultScreenDevice();
      // GraphicsDevice[] devs = ge.getScreenDevices();
      // if (devs.length > 1) {
      // _gd = ge.getScreenDevices()[1];
      // }
    }
    _gd.setFullScreenWindow(_frame);
    _frame.requestFocus();

    DisplayMode[] affichages = _gd.getDisplayModes();
    int i;
    for (i = 0; i < affichages.length; i++) {
      if ((affichages[i].getWidth() == _w) && (affichages[i].getHeight() == _h) && (affichages[i].getBitDepth() == 32)) {
        // if((affichages[i].getWidth()==320)&&(affichages[i].getHeight()==240)&&(affichages[i].getBitDepth()==32)){

        // if((affichages[i].getWidth()==1025)&&(affichages[i].getHeight()==768)&&(affichages[i].getBitDepth()==32)){
        _gd.setDisplayMode(affichages[i]);
      }
    }
  }

  static class MyAuthenticator extends Authenticator {
    public PasswordAuthentication getPasswordAuthentication() {
      // I haven't checked getRequestingScheme() here, since for NTLM
      // and Negotiate, the usrname and password are all the same.
      //System.out.println("Feeding username and password for " + getRequestingScheme());
      return (new PasswordAuthentication(userName, password.toCharArray()));
    }
  }

  public void showFonts() {
    System.out.println("---Possible Fonts:");
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    String[] names = ge.getAvailableFontFamilyNames();
    for (int i = 0; i < names.length; i++) {
      System.out.println(names[i]);
    }
  }

 // public void init() {
  //  super.init();
 // }

  public void setup() {
    System.out.println("SetUp");
    size(displayWidth, displayHeight, P3D);

    // frameRate(60);
    if (reflect) {
      println(Capture.list());
      video = new Capture(this, 320, 240, Capture.list()[videoSource]);

      myTracker = new Tracker(this, video);
    }
    for (int i = 0; i < 30; i++) {
      previewPoints.add(new PreviewPoint((int) random(width), (int) random(height), (float) random(6)));
    }

    //movieScreen = new MovieScreen(myFrame, this);
    // showFonts();
    System.out.println("Escape=Quit");
    System.out.println("a= check feeds");
    System.out.println("f= list fonts");
    System.out.println("p= list parameters");
    readInConfig() ;
    if (isApplication) {
      getParams(args);
    } 
    else {
      getParams(appletParams);
    }

    width = width / scaler;
    //topMargin = width / 6;
    height = height / scaler;
    System.out.println("Init");
    // offScreen = new BufferedImage(width, height,
    // BufferedImage.TYPE_INT_RGB);
    // offg = offScreen.createGraphics();

    Font headlineFontj = new Font(font, Font.PLAIN, headLineSize / scaler);
    Font contentFontj = new Font(font, Font.PLAIN, descriptionSize / scaler);
    headlineFont = createFont(font, headLineSize / scaler);
    contentFont = createFont(font, descriptionSize / scaler);

    headlineMetrics = getFontMetrics(headlineFontj);
    contentMetrics = getFontMetrics(contentFontj);

    // showFonts();
    if (isApplication) {
      Authenticator.setDefault(new MyAuthenticator());
    }
    // First set the Proxy settings on the System
    // Properties systemSettings = System.getProperties();
    // systemSettings.put("http.proxyHost","myproxy.com") ;
    // systemSettings.put("http.proxyPort", "80") ;
    readInFeedsList();

    checkSegmentOfDay();
    forceFeed();
    // ps = new QTLivePixelSource(320, 240, 30);
    // ps.setImageType(BufferedImage.TYPE_INT_RGB);
  }

  public void checkSegmentOfDay() {
    if (segmentsOfTheDay.size() == 0) {
      //in response to error at startup
      System.out.println("Segment Size == 0  can't find any segments");
      return;
    }
    else if (segmentsOfTheDay.size() == 1) {
      // Object[] keys = (Object[]) segments.keySet().toArray();
      //System.out.println("Use Default Feed");
      activeFeeds = (ArrayList) segmentsOfTheDay.get(segmentsOfTheDay.firstKey());// .get((Integer)

      // keys[0]);
      // System.out.println("Got Here");
    } 
    else {
      Date now = new Date();

      int minutesNow = now.getHours() * 60 + now.getMinutes();
      Object[] keys = (Object[]) segmentsOfTheDay.keySet().toArray();
      int afterWhichSegmentTime = -1;
      for (int i = 0; i < keys.length; i++) {
        Integer thisKey = (Integer) keys[i];
        if (minutesNow > thisKey.intValue()) {
          afterWhichSegmentTime = i;
        }
      }
      if (afterWhichSegmentTime == -1) {
        // System.out.println("Before all the segments, use the last one
        // from the night before");
        activeFeeds = (ArrayList) segmentsOfTheDay.get(keys[keys.length - 1]);
      } 
      else {
        int secs = ((Integer) keys[afterWhichSegmentTime]).intValue();

        activeFeeds = (ArrayList) segmentsOfTheDay.get(keys[afterWhichSegmentTime]);
        // System.out.println("After " + (secs / 60) + " " + (secs % 60)
        // + " " + activeFeeds.size());
      }
    }
    // getPreviews();
  }

  public void captureEvent(Capture whichCapture) {
    video.read();
  }

  public void animate() {

    background(bgColor);

    for (int i = activeScreens.size() - 1; i > -1; i--) {

      Screen thisScreen = (Screen) activeScreens.get(i);

      int mode = thisScreen.move(this);
      if (mode == Screen.STARTOUT) {
        nextScreen();
      } 
      else if (mode == Screen.GONE) {
        thisScreen.unload();
        activeScreens.remove(i);
        // previewScreens.remove(thisScreen);
      }
    }

    if (activeScreens.size() == 0) {
      nextScreen();
    }

    if (video != null) {
      ArrayList newPoints = myTracker.getChangeEdges(previewPoints);
      // println("new points " + newPoints.size());
      for (int i = 0; i < previewPoints.size(); i++) {
        PreviewPoint thisPoint = (PreviewPoint) previewPoints.get(i);
        if (thisPoint.found == false) continue;
        ScreenElement se = thisPoint.getScreenElement();

        if (se != null  && se.screen.mode == Screen.GONE) se.preview(this, width*2 - thisPoint.x*width/myTracker.video.width, thisPoint.y*height/myTracker.video.height-height, thisPoint.angle);
      }
      pushMatrix();
      translate(0, 0, -1);
      scale(-1.0f, 1.0f);
      tint(200, 200, 200, 150);
      // blend(video, 0, 0,video.width,video.height,0,0,width,height,ADD );

      image(video, -(width + 5), -5, width + 10, height + 10);
      noTint();
      popMatrix();
    }
    /*
		 * strokeWeight(2); for (int row = 0; row < video.height-2; row = row +2) { int offset = row* video.width ; for (int col = 0; col < video.width-2; col = col + 2) {
     		 * 
     		 * int thisPixel = video.pixels[offset+ col]; stroke(thisPixel); fill(thisPixel);
     		 * 
     		 * //translate(2,0,0); //ellipse(0,0,4,4); pushMatrix(); translate(width- col*4,row*4,-50); rect(0,0,4,4); //point(0,0); popMatrix(); //ellipse(col*2,row*2,4,4); } //translate(-video.width,2,0); }
     		 */
    /*
		 * for (int i = 0; i < previewPoints.size(); i++) { PreviewPoint expectedPoint = (PreviewPoint) previewPoints.get(i); pushMatrix(); translate(width - expectedPoint.x, expectedPoint.y, -10); ellipse(0, 0, 10, 10); popMatrix(); }
     		 */

    // pushMatrix();
    // translate(0,0,-10);
    // popMatrix();
  }

  public void addToPreview(Screen _screen) {
    ArrayList elements = _screen.getPreviewElements();

    for (int i = 0; i < elements.size(); i++) {
      ScreenElement element = (ScreenElement) elements.get(i);
      //element.setPreview(true);
      placeInPreviewPoints++;
      if (placeInPreviewPoints >= previewPoints.size()) placeInPreviewPoints = 0;
      PreviewPoint thisPoint = (PreviewPoint) previewPoints.get(placeInPreviewPoints);
      thisPoint.setScreenElement(element);
    }
  }

  public void nextScreen() {
    try {
      if (activeFeeds.size() > 0) {
        if (whichFeed >= activeFeeds.size()) whichFeed = 0;
        Feed thisFeed = (Feed) activeFeeds.get(whichFeed);

        Screen newScreen = thisFeed.getScreen();


        if (newScreen != null) {
          //System.out.println(thisFeed.address + " New Screen" + newScreen.uniqueID );
          Screen future = null;
          try {
            future  = thisFeed.preloadFutureScreens();
          } 
          catch (Exception e) {
            System.out.println("Problem with new Screen " + e);
          }
          if (future != null) addToPreview(future);
          // placeInPreviewElements = ((Integer) screensPlaceInAllPreviewElements.get(future)).intValue();

          newScreen.load();

          // newScreen.setEndPoints(mouseX, mouseY, 0, mouseX, mouseY, 0,
          // false);

          newScreen.start();

          activeScreens.add(newScreen);
        }

        whichFeed++;
        //if (whichFeed >= activeFeeds.size()) whichFeed = 0;
      }
    } 
    catch (Exception e) {
      System.out.println("Problem in Next Screen " + e);
    }
  }

  public void mouseReleased() {
    if (mouseX > 19 * width / 20 && mouseY < height / 20) {
      this.exit();
    }
  }

  public void keyPressed(KeyEvent _key) {
    // TODO Auto-generated method stub
    // System.out.println(KeyEvent.getKeyText(_key.getKeyCode()).equals("Escape"));
    if (KeyEvent.getKeyText(_key.getKeyCode()).equals("Escape")) {
      System.exit(5);
    } 
    else if (KeyEvent.getKeyText(_key.getKeyCode()).toLowerCase().equals("a")) {
      forceFeed();
    } 
    else if (KeyEvent.getKeyText(_key.getKeyCode()).toLowerCase().equals("f")) {
      showFonts();
    } 
    else if (KeyEvent.getKeyText(_key.getKeyCode()).toLowerCase().equals("p")) {
      showParams();
    }
  }

  void showParams() {
    System.out.println("---Possible Params:");
    for (int i = 0; i < appletParams.length; i++) {
      System.out.println(appletParams[i]);
    }
  }
}

