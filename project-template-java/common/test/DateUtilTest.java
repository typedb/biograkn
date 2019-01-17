package grakn.template.java;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class DateUtilTest {
    @Test
    public void dateUtilTest() {
        DateUtil dateUtil = new DateUtil();
        assertNotNull(dateUtil.getCurrentTime());
    }
}