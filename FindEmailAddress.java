import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
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
	String urlString;
	static Set<String> emailAddresses = new HashSet<>(); // Contains unique email addresses
	Set<String> domainUrls = new HashSet<>(); // Contains unique urls
	String wwwdomain;

	FindEmailAddress(String url) {
		this.urlString = url;
		int wwwIndex = this.urlString.indexOf("www.");
		this.wwwdomain = this.urlString.substring(( wwwIndex < 0)? 0 : wwwIndex);
	}

	/**
	 * Read web contents line by line If current line contains an email address,
	 * print out the email address If current line contains discoverable page
	 * link, follow the link to get the contents
	 * 
	 * @param mUrlString
	 */
	public void readContents(String mUrlString) {
		try {
			// Open Connection to URL and get stream to read
			//TODO some https websites require passing certificate verify, otherwise no contents can be read
			BufferedReader read = new BufferedReader(new InputStreamReader(getHttpUrl(mUrlString).openStream()));
			if (read.readLine() == null) {
				read = new BufferedReader(new InputStreamReader(getHttpsUrl(mUrlString).openStream()));
			}
			// Read and Save Contents to StringBuilder variable
			String input = "";
			while ((input = read.readLine()) != null) {
				if (!linkChecker(input)) {
					this.findEmails(input);
				}
			}
			read.close();
		} catch (IOException ex) {
		}
	}

	private URL getHttpUrl(String uri) {
		try {
			String fullUri = (uri.startsWith("http:") || uri.startsWith("https:")) ? uri : "http://" + uri;
			this.url = new URL(fullUri); // Initializing URL object
		} catch (MalformedURLException ex) {
			System.out.println("Please specify a valid website, for example: http://www.google.com, www.google.com or google.com");
			System.exit(1);
		}
		return this.url;
	}

	private URL getHttpsUrl(String uri) {
		try {
			String fullUri = (uri.startsWith("https:") || uri.startsWith("http:")) ? uri : "https://" + uri;
			this.url = new URL(fullUri); // Initializing URL object
		} catch (MalformedURLException ex) {
			System.out.println("Please specify a valid website, for example: http://www.google.com, www.google.com or google.com");
			System.exit(1);
		}
		return this.url;
	}

	/**
	 * Check if the string content contains a page link and follow the pages all
	 * the way down to get the contents One link only visits once
	 * 
	 * @param pageSource
	 */
	private boolean linkChecker(String pageSource) {
		Pattern linkPattern = Pattern.compile(anchorRegex);
		Matcher ma = linkPattern.matcher(pageSource);
		// Found a href string
		if (ma.find()) {
			String hrefString = ma.group(1);
			int n = hrefString.indexOf("www.");
			// Only interested in webpages starting with www
			if (n > 0) {
				String embedLink = hrefString.substring(n);
				try {
					// Keep track of visited URLs, only visit once
					if (embedLink.contains(this.wwwdomain) && !domainUrls.contains(embedLink)) {
						domainUrls.add(embedLink);
						readContents(embedLink);
						return true;
					} else {
						return false;
					}
				} catch (Exception ex) {
					System.out.println("Not a valid URL");
					System.exit(1);
				}
			}
			return false;
		} else {
			return false;
		}
	}

	/**
	 * Find the domain related emails from saved contents
	 */
	private void findEmails(String contents) {
		// Creates a Pattern
		Pattern pat = Pattern.compile(pattern);
		// Matches contents against the given Email Address Pattern
		Matcher match = pat.matcher(contents);
		// If match found, append to emailAddresses
		while (match.find()) {
			// find domain related emails
			String hostHame = url.getHost();
			String domainName = hostHame.startsWith("www.") ? hostHame.substring(4) : hostHame;
			if (match.group().contains(domainName)) {
				if (!emailAddresses.contains(match.group())) {
					if (emailAddresses.size() == 0) {
						System.out.println("Found these email addresses: ");
					}
					System.out.println(match.group());
				}
				emailAddresses.add(match.group());
			}
		}
	}
	
	public void checkFoundNoEmails() {
		// In case, no email addresses were found
		if(emailAddresses.size() == 0) {
            System.out.println("No emails were found!");
		}
	}

	public static void main(String args[]) {

		FindEmailAddress extract;

		// Check if the URL argument is supplied
		if (args.length > 0 && args[0] != null) {
			// Initialize Extractor with URL
			extract = new FindEmailAddress(args[0]);
			// Read the URL contents, print email addresses
			extract.readContents(extract.urlString);
			extract.checkFoundNoEmails();
		} else {
			System.out.println("No valid arguments supplied...");
			System.out.println("\n\tExample: java -jar FindEmailAddress.jar www.example.com");
		}
	}
}

