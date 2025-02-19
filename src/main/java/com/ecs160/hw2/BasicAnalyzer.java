package com.ecs160.hw2;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class BasicAnalyzer implements Analyzer{
    @Override
    public double getTotalPosts(List<Post> posts) {
        double total = 0;
        for(Post post: posts){
            if(post.getReplies().isEmpty()){
                total++;
            }
            else{
                total += getTotalPosts(post.getReplies()) + 1;
            }
        }
        return total;
    }

    @Override
    public double getAvgPosts(List<Post> posts) {
        double totalPosts = getTotalPosts(posts);
        if (totalPosts == 0){
            return 0;
        }
        double totalReplies = 0;
        for (Post post : posts) {
            if (!post.getReplies().isEmpty()) {
                totalReplies += getTotalPosts(post.getReplies());
            }
        }
        return totalReplies / totalPosts;
    }

    //The difference between the parent comment time and each of its replies divided by the number of replies
    @Override
    public String getAvgInterval(List<Post> posts) {
        long averageDiff = 0;
        for (Post post: posts){
            if (!post.getReplies().isEmpty()){
                long totalInterval = 0;
                long parent = Instant.parse(post.getTimestamp()).getEpochSecond();
                for (Post reply: post.getReplies()){
                    totalInterval += Math.abs(parent - Instant.parse(reply.getTimestamp()).getEpochSecond());
                }
                averageDiff += (long) (totalInterval);
            }
        }
        averageDiff = (long)(averageDiff / getTotalPosts(posts));
        Duration avgInterval = Duration.ofSeconds(averageDiff);
        return String.format("%02d:%02d:%02d", avgInterval.toHours(), avgInterval.toMinutesPart(), avgInterval.toSecondsPart());
    }
}
