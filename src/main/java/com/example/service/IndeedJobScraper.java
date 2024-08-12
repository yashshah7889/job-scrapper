package com.example.service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IndeedJobScraper {

    //setting up chrome driver to control chrome browser.
    static {
        System.setProperty("webdriver.chrome.driver", "D:/study/self/chromedriver-win64/chromedriver.exe");
    }

    //logging
    private static final Logger logger = LoggerFactory.getLogger(IndeedJobScraper.class);

    //function to write in a csv file.
    private static void writeCsv(String filePath, List<Job> jobs) {
        boolean append = true;  // Set to true to append to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath,append))) {
            // Log the file's initial state
            logger.info("File length before writing: {}", new java.io.File(filePath).length());


            if (new java.io.File(filePath).length() == 0) {
                writer.write("Job Title,Application Link\n"); // Header
                logger.info("Writing header to the CSV file.");
            }
            for (Job job : jobs) {
                writer.write("\"" + job.getTitle() + "\",\"" + job.getLink() + "\"\n");
                logger.info("Wrote job to CSV: Title={}, Link={}", job.getTitle(), job.getLink());
            }

            // Log the file's state after writing
            logger.info("File length after writing: {}", new java.io.File(filePath).length());

            logger.info("Successfully wrote {} jobs to the CSV file.", jobs.size());
        } catch (IOException e) {
            logger.error("Failed to write CSV file", e);
        }
    }

    //class to hold the information of the job.
    private static class Job {
        private final String title;
        private final String link;

        public Job(String title, String link) {
            this.title = title;
            this.link = link;
        }

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }
    }

    //main work happens: opening the browser, searching for jobs, collecting information, and then saving that information.
    private void scrapeWithSelenium(String url) {
        ChromeOptions options = new ChromeOptions();//used to configure chrome browser
        //options.addArguments("--headless"); // Run in headless mode. willwork in background
        WebDriver driver = new ChromeDriver(options);//driver that controls chrome browser.

        try {
            logger.info("Starting scrape for URL: {}", url);
            driver.get(url);//TELLS browser to go to that url.

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));//await for conditions met. here css selector.

            List<Job> jobs = new ArrayList<>();
            boolean hasNextPage = true;

            while(hasNextPage) {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.jcs-JobTitle")));

                // Wait until elements are visible. and create element
                List<WebElement> jobElements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("a.jcs-JobTitle")));

                //scrap for current page
                for (WebElement jobElement : jobElements) {
                    String title = jobElement.getText();
                    String link = "https://www.indeed.com" + jobElement.getAttribute("href");
                    jobs.add(new Job(title, link));
                    logger.info("Scraped job: Title={}, Link={}", title, link);
                }

                //check if there is a "next" button.
                try {
                    WebElement nextButton = driver.findElement(By.cssSelector("a[data-testid='pagination-page-next']"));
                    nextButton.click();
                    // Wait for the next page to load by waiting for job elements to become stale
                    wait.until(ExpectedConditions.stalenessOf(jobElements.get(0)));
                } catch (Exception e) {
                    hasNextPage = false; // No more pages
                }
            }
            // Write jobs to CSV
            writeCsv("D:/study/self/job_data.csv", jobs);

        } catch (Exception e) {
            logger.error("Failed to scrape with Selenium", e);
        } finally {
            driver.quit();
            logger.info("Closed WebDriver.");
        }
    }

    public void scrapeJobs() {
        String url = "https://www.indeed.com/jobs?q=software%20engineer&l=Montreal&start=0";
        logger.info("Starting job scraping process.");
        scrapeWithSelenium(url);
        logger.info("Job scraping process completed.");
    }

    public static void main(String[] args) {
        IndeedJobScraper scraper = new IndeedJobScraper();
        scraper.scrapeJobs();
    }
}
