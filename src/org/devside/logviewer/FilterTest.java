package org.devside.logviewer;

import junit.framework.TestCase;

import java.util.regex.Pattern;

public class FilterTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDefaultFilter() {
        Filter filter = new Filter(1);
        assertTrue(filter.isMatch("1232adfasdfa432dwsf"));
    }

    public void testFilterWithRegex() {
        Filter filter = new Filter("^abc.*", 1, FilterType.INCLUDE);
        assertTrue(filter.isMatch("abcdefg"));
        assertFalse(filter.isMatch("bcdggdee"));
    }

    public void testFilterWithPattern() {
        Pattern p = Pattern.compile("^abc.*");
        Filter filter = new Filter(p, 1, FilterType.INCLUDE);
        assertTrue(filter.isMatch("abcdefg"));
        assertFalse(filter.isMatch("bcdggdee"));
    }
}
