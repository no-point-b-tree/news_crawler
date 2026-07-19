package com.archer.task;

import org.apache.commons.codec.binary.Base64;

public class Base64Test {

    public static void main(String[] args) {
        byte[] decode = new Base64().decode("Lm5ld3MtbGlzdCAubmV3cy1jYXJkQC5uZXdzLWNhcmRfX2xpbmtALm5ld3MtY2FyZF9fZGF0ZUBkIE1NTU0geXl5eUBGUg==");
        System.out.println(new String(decode));
    }
}
