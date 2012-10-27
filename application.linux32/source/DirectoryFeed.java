import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;



class DirectoryFeed extends Feed{
	String directory = "";
	public DirectoryFeed(String _address){
		super(_address);
		directory = _address;
	}
	
	public String getUniqueID(String _thisItem){
		return directory + _thisItem;

	}
	public String getFeedContents(){
		File directory = new File(address);
		if (directory.isDirectory() == false){
			return "";
		}
		return address;
	}
	
	public String[] getItems(String _fileAddress){
		File directory = new File(_fileAddress);
		File[] list = directory.listFiles();
		ArrayList fileNames = new ArrayList();
		for(int i = 0; i < list.length; i++){
			File thisFile = list[i];
			if (thisFile.isFile()){
				fileNames.add(thisFile.getName());
			}
		}
		String[] a  = new String[fileNames.size()];
		fileNames.toArray(a);
		return a;
	}

	public String getOverallDescription(String _rawXML) {
		return "";
	}

	public String getTitle(String _thisItem) {
		
		return "";
	}

	public String getDescription(String _thisItem) {
		return "";
	}

	public String getContent(String _thisItem) {
			return "";
	}

	public String getModDate(String _thisItem) {
		File thisFile = new File(_thisItem);
		String sdate = String.valueOf(thisFile.lastModified());
		return sdate;
	}

	public Calendar getEventDate(String _thisItem) {
		return null;
	}
/*
	public String getPicture(String _thisItem) {
		if (_thisItem.endsWith(".png") || _thisItem.endsWith(".jpg")){
			return directory + _thisItem;
		}
		return null;
	}
	*/
	public String[] getPictures(String _thisItem) {
		// System.out.println("get Picture" +address);
		String[] returnArray = new String[1];
		if (_thisItem.endsWith(".png") || _thisItem.endsWith(".jpg")){
			returnArray[0] = ( directory + _thisItem);
		}

		return returnArray;
	}
	
	public String[] getVideos(String _thisItem) {
		// System.out.println("get Picture" +address);
		String[] returnArray = new String[1];
		if (_thisItem.endsWith(".mov") || _thisItem.endsWith(".mp4")){
			returnArray[0] = ( directory + _thisItem);
		}

		return returnArray;
	}
/*
	public String getVideo(String _thisItem) {
		if (_thisItem.endsWith(".mov") || _thisItem.endsWith(".mpg")){
			return directory + _thisItem;
		}
		return null;
	}
*/
}
