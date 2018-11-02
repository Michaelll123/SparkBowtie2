package cn.edu.nwsuaf;

import java.io.File;
import java.io.Serializable;

import java.util.ArrayList;

public class sparkbowtie2 implements Serializable{

    public void runbowtie2(String index, String path1, String path2, String outPath, ArrayList <String> options){

        ArrayList<String> st=new ArrayList<String>();
        st.add("bowtie2");
        for (String str:options)
        {
            st.add(str);
            System.out.println("options: "+str);
        }
        st.add("-x");
        st.add(index);
        st.add("-1");
        st.add(path1);
        st.add("-2");
        st.add(path2);
        st.add("-S");
        st.add(outPath);
        String [] array= new String [st.size()];

        for(String s:st)
            System.out.println(s);
         sparkjni jni=new sparkjni();
        int returnedcode =jni.spark_Jni(st.toArray(array));
        File FastqFile1 = new File(path1);
        File FastqFile2 = new File(path2);
        FastqFile1.delete();
        FastqFile2.delete();
        System.out.println("return: "+returnedcode);
    }
}
