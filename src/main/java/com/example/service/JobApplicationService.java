package com.example.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobApplicationService {

    @Autowired
    private CsvReadingService csvReadingService;

    static {
        System.setProperty("webdriver.chrome.driver", "D:/study/self/chromedriver-win64/chromedriver.exe");
    }

    public void applyToJobs(String csvFilePath) {
        List<CsvReadingService.Job> jobs = csvReadingService.readJobsFromCsv(csvFilePath);
        for (CsvReadingService.Job job : jobs) {
            if (job.isEasyApply()) {
                applyToJob(job.getLink());
            }
        }
    }

    private void applyToJob(String jobLink) {
        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(jobLink);
            // You will need to adjust the selectors and logic based on the actual application form
            WebElement applyButton = driver.findElement(By.cssSelector("button.apply-button")); // Example selector
            applyButton.click();

            // Handle the application process


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
