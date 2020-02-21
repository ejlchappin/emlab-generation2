package emlab.gen.domain.technology;

import emlab.gen.trend.TimeSeriesCSVReader;


public class IntermittentResourceProfile extends TimeSeriesCSVReader {

    PowerGeneratingTechnology intermittentTechnology;
    
	PowerGridNode intermittentProductionNode;

	public PowerGridNode getIntermittentProductionNode() {
		return intermittentProductionNode;
	}

	public void setIntermittentProductionNode(
	        PowerGridNode intermittentProductionNode) {
		this.intermittentProductionNode = intermittentProductionNode;
	}

	public PowerGeneratingTechnology getIntermittentTechnology() {
        return intermittentTechnology;
    }

    public void setIntermittentTechnology(PowerGeneratingTechnology intermittentTechnology) {
        this.intermittentTechnology = intermittentTechnology;
    }

}
