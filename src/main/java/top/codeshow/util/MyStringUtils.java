package top.codeshow.util;

import java.util.List;

public class MyStringUtils {
    public static String join(List<String> list, String separator) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i != list.size() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }
}
