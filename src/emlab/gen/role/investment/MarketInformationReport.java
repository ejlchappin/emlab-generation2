package emlab.gen.role.investment;

public class MarketInformationReport {

	
    private double expectedSegmentLoad; 
    private double segmentID; 
    private double segmentSupply; 
    private double time;
    private double totalCapacityAvailable;
    private int result;
    
	public double getExpectedSegmentLoad() {
		return expectedSegmentLoad;
	}
	public void setExpectedSegmentLoad(double expectedSegmentLoad) {
		this.expectedSegmentLoad = expectedSegmentLoad;
	}
	public double getSegmentID() {
		return segmentID;
	}
	public void setSegmentID(double segmentID) {
		this.segmentID = segmentID;
	}
	public double getSegmentSupply() {
		return segmentSupply;
	}
	public void setSegmentSupply(double segmentSupply) {
		this.segmentSupply = segmentSupply;
	}
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public double getTotalCapacityAvailable() {
		return totalCapacityAvailable;
	}
	public void setTotalCapacityAvailable(double totalCapacityAvailable) {
		this.totalCapacityAvailable = totalCapacityAvailable;
	}
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	} 

}
