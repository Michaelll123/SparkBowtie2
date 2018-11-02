package cn.edu.nwsuaf;

import cz.adamh.utils.NativeUtils;

import java.io.IOException;


public class sparkjni {
    static {
        try {
            NativeUtils.loadLibraryFromJar("/libsparkbowtie2.so");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    public static int spark_Jni(String[] args) {

        int[] lenStrings = new int[args.length];

        int i = 0;

        for (String argumento : args) {
            lenStrings[i] = argumento.length();
        }

        int returnCode = new sparkjni().bowtie2_jni(args.length, args, lenStrings);

        return returnCode;
    }

    private native int bowtie2_jni(int argc, String[] argv, int[] lenStrings);
}

