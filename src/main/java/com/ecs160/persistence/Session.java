package com.ecs160.persistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import redis.clients.jedis.Jedis;



// Assumption - only support int/long/and string values
public class Session {

    private Jedis jedisSession;
    private List<Object> objQueue;

    private Session() {
        jedisSession = new Jedis("localhost", 6379);
        objQueue = new ArrayList<>();
    }

    public void add(Object obj) {
        if(obj.getClass().isAnnotationPresent(Persistable.class)){
            objQueue.add(obj);
        }
    }


    public void persistAll() throws IllegalAccessException, ClassNotFoundException {
        for(Object obj : objQueue){
            Class<?> clazz = obj.getClass();
            Integer id = null;
            Map<String,String> dataMap = new HashMap<>();
            for (Field field : obj.getClass().getDeclaredFields()){
                field.setAccessible(true);
                if (field.isAnnotationPresent(PersistableId.class)){
                    id = field.getInt(obj);
                } else if (field.isAnnotationPresent(PersistableField.class)) {
                    dataMap.put(field.getName(), String.valueOf(field.get(obj)));
                }
                else if (field.isAnnotationPresent(PersistableListField.class)){
                    List<?> list = (List<?>) field.get(obj);
                    if (list != null){
                        StringBuilder listId = new StringBuilder();
                        for (Object item : list){
                            for (Field itemField : item.getClass().getDeclaredFields()){
                                if (itemField.isAnnotationPresent(PersistableId.class)){
                                    itemField.setAccessible(true);
                                    listId.append(String.valueOf(itemField.getInt(item))).append(",");
                                    break;
                                }
                            }
                        }
                        dataMap.put(field.getName(), listId.toString());
                    }
                }
            }
            if (id != null){
                jedisSession.hmset(String.valueOf(id), dataMap);
            }
        }
    }


    public Object load(Object object)  {
        return null;
    }

}
