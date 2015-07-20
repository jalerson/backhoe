package br.ufrn.ppgsc.backhoe.repository.code.gitAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

class StreamGobbler extends Thread{
    private InputStream is;
    private List<String> outputLines;
    
    StreamGobbler(InputStream is, List<String> outputLines){
        this.is = is;
        this.outputLines = outputLines;
    }
  
    public void run(){
        try{
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ((line = br.readLine()) != null){
            	this.outputLines.add(0, line);
            }
        } catch (IOException ioe){
            ioe.printStackTrace();  
        }
    }

}