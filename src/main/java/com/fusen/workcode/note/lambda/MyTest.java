package com.fusen.workcode.note.lambda;

@FunctionalInterface
public interface MyTest {
    /**
     * 引用类方法
     * @param target
     * @return
     */
//    Integer take(String target);

    /**
     * 指定对象的实例方法
     * @param target
     * @return
     */
//    int take(String target);

    /**
     * 类对象的实例方法
     * @param str
     * @param target
     * @return
     */
//    boolean take(String str,String target);

    /**
     * 引用构造器
     * @param str
     * @return
     */
    InfoEntity take(String title);
}
