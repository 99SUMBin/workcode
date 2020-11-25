package com.fusen.workcode.utils;

import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat sdf_m = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static SimpleDateFormat sdf_d = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdfANSI = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
	private static SimpleDateFormat sdfANSI_m = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
	private static SimpleDateFormat sdfANSI_d = new SimpleDateFormat("yyyy年MM月dd日");
	private static SimpleDateFormat sdf_no_space = new SimpleDateFormat("yyyyMMddHHmmss");


	/**
     * 判断日期是否是合法的字符串
     * @param str 日期字符串
     * @return
     */
    public static boolean isValidDate(String str) {
        boolean convertSuccess=true;
        try {
            sdf_d.setLenient(false);
            sdf_d.parse(str);
        } catch (ParseException e) {
            convertSuccess=false;
        }
        return convertSuccess;
    }
	
	//格式化日期,精确到秒
	public static String format_s(Date date){
		if (date==null) {
			System.out.println("日期格式化异常！传入的参数为null");
			return "1970-01-01 08:00:00";
		}
		return sdf.format(date);
	}
	//格式化日期,精确到分
	public static String format_m(Date date){
		if (date==null) {
			System.out.println("日期格式化异常！传入的参数为null");
			return "1970-01-01 08:00";
		}
		return sdf_m.format(date);
	}
	//格式化日期,精确到天
	public static String format_d(Date date){
		if (date==null) {
			System.out.println("日期格式化异常！传入的参数为null");
			return "1970-01-01";
		}
		return sdf_d.format(date);
	}
	//格式化日期,精确到秒
	public static String formatANSI(Date date){
		if (date==null) {
			System.out.println("日期格式化异常！传入的参数为null");
			return "1970年01月01日 08:00:00";
		}
		return sdfANSI.format(date);
	}
	//格式化日期,精确到分
	public static String formatANSI_m(Date date){
		if (date==null) {
			System.out.println("日期格式化异常！传入的参数为null");
			return "1970年01月01日 08:00";
		}
		return sdfANSI_m.format(date);
	}
	//格式化日期,精确到天
	public static String formatANSI_d(Date date){
		if (date==null) {
			System.out.println("日期格式化异常！传入的参数为null");
			return "1970年01月01日";
		}
		return sdfANSI_d.format(date);
	}
	
	//将字符串形式的日期转换为日期对象,精确到秒
	public static Date parse_s(String dateStr) throws ParseException{
		if (StringUtils.isEmpty(dateStr)){
			System.out.println("日期格式化异常！传入的参数为null");
			return new Date();
		}
		return sdf.parse(dateStr);
	}

	//将字符串形式的日期转换为日期对象,精确到天
	public static Date parse_d(String dateStr) throws ParseException{
		if (StringUtils.isEmpty(dateStr)){
			System.out.println("日期格式化异常！传入的参数为null");
			return new Date();
		}
		return sdf_d.parse(dateStr);
	}
	
	//将字符串形式的日期转换为日期对象
	public static Date parseANSI(String dateStr){
		try {
			return sdfANSI.parse(dateStr);
		} catch (ParseException e) {
			System.out.println("将("+dateStr+")转换为日期对象异常！-->"+e);
			return new Date(0);
		}
	}

	public static String format_no_space(Date date){
		if (date==null){
			return "19700101080000";
		}
		return sdf_no_space.format(date);
	}

	//计算两段时间的间隔时长
	public static String dateDiff(Date begin, Date end, int style) throws Exception{
		long diff = (end.getTime()-begin.getTime())/1000;
		if (diff<=0){
			throw new Exception("DateUtils.dateDiff() exception! -->结束时间须大于开始时间！");
		}
		long days = diff / (60 * 60 * 24);
		long hours = (diff % (60 * 60 * 24))/(60 * 60);
		long minutes = (diff % (60 * 60))/ 60;
		long seconds = diff % 60;
		//System.out.println(days+"天"+hours+"小时"+minutes+"分"+seconds+"秒");
		String retval = null;
		switch (style){
			case 1:
				retval = days+"天"+hours+"小时"+minutes+"分"+seconds+"秒";
				break;
			case 2:
				retval = days+"天"+hours+"小时"+minutes+"分";
				break;
			case 3:
				retval = days+"天"+hours+"小时";
				break;
			case 4:
				retval = days+"天";
				break;
			default:
				throw new Exception("DateUtils.dateDiff() exception! -->参数style无效！");
		}
		return retval;
	}
	
	
	/**
     * 获取等待时间
     * @param startTime
     * @return
     */
    public static String getTime(String startTime){
        String tStr="已等待";
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long endTime = 0L;
        try {
            Date date = format.parse(startTime);
            endTime = date.getTime();
			//秒值
            int seTime = (int) (System.currentTimeMillis()-endTime)/1000;

            int day= (seTime/(24*60*60));
            int hour = ((seTime)%(24*60*60)/3600);
            int mins = ((seTime)%3600/60);
            int seconds = seTime%60;
            if (day>0){
                tStr += day+"天";
            }
            if (hour>0){
                tStr += hour + "时";
            }
            if (mins>=0){
                tStr += mins+"分";
            }
            if (seconds>0){
                tStr += seconds+"秒";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return tStr;
    }
}
