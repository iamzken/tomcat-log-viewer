package org.devside.logviewer;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Filter {
    private Pattern pattern;

    private int id;

    private FilterType type;

    public Filter(int id) {
        this.pattern = Pattern.compile(".*");
        this.id = id;
        this.type = FilterType.INCLUDE;
    }

    public Filter(String regex, int id, FilterType type) {
        this.pattern = Pattern.compile(regex);
        this.id = id;
        this.type = type;
    }

    public Filter(Pattern pattern, int id, FilterType type) {
        this.pattern = pattern;
        this.id = id;
        this.type = type;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public int getId() {
        return id;
    }

    public FilterType getType() {
        return type;
    }

    public boolean isMatch(String line) {
        Matcher m = pattern.matcher(line);
        return m.find();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Filter filter = (Filter) o;

        return id == filter.id;
    }

    public int hashCode() {
        return id;
    }
}
