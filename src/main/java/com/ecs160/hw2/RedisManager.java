package com.ecs160.hw2;

import com.google.gson.JsonArray;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisManager {
    private Jedis jedis;
    private static int currentId;

    public RedisManager() {
        this.jedis = new Jedis("localhost", 6379);
        this.currentId = 0;
    }

    public void storePost(Post post) {
        Map<String, String> postMap = new HashMap<>();
        postMap.put("cid", post.getCid());
        postMap.put("uri", post.getUri());
        postMap.put("authorName", post.getAuthor());
        postMap.put("postDate", post.getTimestamp());
        postMap.put("postContent", post.getContent());
        postMap.put("replyCount", Integer.toString(post.getReplyCount()));
        jedis.hset(String.valueOf(currentId), postMap);
        currentId++;

        if (!post.getReplies().isEmpty()){
            storeReplies(post, post.getReplies());
        }
    }

    private void storeReplies(Post parent, List<Post> replies){
        for (Post reply: replies){
            Map<String, String> postMap = new HashMap<>();
            postMap.put("cid", reply.getCid());
            postMap.put("uri", reply.getUri());
            postMap.put("authorName", reply.getAuthor());
            postMap.put("postDate", reply.getTimestamp());
            postMap.put("postContent", reply.getContent());
            postMap.put("replyCount", Integer.toString(reply.getReplyCount()));
            postMap.put("parentPost", parent.getCid());
            jedis.hset(String.valueOf(currentId), postMap);
            currentId++;
            if (!reply.getReplies().isEmpty()){
                storeReplies(reply, reply.getReplies());
            }
        }

    }
}