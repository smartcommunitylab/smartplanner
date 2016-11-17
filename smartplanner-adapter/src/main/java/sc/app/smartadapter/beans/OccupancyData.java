package sc.app.smartadapter.beans;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OccupancyData {

	@JsonIgnore
	Map<String, OccupancyRate> occupancyRateMap;

	OccupancyRate dH1;
	OccupancyRate dH2;
	OccupancyRate dH3;
	OccupancyRate dH4;
	OccupancyRate dH5;
	OccupancyRate dH6;
	OccupancyRate dH7;

	OccupancyRate h0;
	OccupancyRate h8;
	OccupancyRate h9;
	OccupancyRate h10;
	OccupancyRate h11;
	OccupancyRate h12;
	OccupancyRate h13;
	OccupancyRate h14;
	OccupancyRate h15;
	OccupancyRate h16;
	OccupancyRate h17;
	OccupancyRate h18;
	OccupancyRate h19;
	OccupancyRate h20;
	OccupancyRate h21;
	OccupancyRate h22;
	OccupancyRate h23;

	private OccupancyData() {
		occupancyRateMap = new HashMap<String, OccupancyRate>();
	}

	public Map<String, OccupancyRate> getOccupancyRateMap() {
		return occupancyRateMap;
	}

	public void setOccupancyRateMap(Map<String, OccupancyRate> occupancyRateMap) {
		this.occupancyRateMap = occupancyRateMap;
	}

	@JsonProperty("1")
	public OccupancyRate getdH1() {
		return dH1;
	}

	@JsonProperty("1")
	public void setdH1(OccupancyRate dH1) {
		occupancyRateMap.put("1", dH1);
		this.dH1 = dH1;
	}

	@JsonProperty("2")
	public OccupancyRate getdH2() {
		return dH2;
	}

	@JsonProperty("2")
	public void setdH2(OccupancyRate dH2) {
		occupancyRateMap.put("2", dH2);
		this.dH2 = dH2;
	}

	@JsonProperty("3")
	public OccupancyRate getdH3() {
		return dH3;
	}

	@JsonProperty("3")
	public void setdH3(OccupancyRate dH3) {
		occupancyRateMap.put("3", dH3);
		this.dH3 = dH3;
	}

	@JsonProperty("4")
	public OccupancyRate getdH4() {
		return dH4;
	}

	@JsonProperty("4")
	public void setdH4(OccupancyRate dH4) {
		occupancyRateMap.put("4", dH4);
		this.dH4 = dH4;
	}

	@JsonProperty("5")
	public OccupancyRate getdH5() {
		return dH5;
	}

	@JsonProperty("5")
	public void setdH5(OccupancyRate dH5) {
		occupancyRateMap.put("5", dH5);
		this.dH5 = dH5;
	}

	@JsonProperty("6")
	public OccupancyRate getdH6() {
		return dH6;
	}

	@JsonProperty("6")
	public void setdH6(OccupancyRate dH6) {
		occupancyRateMap.put("6", dH6);
		this.dH6 = dH6;
	}

	@JsonProperty("7")
	public OccupancyRate getdH7() {
		return dH7;
	}

	@JsonProperty("7")
	public void setdH7(OccupancyRate dH7) {
		occupancyRateMap.put("7", dH7);
		this.dH7 = dH7;
	}

	@JsonProperty("0")
	public OccupancyRate getH0() {
		return h0;
	}

	@JsonProperty("0")
	public void setH0(OccupancyRate h0) {
		occupancyRateMap.put("0", h0);
		this.h0 = h0;
	}

	@JsonProperty("8")
	public OccupancyRate getH8() {
		return h8;
	}

	@JsonProperty("8")
	public void setH8(OccupancyRate h8) {
		occupancyRateMap.put("8", h8);
		this.h8 = h8;
	}

	@JsonProperty("9")
	public OccupancyRate getH9() {
		return h9;
	}

	@JsonProperty("9")
	public void setH9(OccupancyRate h9) {
		occupancyRateMap.put("9", h9);
		this.h9 = h9;
	}

	@JsonProperty("10")
	public OccupancyRate getH10() {
		return h10;
	}

	@JsonProperty("10")
	public void setH10(OccupancyRate h10) {
		occupancyRateMap.put("10", h10);
		this.h10 = h10;
	}

	@JsonProperty("11")
	public OccupancyRate getH11() {
		return h11;
	}

	@JsonProperty("11")
	public void setH11(OccupancyRate h11) {
		occupancyRateMap.put("11", h11);
		this.h11 = h11;
	}

	@JsonProperty("12")
	public OccupancyRate getH12() {
		return h12;
	}

	@JsonProperty("12")
	public void setH12(OccupancyRate h12) {
		occupancyRateMap.put("12", h12);
		this.h12 = h12;
	}

	@JsonProperty("13")
	public OccupancyRate getH13() {
		return h13;
	}

	@JsonProperty("13")
	public void setH13(OccupancyRate h13) {
		occupancyRateMap.put("13", h13);
		this.h13 = h13;
	}

	@JsonProperty("14")
	public OccupancyRate getH14() {
		return h14;
	}

	@JsonProperty("14")
	public void setH14(OccupancyRate h14) {
		occupancyRateMap.put("14", h14);
		this.h14 = h14;
	}

	@JsonProperty("15")
	public OccupancyRate getH15() {
		return h15;
	}

	@JsonProperty("15")
	public void setH15(OccupancyRate h15) {
		occupancyRateMap.put("15", h15);
		this.h15 = h15;
	}

	@JsonProperty("15")
	public OccupancyRate getH16() {
		return h16;
	}

	@JsonProperty("16")
	public void setH16(OccupancyRate h16) {
		occupancyRateMap.put("16", h16);
		this.h16 = h16;
	}

	@JsonProperty("16")
	public OccupancyRate getH17() {
		return h17;
	}

	@JsonProperty("17")
	public void setH17(OccupancyRate h17) {
		occupancyRateMap.put("17", h17);
		this.h17 = h17;
	}

	@JsonProperty("17")
	public OccupancyRate getH18() {
		return h18;
	}

	@JsonProperty("18")
	public void setH18(OccupancyRate h18) {
		occupancyRateMap.put("18", h18);
		this.h18 = h18;
	}

	@JsonProperty("18")
	public OccupancyRate getH19() {
		return h19;
	}

	@JsonProperty("19")
	public void setH19(OccupancyRate h19) {
		occupancyRateMap.put("19", h19);
		this.h19 = h19;
	}

	@JsonProperty("20")
	public OccupancyRate getH20() {
		return h20;
	}

	@JsonProperty("20")
	public void setH20(OccupancyRate h20) {
		occupancyRateMap.put("20", h20);
		this.h20 = h20;
	}

	@JsonProperty("21")
	public OccupancyRate getH21() {
		return h21;
	}

	@JsonProperty("21")
	public void setH21(OccupancyRate h21) {
		occupancyRateMap.put("21", h21);
		this.h21 = h21;
	}

	@JsonProperty("22")
	public OccupancyRate getH22() {
		return h22;
	}

	@JsonProperty("22")
	public void setH22(OccupancyRate h22) {
		occupancyRateMap.put("22", h21);
		this.h22 = h22;
	}

	@JsonProperty("23")
	public OccupancyRate getH23() {
		return h23;
	}

	@JsonProperty("23")
	public void setH23(OccupancyRate h23) {
		occupancyRateMap.put("23", h23);
		this.h23 = h23;
	}

}
