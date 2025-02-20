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

import com.ecs160.hw2.*;
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

    public static Session getInstance(){
        return new Session();
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
                    id = (Integer) field.get(obj);
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
                                    listId.append(String.valueOf(itemField.get(item))).append(",");
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
        objQueue.clear();
    }

    public Object load(Object object) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException, ClassNotFoundException {
        if (!object.getClass().isAnnotationPresent(Persistable.class)){
            throw new IllegalArgumentException("Class is not Persistable");
        }
        Integer id = null;
        for (Field field : object.getClass().getDeclaredFields()){
            if (field.isAnnotationPresent(PersistableId.class)){
                field.setAccessible(true);
                id = (Integer) field.get(object);
                break;
            }
        }
        if (id == null){
            return null;
        }
        Map<String, String> dataMap = jedisSession.hgetAll(String.valueOf(id));
        if (dataMap == null || dataMap.isEmpty()) return null;
        for (Field field : object.getClass().getDeclaredFields()){
            field.setAccessible(true);
            if (field.isAnnotationPresent(PersistableField.class)){
                field.set(object, dataMap.get(field.getName()));
            } else if (field.isAnnotationPresent(PersistableListField.class)) {
                if (dataMap.get(field.getName()) != null && !dataMap.get(field.getName()).isEmpty()){
                    if (field.isAnnotationPresent(LazyLoad.class)){
                        field.set(object, createProxy(object));
                    }
                    else{
                        String[] list = dataMap.get(field.getName()).split(",");
                        //Class<?> clazz = Class.forName(field.getAnnotation(PersistableListField.class).className());
                        List<Object> itemList = new ArrayList<>();
                        for (String itemId : list){
                            itemList.add(object.getClass().getDeclaredConstructor(Integer.class).newInstance(Integer.valueOf(itemId)));
                        }
                        for (Object item : itemList){
                            load(item);
                        }
                        field.set(object, itemList);
                    }
                }
            }
        }
        return object;
    }

    private List<?> createProxy(Object object) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(ArrayList.class);

        MethodHandler methodHandler = new MethodHandler() {
            @Override
            public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                System.out.println(thisMethod.getName());
                System.out.println(thisMethod.getAnnotations());
                System.out.println(thisMethod.getAnnotatedReturnType());
                return proceed.invoke(self, args);
            }
        };

        Class<?> proxyClass = proxyFactory.createClass();
        List<?> proxyObject = (List<?>) proxyClass.getDeclaredConstructor().newInstance();
        ((javassist.util.proxy.Proxy) proxyObject).setHandler(methodHandler);
        return proxyObject;
    }

}
