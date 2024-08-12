package com.example.Controller;

import com.example.service.IndeedJobScraper;
import com.example.service.LinkedInJobScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scrape")
public class JobScraperController {

    @Autowired
    private IndeedJobScraper indeedJobScraper;

    @Autowired
    private LinkedInJobScraper linkedInJobScraper;

    @GetMapping("/indeed")
    public String scrapeIndeed() {
        try {
            indeedJobScraper.scrapeJobs();
            return "Indeed jobs scraped successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to scrape Indeed jobs.";
        }
    }

    @GetMapping("/linkedin")
    public String scrapeLinkedIn() {
        try {
            linkedInJobScraper.scrapeJobs();
            return "LinkedIn jobs scraped successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to scrape LinkedIn jobs.";
        }
    }
}
