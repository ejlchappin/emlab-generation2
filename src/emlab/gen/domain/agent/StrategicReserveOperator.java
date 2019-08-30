package emlab.gen.domain.agent;

import emlab.gen.domain.gis.Zone;

/**
 * 
 * @author pbhagwat
 * 
 */

public class StrategicReserveOperator extends EMLabAgent {

	public double reserveVolume;

//	@RelatedTo(type = "SROPERATOR_ZONE", elementClass = Zone.class, direction = Direction.OUTGOING)
	private Zone zone;

//	@SimulationParameter(label = "Dispatch price of strategic reserve capacity ", from = 0, to = 20000)
	private double reservePriceSR;

	// @SimulationParameter(label =
	// "Price Mark-Up for strategic reserve capacity (as multiplier)", from = 1,
	// to = 2)
	// private double reservePriceMarkUp;

//	@SimulationParameter(label = "percentage of demand as strategic reserve", from = 0, to = 1)
	private double reserveVolumePercentSR;

	public double getReserveVolume() {
		return reserveVolume;
	}

	public void setReserveVolume(double reserveVolume) {
		this.reserveVolume = reserveVolume;
	}

	public Zone getZone() {
		return zone;
	}

	public void setZone(Zone zone) {
		this.zone = zone;
	}

	public double getReservePriceSR() {
		return reservePriceSR;
	}

	public void setReservePriceSR(double reservePriceSR) {
		this.reservePriceSR = reservePriceSR;
	}

	public double getReserveVolumePercentSR() {
		return reserveVolumePercentSR;
	}

	public void setReserveVolumePercentSR(double reserveVolumePercentSR) {
		this.reserveVolumePercentSR = reserveVolumePercentSR;
	}

}
