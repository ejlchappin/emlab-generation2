package emlab.gen.role.investment;

import java.util.Map;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.technology.Substance;
import emlab.gen.engine.Schedule;

public class MarketInformationReport {

	
    long time;
    
    long iteration;
    
    public Schedule schedule;
    
    private ElectricitySpotMarket market;
    
    private EnergyProducer agent;
    
	private Segment segment; 
    
	private double expectedSegmentLoad; 
    
	private double segmentSupply; 
    
	private double totalCapacityAvailable;
    
	private int result;
	
	private double expectedElectricityPrice;
	
	
	Map<ElectricitySpotMarket, Double> expectedDemand; 
	
	Map<Substance, Double> fuelPrices;
	
	double co2price;
	
	
	
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

	public Map<ElectricitySpotMarket, Double> getExpectedDemand() {
		return expectedDemand;
	}

	public void setExpectedDemand(Map<ElectricitySpotMarket, Double> expectedDemand) {
		this.expectedDemand = expectedDemand;
	}

	public Map<Substance, Double> getFuelPrices() {
		return fuelPrices;
	}

	public void setFuelPrices(Map<Substance, Double> fuelPrices) {
		this.fuelPrices = fuelPrices;
	}

	public double getCO2price() {
		return co2price;
	}

	public void setCO2price(double co2price) {
		this.co2price = co2price;
	}

	public ElectricitySpotMarket getMarket() {
		return market;
	}

	public void setMarket(ElectricitySpotMarket market) {
		this.market = market;
	} 

}
