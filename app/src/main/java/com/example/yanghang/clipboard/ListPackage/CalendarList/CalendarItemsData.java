package com.example.yanghang.clipboard.ListPackage.CalendarList;

/**
 * Created by young on 2017/6/28.
 */

public class CalendarItemsData {
    private String CalendarItemName;
    private String CalendarItemPic;

    public CalendarItemsData(String calendarItemName, String calendarItemPic) {
        CalendarItemName = calendarItemName;
        CalendarItemPic = calendarItemPic;
    }

    public String getCalendarItemName() {
        return CalendarItemName;
    }

    public void setCalendarItemName(String calendarItemName) {
        CalendarItemName = calendarItemName;
    }

    public String getCalendarItemPic() {
        return CalendarItemPic;
    }

    public void setCalendarItemPic(String calendarItemPic) {
        CalendarItemPic = calendarItemPic;
    }
}
