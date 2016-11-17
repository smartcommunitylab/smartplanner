package sc.app.smartadapter.beans;

public class OccupancyRate {

	int count;
	double aggregateValue;
	double lastValue;
	long lastUpdate;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public double getAggregateValue() {
		return aggregateValue;
	}

	public void setAggregateValue(double aggregateValue) {
		this.aggregateValue = aggregateValue;
	}

	public double getLastValue() {
		return lastValue;
	}

	public void setLastValue(double lastValue) {
		this.lastValue = lastValue;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

}
