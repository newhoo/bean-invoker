package io.github.newhoo.invoker.server;

import io.github.newhoo.invoker.common.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.net.Socket;

import static io.github.newhoo.invoker.common.Constant.SERVICE_METHOD_SPLIT;

/**
 * Spring容器服务器
 * <p>
 * 支持单个Spring容器，目前常用
 *
 * @author huzunrong
 */
public final class ApplicationContextServer {

    private static volatile boolean working = false;
    private static Object ctx;

    /**
     * 启动bean上下文服务器
     */
    public static void startServer(Object applicationContext) {
        try {
            Method getParent = applicationContext.getClass().getMethod("getParent");
            if (ctx == null || getParent.invoke(applicationContext) == ctx) {
                ctx = applicationContext;

                if (!working) {
                    doStartServer();
                }
            }
        } catch (Exception e) {
            System.err.println("Bean invoker initialized with exception: " + e.toString());
        }
    }

    private static void doStartServer() {
        int port = Config.beanInvokePort;
        System.out.println("Bean invoker initialized with port: " + port);
        if (!working) {
            working = true;
            Thread thread = new Thread(() -> {
                ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket(port);
                    while (true) {
                        Socket socket = serverSocket.accept();
                        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String content = br.readLine();

                        if (content == null || content.length() == 0) {
                            socket.close();
                            continue;
                        }
                        if (!content.contains(SERVICE_METHOD_SPLIT)) {
                            System.err.println("Please use bean invoker client to call bean service: " + content);
                            socket.close();
                            continue;
                        }

                        try {
                            String[] split = content.split(SERVICE_METHOD_SPLIT);
                            String className = split[0];
                            String methodName = split[1];

                            handleRequest(className, methodName);
                        } catch (InvocationTargetException e) {
                            System.err.println("Invoke method error: " + e.toString());
                            e.printStackTrace();
                        } catch (ClassNotFoundException | NoSuchMethodException e) {
                            System.err.println("Invoke method not found: " + e.toString());
                            e.printStackTrace();
                        } catch (Exception e) {
                            System.err.println("unknown exception: " + e.toString());
                            e.printStackTrace();
                        }

                        socket.close();
                    }
                } catch (Exception e) {
                    System.err.println("Bean invoker initialized with exception: " + e.toString());
                    e.printStackTrace();
                } finally {
                    System.out.println("Bean invoker stop...");
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * 处理调用
     *
     * @param className com.seewo.iot.platform.web.controller.BrokerController
     * @param methodName sayHello
     */
    private static void handleRequest(String className, String methodName) throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        System.out.println(String.format("###################################### %s#%s ######################################", className,
                methodName));

        Class<?> targetClass = Thread.currentThread().getContextClassLoader().loadClass(className);
        Method targetMethod = targetClass.getDeclaredMethod(methodName);

        if (!Modifier.isPublic(targetMethod.getModifiers())) {
            System.out.println("Invoke method must be public with no param.");
            return;
        }

        Object invokeObj = null;
        // 非static方法
        if (!Modifier.isStatic(targetMethod.getModifiers())) {
            Method BeanFactory_getBean = ctx.getClass().getMethod("getBean", Class.class);
            invokeObj = BeanFactory_getBean.invoke(ctx, targetClass);
        }
        Object result = targetMethod.invoke(invokeObj);
        if (result != null) {
            System.out.println("Result: " + result);
        }
    }
}