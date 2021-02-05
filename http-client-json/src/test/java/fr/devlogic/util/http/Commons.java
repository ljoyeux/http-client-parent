package fr.devlogic.util.http;

import java.util.Objects;

public final class Commons {
    private Commons() {
    }

    public static final String ACCENTS = "éàù";
    public static final byte[] ACCENTS_UTF8 = new byte[]{(byte)0xc3, (byte)0xa9, (byte)0xc3, (byte)0xa0, (byte)0xc3, (byte)0xb9};
    public static final byte[] ACCENTS_ISO_8859_1 = new byte[]{(byte) 0xE9,(byte) 0xE0, (byte) 0xf9};

    public static class Model {
        private String accents;

        public String getAccents() {
            return accents;
        }

        public void setAccents(String accents) {
            this.accents = accents;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Model model = (Model) o;
            return Objects.equals(accents, model.accents);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accents);
        }
    }

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

    public static String dump(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder();
        for(byte b: data) {
            stringBuilder.append(String.format("%02x ", b));
        }

        return stringBuilder.toString();
    }


    public static byte[] subArray(byte[] data, int begin, int end) {
        byte[] bytes = new byte[end - begin];
        System.arraycopy(data, begin, bytes, 0, bytes.length);
        return bytes;
    }
}
