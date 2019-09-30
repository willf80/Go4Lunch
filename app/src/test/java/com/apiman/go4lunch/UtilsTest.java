package com.apiman.go4lunch;

import com.apiman.go4lunch.models.DayTime;
import com.apiman.go4lunch.models.Period;
import com.apiman.go4lunch.services.Utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

public class UtilsTest {

    @Test
    public void should_return_0m() {
        int result = Utils.distanceInMeters(0, 0, 0, 0);
        assertEquals(0, result);
    }

    @Test
    public void should_return_145m() {
        int result = Utils.distanceInMeters(48.915079, 2.289694, 48.914684, 2.287805);
        assertEquals(145, result);
    }

    @Test
    public void closingSoon_should_be_true_when_hour_plus_60_greater_than_closingTime() {
        // Arrange
        List<Period> periods = new ArrayList<>();

        Period period = new Period();
        period.open = new DayTime(6, "1100");
        period.close = new DayTime(6, "2330");

        periods.add(period);

        // Act
        boolean result = Utils.isClosingSoon(periods, 6, 2231);

        // Assert
        assertTrue(result);
    }

    @Test
    public void with_2_periods_closingSoon_should_be_true_when_hour_plus_60_greater_than_closingTime() {
        List<Period> periods = new ArrayList<>();

        Period period1 = new Period();
        period1.open = new DayTime(6, "1100");
        period1.close = new DayTime(6, "1430");

        Period period2 = new Period();
        period2.open = new DayTime(6, "1800");
        period2.close = new DayTime(6, "2230");

        periods.add(period1);
        periods.add(period2);

        boolean result = Utils.isClosingSoon(periods, 6, 2200);

        assertTrue(result);
    }

    private List<Period> createPeriodMock() {
        List<Period> periods = new ArrayList<>();

        Period period1 = new Period();
        period1.open = new DayTime(6, "1100");
        period1.close = new DayTime(6, "1430");

        Period period2 = new Period();
        period2.open = new DayTime(6, "1800");
        period2.close = new DayTime(0, "0030");

        Period period3 = new Period();
        period3.open = new DayTime(5, "1000");
        period3.close = new DayTime(6, "0030");

        periods.add(period1);
        periods.add(period2);
        periods.add(period3);

        return periods;
    }

    @Test
    public void with_2_periods_closingSoon_should_be_true_when_closeTime_is_on_other_day() {
        List<Period> periodList = createPeriodMock();

        assertTrue(Utils.isClosingSoon(periodList, 6, 1331));
        assertTrue(Utils.isClosingSoon(periodList, 6, 2331));
        assertTrue(Utils.isClosingSoon(periodList, 6, 0));//00:00
        assertTrue(Utils.isClosingSoon(periodList, 5, 2345));
    }

    @Test
    public void should_get_current_period() {
        List<Period> periodList = createPeriodMock();

        Period period = Utils.getCurrentPeriod(periodList, 5, 1831);

        assertNotNull(period);
        assertEquals("1000", period.open.timeText);
    }

    @Test
    public void should_get_time_in_fr() {
        List<Period> periodList = createPeriodMock();

        Period period = periodList.get(0);

        assertNotNull(period);
        assertEquals("14h30", period.close.getTime(Locale.FRANCE));
    }

    @Test
    public void should_return_time_in_en() {
        List<Period> periodList = createPeriodMock();

        Period period = periodList.get(0);

        assertNotNull(period);
        assertEquals("2.30pm", period.close.getTime(Locale.ENGLISH));
    }

    @Test
    public void should_return_restaurant_status_closed() {
        List<Period> periodList = createPeriodMock();
        boolean isOpenNow = false;
        boolean isClosingSoon = false;

        Period period = periodList.get(0);
        String status = Utils.restaurantStatus(isOpenNow, isClosingSoon, period);

        assertNotNull(period);
        assertEquals("Closed", status);
    }

    @Test
    public void should_return_restaurant_status_ClosingSoon() {
        List<Period> periodList = createPeriodMock();
        boolean isOpenNow = true;
        boolean isClosingSoon = Utils.isClosingSoon(periodList, 6, 1331);
        Period period = Utils.getCurrentPeriod(periodList, 6, 1331);

        String status = Utils.restaurantStatus(isOpenNow, isClosingSoon, period);

        assertNotNull(period);
        assertEquals("Closing soon", status);
    }

    @Test
    public void should_return_restaurant_status_Open24_7() {
        boolean isOpenNow = true;
        boolean isClosingSoon = false;
        Period period = new Period();
        period.open = new DayTime(0, "0000");

        String status = Utils.restaurantStatus(isOpenNow, isClosingSoon, period);

        assertNotNull(period);
        assertEquals("Open 24/7", status);
    }

    @Test
    public void should_return_restaurant_status_OpenUntil() {
        List<Period> periodList = createPeriodMock();
        boolean isOpenNow = true;
        boolean isClosingSoon = false;

//        period.open = "1100"
//        period.close = "1430"
        Period period = periodList.get(0);

        String status = Utils.restaurantStatus(isOpenNow, isClosingSoon, period, Locale.ENGLISH);

        assertNotNull(period);
        assertEquals("Open until 2.30pm", status);
    }
}
