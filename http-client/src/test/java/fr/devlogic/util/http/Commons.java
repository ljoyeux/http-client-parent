package fr.devlogic.util.http;

public class Commons {
    public static boolean find(byte[] pattern, byte[] data) {
        if (data.length < pattern.length) {
            return false;
        }

        next:
        for(int i=0; i<data.length-pattern.length; i++) {
            for(int j=0; j<pattern.length; j++) {
                if(pattern[j] != data[i+j])
                    continue next;
            }
            return true;
        }
        return false;
    }
}
