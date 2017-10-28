package com.rainmachine.data.remote;

import com.rainmachine.data.remote.util.RemoteUtils;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RemoteUtilsTest {

    @Test
    public void validURI() {
        assertThat(RemoteUtils.isValidURI("fgh"), is(true));
    }

    @Test
    public void validMacAddress() {
        String upperCase = "E8:DE:27:18:43:28";
        assertThat(RemoteUtils.isValidMacAddress(upperCase), is(true));
        String lowerCase = "e8:de:27:18:43:28";
        assertThat(RemoteUtils.isValidMacAddress(lowerCase), is(true));
    }

    @Test
    public void toIntWorksCorrectly() {
        int val1 = RemoteUtils.toInt(true);
        assertThat(val1, is(1));
        int val2 = RemoteUtils.toInt(false);
        assertThat(val2, is(0));
    }

    @Test
    public void toBooleanWorksCorrectly() {
        boolean val1 = RemoteUtils.toBoolean(1);
        assertThat(val1, is(true));
        boolean val2 = RemoteUtils.toBoolean(0);
        assertThat(val2, is(false));
        boolean val3 = RemoteUtils.toBoolean(2);
        assertThat(val3, is(true));
        boolean val4 = RemoteUtils.toBoolean(-2);
        assertThat(val4, is(true));
        boolean val5 = RemoteUtils.toBoolean(1000);
        assertThat(val5, is(true));
    }
}
