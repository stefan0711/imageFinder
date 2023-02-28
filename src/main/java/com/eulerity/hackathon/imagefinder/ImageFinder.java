package com.eulerity.hackathon.imagefinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@WebServlet(
        name = "ImageFinder",
        urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ImageFinder.class);
    private static final long serialVersionUID = 1L;

    protected static final Gson GSON = new GsonBuilder().create();

    public static final String[] testImages = {
            "https://images.pexels.com/photos/545063/pexels-photo-545063.jpeg?auto=compress&format=tiny",
            "https://images.pexels.com/photos/464664/pexels-photo-464664.jpeg?auto=compress&format=tiny",
            "https://images.pexels.com/photos/406014/pexels-photo-406014.jpeg?auto=compress&format=tiny",
            "https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&format=tiny"
    };

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) {
        processRequest(req, resp, GSON);
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse resp, Gson GSON) {
        String path = req.getServletPath();
        String url = req.getParameter("url");
        logger.info("-------> Got request of:" + path + " with query param:" + url);
        ArrayList<String> images = new ArrayList<>();
        if (url == null) {
            throw new RuntimeException("url is Null");
        } else if (Arrays.asList(testImages).contains(url)) {
           images.add(url);
        } else {
            if (!url.startsWith("https")) {
                url = "https://" + url;
            }
            //Get array of scraped images
            images = getImages(url);
            logger.info("-----> total images number: " + images.size());
        }
        String json = GSON.toJson(images);

        //Respond to POST request sent to servlet
        resp.setContentType("text/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            resp.getWriter().print(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public ArrayList<String> getImages(String url) {
        // Scrape URLs
        UrlScraper urlScraper = new UrlScraper(url);
        Thread t = new Thread(urlScraper);
        t.start();

		//Get list of urls for all webpages to scrape images from
		ArrayList<String> urLs = urlScraper.getURLs();
		ArrayList<String> imagesList = new ArrayList<>();
		ArrayList<Thread> threads = new ArrayList<>(); //empty ArrayList for threads (to wait for them to finish)
        try {
            //Wait for thread to finish
            t.join();

            //Loop through scraped URLs and scrape them for images
            logger.info("scraped urls-------->"+urLs);
            for (String link : urLs) {
                ImgScraper imageScraper = new ImgScraper(link, imagesList);
                Thread imageScraperThread = new Thread(imageScraper);
                threads.add(imageScraperThread);
            }

            int count = 0;
            //Start threads, add delay to prevent firewall blocks
            for (Thread thread : threads) {
                Thread.sleep(100);
                thread.start();
                count++;
            }
            logger.info(count + " started <------");
            count = 0;
            //Wait for image scraping threads to finish
            for (Thread thread : threads) {
                thread.join();
                count++;
            }
            logger.info(count+" finished <------");
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted");
        }
		return imagesList;
    }

}
