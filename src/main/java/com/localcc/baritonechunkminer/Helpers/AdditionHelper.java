package com.localcc.baritonechunkminer.Helpers;

public class AdditionHelper {
    public static int add(int f, int adder) {
        if(f < 0) return f - adder;
        else return f + adder;
    }
}
