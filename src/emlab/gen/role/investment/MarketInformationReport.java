package emlab.gen.role.investment;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.engine.Schedule;

public class MarketInformationReport {

	
    long time;
    
    long iteration;
    
    public Schedule schedule;
    
    private EnergyProducer agent;
    
	private Segment segment; 
    
	private double expectedSegmentLoad; 
    
	private double segmentSupply; 
    
	private double totalCapacityAvailable;
    
	private int result;
	
	private double expectedElectricityPrice;
	
    public EnergyProducer getAgent() {
		return agent;
	}

	public void setAgent(EnergyProducer agent) {
		this.agent = agent;
	}

	public long getIteration() {
        return iteration;
    }
    
	public double getExpectedSegmentLoad() {
		return expectedSegmentLoad;
	}
	public void setExpectedSegmentLoad(double expectedSegmentLoad) {
		this.expectedSegmentLoad = expectedSegmentLoad;
	}
	public Segment getSegment() {
		return segment;
	}
	public void setSegment(Segment segment) {
		this.segment = segment;
	}
	public double getSegmentSupply() {
		return segmentSupply;
	}
	public void setSegmentSupply(double segmentSupply) {
		this.segmentSupply = segmentSupply;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
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

	public double getExpectedElectricityPrice() {
		return expectedElectricityPrice;
	}

	public void setExpectedElectricityPrice(double expectedElectricityPrice) {
		this.expectedElectricityPrice = expectedElectricityPrice;
	} 

}
