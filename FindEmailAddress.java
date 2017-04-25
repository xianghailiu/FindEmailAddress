import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class FindEmailAddress {
	// Email Address Pattern
	String pattern = "\\b[a-zA-Z0-9.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z0-9.-]+\\b";
	// embedded link pattern <a href="....">...</a>
	String anchorRegex = "(?s)<\\s*a\\s+.*?href\\s*=\\s*['\"]([^\\s>]*)['\"]";
	URL url; // URL Instance Variable
	StringBuilder contents = new StringBuilder();; // Stores our URL Contents
	Set<String> emailAddresses = new HashSet<>(); // Contains unique email addresses
	HashMap<Integer, URL> hmap = new HashMap<Integer, URL>(); //Contains unique urls
	static int index = 0;

	FindEmailAddress(String url) {
		//build a valid URL
		String fullUrl = "http://" + url;
		try {
			this.url = new URL(fullUrl); // Initializing URL object
		} catch (MalformedURLException ex) {
			System.out.println("Please specify a valid website, for example: www.google.com or google.com");
			System.exit(1);
		}
	}

	/**
	 * Read web contents line by line, stores the contents
	 * If current web page contains discoverable pages, follow the links to get the contents
	 * 
	 * @param mUrl
	 */
	public void readContents(URL mUrl) {
		try {
			// Open Connection to URL and get stream to read
			BufferedReader read = new BufferedReader(new InputStreamReader(mUrl.openStream()));
			// Read and Save Contents to StringBuilder variable
			String input = "";
			while ((input = read.readLine()) != null) {
				contents.append(input);
				linkChecker(input);
			}
		} catch (IOException ex) {
			System.out.println("\tUnable to read URL due to Unknown Host..");
		}
	}

	/**
	 * Check if the string content contains a page link
	 * and follow the pages all the way down to get the contents
	 * One link only visits once
	 * 
	 * @param pageSource
	 */
	private void linkChecker(String pageSource) {
		Pattern linkPattern = Pattern.compile(anchorRegex);
		Matcher ma = linkPattern.matcher(pageSource);
		//Found a href string
		if (ma.find()) {
			String hrefString = ma.group(1);
			int n = hrefString.indexOf("www.");
			//Only interested in webpages starting with www
			if (n > 0) {
				String embedLink = hrefString.substring(n);
				String mUrl = "http://" + embedLink;
				URL embedUrl;
				try {
					embedUrl = new URL(mUrl); // Initializing our URL object
					// Keep track of visited URLs, only visit once
					if (mUrl.contains(url.getHost()) && !hmap.containsValue(embedUrl)) {
						hmap.put(index++, embedUrl);
						readContents(embedUrl);
					} else {
						return;
					}
				} catch (MalformedURLException ex) {
					System.out.println("Not a valid URL");
					System.exit(1);
				}
			}

		}
	}

	/**
	 * Find the domain related emails from saved contents
	 */
	public void findEmails() {
		// Creates a Pattern
		Pattern pat = Pattern.compile(pattern);
		// Matches contents against the given Email Address Pattern
		Matcher match = pat.matcher(contents);
		// If match found, append to emailAddresses
		while (match.find()) {
			//find domain related emails
			String hostHame = url.getHost();
			String domainName = hostHame.startsWith("www.") ? hostHame.substring(4) : hostHame;
			if (match.group().contains(domainName)) {
				emailAddresses.add(match.group());
			}
		}
	}

	/**
	 * Print out collected email addresses
	 */
	public void printEmailAddresses() {
		// Check if email addresses have been extracted
		if (emailAddresses.size() > 0) {
			// Print out all the extracted emails
			System.out.println("Found these email addresses: ");
			for (String emails : emailAddresses) {
				System.out.println(emails);
			}
		} else {
			// In case, no email addresses were found
			System.out.println("No emails were found!");
		}
	}

	public static void main(String args[]) {

		FindEmailAddress extract;

		// Check if the URL argument is supplied
		if (args.length > 0 && args[0] != null) {
			// Initialize Extractor with URL
			extract = new FindEmailAddress(args[0]);
			// Read the URL contents
			extract.readContents(extract.url);
			// Find all the email addresses
			extract.findEmails();

			// Otherwise normally display the email addresses
			extract.printEmailAddresses();
		} else {
			System.out.println("No valid arguments supplied...");
			System.out.println("\n\tExample: java -jar FindEmailAddress.jar www.example.com");
		}
	}
}
