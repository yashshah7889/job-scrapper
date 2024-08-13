package com.example.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CsvReadingService {

    public List<Job> readJobsFromCsv(String filePath) {
        List<Job> jobs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Skip header
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 3) {
                    String title = values[0].replace("\"", "");
                    String link = values[1].replace("\"", "");
                    boolean isEasyApply = "Easily apply".equals(values[2].replace("\"", ""));
                    jobs.add(new Job(title, link, isEasyApply));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jobs;
    }

    public static class Job {
        private final String title;
        private final String link;
        private final boolean isEasyApply;

        public Job(String title, String link, boolean isEasyApply) {
            this.title = title;
            this.link = link;
            this.isEasyApply = isEasyApply;
        }

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }

        public boolean isEasyApply() {
            return isEasyApply;
        }
    }
}
