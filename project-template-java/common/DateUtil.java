package grakn.template.java;

import java.util.Date;

public class DateUtil {
    public String getCurrentTime() {
        return (new Date()).toString();
    }
}