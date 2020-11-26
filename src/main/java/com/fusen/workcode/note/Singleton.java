package com.fusen.workcode.note;

/**
 * @author: WBin
 * @create: 2020-08-04 12:29
 * @description: 单例写法
 */
public class Singleton {
    //懒汉式,线程不安全,性能好
    private Singleton(){};
    /*private static Singleton s;
    public static Singleton getInstance(){
        if (s==null){
            s = new Singleton();
        }
        return s;
    }*/

    /**
     * 解决懒汉式线程不安全问题,加双重检查锁; volatile:避免编译器对指令进行重排序,保证先写后读.
     * 正常顺序:给s分配内存,初始化对象,把s指向内存;  重排序会造成先将s指向内存,再进行初始化,可能会出现错误
     */
    private volatile static Singleton s;
    public static Singleton getInstance(){
        if (s == null) {
            synchronized (Singleton.class){
                if (s==null){
                    s = new Singleton();
                }
            }
        }
        return s;
    }



    //饿汉式,线程安全,性能差
    /*private Singleton(){};
    private static Singleton singleton = new Singleton();
    public static Singleton getInstance(){
        return singleton;
    }*/
}
