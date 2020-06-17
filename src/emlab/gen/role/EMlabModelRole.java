/** *****************************************************************************
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************** */
package emlab.gen.role;

import cern.colt.Timer;
import emlab.gen.domain.agent.EMLabModel;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.engine.AbstractRole;
import emlab.gen.engine.Role;
import emlab.gen.engine.Schedule;
import emlab.gen.role.capacitymechanisms.ProcessAcceptedPowerPlantDispatchRoleinSR;
import emlab.gen.role.capacitymechanisms.StrategicReserveOperatorRole;
import emlab.gen.role.co2policy.MarketStabilityReserveRole;
import emlab.gen.role.co2policy.RenewableAdaptiveCO2CapRole;
import emlab.gen.role.investment.DismantlePowerPlantOperationalLossRole;
import emlab.gen.role.investment.DismantlePowerPlantPastTechnicalLifetimeRole;
import emlab.gen.role.investment.GenericInvestmentRole;
import emlab.gen.role.market.ClearCommodityMarketRole;
import emlab.gen.role.market.ClearIterativeCO2AndElectricitySpotMarketTwoCountryRole;
import emlab.gen.role.market.CreatingFinancialReports;
import emlab.gen.role.market.DetermineResidualLoadCurvesForTwoCountriesRole;
import emlab.gen.role.market.ProcessAcceptedBidsRole;
import emlab.gen.role.market.ProcessAcceptedPowerPlantDispatchRole;
import emlab.gen.role.market.ReassignPowerPlantsToLongTermElectricityContractsRole;
import emlab.gen.role.market.ReceiveLongTermContractPowerRevenuesRole;
import emlab.gen.role.market.SelectLongTermElectricityContractsRole;
import emlab.gen.role.market.SubmitBidsToCommodityMarketRole;
import emlab.gen.role.market.SubmitLongTermElectricityContractsRole;
import emlab.gen.role.market.SubmitOffersToCommodityMarketRole;
import emlab.gen.role.market.SubmitOffersToElectricitySpotMarketRole;
import emlab.gen.role.operating.DetermineFuelMixRole;
import emlab.gen.role.operating.PayCO2AuctionRole;
import emlab.gen.role.operating.PayCO2TaxRole;
import emlab.gen.role.operating.PayForLoansRole;
import emlab.gen.role.operating.PayOperatingAndMaintainanceCostsRole;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Main model role.
 *
 * @author alfredas, ejlchappin, jcrichstein
 *
 */
public class EMlabModelRole extends AbstractRole<EMLabModel> implements Role<EMLabModel> {

    private final PayCO2TaxRole payCO2TaxRole = new PayCO2TaxRole(schedule);
    private final PayCO2AuctionRole payCO2AuctionRole = new PayCO2AuctionRole(schedule);
    private final GenericInvestmentRole<EnergyProducer> genericInvestmentRole = new GenericInvestmentRole<EnergyProducer>(schedule);
    private final SubmitOffersToElectricitySpotMarketRole submitOffersToElectricitySpotMarketRole = new SubmitOffersToElectricitySpotMarketRole(schedule);
    private final ClearCommodityMarketRole clearCommodityMarketRole = new ClearCommodityMarketRole(schedule);
    private final SubmitBidsToCommodityMarketRole submitBidsToCommodityMarketRole = new SubmitBidsToCommodityMarketRole(schedule);
    private final SubmitOffersToCommodityMarketRole submitOffersToCommodityMarketRole = new SubmitOffersToCommodityMarketRole(schedule);
    private final SubmitLongTermElectricityContractsRole submitLongTermElectricityContractsRole = new SubmitLongTermElectricityContractsRole(schedule);
    private final SelectLongTermElectricityContractsRole selectLongTermElectricityContractsRole = new SelectLongTermElectricityContractsRole(schedule);
    //private final DismantlePowerPlantPastTechnicalLifetimeRole dismantlePowerPlantRole = new DismantlePowerPlantPastTechnicalLifetimeRole(schedule);
    private final DismantlePowerPlantOperationalLossRole dismantlePowerPlantRole = new DismantlePowerPlantOperationalLossRole(schedule);
    private final ReassignPowerPlantsToLongTermElectricityContractsRole reassignPowerPlantsToLongTermElectricityContractsRole = new ReassignPowerPlantsToLongTermElectricityContractsRole(schedule);
    private final ClearIterativeCO2AndElectricitySpotMarketTwoCountryRole clearIterativeCO2AndElectricitySpotMarketTwoCountryRole = new ClearIterativeCO2AndElectricitySpotMarketTwoCountryRole(schedule);
    private final DetermineFuelMixRole determineFuelMixRole = new DetermineFuelMixRole(schedule);
    private final ReceiveLongTermContractPowerRevenuesRole receiveLongTermContractPowerRevenuesRole = new ReceiveLongTermContractPowerRevenuesRole(schedule);
    private final ProcessAcceptedPowerPlantDispatchRole processAcceptedPowerPlantDispatchRole = new ProcessAcceptedPowerPlantDispatchRole(schedule);
    private final ProcessAcceptedBidsRole processAcceptedBidsRole = new ProcessAcceptedBidsRole(schedule);
    private final PayForLoansRole payForLoansRole = new PayForLoansRole(schedule);
    private final PayOperatingAndMaintainanceCostsRole payOperatingAndMaintainanceCostsRole = new PayOperatingAndMaintainanceCostsRole(schedule);
    private final StrategicReserveOperatorRole strategicReserveOperatorRole = new StrategicReserveOperatorRole(schedule);
    private final ProcessAcceptedPowerPlantDispatchRoleinSR acceptedPowerPlantDispatchRoleinSR = new ProcessAcceptedPowerPlantDispatchRoleinSR(schedule);
    private final RenewableAdaptiveCO2CapRole renewableAdaptiveCO2CapRole = new RenewableAdaptiveCO2CapRole(schedule);
    private final MarketStabilityReserveRole marketStabilityReserveRole = new MarketStabilityReserveRole(schedule);
    private final DetermineResidualLoadCurvesForTwoCountriesRole determineResidualLoadCurve = new DetermineResidualLoadCurvesForTwoCountriesRole(schedule);
    private final CreatingFinancialReports creatingFinancialReports = new CreatingFinancialReports(schedule);
    private final EmptyRoleBeginning emptyRoleBeginning = new EmptyRoleBeginning(schedule);

