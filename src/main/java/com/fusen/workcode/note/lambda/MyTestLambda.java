package com.fusen.workcode.note.lambda;

/**
 * @author: WBin
 * @create: 2020-11-25 16:26
 * @description: 方法引用和构造器引用
 */
public class MyTestLambda {
    public void  fun(){
        //1.引用类方法
//        MyTest my = a->Integer.valueOf(a);
        /*MyTest my = Integer::valueOf;
        Integer i = my.take("20");*/

        //2.指定对象的实例方法
//        MyTest my = a->"java".indexOf(a);
        /*MyTest my = "java"::indexOf;
        int i = my.take("av");*/

        //类对象的实例方法
//        MyTest my = (a,b)->a.startsWith(b);
        /*MyTest my = String::startsWith;
        boolean b = my.take("java", "qja");*/

        //引用构造器
//        MyTest my = a -> new InfoEntity(a);
        MyTest my =InfoEntity::new;
        InfoEntity info = my.take("张三");
    }


    public static void main(String[] args) {
        MyTestLambda testLambda = new MyTestLambda();
        testLambda.fun();
    }
}
