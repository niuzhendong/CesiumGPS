package com.founder.niuzhendong.cesiumgps.rabbit;

import android.os.AsyncTask;
import android.util.Log;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.lang.String;
import java.lang.System;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by niuzh on 2017-10-17.
 */

public class RabbitClient  extends AsyncTask {
    private final static String QUEUE_NAME = "hello";
    ConnectionFactory factory = null;

    public RabbitClient() {
        //System.out.println("Hello World!");
        factory = new ConnectionFactory();
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setHost("18.221.33.144");
        factory.setVirtualHost("/");
        factory.setPort(5672);
    }

    public void doPost(String params){
        Object[] objects = new Object[] { params };
        this.execute(objects);
    }

    public void doPost(String params,String t){
        Object[] objects = new Object[] { params };
        this.doInBackground(objects);
    }

    @Override
    protected String doInBackground(Object[] params) {

        try {
            Connection connection = factory.newConnection();
            Channel channel =  connection.createChannel();

            // 声明一个队列
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            //发送消息到队列中
            String message = (String)params[0];
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
            Log.i("Producer Send", message);

            channel.close();
            connection.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "1";
    }
}
