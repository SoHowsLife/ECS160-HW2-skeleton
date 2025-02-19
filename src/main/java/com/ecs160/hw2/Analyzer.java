package com.ecs160.hw2;

import java.util.List;

public interface Analyzer {
    double getTotalPosts(List<Post> posts);
    double getAvgPosts(List<Post> posts);
    String getAvgInterval(List<Post> posts);
}
