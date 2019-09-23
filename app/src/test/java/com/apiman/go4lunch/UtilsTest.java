package com.apiman.go4lunch;

import com.apiman.go4lunch.services.Utils;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class UtilsTest {

    @Test
    public void should_return_0m() {

        int result = Utils.distanceInMeters(0, 0, 0, 0);

        assertEquals(0, result);
    }

    @Test
    public void should_return_100m() {

        int result = Utils.distanceInMeters(48.915079, 2.289694, 48.914684, 2.287805);

        assertEquals(145, result);
    }
}
