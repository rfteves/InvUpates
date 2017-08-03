/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.data;

import org.bson.Document;

/**
 *
 * @author Ricardo
 */
public class DocumentProcessor extends Thread {
    
    private static boolean processing;
    public static void process(Document vendors) {
        processing = true;
    }
    
    public static boolean isProcessing() {
        return processing;
    }
}
