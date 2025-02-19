package com.ecs160.hw2;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class WeightedAnalyzer implements Analyzer{
    @Override
    public double getTotalPosts(List<Post> posts) {
        int longest = findLongestPost(posts);
        double totalWeight = 0;
        for (Post post:posts){
            double weight = 1 + ((double) countWords(post.getContent()) / longest);
            totalWeight += weight;
            if (!post.getReplies().isEmpty()){
                totalWeight += getTotalPosts(post.getReplies(), longest);
            }
        }
        return totalWeight;
    }

    public double getTotalPosts(List<Post> posts, int longest) {
        double totalWeight = 0;
        for (Post post:posts){
            double weight = 1 + ((double) countWords(post.getContent()) / longest);
            totalWeight += weight;
            if (!post.getReplies().isEmpty()){
                totalWeight += getTotalPosts(post.getReplies(), longest);
            }
        }
        return totalWeight;
    }

    @Override
    public double getAvgPosts(List<Post> posts) {
        double total = new BasicAnalyzer().getTotalPosts(posts);
        int longest = findLongestPost(posts);
        if (total == 0){
            return 0;
        }
        double totalWeight = 0;
        for (Post post:posts){
            if (!post.getReplies().isEmpty()){
                totalWeight += getTotalPosts(post.getReplies(), longest);
            }
        }
        return totalWeight / total;
    }

    //Assuming Interval isn't affected by weights
    @Override
    public String getAvgInterval(List<Post> posts) {
        return new BasicAnalyzer().getAvgInterval(posts);
    }

    private int findLongestPost(List<Post> posts){
        int longest = 0;
        for (Post post : posts){
            int postLength = countWords(post.getContent());
            if (postLength > longest){
                longest = postLength;
            }
            if (!post.getReplies().isEmpty()){
                int replyLength = findLongestPost(post.getReplies());
                if (replyLength > longest){
                    longest = replyLength;
                }
            }
        }
        return longest;
    }

    private int countWords(String content){
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        return content.trim().split("\\s+").length;
    }
}
