package com.ecs160.hw2;

import com.ecs160.persistence.*;
import com.google.gson.JsonObject;

import java.util.List;

@Persistable
public class Thread implements Post{
    @PersistableId
    private Integer postId;
    private final String uri;
    private final String cid;
    private final String author;
    @PersistableField
    private final String content;
    private final Integer replyCount;
    private final String timestamp;
    @PersistableListField(className = "Post")
    private final List<Post> replies;

    public Thread() {
        this.uri = "";
        this.cid = "";
        this.author = "";
        this.content = "";
        this.replyCount = 0;
        this.timestamp = "2000-01-01T00:00:00.000Z";
        this.replies = List.of();
    }

    public Thread(String uri, String cid, String author, String content, Integer replyCount, String timestamp, List<Post> replies) {
        this.uri = uri;
        this.cid = cid;
        this.author = author;
        this.content = content;
        this.replyCount = replyCount;
        this.timestamp = timestamp;
        this.replies = replies;
    }

    @Override
    public Integer getPostId() {
        return postId;
    }
    
    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getCid() {
        return cid;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public Integer getReplyCount() {
        return replyCount;
    }

    @Override
    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public List<Post> getReplies() {
        return replies;
    }

}
