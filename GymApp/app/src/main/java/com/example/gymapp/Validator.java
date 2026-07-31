package com.example.gymapp;

import java.net.URL;

/* Class that deals with general validations*/
public class Validator {

    public static boolean check_URL(String str) {
        try {
            new URL(str).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
