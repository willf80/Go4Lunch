package com.apiman.go4lunch;

import com.apiman.go4lunch.models.ApiDetailsResult;
import com.apiman.go4lunch.models.OpenCloseHour;
import com.apiman.go4lunch.services.Utils;
import com.google.android.libraries.places.api.model.Period;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

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

    @Test
    public void should_get_last_closing_hour() {
        String[] periods = new String[]{
                "Monday: 11:00 AM – 11:00 PM",
                "Tuesday: 11:00 AM – 11:00 PM",
                "Wednesday: 11:00 AM – 11:00 PM",
                "Thursday: 11:00 AM – 11:00 PM",
                "Friday: 11:00 AM – 11:30 PM",
                //"Saturday: 11:00 AM – 11:30 PM",
                "Saturday: 11:00 AM – 2:30 PM, 6:00 – 10:30 PM",
                "Sunday: 6:00 – 11:30 PM"
        };

//        String[] periods = new String[]{
//                "Monday: 11:00 AM – 2:30 PM, 6:00 – 10:30 PM",
//                "Tuesday: 11:00 AM – 2:50 PM, 6:00 – 10:30 PM",
//                "Wednesday: 11:00 AM – 2:30 PM, 6:00 – 10:30 PM",
//                "Thursday: 11:00 AM – 2:30 PM, 6:00 – 10:30 PM",
//                "Friday: 11:00 AM – 2:30 PM, 6:00 – 10:30 PM",
//                "Saturday: 11:00 AM – 2:30 PM, 6:00 – 10:30 PM",
//                "Sunday: 6:00 – 10:30 PM"
//        };

        String result = Utils.getHour(periods[4]);

        assertEquals("11:30 PM", result);
        assertEquals("10:30 PM", Utils.getHour(periods[5]));
    }


    /**
     * "periods" : [
     *      {
     *          "close" : {
     *             "day" : 1,
     *             "time" : "1500"
     *          },
     *          "open" : {
     *             "day" : 1,
     *             "time" : "0900"
     *          }
     *       },
     *       {
     *          "close" : {
     *             "day" : 2,
     *             "time" : "1500"
     *          },
     *          "open" : {
     *             "day" : 2,
     *             "time" : "0900"
     *          }
     *       },
     *       {
     *          "close" : {
     *             "day" : 2,
     *             "time" : "2300"
     *          },
     *          "open" : {
     *             "day" : 2,
     *             "time" : "1900"
     *          }
     *       },
     *       {
     *          "close" : {
     *             "day" : 3,
     *             "time" : "1500"
     *          },
     *          "open" : {
     *             "day" : 3,
     *             "time" : "0900"
     *          }
     *       },
     *       {
     *          "close" : {
     *             "day" : 3,
     *             "time" : "2300"
     *          },
     *          "open" : {
     *             "day" : 3,
     *             "time" : "1900"
     *          }
     *       },
     *       {
     *          "close" : {
     *             "day" : 4,
     *             "time" : "1500"
     *          },
     *          "open" : {
     *             "day" : 4,
     *             "time" : "0900"
     *          }
     *       },
     *       {
     *          "close" : {
     *             "day" : 4,
     *             "time" : "2300"
     *          },
     *          "open" : {
     *             "day" : 4,
     *             "time" : "1900"
     *          }
     *       },
     *       {
     *          "close" : {
     *             "day" : 5,
     *             "time" : "1500"
     *          },
     *          "open" : {
     *             "day" : 5,
     *             "time" : "0900"
     *          }
     *       },
     *       {
     *          "close" : {
     *             "day" : 5,
     *             "time" : "2300"
     *          },
     *          "open" : {
     *             "day" : 5,
     *             "time" : "1900"
     *          }
     *       },
     *       {
     *          "close" : {
     *             "day" : 6,
     *             "time" : "1500"
     *          },
     *          "open" : {
     *             "day" : 6,
     *             "time" : "1000"
     *          }
     *       },
     *       {
     *          "close" : {
     *             "day" : 6,
     *             "time" : "2300"
     *          },
     *          "open" : {
     *             "day" : 6,
     *             "time" : "1900"
     *          }
     *       }
     *    ],
     *    "weekday_text" : [
     *       "Monday: 9:00 AM – 3:00 PM",
     *       "Tuesday: 9:00 AM – 3:00 PM, 7:00 – 11:00 PM",
     *       "Wednesday: 9:00 AM – 3:00 PM, 7:00 – 11:00 PM",
     *       "Thursday: 9:00 AM – 3:00 PM, 7:00 – 11:00 PM",
     *       "Friday: 9:00 AM – 3:00 PM, 7:00 – 11:00 PM",
     *       "Saturday: 10:00 AM – 3:00 PM, 7:00 – 11:00 PM",
     *       "Sunday: Closed"
     *    ]
     * },
     * "place_id" : "ChIJ6VBalmVv5kcRoDnnmF8QYeg"
     */
    @Test
    public void Should_open() {
        ApiDetailsResult.OpeningHour openCloseHour = new ApiDetailsResult.OpeningHour();
        List<ApiDetailsResult.Period> periods = openCloseHour.periods;

        ApiDetailsResult.Period period = new ApiDetailsResult.Period();
        period.open = new ApiDetailsResult.DayTime();
        period.close = new ApiDetailsResult.DayTime();
//        period.close.day

//        String[] periods = new String[]{
//                "Monday: 11:00 AM – 2:30 PM, 6:00 – 10:30 PM",
//                "Tuesday: 11:00 AM – 2:50 PM, 6:00 – 10:30 PM",
//                "Wednesday: 11:00 AM – 2:30 PM, 6:00 – 10:30 PM",
//                "Thursday: 11:00 AM – 2:30 PM, 6:00 – 10:30 PM",
//                "Friday: 11:00 AM – 2:30 PM, 6:00 – 10:30 PM",
//                "Saturday: 11:00 AM – 2:30 PM, 6:00 – 10:30 PM",
//                "Sunday: 6:00 – 10:30 PM"
//        };

//        String result = Utils.getHour(periods[4]);
//
//        assertEquals("11:30 PM", result);
//        assertEquals("10:30 PM", Utils.getHour(periods[5]));
    }
}
