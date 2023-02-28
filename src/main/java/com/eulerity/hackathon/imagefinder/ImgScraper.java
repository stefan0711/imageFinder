package com.eulerity.hackathon.imagefinder;

import org.jsoup.Connection;
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
 * @date 2023-02-27 02:04
 */
public class ImgScraper implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ImgScraper.class);
    private String url;
    private ArrayList<String> imagesList;

    public ImgScraper(String url, ArrayList<String> outputImages) {
        this.url = url;
        this.imagesList = outputImages;
    }

    @Override
    public void run() {
        initImgScraper();
    }

    /**
     * Print thread ID to console, and initialize image scraping from web.
     */
    public void initImgScraper() {
        logger.info("URL Scraper Thread " + Thread.currentThread().getId() + " is running: "+ url);
        try {
            scrapeImages(url);
        } catch (IOException e) {
            logger.warn("Exception on Thread " + Thread.currentThread().getId() + "url: "+ url+"message: "+e.getMessage());
        }
    }

    /**
     * Scrape website for all image tags, filter invalid file extensions, and append valid tags to an arrayList.
     */
    protected void scrapeImages(String url) throws IOException {
        Connection conn = Jsoup.connect(url).timeout(1000);
        Document doc = conn.get();
        // Get all img tags
        Elements img = doc.getElementsByTag("img");

        // Loop through img tags
        for (Element element : img) {
            String tag = element.attr("abs:src");

            //Do not include img sources that don't have a valid image file extension
            if (!tag.contains(".png") && !tag.contains(".apng") &&
                    !tag.contains(".jpeg") && !tag.contains(".jpg") &&
                    !tag.contains(".pjp") && !tag.contains(".tiff") &&
                    !tag.contains(".tif") && !tag.contains(".cur") &&
                    !tag.contains(".svg") && !tag.contains(".webp") &&
                    !tag.contains(".bmp") && !tag.contains(".ico")) {
                continue;
            }

            if (!imagesList.contains(tag)) {
                imagesList.add(tag);
            }
        }
    }

}