    public EMlabModelRole(Schedule schedule) {
        super(schedule);
    }

    /**
     * Main model script. Executes other roles in the right sequence.
     */
    @Override
    public void act(EMLabModel model) {

        /*
        * Use timer to track time spent in parts of this tick
         */
        Timer timer = new Timer();

        /*
         * Finish simulation
         */
        if (getCurrentTick() >= model.getSimulationLength()) {
            logger.log(Level.INFO, "Simulation is stopping");
            schedule.stop();
            if (getCurrentTick() > model.getSimulationLength() && model.isExitSimulationAfterSimulationLength()) {
                logger.log(Level.INFO, "Simulation is terminating");
                System.exit(0);
            }
        }

        if (model.isDeletionOldPPDPBidsAndCashFlowsEnabled()){// && (getCurrentTick() - model.getDeletionAge() >= 0)) {
            logger.log(Level.FINER, "  0. Delete old nodes in year {0}.", (getCurrentTick() - model.getDeletionAge()));
            getReps().removeBidsUpToTime(getCurrentTick() - 1);
            getReps().removeCashFlowsUpToTime(getCurrentTick() - 1);
            getReps().removePowerPlantsDismantledUpToTime(getCurrentTick() - 1);
            getReps().removeAllPowerPlantDispatchPlansUpToTime(getCurrentTick() - 1);
            getReps().removeAllPowerPlantDispatchPlansWithForecast(true);
            getReps().removeFinancialPowerPlantReportsUpToTime(getCurrentTick() - 6);
            getReps().marketInformationReports = new ArrayList<>();
            //logger.warning("Plans: " + getReps().powerPlantDispatchPlans.size());
        }
          
        logger.log(Level.INFO, "  0. Empty Role at the Beginning");
        emptyRoleBeginning.act(model);
        
        /*
         * Load duration curve (if renewable data is implemented)
         */
        logger.log(Level.INFO, "  0a. Determing load duration curves.");
        if (model.isRealRenewableDataImplemented()) {
            determineResidualLoadCurve.act(model);
        }

        /*
         * Dismantling & paying loans
         */
        logger.log(Level.INFO, "  0. Dismantling & paying loans");
        dismantlePowerPlantRole.act(getReps().findEnergyProducersAtRandom());
        payForLoansRole.act(getReps().findEnergyProducersAtRandom());


        /*
         * Determine fuel mix of power plants
         */
        logger.log(Level.INFO, "  1. Determining fuel mix");
        determineFuelMixRole.act(getReps().findEnergyProducersAtRandom());

        /*
         * Submit and select long-term electricity contracts
         */
        if (model.isLongTermContractsImplemented()) {
            logger.log(Level.INFO, "  2. Submit and select long-term electricity contracts");
            submitLongTermElectricityContractsRole.act(getReps().findEnergyProducersAtRandom());
            selectLongTermElectricityContractsRole.act(getReps().findEnergyConsumersAtRandom());
        }

        /*
         * Clear electricity spot and CO2 markets and determine also the commitment of powerplants.
         */
        logger.log(Level.INFO, "  3. Submitting offers to market");
        submitOffersToElectricitySpotMarketRole.act(getReps().findEnergyProducersAtRandom());

        /*
         * Contract strategic reserve volume and set strategic reserve dispatch
         * price
         */
        strategicReserveOperatorRole.act(getReps().strategicReserveOperators);

        Government government = getReps().government;
        if (getCurrentTick() > 0 && government.getCo2CapTrend() != null && government.isActivelyAdjustingTheCO2Cap()) {
            logger.log(Level.INFO, "3a. Lowering cap according to RES installations");
            renewableAdaptiveCO2CapRole.act(government);
        }

        if (getCurrentTick() >= model.getStabilityReserveFirstYearOfOperation() && model.isStabilityReserveIsActive()) {
            logger.log(Level.INFO, "3b. CO2 Market Stability Reserve");
            marketStabilityReserveRole.act(government);
        }

        /*
        * 
         */
        timer.reset();
        timer.start();
        logger.log(Level.INFO, "  4. Clearing electricity spot and CO2 markets");
        clearIterativeCO2AndElectricitySpotMarketTwoCountryRole.act(model);
        timer.stop();
        logger.log(Level.INFO, "        took: {0} seconds.", timer.seconds());


        // TODO, now we stop recalculating 
        getReps().powerPlants.forEach(p -> p.calculateElectricityOutputAtTime(getCurrentTick(), false));
        getReps().powerPlants.forEach(p -> p.flagOutputChanged = false);
        /*
        * Payment for long term contracts and strategic reserve, for maintenance and CO2
         */
        logger.log(Level.INFO, "  5. Paying for maintenance & co2");
        receiveLongTermContractPowerRevenuesRole.act(getReps().energyProducers);
        processAcceptedPowerPlantDispatchRole.act(getReps().electricitySpotMarkets);
        acceptedPowerPlantDispatchRoleinSR.act(getReps().strategicReserveOperators);
        payOperatingAndMaintainanceCostsRole.act(getReps().energyProducers);
        payCO2TaxRole.act(getReps().energyProducers);
        if (model.isCo2TradingImplemented()) {
            payCO2AuctionRole.act(getReps().energyProducers);
        }


        /*
         * COMMODITY MARKETS
         */
        logger.log(Level.INFO, "  6. Purchasing commodities");
        submitOffersToCommodityMarketRole.act(getReps().findCommoditySuppliersAtRandom());
        submitBidsToCommodityMarketRole.act(getReps().findEnergyProducersAtRandom());
        clearCommodityMarketRole.act(getReps().findCommodityMarketsAtRandom());
        processAcceptedBidsRole.act(getReps().findCommodityMarketsAtRandom());


        /*
        * Financial reports
         */
        logger.log(Level.INFO, "  6.b) Creating power plant financial reports.");
        creatingFinancialReports.act(model);

        /*
        * Investing
         */
        logger.log(Level.INFO, "  7. Investing");
        timer.reset();
        timer.start();

        logger.log(Level.INFO, "\t Private investment");
        if (getCurrentTick() > 1) {//TODO DISABLED
            boolean someOneStillWillingToInvest = true;
            while (someOneStillWillingToInvest) {
                someOneStillWillingToInvest = false;
                for (EnergyProducer producer : getReps().findAllEnergyProducersExceptForRenewableTargetInvestorsAtRandom()) {

                    // invest in new plants
                    if (producer.isWillingToInvest()) {
                        genericInvestmentRole.act(producer);
                        someOneStillWillingToInvest = true;
                    }
                }
            }
            //reset willingness to invest
            for (EnergyProducer producer : getReps().findEnergyProducersAtRandom()) {
                producer.setWillingToInvest(true);
            }
        }

        logger.log(Level.INFO, "\t subsidized investment.");
        genericInvestmentRole.act(getReps().targetInvestors);
        timer.stop();
        logger.log(Level.INFO, "        took: {0} seconds.", timer.seconds());

        if (model.isLongTermContractsImplemented()) {
            logger.log(Level.INFO, "  7. Reassign LTCs");
            reassignPowerPlantsToLongTermElectricityContractsRole.act(getReps().findEnergyProducersAtRandom());
        }

      

        
        /*
        * Perform consistency checks
         */
        getReps().powerPlants.stream().filter(p -> p.getTechnology().getFuels().size() > 0).filter(p -> p.getFuelMix() == null).forEach(p -> logger.severe("CHECK ERROR: No fuel mix for " + p));
        
        //reset flag for power plant.
        getReps().powerPlants.forEach(p -> p.flagOutputChanged = true);
    }

}
