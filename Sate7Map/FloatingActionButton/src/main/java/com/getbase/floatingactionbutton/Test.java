package com.getbase.floatingactionbutton;

import java.util.HashSet;

public class Test {
    public static void main(String[] args) {
        HashSet<String> set = new HashSet<>();
        set.add("玉玉");
        set.add("玉玉");
        set.add("玉玉");
        String name = "玉玉";
        if(set.contains("玉玉")){
            System.out.println("exit ... ");
        }else{
            System.out.println("not exit ... ");
        }

        double progress = 2356d / 26545;
        System.out.println(progress);
    }
}
