package com.surg.scp;

public class HelperClass {
    // Some other methods

    public static int clamp( int min, int max, int value )
    {
        if( value > max )
            return max;
        else if( value < min )
            return min;
        else
            return value;
    }
}
