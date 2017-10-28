package com.rainmachine;

import org.joda.time.LocalTime;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JodaTimeTest {

    @Test
    public void timeComparison() {
        LocalTime now = new LocalTime(6, 0, 0);
        LocalTime time = new LocalTime(6, 0, 0);
        assertThat(time.isAfter(now), is(false));
        assertThat(time.isBefore(now), is(false));
    }
}
