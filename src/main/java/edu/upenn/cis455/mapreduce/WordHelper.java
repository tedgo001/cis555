package edu.upenn.cis455.mapreduce;

import java.util.ArrayList;
import java.util.List;

import java.io.Serializable;

public class WordHelper implements Serializable{
    private String word;
    private List<String> cnt;
    
    public WordHelper(String word){
        this.word = word;
        this.cnt = new ArrayList<String>();
    }
    
    public final String getWord(){
        return this.word;
    }
    
    public final List<String> getCount(){
        return this.cnt;
    }
    
    public final void addCount(String value){
        this.cnt.add(value);
    }
    
    public String toString(){
        return "[Word: count = " + this.cnt + ", word = " + this.word + " ]";
    }
    
    @Override
    public int hashCode(){
        return this.word != null ? this.word.hashCode() : 0;
    }
    // keep it as HW2, but not sure if it will be needed 
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null || this.getClass() != obj.getClass()){
            return false;
        }
        if(!(obj instanceof WordHelper)){
            return false;
        }
        WordHelper other = (WordHelper) obj;
        if(this.word != null ? !this.word.equals(other.word) : other.word !=null){
            return false;
        }
        return true;
    }
}
