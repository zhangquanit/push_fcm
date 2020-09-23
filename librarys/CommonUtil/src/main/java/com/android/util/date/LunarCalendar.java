package com.android.util.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 农历日历
 */
public class LunarCalendar {
    /* 农历年月日 */
    private int lyear, lmonth, lday = 0;
    /* 天干地支 年月日 */
    private int yearCyl, monCyl, dayCyl = 0;
    /* 节气,公历节日,农历节日 */
    private String solarTerms, solarFestival, lunarFestival = "";
    /* 是否闰月 */
    private boolean leap = false;

    private final Calendar baseDate = Calendar.getInstance();
    private final Calendar offDate = Calendar.getInstance();
    private final static long[] lunarInfo = new long[]{//
            0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,// 191
            0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,// 192
            0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,// 193
            0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,// 194
            0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,// 195
            0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,// 196
            0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,// 197
            0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,// 198
            0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,// 199
            0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,// 200
            0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,// 201
            0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,// 202
            0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,// 203
            0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,// 204
            0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0 // 205
    };
    final static String[] chineseTen = new String[]{"初", "十", "廿", "三"};
    final static String[] chineseNum = new String[]{"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    final static String[] Gan = new String[]{"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
    final static String[] Zhi = new String[]{"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
    final static String chineseMonthNumber[] = {"正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"};
    final static String[] Animals = new String[]{"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
    final static long[] STermInfo = new long[]{0, 21208, 42467, 63836, 85337, 107014, 128867, 150921, 173149, 195551, 218072, 240693, 263343, 285989, 308563, 331033, 353350, 375494, 397447, 419210, 440795, 462224, 483532, 504758};
    final static String[] SolarTerm = new String[]{"小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨", "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"};
    final static String[] sFtv = new String[]{"0101*元旦", "0214 情人节", "0308 妇女节", "0312 植树节", "0314 警察日",
// "0315 消费者权益",
            "0323 气象日", "0401 愚人节", "0407 卫生日", "0501*劳动节", "0504 青年节",
// "0508 红十字日",
            "0512 护士节", "0515 家庭日", "0517 电信日",
// "0519 全国助残日",
            "0531 无烟日", "0601 儿童节", "0605 环境日",
// "0606 全国爱眼日", "0623 奥林匹克日", "0625 全国土地日", "0626 反毒品日",
            "0701 建党节",
// "0707 抗战纪念日",
            "0711 人口日", "0801 建军节", "0908 扫盲日",
// "0909 毛泽东逝世纪念",
            "0910 教师节", "0917 和平日", "0920 爱牙日", "0922 聋人节", "0927 旅游日",
// "0928 孔子诞辰",
            "1001*国庆节", "1004 动物日", "1006 老人节", "1007 住房日", "1009 邮政日", "1015 盲人节", "1016 粮食日",
// "1024 联合国日",
            "1101 万圣节",
// "1108 中国记者日", "1109 消防宣传日","1112 孙中山诞辰", "1114 糖尿病日", "1117 大学生节",
            "1128 感恩节",
// "1201 艾滋病日", "1203 残疾人日",
            "1209 足球日",
// "1220 澳门回归",
            "1225 圣诞节",
// "1226 毛泽东诞辰"
    };
    final static String[] lFtv = {"0101*春节", "0115 元宵", "0505 端午", "0707 七夕", "0815 中秋", "0909 重阳", "1208 腊八", "1223 小年", "0100*除夕"};
    final static String[] wFtv = {"0521 母亲节", "0631 父亲节"};// 每年6月第3个星期日是父亲节,5月的第2个星期日是母亲节

    // 星期日是一个周的第1天第3个星期日也就是第3个完整周的第一天
    public LunarCalendar() {
        baseDate.setMinimalDaysInFirstWeek(7);// 设置一个月的第一个周是一个完整周
    }

    // 农历 y年的总天数
    final private synchronized static int lYearDays(int y) {
        int i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            if ((lunarInfo[y - 1900] & i) != 0) sum += 1;
        }
        return (sum + leapDays(y));
    }

    // 农历 y年闰月的天数
    final private synchronized static int leapDays(int y) {
        if (leapMonth(y) != 0) {
            if ((lunarInfo[y - 1900] & 0x10000) != 0) return 30;
            else return 29;
        } else return 0;
    }

    // 农历 y年闰哪个月 1-12 , 没闰传回 0
    final private synchronized static int leapMonth(int y) {
        return (int) (lunarInfo[y - 1900] & 0xf);
    }

    // 农历 y年m月的总天数
    final private synchronized static int monthDays(int y, int m) {
        if ((lunarInfo[y - 1900] & (0x10000 >> m)) == 0) return 29;
        else return 30;
    }

    // 农历 y年的生肖
    final private static String animalsYear(int y) {
        return Animals[(y - 4) % 12];
    }

    // 的offset 传回干支,
    final private static String cyclical(int num) {
        return (Gan[num % 10] + Zhi[num % 12]);// 0=甲子
    }

    // 某年的第n个节气为几日(从0小寒起算)
    final private int sTerm(int y, int n) {
        offDate.set(1900, 0, 6, 2, 5, 0);
        long temp = offDate.getTime().getTime();
        offDate.setTime(new Date((long) ((31556925974.7 * (y - 1900) + STermInfo[n] * 60000L) + temp)));
        return offDate.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 传出y年m月d日对应的农历.
     */
    private void calculateLunarCalendar(int y, int m, int d) {
        int leapMonth = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        try {
            baseDate.setTime(dateFormat.parse("1900年1月31日"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long base = baseDate.getTimeInMillis();
        try {
            baseDate.setTime(dateFormat.parse(y + "年" + m + "月" + d + "日"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long obj = baseDate.getTimeInMillis();
        int offset = (int) ((obj - base) / 86400000L);
        // 求出和1900年1月31日相差的天数
        dayCyl = offset + 40;// 干支天
        monCyl = 14;// 干支月
        // 用offset减去每农历年的天数
        // 计算当天是农历第几天
        // i最终结果是农历的年份
        // offset是当年的第几天
        int iYear, daysOfYear = 0;
        for (iYear = 1900; iYear < 2050 && offset > 0; iYear++) {
            daysOfYear = lYearDays(iYear);
            offset -= daysOfYear;
            monCyl += 12;
        }
        if (offset < 0) {
            offset += daysOfYear;
            iYear--;
            monCyl -= 12;
        }
        // 农历年份
        lyear = iYear;
        yearCyl = iYear - 1864;
        // ***********干支年**********//
        leapMonth = leapMonth(iYear); // 闰哪个月,1-12
        leap = false;
        // 用当年的天数offset,逐个减去每月（农历）的天数，求出当天是本月的第几天
        int iMonth, daysOfMonth = 0;
        for (iMonth = 1; iMonth < 13 && offset > 0; iMonth++) {
            // 闰月
            if (leapMonth > 0 && iMonth == (leapMonth + 1) && !leap) {
                --iMonth;
                leap = true;
                daysOfMonth = leapDays(iYear);
            } else daysOfMonth = monthDays(iYear, iMonth);
            offset -= daysOfMonth;
            // 解除闰月
            if (leap && iMonth == (leapMonth + 1)) leap = false;
            if (!leap) monCyl++;
        }
        // offset为0时，并且刚才计算的月份是闰月，要校正
        if (offset == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
            if (leap) {
                leap = false;
            } else {
                leap = true;
                --iMonth;
                --monCyl;
            }
        }
        // offset小于0时，也要校正
        if (offset < 0) {
            offset += daysOfMonth;
            --iMonth;
            --monCyl;
        }
        lmonth = iMonth;
        lday = offset + 1;

        // ******************计算节气**********//
        if (d == sTerm(y, (m - 1) * 2)) solarTerms = SolarTerm[(m - 1) * 2];
        else if (d == sTerm(y, (m - 1) * 2 + 1)) solarTerms = SolarTerm[(m - 1) * 2 + 1];
        else solarTerms = "";

        // 指定节气
        if (y == 2011 && solarTerms == "立冬") solarTerms = "";
        if (y == 2011 && m == 11 && d == 8) solarTerms = "立冬";

        if (y == 2011 && solarTerms == "小雪") solarTerms = "";
        if (y == 2011 && m == 11 && d == 23) solarTerms = "小雪";

        if (y == 2012 && solarTerms == "小寒") solarTerms = "";
        if (y == 2012 && m == 1 && d == 6) solarTerms = "小寒";

        if (y == 2012 && solarTerms == "大寒") solarTerms = "";
        if (y == 2012 && m == 1 && d == 21) solarTerms = "大寒";

        if (y == 2012 && solarTerms == "立夏") solarTerms = "";
        if (y == 2012 && m == 5 && d == 5) solarTerms = "立夏";

        if (y == 2012 && solarTerms == "小满") solarTerms = "";
        if (y == 2012 && m == 5 && d == 20) solarTerms = "小满";

        if (y == 2013 && solarTerms == "立春") solarTerms = "";
        if (y == 2013 && m == 2 && d == 4) solarTerms = "立春";

        if (y == 2013 && solarTerms == "雨水") solarTerms = "";
        if (y == 2013 && m == 2 && d == 18) solarTerms = "雨水";

        // 计算公历节日
        this.solarFestival = "";
        for (int i = 0; i < sFtv.length; i++) {
            if (Integer.parseInt(sFtv[i].substring(0, 2)) == m && Integer.parseInt(sFtv[i].substring(2, 4)) == d) {
                solarFestival = sFtv[i].substring(5);
            }
        }
        // 计算农历节日
        this.lunarFestival = "";
        for (int i = 0; i < lFtv.length; i++) {
            if (Integer.parseInt(lFtv[i].substring(0, 2)) == lmonth && Integer.parseInt(lFtv[i].substring(2, 4)) == lday) {
                lunarFestival = lFtv[i].substring(5);
            }
        }
        // 计算月周节日
        for (int i = 0; i < wFtv.length; i++) {
            if (Integer.parseInt(wFtv[i].substring(0, 2)) == m//
                    && Integer.parseInt(wFtv[i].substring(2, 3)) == baseDate.get(Calendar.DAY_OF_WEEK_IN_MONTH) //
                    && Integer.parseInt(wFtv[i].substring(3, 4)) == baseDate.get(Calendar.DAY_OF_WEEK)) {
                solarFestival += wFtv[i].substring(5);
            }
        }
    }

    /**
     * 设置日期
     *
     * @param 公历年
     * @param 公历月
     * @param 公历日
     */
    public synchronized void set(int y, int m, int d) {
        int maxYear = 1900 + lunarInfo.length - 1;
        if (y > maxYear) throw new RuntimeException("Max Year " + maxYear);
        calculateLunarCalendar(y, m, d);
    }

    /**
     * 设置日期
     *
     * @param 公历日历对象
     */
    public synchronized void set(Calendar cal) {
        calculateLunarCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * 获得农历日
     *
     * @return String
     */
    public synchronized String getLunarDayString() {
        int n = lday % 10 == 0 ? 9 : lday % 10 - 1;
        if (lday == 30) return "三十";
        else if (lday == 10) return "初十";
        else if (lday == 20) return "二十";
        else if (n == 0) return chineseTen[lday / 10] + "一";
        else return chineseTen[lday / 10] + chineseNum[lday % 10];
    }

    /**
     * 获得农历年
     *
     * @return String
     */
    public synchronized String getLunarYearString() {
        String yearStr = "";
        char[] charArray = String.valueOf(lyear).toCharArray();
        for (char c : charArray) {
            yearStr += chineseNum[c - 48];
        }
        return yearStr;
    }

    /**
     * 获得农历月
     *
     * @return String
     */
    public synchronized String getLunarMonthString() {
        return chineseMonthNumber[lmonth - 1];
    }

    /**
     * 获得生肖
     *
     * @return animals
     */
    public String getAnimals() {
        return animalsYear(lyear);
    }

    /**
     * 获得天干地支_年
     *
     * @return String
     */
    public String getYearGZ() {
        return cyclical(yearCyl) + "年";
    }

    /**
     * 获得天干地支_月
     *
     * @return String
     */
    public String getMonthGZ() {
        return cyclical(monCyl);
    }

    /**
     * 获得天干地支_日
     *
     * @return String
     */
    public String getDayGZ() {
        return cyclical(dayCyl);
    }

    /**
     * 获得节气
     *
     * @return String
     */
    public String getSolarTerms() {
        return solarTerms;
    }

    /**
     * 获得公历节日
     *
     * @return String
     */
    public String getSolarFestival() {
        return solarFestival;
    }

    /**
     * 获得农历节日
     *
     * @return String
     */
    public String getLunarFestival() {
        return lunarFestival;
    }
}