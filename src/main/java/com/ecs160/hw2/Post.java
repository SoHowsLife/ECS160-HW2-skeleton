package com.ecs160.hw2;

import com.ecs160.persistence.*;
import com.google.gson.JsonObject;

import java.util.List;

@Persistable
public interface Post {
    Integer getPostId();
    String getUri();
    String getCid();
    String getAuthor();
    String getContent();
    Integer getReplyCount();
    String getTimestamp();
    List<Post> getReplies();
}
