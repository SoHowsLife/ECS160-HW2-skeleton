package com.ecs160;


import com.ecs160.hw2.*;
import com.ecs160.persistence.Session;
import org.apache.commons.cli.*;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.List;

public class MyApp {
    public static void main(String[] args) throws FileNotFoundException, URISyntaxException, NoSuchFieldException {
        String filePath = "src/resources/input.json";
        Options options = new Options();

        Option fileOption = new Option("f", "file", true, "Path to the input JSON file");
        fileOption.setRequired(false);
        options.addOption(fileOption);
        CommandLineParser clParser = new DefaultParser();

        try {
            CommandLine cmd = clParser.parse(options, args);
            filePath = cmd.hasOption("file") ? cmd.getOptionValue("file") : "src/resources/input.json";

        } catch (ParseException e) {
            System.out.println("Error parsing command-line arguments: " + e.getMessage());
            System.exit(1);
        }

        try {
            PostParser pParser = new PostParser();
            List<Post> posts = pParser.parseJson(filePath);
            Session session = Session.getInstance();
            for(Post post:posts){
                addPosts(session, post);
            }
            session.persistAll();

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Json file not found.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addPosts(Session session, Post parent){
        session.add(parent);
        for (Post reply : parent.getReplies()){
            addPosts(session, reply);
        }
    }
}
