package com.ecs160.hw2;

import com.google.gson.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PostParser {
    private static int currentId = 0;
    public List<Post> parseJson(String fileName) throws FileNotFoundException {
        List<Post> posts = new ArrayList<>();
        try {
            JsonElement element = JsonParser.parseReader(new FileReader(fileName));
            if (element.isJsonObject()) {
                JsonArray feedArray = element.getAsJsonObject().get("feed").getAsJsonArray();
                for (JsonElement feedObject : feedArray) {
                    if (feedObject.getAsJsonObject().has("thread")) {
                        posts.add(parsePost(feedObject.getAsJsonObject().get("thread").getAsJsonObject()));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Json file not found.");
        }

        return posts;
    }
    private Post parsePost(JsonObject postObject) {
        JsonObject postData = postObject.get("post").getAsJsonObject();
        Integer postId = currentId;
        currentId++;
        String uri = postData.get("uri").getAsString();
        String cid = postData.get("cid").getAsString();
        String author = postData.get("author").getAsJsonObject().get("handle").getAsString();
        String content = postData.get("record").getAsJsonObject().get("text").getAsString();
        String timestamp = postData.get("indexedAt").getAsString();
        JsonArray replies = null;
        if (postObject.get("replies") != null){
            replies = postObject.get("replies").getAsJsonArray();
        }
        Post post;

        if (replies == null || replies.isEmpty()) {
            post = new SinglePost(postId, uri, cid, author, content, timestamp);
        }
        else{
            Integer replyCount = postData.get("replyCount").getAsInt();
            List<Post> replyPosts = new ArrayList<>();
            for(int i = 0; i < replies.size(); i++) {
                Post reply = parsePost(replies.get(i).getAsJsonObject());
                replyPosts.add(reply);
            }
            post = new Thread(postId, uri, cid, author, content, replyCount, timestamp, replyPosts);
        }
        return post;
    }
}
