package com.example.service;
import org.openqa.selenium.JavascriptExecutor;
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
                writer.write("Job Title,Application Link,Easy Apply\n"); // Header
                logger.info("Writing header to the CSV file.");
            }
            for (Job job : jobs) {
                writer.write("\"" + job.getTitle() + "\",\"" + job.getLink() + "\",\"" + job.getEasyApply() + "\"\n");
                logger.info("Wrote job to CSV: Title={}, Link={}, Easy Apply={}", job.getTitle(), job.getLink(), job.getEasyApply());
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
        private final boolean easyApply;

        public Job(String title, String link, boolean easyApply) {
            this.title = title;
            this.link = link;
            this.easyApply=easyApply;
        }

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }

        public boolean getEasyApply(){return easyApply;}
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
            boolean hasMoreJobs = true;

            while(hasNextPage) {
                try {
                    // Adding random delay
                    Thread.sleep((long) (Math.random() * 4000 + 2000));  // 3-8 seconds
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.jcs-JobTitle")));

                    // Wait until elements are visible. and create element
                    List<WebElement> jobElements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("a.jcs-JobTitle")));

                    //scrap for current page
                    for (WebElement jobElement : jobElements) {
                        String title = jobElement.getText();
                        String link =  jobElement.getAttribute("href");

                        //check for easy apply
                        boolean easyApply = jobElement.findElements(By.cssSelector("div.css-10vq04l span.css-wftrf9 span.ialbl.iaTextBlack.css-130a5xa.eu4oa1w0")).size() > 0;
                        jobs.add(new Job(title, link, easyApply));
                        logger.info("Scraped job: Title={}, Link={}", title, link, easyApply);
                    }
                    ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
                    logger.info("Scrolled to bottom of the page.");

                    // Wait for new jobs to load
                    try {
                        Thread.sleep(2000); // 2 seconds delay to allow for new jobs to load
                    } catch (InterruptedException e) {
                        logger.error("Interrupted during sleep", e);
                    }

                    // Check if there are more jobs to scrape
                    List<WebElement> newJobElements = driver.findElements(By.cssSelector("a.jcs-JobTitle"));
                    if (newJobElements.size() == jobElements.size()) {
                        hasMoreJobs = false; // No new jobs found, stop scraping

                    } else {
                        jobElements = newJobElements; // Update the job elements list
                    }

                    //check if there is a "next" button.
                    try {
                        WebElement nextButton = driver.findElement(By.cssSelector("a[data-testid='pagination-page-next']"));
                        nextButton.click();
                        // Wait for the next page to load by waiting for job elements to become stale
                        wait.until(ExpectedConditions.stalenessOf(jobElements.get(0)));
                    } catch (Exception e) {
                        hasNextPage = false; // No more pages
                        logger.info("as no more pages");
                    }
                }catch(Exception e){}
            }
            logger.info("No more new jobs found. Stopping scrape.");
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
        String url = "https://www.indeed.com/jobs?q=software%20engineer&l=montreal";
        logger.info("Starting job scraping process.");
        scrapeWithSelenium(url);
        logger.info("Job scraping process completed.");
    }

    public static void main(String[] args) {
        IndeedJobScraper scraper = new IndeedJobScraper();
        scraper.scrapeJobs();
    }
}
