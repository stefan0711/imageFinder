package com.eulerity.hackathon.imagefinder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Zhipeng Yin
 * @date 2023-02-26 23:00
 */
public class UrlScraper implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(UrlScraper.class);

    private String url;

    private ArrayList<String> allURLs = new ArrayList<>();

    public UrlScraper(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        initScraper();
    }

    /**
     * Print thread ID to console, and initialize URL scraping from web.
     */
    public void initScraper()  {

        logger.info("URL Scraper Thread " + Thread.currentThread().getId() + " is running");
        try {
            scrapeURLs(url);
        } catch (IOException e) {
            logger.warn("Exception on Thread " + Thread.currentThread().getId() + "url: "+ url+"message: "+e.getMessage());
        }
    }


    /**
     * Scrape webpage for links to other pages.
     */
    protected void scrapeURLs(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();

        //Get parsed domain from initially provided url
        String domain = getRootURL(url);

        Element firstLink = doc.select("a").first();
        String absHref = firstLink.attr("abs:href");
        allURLs.add(absHref);

        // Get all url tags
        Elements urls = doc.select("a[href]");

        for (Element el : urls) {
            //Get absolute path of url
            String link = el.attr("abs:href");

            //Remove reference to section in page (to prevent multiple calls to the same page)
            link = link.split("#")[0];

            //Do not add link if not in domain
            if (!link.startsWith(url) && !domain.equals(getRootURL(link))) {
                continue;
            }

            //Remove slash at end of URL (to prevent multiple calls to the same page)
            if (link.endsWith("/")) {
                link = link.substring(0, link.length() - 1);
            }

            if (!allURLs.contains(link)) {
                allURLs.add(link);
            }
        }
    }


    /**
     * Parse a URL string and return its domain.
     */
    public String getRootURL(String url) {

        String[] splitRootURL = url.split("//"); //example: "https://coronavirus.lehigh.edu/home" => ["https:","coronavirus.Stevens.edu/home"]
        String domain = splitRootURL[1].split("/")[0];
        String[] rootDomain = domain.split("\\.");
        //example: ["https:","coronavirus.Stevens.edu/home"] & ["coronavirus","Stevens","edu"] => "https://Stevens.edu/"
        String rootURL = splitRootURL[0] + "//" + rootDomain[rootDomain.length - 2] + "." + rootDomain[rootDomain.length - 1] + "/";
        return rootURL;
    }


    public ArrayList<String> getURLs() {
        return allURLs;
    }


}
