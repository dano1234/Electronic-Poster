import java.util.Calendar;



class FlikrFeed extends Feed{
	
	public FlikrFeed(String _url){
		super(_url);
	}
/*
	public String[] getItems(String _rawXML) {
		return Feed.getArray("entry", _rawXML);
	}
*/
	public String getOverallDescription(String _rawXML) {
		String[] overallDescriptionArray = Feed.getArray("title", _rawXML);

		if (overallDescriptionArray.length > 0)
			return overallDescriptionArray[0];
		else
			return "";
	}

	public String getTitle(String _thisItem) {
		
		String[] titles = Feed.getArray("title", _thisItem);
		if (titles.length > 0)
			return "";
			//return titles[0];
		else
			return "";
	}

	public String getDescription(String _thisItem) {
		String[] descriptions = Feed.getArray("media:text", _thisItem);
		if (descriptions.length > 0)
			return descriptions[0];
		else
			return "";
	}

	public String getContent(String _thisItem) {
			return "";
	}

	public String getModDate(String _thisItem) {
		String[] getPubDates = Feed.getArray("pubDate", _thisItem);
		if (getPubDates.length > 0)
			return getPubDates[0];
		else
			return "";
	}

	public Calendar getEventDate(String _thisItem) {
		return null;
	}

	public String getPicture(String _thisItem) {
		System.out.println("get Picture" +address);


		String urlString = null;
		//String landMark =  "link rel=\"enclosure\"" ;
		String landMark =  "<media:content url=\"";
		int beg = _thisItem.indexOf(landMark) + landMark.length();
		//int beg = _thisItem.indexOf("href=\"",firstplace + landMark.length()) + 6;
		int end = _thisItem.indexOf("\"",beg );
		urlString = _thisItem.substring(beg , end );
				System.out.println("Flickr url" + urlString);
	
		return urlString;
	}

}
