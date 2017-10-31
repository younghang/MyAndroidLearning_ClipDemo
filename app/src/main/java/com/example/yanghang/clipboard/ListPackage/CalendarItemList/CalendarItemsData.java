package com.example.yanghang.clipboard.ListPackage.CalendarItemList;

/**
 * Created by young on 2017/6/28.
 */

public class CalendarItemsData {
    private String CalendarItemName;
    private String CalendarItemPic;
    private Boolean ShowImage=true;

    public CalendarItemsData() {
    }

    public CalendarItemsData(String calendarItemName, String calendarItemPic) {
        CalendarItemName = calendarItemName;
        CalendarItemPic = calendarItemPic;
    }
    public CalendarItemsData(String calendarItemName, String calendarItemPic,Boolean showImage) {
        CalendarItemName = calendarItemName;
        CalendarItemPic = calendarItemPic;
        ShowImage=showImage;
    }

    public Boolean getShowImage() {
        return ShowImage;
    }

    public void setShowImage(Boolean showImage) {
        ShowImage = showImage;
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
