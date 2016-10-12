package it.sayservice.platform.smartplanner.areainfo;

import java.util.ArrayList;
import java.util.List;

public class SearchTimeWeekDayList {

	TimeSlotList d1;
	TimeSlotList d2;
	TimeSlotList d3;
	TimeSlotList d4;
	TimeSlotList d5;
	TimeSlotList d6;
	TimeSlotList d7;

	public static final int dom = 1;
	public static final int lun = 2;
	public static final int mart = 3;
	public static final int merc = 4;
	public static final int giov = 5;
	public static final int ven = 6;
	public static final int sab = 7;

	public TimeSlotList get(int dayNumber) {
		if (dayNumber==1){
			return getD1();
		}
		if (dayNumber==2){
			return getD3();
		}

		if (dayNumber==3){
			return getD3();
		}

		if (dayNumber==4){
			return getD4();
		}

		if (dayNumber==5){
			return getD5();
		}

		if (dayNumber==6){
			return getD6();
		}

		if (dayNumber==7){
			return getD7();
		}

		return null;
	}

	public TimeSlotList getD1() {
		return d1;
	}

	public void setD1(TimeSlotList d1) {
		this.d1 = d1;
	}

	public TimeSlotList getD2() {
		return d2;
	}

	public void setD2(TimeSlotList d2) {
		this.d2 = d2;
	}

	public TimeSlotList getD3() {
		return d3;
	}

	public void setD3(TimeSlotList d3) {
		this.d3 = d3;
	}

	public TimeSlotList getD4() {
		return d4;
	}

	public void setD4(TimeSlotList d4) {
		this.d4 = d4;
	}

	public TimeSlotList getD5() {
		return d5;
	}

	public void setD5(TimeSlotList d5) {
		this.d5 = d5;
	}

	public TimeSlotList getD6() {
		return d6;
	}

	public void setD6(TimeSlotList d6) {
		this.d6 = d6;
	}

	public TimeSlotList getD7() {
		return d7;
	}

	public void setD7(TimeSlotList d7) {
		this.d7 = d7;
	}

}
