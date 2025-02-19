package com.ecs160.hw2;

import org.apache.commons.cli.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.List;


public class SocialMediaAnalyzerDriver {
    public static void main(String[] args) throws FileNotFoundException {
        String filePath = "src/resources/input.json";
        boolean weighted = false;
        Options options = new Options();

        Option fileOption = new Option("f", "file", true, "Path to the input JSON file");
        fileOption.setRequired(false);
        options.addOption(fileOption);

        Option weightedOption = new Option("w", "weighted", false, "Use weighted average (true|false)");
        weightedOption.setRequired(false);
        options.addOption(weightedOption);

        CommandLineParser clParser = new DefaultParser();

        try {
            CommandLine cmd = clParser.parse(options, args);
            filePath = cmd.hasOption("file") ? cmd.getOptionValue("file") : "src/resources/input.json";
            weighted = cmd.hasOption("weighted");

        } catch (ParseException e) {
            System.out.println("Error parsing command-line arguments: " + e.getMessage());
            System.exit(1);
        }

        try {
            PostParser pParser = new PostParser();
            List<Post> posts = pParser.parseJson(filePath);
            RedisManager rm = new RedisManager();
            for(Post post:posts){
                rm.storePost(post);
            }
            Analyzer analyzer;
            if (weighted){
                analyzer = new WeightedAnalyzer();
            }
            else{
                analyzer = new BasicAnalyzer();
            }
            System.out.printf("Total Posts:%.2f\nAverage Replies:%.2f\nAverage Interval:%s\n",analyzer.getTotalPosts(posts), analyzer.getAvgPosts(posts), analyzer.getAvgInterval(posts));

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Json file not found.");
        }
    }
}
