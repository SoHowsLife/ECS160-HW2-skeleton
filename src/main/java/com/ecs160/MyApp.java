package com.ecs160;


import com.ecs160.hw2.*;
import com.ecs160.hw2.Thread;
import com.ecs160.persistence.Session;
import org.apache.commons.cli.*;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Scanner;

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

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a post ID to retrieve: ");
            int postId = scanner.nextInt();

            Post post = new Thread(postId);
            session.load(post);
            printPosts(post, "> ");

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Json file not found.");
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addPosts(Session session, Post parent){
        session.add(parent);
        for (Post reply : parent.getReplies()){
            addPosts(session, reply);
        }
    }

    private static void printPosts(Post parent, String prefix){
        System.out.println(prefix + parent.getContent());
        for (Post reply : parent.getReplies()){
            printPosts(reply, prefix + "--> ");
        }

    }
}
