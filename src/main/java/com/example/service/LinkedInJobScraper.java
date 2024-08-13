package com.example.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
public class LinkedInJobScraper {

    // Setting up ChromeDriver to control Chrome browser.
    static {
        System.setProperty("webdriver.chrome.driver", "D:/study/self/chromedriver-win64/chromedriver.exe");
    }

    // Logging
    private static final Logger logger = LoggerFactory.getLogger(LinkedInJobScraper.class);

    // Function to write in a CSV file.
    private static void writeCsv(String filePath, List<Job> jobs) {
        boolean append = true;  // Set to true to append to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, append))) {
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

    // Class to hold the information of the job.
    private static class Job {
        private final String title;
        private final String link;
        private final boolean easyApply;

        public Job(String title, String link, boolean easyApply) {
            this.title = title;
            this.link = link;
            this.easyApply = easyApply;
        }

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }

        public boolean isEasyApply() {
            return easyApply;
        }
    }

    // Main work happens: opening the browser, searching for jobs, collecting information, and then saving that information.
    private void scrapeWithSelenium(String url) {
        ChromeOptions options = new ChromeOptions(); // Used to configure Chrome browser
        WebDriver driver = new ChromeDriver(options); // Driver that controls Chrome browser.

        try {

            logger.info("Starting scrape for URL: {}", url);
            driver.get(url); // Tells browser to go to that URL.

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(300)); // Await for conditions met.

            List<Job> jobs = new ArrayList<>();
            boolean hasNextPage = true;

            while (hasNextPage) {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.job-card-container__link.job-card-list__title.job-card-list__title--link")));

                // Wait until elements are visible and create element
                List<WebElement> jobElements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("a.job-card-container__link.job-card-list__title.job-card-list__title--link")));


                // Scrape for current page
                for (WebElement jobElement : jobElements) {
                    String title = jobElement.getText();
                    String link = jobElement.getAttribute("href");

                    //check for easy apply
                    boolean easyApply = jobElement.findElements(By.cssSelector("span.job-card-container__easy-apply")).size() > 0;
                    jobs.add(new Job(title, link,easyApply));
                    logger.info("Scraped job: Title={}, Link={}", title, link,easyApply);
                }

                // Check if there is a "next" button
                try {
                    WebElement nextButton = driver.findElement(By.cssSelector("li.artdeco-pagination__indicator--number button[aria-label='Page 3']"));
                    if (nextButton.isEnabled()) {
                        nextButton.click();
                        // Wait for the next page to load by waiting for job elements to become stale
                        wait.until(ExpectedConditions.stalenessOf(jobElements.get(0)));
                    } else {
                        hasNextPage = false; // No more pages
                    }
                } catch (Exception e) {
                    hasNextPage = false; // No more pages
                }

            }
            // Write jobs to CSV
            writeCsv("D:/study/self/linkedin_job_data.csv", jobs);

        } catch (Exception e) {
            logger.error("Failed to scrape with Selenium", e);
        } finally {
            driver.quit();
            logger.info("Closed WebDriver.");
        }
    }

    public void scrapeJobs() {
        String url = "https://www.linkedin.com/jobs/search/?keywords=software%20developer&location=Canada";
        logger.info("Starting job scraping process.");
        scrapeWithSelenium(url);
        logger.info("Job scraping process completed.");
    }

    public static void main(String[] args) {
        LinkedInJobScraper scraper = new LinkedInJobScraper();
        scraper.scrapeJobs();
    }
}
