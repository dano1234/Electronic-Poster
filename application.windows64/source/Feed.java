import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

class Feed extends Thread {
	// long lastChange;

	// long lastTimeCheckedWeb;

	ArrayList screens = new ArrayList(); // all the screens made from this
	// feed

	int placeInScreens = 0; // current place shwoing in this feed

	// int periodForChecking = 3 * 60000; // check every minute

	String address; // url of this screen

	String overallDescription = ""; // for the promotion screen

	int addressScreenEvery = 0;

	String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };

	public Feed(String _url) {
		// System.out.println("New Screen");
		// long now = System.currentTimeMillis();
		// lastTimeCheckedWeb = now - periodForChecking;
		String[] urlParams = _url.split(" ");
		if (urlParams.length > 1) {
			try {
				addressScreenEvery = Integer.parseInt(urlParams[1]);
			} catch (Exception e) {
				System.out.println("Bad number for feed promo ratio");
			}
		}
		address = _url;

	}

	public Screen preloadFutureScreens() {

		Screen newScreen = null;
		if (screens.size() > 0) {
			int future = placeInScreens + 1;
			if (future > screens.size() - 1) {
				future = 0;
			}

			newScreen = (Screen) screens.get(future);

			newScreen.load();
		}
		return newScreen;
	}

	public Screen getScreen() {
		if (screens.size() == 0)
			return null;
		placeInScreens++;
		if (placeInScreens > screens.size() - 1) {
			placeInScreens = 0;
		}

		Screen newScreen = (Screen) screens.get(placeInScreens);

		// System.out.println( "place in screen" + placeInScreens + " of " +
		// screens.size());

		return newScreen;
	}

	public String getAddress() {
		return address;
	}

	public void check(boolean _force) {
		// if (_force || System.currentTimeMillis() - lastTimeCheckedWeb >
		// periodForChecking) {
		// lastTimeCheckedWeb = System.currentTimeMillis();
		Checker check = new Checker();
		check.start();
		// }

	}

	public void finishedCheck(ArrayList _screens) {
		screens = _screens;
		System.out.println(_screens.size() + " Finished Checking" + address);
	}

	public String[] getItems(String _rawXML) {
		return Feed.getArray("item", _rawXML);
	}

	public String getOverallDescription(String _rawXML) {
		String[] overallDescriptionArray = Feed.getArray("description", _rawXML);

		if (overallDescriptionArray.length > 0)
			return overallDescriptionArray[0];
		else
			return "";
	}

	public String getTitle(String _thisItem) {
		String[] titles = Feed.getArray("title", _thisItem);
		if (titles.length > 0)
			return titles[0];
		else
			return "";
	}

	public String getDescription(String _thisItem) {
		String[] descriptions = Feed.getArray("description", _thisItem);
		if (descriptions.length > 0) {
			descriptions[0] = descriptions[0].replaceAll("<!\\[CDATA\\[", "");
			descriptions[0] = descriptions[0].replaceAll("\\]\\]>", "");
			return descriptions[0];
		} else
			return "";
	}

	public String getUniqueID(String _thisItem) {
		String[] ids = Feed.getArray("guid", _thisItem);

		if (ids.length > 0)
			return ids[0];
		else
			return "";
	}

	// public void setContent(String _content){

	// }
	public String getContent(String _thisItem) {
		String[] contents = Feed.getArray("content:encoded", _thisItem);
		if (contents.length > 0) {
			String content = contents[0];
			content = content.replaceAll("<!\\[CDATA\\[", "");
			content = content.replaceAll("\\]\\]>", "");

			return content;
		} else
			return "";
	}

	public String getModDate(String _thisItem) {
		String[] getPubDates = Feed.getArray("pubDate", _thisItem);
		if (getPubDates.length > 0)
			return getPubDates[0];
		else
			return "";
	}

	public Calendar[] getEventDates(String _thisItem) {
		String[] allDates = Feed.getArray("div id='ftcal_post_schedule'", "/div", _thisItem);
	//	 System.out.println("number of dates" + allDates.length);

		ArrayList calendars = new ArrayList<GregorianCalendar>(); // eventDate =
																	// new
																	// Calendar[0];
		String rawDescription = getDescription(_thisItem);
		// String[] eventDates = new String[0];
		// String[] multipleDates = new String[0];
		// String tellTaleOfDate = "<![CDATA[["; //there is an extra bracket at
		// the end if it has a Adate
		// String tellTaleOfDate = "["; // there is an extra bracket at the end
		// if it has a date

		/*
		 * if (rawDescription.startsWith(tellTaleOfDate)) {
		 * 
		 * String justDate = rawDescription.substring(tellTaleOfDate.length(),
		 * rawDescription.length()); int end = justDate.indexOf("]"); justDate =
		 * justDate.substring(0, end); // System.out.println("Date----------- "
		 * + justDate);
		 * 
		 * multipleDates = justDate.trim().split(" to "); }
		 */

		// if (dates.length > 0 ) {
		// String noHTMLString = dates[0].replaceAll("\\<.*?\\>", "");

		// System.out.println("AHA " + noHTMLString);
		// }
		// for (int i = 0; i < multipleDates.length; i++){
		if (allDates.length > 0) {
			String[] eventDates = allDates[0].split("</span>"); // id='ftcal-.*?'","/span",_thisItem);

			System.out.println("This Guy" + eventDates.length);
			for (int i = 0; i < eventDates.length; i++) {
				// String thisGuy =
				// multipleDates[i].trim().replaceAll("\\<.*?\\>", "");
				eventDates[i] = eventDates[i].trim().replaceAll("\\<.*?\\>", "");
				try {

					// = multipleDates[0].trim().split("</span>");
					System.out.println(eventDates.length + " lenghth " + eventDates[i]);
					// if (multipleDates[0].length() < 10){ //to phrase is just
					// a time
					// continue;
					// }else if (eventDates.length > 0) {
					//String dateAndTime[] = eventDates[i].split("-");
					String dateAndTime[] = eventDates[i].split(":",2);
					String dater = dateAndTime[0];
					//dateAndTime[0] = eventDates[0].replaceAll(";", " ");
					//dateAndTime[0] = eventDates[0].replaceAll("\\.", " ");
					String[] dateParts = dater.split(",");
					String[] monthParts = dateParts[1].trim().split(" ");
					String monthName = monthParts[0].trim();
					// if (monthParts.length < 2)continue;
					int dayNumber = Integer.parseInt(monthParts[1].trim());
					String[] yearAndTime = dateParts[2].trim().split(" ");
					if (yearAndTime.length > 1) {
						dateParts[2] = yearAndTime[0]; // take time off of it
					}
					int yearNumber = Integer.parseInt(dateParts[2].trim());
					int monthNumber = 0;
					for (int m = 0; m < months.length; m++) {
						if (months[m].equals(monthName)) {
							monthNumber = m;
							break;
						}
					}
					if (dateAndTime.length == 2) { // time also
						// System.out.println("time too" );
						String time = dateAndTime[1];
						// System.out.println("time too" + eventDates[1]);
						String[] timeParts = time.trim().split(" ");
						String[] numberParts = timeParts[0].split(":");
						int hour = Integer.parseInt(numberParts[0]);
						int min = Integer.parseInt(numberParts[1]);
						if (timeParts[1].trim().equals("pm"))
							hour = hour + 12;
						calendars.add(new GregorianCalendar(yearNumber, monthNumber, dayNumber, hour, min));
					} else {
						calendars.add(new GregorianCalendar(yearNumber, monthNumber, dayNumber));

					}
					// System.out.println(eventDate.toString());

					// }
				} catch (Exception e) {
					System.out.println("Error getting calendar" + e);

				}
			}
		}
		Calendar[] outputArray = new Calendar[calendars.size()];
		calendars.toArray(outputArray);
		return outputArray;
	}

	/*
	 * public String getVideo(String _thisItem) { String urlString = null; int
	 * end = _thisItem.indexOf(".mov"); if (end != -1) { int start =
	 * _thisItem.lastIndexOf("href=", end); urlString =
	 * _thisItem.substring(start + 6, end + 4); } return urlString; }
	 */
	public String[] getVideos(String _thisItem) {
		// System.out.println("get Picture" +address);
		ArrayList allStrings = new ArrayList();
		int startingPos = 0;

		String content = getContent(_thisItem);
		content = content.replaceAll("\\<nod\\>.*\\<yod\\>", "");

		while (true) {
			String urlString = null;

			int end = _thisItem.indexOf(".mov", startingPos);
			if (end != -1) {
				int start = _thisItem.lastIndexOf("href=", end);
				urlString = _thisItem.substring(start + 6, end + 4);
				// System.out.println(urlString);
				allStrings.add(urlString);
				startingPos = end + 4;
			} else {
				break;
			}
		}
		if (allStrings.size() == 0)
			allStrings.add(null); // hack to not allow lack of a picture from
									// stopping it going once
		String[] returnArray = new String[allStrings.size()];
		allStrings.toArray(returnArray);
		return returnArray;
	}

	public String[] getPictures(String _thisItem) {
		// System.out.println("get Picture" +address);
		ArrayList allStrings = new ArrayList();
		int startingPos = 0;

		String content = getContent(_thisItem);
		content = content.replaceAll("\\<nod\\>.*\\<yod\\>", "");

		while (true) {
			String urlString = null;
			int beg = content.indexOf("<img", startingPos);

			if (beg != -1) {
				beg = content.indexOf("src=", beg);

				// bottomOfHeadlines = bottomOfHeadlines +
				// DisplayApp.headLineSize /
				// DisplayApp.scaler;

				int end = content.indexOf(" ", beg + 5);

				urlString = content.substring(beg + 5, end - 1);
				urlString = urlString.replaceAll(".thumbnail", "");
				urlString = urlString.replaceAll("-150x150", "");
				// System.out.println("url" + urlString);
				allStrings.add(urlString);
				startingPos = end;
			} else {
				break;
			}
		}
		if (allStrings.size() == 0)
			allStrings.add(null); // hack to not allow lack of a picture from
									// stopping it going once
		String[] returnArray = new String[allStrings.size()];
		allStrings.toArray(returnArray);
		return returnArray;
	}

	/*
	 * public String getMovie(String _thisItem){ int end =
	 * contents[0].indexOf(".mov"); } else if (end != -1){
	 * 
	 * int start = contents[0].lastIndexOf("href=", end);
	 * 
	 * String urlString = contents[0].substring(start + 6, end + 4); } }
	 */

	public ArrayList parseContents(String _rawContents) {
		// System.out.println("parse xml" +address);

		String[] items = getItems(_rawContents);
		ArrayList screensInWaiting = new ArrayList();
		overallDescription = getOverallDescription(_rawContents);
		for (int i = 0; i < items.length; i++) {
			String thisItem = items[i];
			String title = getTitle(thisItem);
			String description = getDescription(thisItem);
			Calendar[] eventDates = getEventDates(thisItem);
			// System.out.println(title + " " + eventDates.length);
			String content = getContent(thisItem);
			// System.out.println(content + "----" + description);
			if (content.equals(description)) {
				content = ""; // if there is only date info in the contents.
			}
			String modDate = getModDate(thisItem);
			String uniqueID = getUniqueID(thisItem);
			String[] videoURLs = getVideos(thisItem);
			if (videoURLs.length > 1)
				title = "";
			// for(int u = 0; u < videoURLs.length; u++){
			// System.out.println("Video name" + videoURLs[u]);
			// }
			String[] pictureURLs = getPictures(thisItem);
			// if (pictureURLs[0] == null) pictureURLs = videoURLs;
			// String videoURL = getVideo(thisItem);
			if (pictureURLs.length > 1)
				title = "";
			boolean tooOld = false;

			for (int k = 0; k < Math.max(pictureURLs.length, videoURLs.length); k++) {
				String pictureURL = null;
				if (k < pictureURLs.length) {
					pictureURL = pictureURLs[k];
					uniqueID = uniqueID + pictureURL;
				}
				// System.out.println("Pictures" + pictureURL);

				String videoURL = null;
				if (k < videoURLs.length) {
					videoURL = videoURLs[k];
					uniqueID = uniqueID + videoURL;
				}
				// System.out.println("Videos" +videoURL);
				// only date in content
				if (content.trim().startsWith("[ ") && content.trim().endsWith(";  ]")) {
					content = "";
				}
				String dates = "";
				String to = "";
				for (int j = 0; j < eventDates.length; j++) {
					// if (eventDate != null) {
					GregorianCalendar today = new GregorianCalendar();
					if (eventDates[j].before(today)) { // old
						// System.out.println("too old" + title);
						tooOld = true;
						break;
					}
					String[] days = { "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
					String[] ampm = { "am", "pm" };
					String monthName = months[eventDates[j].get(Calendar.MONTH)];
					String dayName = days[eventDates[j].get(Calendar.DAY_OF_WEEK)];
					String partOfDay = ampm[eventDates[j].get(Calendar.AM_PM)];

					String time = "";
					if (eventDates[j].get(Calendar.HOUR) != 0 || eventDates[j].get(Calendar.MINUTE) != 0) {
						String minutes = ":" + eventDates[j].get(Calendar.MINUTE);
						if (eventDates[j].get(Calendar.MINUTE) == 0)
							minutes = "";
						time = "\n" + eventDates[j].get(Calendar.HOUR) + minutes + " " + partOfDay;
					}
					dates = dates + to + "\n" + dayName + "\n" + monthName + " " + eventDates[j].get(Calendar.DAY_OF_MONTH) + time;
					to = "\n" + "to" + "\n";
				}
				if (tooOld) {
					// System.out.println("too old second break" + title);
					break;
				}
				// if (tooOld ) continue;
				// System.out.println("titel" + title);
				title = title + dates;
				//
				boolean foundExistingScreen = false;

				for (int j = 0; j < screens.size(); j++) {
					Screen thisScreen = (Screen) screens.get(j);
					if (uniqueID.equals(thisScreen.getUniqueID())) {
						foundExistingScreen = true;
						// System.out.println(modDate + " dates " +
						// thisScreen.getDate());
						if (modDate.equals(thisScreen.getDate())) {
							// keep screen the way it is;
							// advertiseThisFeed(screensInWaiting);
							// System.out.println("Nothing Changed " +
							// headlines[0]);
							screensInWaiting.add(thisScreen);
							// System.out.println(uniqueID + " Not Updated " +
							// thisScreen.getUniqueID());
						} else {
							// make a new one using updated info
							System.out.println("Updated " + thisScreen.getUniqueID());
							// advertiseThisFeed(screensInWaiting);

							screensInWaiting.add(new Screen(uniqueID, title, content, modDate, description, pictureURL, videoURL, this));

						}
						break;
					}

				}

				if (foundExistingScreen == false) {
					// System.out.println("Created New Screen for " +
					// headlines[0]);
					advertiseThisFeed(screensInWaiting);
					screensInWaiting.add(new Screen(uniqueID, title, content, modDate, description, pictureURL, videoURL, this));

				}
			}
		}

		return screensInWaiting;
	}

	public String shortenAddress() {
		String shortAddress = "Blog";
		int beg = address.indexOf("http://");
		if (beg != -1) {
			beg = beg + 7;
			int end = address.indexOf("/?", beg);
			if (end != -1) {
				shortAddress = address.substring(beg, end);
			}

		}
		return shortAddress;
	}

	public void advertiseThisFeed(ArrayList _screensInWaiting) {
		if (addressScreenEvery != 0 && ((_screensInWaiting.size()) % addressScreenEvery) == 0) {

			Screen thisScreen = null;
			String shortAddress = shortenAddress();
			thisScreen = new Screen(shortAddress, shortAddress, overallDescription.trim(), "", "", null, null, this);
			_screensInWaiting.add(thisScreen);
		}
	}

	static public String[] getArray(String _tagName, String _inString) {
		String endTag = "/" + _tagName;
		return getArray(_tagName, endTag, _inString);

	}

	static public String[] getArray(String _tagName, String _endTag, String _inString) {
		ArrayList list = new ArrayList();
		String before = "<" + _tagName; // + ">";
		String after = "<" + _endTag + ">";
		int start = 0;
		while (true) {
			// A function that returns a substring between two substrings
			String found = "";
			start = _inString.indexOf(before, start); // Find the index of the
			// beginning tag
			if (start == -1)
				break; // If we don't find anything, send back a blank String
			start = _inString.indexOf(">", start) + 1;
			// start += before.length(); // Move to the end of the beginning tag
			int end = _inString.indexOf(after, start); // Find the index of the
			// end tag
			if (end == -1)
				break; // If we don't find the end tag, send back a blank
			// String
			// ; // Return the text in between
			list.add(_inString.substring(start, end));
		}
		String[] returnArray = new String[list.size()];
		list.toArray(returnArray);

		return returnArray;
	}

	public String getFeedContents() {

		String returnedString = "";
		try {
			URL myURL = new URL(address);
			HttpURLConnection urlConnection = null;
			urlConnection = (HttpURLConnection) myURL.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
			urlConnection.setRequestProperty("Content-type", "application/x-www-form-urlenCcoded");
			urlConnection.setAllowUserInteraction(true);
			urlConnection.connect();
			BufferedReader dis = new BufferedReader(new InputStreamReader(((HttpURLConnection) urlConnection).getInputStream()));
			String line = dis.readLine();
			while (line != null) {
				returnedString = returnedString + line;
				line = dis.readLine();

			}
		} catch (IOException e1) {
			System.out.println("Problem with Network Connection " + address + " ");
			return "Error";
		}

		return returnedString;
	}

	public class Checker extends Thread {

		public void run() {
			System.out.println("Checking" + address);
			// lastTimeCheckedWeb = System.currentTimeMillis();
			String feedContents = getFeedContents();
			//System.out.println("Contents" + feedContents);
			if (!(feedContents.equals("Error") || feedContents.equals(""))) {
				ArrayList listOfScreens = parseContents(feedContents);
				// System.out.println("P" + parsedXML);

				finishedCheck(listOfScreens);
			} else if (feedContents.equals("Error")) {
				// lastTimeCheckedWeb = 0;
				DisplayApp.needToCheckAgain();
				System.out.println("please check back");
			}

		}
	}
}

