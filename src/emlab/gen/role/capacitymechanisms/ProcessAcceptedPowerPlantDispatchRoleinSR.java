package emlab.gen.role.capacitymechanisms;

import emlab.gen.domain.agent.StrategicReserveOperator;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentClearingPoint;
import emlab.gen.engine.AbstractRole;
import emlab.gen.engine.Role;
import emlab.gen.engine.Schedule;
import emlab.gen.repository.Reps;

/**
 * 
 * @author pbhagwat
 *
 */
public class ProcessAcceptedPowerPlantDispatchRoleinSR extends AbstractRole<StrategicReserveOperator> implements Role<StrategicReserveOperator>{

    public ProcessAcceptedPowerPlantDispatchRoleinSR(Schedule schedule) {
        super(schedule);
    }

    public void act(StrategicReserveOperator strategicReserveOperator) {

        Zone curZone = strategicReserveOperator.getZone();
        //logger.warn("Entering Zone loop");
        ElectricitySpotMarket market = getReps().findElectricitySpotMarketForZone(curZone);
        //logger.warn(market.getName());
        for (Segment segment : getReps().segments) {
            //logger.warn("Entering Segment Loop" + segment.getLengthInHours());
            SegmentClearingPoint scp = getReps().findOneSegmentClearingPointForMarketSegmentAndTime(
                    getCurrentTick(), segment, market, false);
            //logger.warn("Clearing Price " + scp.getPrice());
            for (PowerPlantDispatchPlan plan : getReps().findAllPowerPlantDispatchPlansForSegmentForTime(segment, getCurrentTick(), false)) {
                //logger.warn("Entering PPDP LOOP Successfully" +plan.getOldPrice());
                if (plan.getBiddingMarket().equals(market)){
                    //logger.warn("Bidding Market LOOP entered successfully " + plan.getBiddingMarket().getName());
                    if (plan.getStatus()>=2){
                        //logger.warn("Checking Accepted Bids finding accepted bids " +plan.getStatus());
                        if (plan.getSRstatus() <= -10){
                            //logger.warn("Checking SR Status Contracted " + plan.getSRstatus());
                            double moneyReturned = ((plan.getAcceptedAmount()*scp.getPrice()*segment.getLengthInHours())- ((plan.getAcceptedAmount()*plan.getOldPrice()*segment.getLengthInHours())));
                            // Price mark up /(plan.getPowerPlant().getOwner().getPriceMarkUp())
                            //logger.warn("Money Earned " +(plan.getAcceptedAmount()*scp.getPrice()*segment.getLengthInHours()));
                            //logger.warn("Money Kept "+ (plan.getAcceptedAmount()*plan.getOldPrice()*segment.getLengthInHours()));
                            //logger.warn("money Returned " +moneyReturned);

                            //logger.warn("SRO "+ strategicReserveOperator.getName() +" CASH Before" +strategicReserveOperator.getCash());
                            //logger.warn("Owner " + plan.getBidder().getName() + "money After" +plan.getBidder().getCash());

                            getReps().createCashFlow(plan.getBidder(), strategicReserveOperator, moneyReturned, CashFlow.STRRESPAYMENT, getCurrentTick(), plan.getPowerPlant());

                            //logger.warn("SRO's CASH After" +strategicReserveOperator.getCash());
                            //logger.warn("Owner " + plan.getBidder().getName() + " money After" +plan.getBidder().getCash());
                        }
                    }
                }

            }

        }
    }
}