/*
 * event calendar 3 String[] dates =
 * Feed.getArray("div id='ftcal_post_schedule'","/div",_thisItem);
 * //System.out.println("check date" + _thisItem); if (dates.length > 0 ) {
 * String noHTMLString = dates[0].replaceAll("\\<.*?\\>", "");
 * 
 * System.out.println("AHA " + noHTMLString); } ArrayList calendars = new
 * ArrayList<GregorianCalendar>(); // eventDate = new Calendar[0]; String
 * rawDescription = getDescription(_thisItem); String[] eventDates = new
 * String[0]; String[] multipleDates = new String[0]; // String tellTaleOfDate =
 * "<![CDATA[["; //there is an extra bracket at // the end if it has a Adate
 * String tellTaleOfDate = "["; // there is an extra bracket at the end // if it
 * has a date
 * 
 * if (rawDescription.startsWith(tellTaleOfDate)) {
 * 
 * String justDate = rawDescription.substring(tellTaleOfDate.length(),
 * rawDescription.length()); int end = justDate.indexOf("]"); justDate =
 * justDate.substring(0, end); // System.out.println("Date----------- " +
 * justDate);
 * 
 * multipleDates = justDate.trim().split(" to "); } for (int i = 0; i <
 * multipleDates.length; i++){
 * 
 * try {
 * 
 * eventDates = multipleDates[i].trim().split(";");
 * 
 * if (multipleDates[i].length() < 10){ //to phrase is just a time continue;
 * }else if (eventDates.length > 0) { eventDates[0] =
 * eventDates[0].replaceAll(";"," "); eventDates[0] =
 * eventDates[0].replaceAll("\\."," "); String[] dateParts =
 * eventDates[0].split(","); String[] monthParts =
 * dateParts[1].trim().split(" "); String monthName = monthParts[0].trim(); if
 * (monthParts.length < 2)continue; int dayNumber =
 * Integer.parseInt(monthParts[1].trim()); String[] yearAndTime =
 * dateParts[2].trim().split(" "); if (yearAndTime.length > 1) { dateParts[2] =
 * yearAndTime[0]; //take time off of it } int yearNumber =
 * Integer.parseInt(dateParts[2].trim()); int monthNumber = 0; for (int m = 0; m
 * < months.length; m++) { if (months[m].equals(monthName)) { monthNumber = m;
 * break; } } if (eventDates.length == 2) { // time also
 * //System.out.println("time too" ); String time = eventDates[1]; //
 * System.out.println("time too" + eventDates[1]); String[] timeParts =
 * time.trim().split(" "); String[] numberParts = timeParts[0].split(":"); int
 * hour = Integer.parseInt(numberParts[0]); int min =
 * Integer.parseInt(numberParts[1]); if (timeParts[1].trim().equals("pm")) hour
 * = hour + 12; calendars.add(new GregorianCalendar(yearNumber, monthNumber,
 * dayNumber, hour, min)); } else{ calendars.add(new
 * GregorianCalendar(yearNumber, monthNumber, dayNumber));
 * 
 * } // System.out.println(eventDate.toString());
 * 
 * } } catch (Exception e) { System.out.println("Error getting calendar" + e);
 * 
 * } } Calendar[] outputArray = new Calendar[calendars.size()];
 * calendars.toArray(outputArray); return outputArray;
 */
