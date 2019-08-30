/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emlab.gen.scenarios;

import emlab.gen.domain.agent.BigBank;
import emlab.gen.engine.Scenario;
import emlab.gen.domain.agent.EMLabModel;
import emlab.gen.domain.agent.EnergyConsumer;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.agent.NationalGovernment;
import emlab.gen.domain.factory.FuelFactory;
import emlab.gen.domain.factory.LDCFactory;
import emlab.gen.domain.factory.PowerPlantCSVFactory;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.technology.Interconnector;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.engine.Schedule;
import emlab.gen.repository.Reps;
import emlab.gen.role.investment.InvestInPowerGenerationTechnologiesRole;
import emlab.gen.trend.GeometricTrend;
import emlab.gen.trend.StepTrend;
import emlab.gen.trend.TimeSeriesCSVReader;
import emlab.gen.trend.TriangularTrend;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ejlchappin
 */
public class DefaultScenario implements Scenario {

    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void build(Schedule schedule) {
        
        Reps reps = schedule.reps;
        
        //Main simulation
        reps.emlabModel = new EMLabModel();
        reps.emlabModel.setExitSimulationAfterSimulationLength(true);
        reps.emlabModel.setSimulationLength(50);
        reps.emlabModel.setDeletionAge(5);
        reps.emlabModel.setDeletionOldPPDPBidsAndCashFlowsEnabled(true);//speed up simulation by deleting old objects
        reps.emlabModel.setCapDeviationCriterion(0.03);
        reps.emlabModel.setIterationSpeedCriterion(0.005);
        reps.emlabModel.setIterationSpeedFactor(3);
        reps.emlabModel.setRealRenewableDataImplemented(false);
        reps.emlabModel.setCo2TradingImplemented(true);
       
        reps.emlabModel.setName("EMLab Model");

        //Demand
        TriangularTrend demandGrowthTrendNL = new TriangularTrend();
        demandGrowthTrendNL.setTop(1.00);
        demandGrowthTrendNL.setMax(1.00);
        demandGrowthTrendNL.setMin(1.00);
        demandGrowthTrendNL.setStart(1.00);

        FuelFactory fuelFactory = new FuelFactory(reps);
        Substance coal = fuelFactory.createFuel("coal",2.784,29000,1,new TriangularTrend(1,1.04,.97,60));
        Substance naturalGas = fuelFactory.createFuel("natural.gas",0.00187,36,1,new TriangularTrend(1.01,1.06,.95,.32));
        
        Substance electricity = new Substance();
        electricity.setName("electricity");
        electricity.setCo2Density(0);
        electricity.setEnergyDensity(0);
        electricity.setQuality(1);
        reps.substances.add(electricity);

        Substance co2 = new Substance();
        co2.setName("CO2");
        co2.setCo2Density(1);
        co2.setEnergyDensity(0);
        co2.setQuality(1);
        reps.substances.add(co2);

        Zone nl = new Zone();
        nl.setName("CountryA");
        reps.zones.add(nl);

        PowerGridNode beneluxNode = new PowerGridNode();
        beneluxNode.setName("BeneluxNode");
        beneluxNode.setCapacityMultiplicationFactor(1.0);
        beneluxNode.setZone(nl);
        reps.powerGridNodes.add(beneluxNode);

        Interconnector interconnectorBeneluxGermany = new Interconnector();
        interconnectorBeneluxGermany.setCapacity(0);
        Set<PowerGridNode> connections = new HashSet<>();
        connections.add(beneluxNode);
        interconnectorBeneluxGermany.setConnections(connections);
        reps.interconnector = interconnectorBeneluxGermany;

        LDCFactory ldcFactory = new LDCFactory(reps);
        TimeSeriesCSVReader ldcReader = new TimeSeriesCSVReader();
        ldcReader.setFilename("/data/ldcTOY.csv");
        ldcReader.setDelimiter(",");       
        
        ldcReader.readCSVVariable("lengthInHours");
        ldcFactory.createSegments(ldcReader.getTimeSeries());
        
        ldcReader.readCSVVariable("load");
        Set<SegmentLoad> loadDurationCurveNL = ldcFactory.createLDC(ldcReader.getTimeSeries());
        
        ElectricitySpotMarket beneluxElectricitySpotMarket = reps.createElectricitySpotMarket("CountryAMarket", 2000, 40, false, electricity, demandGrowthTrendNL, loadDurationCurveNL, nl);
        
        reps.createCO2Auction("CO2Auction", 0, true, co2);
        
        EnergyConsumer energyConsumer = new EnergyConsumer();
        energyConsumer.setName("EnergyConsumer");
        energyConsumer.setContractWillingnessToPayFactor(1.2);
        energyConsumer.setContractDurationPreferenceFactor(.03);
        energyConsumer.setLtcMaximumCoverageFraction(0.8);
        reps.energyConsumers.add(energyConsumer);
        
        InvestInPowerGenerationTechnologiesRole defaultInvestmentRole = new InvestInPowerGenerationTechnologiesRole(schedule);

        EnergyProducer energyProducerA = reps.createEnergyProducer();
        energyProducerA.setName("Energy Producer A");
        energyProducerA.setInvestorMarket(beneluxElectricitySpotMarket);
        energyProducerA.setNumberOfYearsBacklookingForForecasting(5);
        energyProducerA.setPriceMarkUp(1.0);
        energyProducerA.setWillingToInvest(true);
        energyProducerA.setDownpaymentFractionOfCash(.5);
        energyProducerA.setDismantlingRequiredOperatingProfit(0);
        energyProducerA.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
        energyProducerA.setDebtRatioOfInvestments(0.7);
        energyProducerA.setLoanInterestRate(0.1);
        energyProducerA.setEquityInterestRate(0.1);
        energyProducerA.setPastTimeHorizon(5);
        energyProducerA.setInvestmentFutureTimeHorizon(7);
        energyProducerA.setLongTermContractPastTimeHorizon(3);
        energyProducerA.setLongTermContractMargin(0.1);
        energyProducerA.setCash(3e9);
        energyProducerA.setInvestmentRole(defaultInvestmentRole);

        EnergyProducer energyProducerB = reps.createEnergyProducer();
        energyProducerB.setName("Energy Producer B");
        energyProducerB.setInvestorMarket(beneluxElectricitySpotMarket);
        energyProducerB.setNumberOfYearsBacklookingForForecasting(5);
        energyProducerB.setPriceMarkUp(1.0);
        energyProducerB.setWillingToInvest(true);
        energyProducerB.setDownpaymentFractionOfCash(.5);
        energyProducerB.setDismantlingRequiredOperatingProfit(0);
        energyProducerB.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
        energyProducerB.setDebtRatioOfInvestments(0.7);
        energyProducerB.setLoanInterestRate(0.1);
        energyProducerB.setEquityInterestRate(0.1);
        energyProducerB.setPastTimeHorizon(5);
        energyProducerB.setInvestmentFutureTimeHorizon(7);
        energyProducerB.setLongTermContractPastTimeHorizon(3);
        energyProducerB.setLongTermContractMargin(0.1);
        energyProducerB.setCash(3e9);
        energyProducerB.setInvestmentRole(defaultInvestmentRole);       
        
        reps.bigBank = new BigBank();

        StepTrend co2TaxTrend = new StepTrend();
        co2TaxTrend.setDuration(1);
        co2TaxTrend.setStart(5);
        co2TaxTrend.setMinValue(5);
        co2TaxTrend.setIncrement(0);

        StepTrend co2CapTrend = new StepTrend();
        co2CapTrend.setDuration(1);
        co2CapTrend.setStart(10e9);
        co2CapTrend.setMinValue(0);
        co2CapTrend.setIncrement(0);

        StepTrend minCo2PriceTrend = new StepTrend();
        minCo2PriceTrend.setDuration(1);
        minCo2PriceTrend.setStart(0);
        minCo2PriceTrend.setMinValue(0);
        minCo2PriceTrend.setIncrement(0);

        reps.government = new Government();
        reps.government.setName("EuropeanGov");
        reps.government.setCo2Penalty(500);
        reps.government.setCo2TaxTrend(co2TaxTrend);
        reps.government.setCo2CapTrend(co2CapTrend);
        reps.government.setMinCo2PriceTrend(minCo2PriceTrend);

        StepTrend minCo2PriceTrendNL = new StepTrend();
        minCo2PriceTrendNL.setDuration(1);
        minCo2PriceTrendNL.setStart(0);
        minCo2PriceTrendNL.setMinValue(0);
        minCo2PriceTrendNL.setIncrement(0);

        NationalGovernment governmentNL = reps.createNationalGovernment("CountryAGov", nl, minCo2PriceTrendNL);

        GeometricTrend coalPulverizedInvestmentCostTimeSeries = new GeometricTrend();
        coalPulverizedInvestmentCostTimeSeries.setStart(1434500);

        GeometricTrend coalPulverizedFixedOperatingCostTimeSeries = new GeometricTrend();
        coalPulverizedFixedOperatingCostTimeSeries.setStart(56770);

        GeometricTrend coalPulverizedEfficiencyTimeSeries = new GeometricTrend();
        coalPulverizedEfficiencyTimeSeries.setStart(.44);

        PowerGeneratingTechnology coalPulverizedSuperCritical = reps.createPowerGeneratingTechnology();
        coalPulverizedSuperCritical.setName("CoalPulverizedSuperCritical");
        coalPulverizedSuperCritical.setCapacity(500);
        coalPulverizedSuperCritical.setIntermittent(false);
        coalPulverizedSuperCritical.setApplicableForLongTermContract(true);
        coalPulverizedSuperCritical.setPeakSegmentDependentAvailability(1);
        coalPulverizedSuperCritical.setBaseSegmentDependentAvailability(1);
        coalPulverizedSuperCritical.setMaximumInstalledCapacityFractionPerAgent(1);
        coalPulverizedSuperCritical.setMaximumInstalledCapacityFractionInCountry(1);
        coalPulverizedSuperCritical.setMinimumFuelQuality(.9);
        coalPulverizedSuperCritical.setExpectedPermittime(1);
        coalPulverizedSuperCritical.setExpectedLeadtime(4);
        coalPulverizedSuperCritical.setExpectedLifetime(40);
        coalPulverizedSuperCritical.setFixedOperatingCostModifierAfterLifetime(.05);
        coalPulverizedSuperCritical.setMinimumRunningHours(5000);
        coalPulverizedSuperCritical.setDepreciationTime(20);
        coalPulverizedSuperCritical.setEfficiencyTimeSeries(coalPulverizedEfficiencyTimeSeries);
        coalPulverizedSuperCritical.setFixedOperatingCostTimeSeries(coalPulverizedFixedOperatingCostTimeSeries);
        coalPulverizedSuperCritical.setInvestmentCostTimeSeries(coalPulverizedInvestmentCostTimeSeries);
        Set<Substance> coalPulverizedSuperCriticalFuels = new HashSet<>();
        coalPulverizedSuperCriticalFuels.add(coal);
        coalPulverizedSuperCritical.setFuels(coalPulverizedSuperCriticalFuels);

        GeometricTrend ccgtInvestmentCostTimeSeries = new GeometricTrend();
        ccgtInvestmentCostTimeSeries.setStart(679500);

        GeometricTrend ccgtFixedOperatingCostTimeSeries = new GeometricTrend();
        ccgtFixedOperatingCostTimeSeries.setStart(22380);

        GeometricTrend ccgtEfficiencyTimeSeries = new GeometricTrend();
        ccgtEfficiencyTimeSeries.setStart(.56);

        PowerGeneratingTechnology ccgt = reps.createPowerGeneratingTechnology();
        ccgt.setName("CCGT");
        ccgt.setCapacity(500);
        ccgt.setIntermittent(false);
        ccgt.setApplicableForLongTermContract(true);
        ccgt.setPeakSegmentDependentAvailability(1);
        ccgt.setBaseSegmentDependentAvailability(1);
        ccgt.setMaximumInstalledCapacityFractionPerAgent(1);
        ccgt.setMaximumInstalledCapacityFractionInCountry(1);
        ccgt.setMinimumFuelQuality(1);
        ccgt.setExpectedPermittime(1);
        ccgt.setExpectedLeadtime(2);
        ccgt.setExpectedLifetime(30);
        ccgt.setFixedOperatingCostModifierAfterLifetime(.05);
        ccgt.setMinimumRunningHours(0);
        ccgt.setDepreciationTime(15);
        ccgt.setEfficiencyTimeSeries(ccgtEfficiencyTimeSeries);
        ccgt.setFixedOperatingCostTimeSeries(ccgtFixedOperatingCostTimeSeries);
        ccgt.setInvestmentCostTimeSeries(ccgtInvestmentCostTimeSeries);
        Set<Substance> ccgtFuels = new HashSet<>();
        ccgtFuels.add(naturalGas);
        ccgt.setFuels(ccgtFuels);

        GeometricTrend ocgtInvestmentCostTimeSeries = new GeometricTrend();
        ocgtInvestmentCostTimeSeries.setStart(251666.66);

        GeometricTrend ocgtFixedOperatingCostTimeSeries = new GeometricTrend();
        ocgtFixedOperatingCostTimeSeries.setStart(67110);

        GeometricTrend ocgtEfficiencyTimeSeries = new GeometricTrend();
        ocgtEfficiencyTimeSeries.setStart(.38);

        PowerGeneratingTechnology ocgt = reps.createPowerGeneratingTechnology();
        ocgt.setName("OCGT");
        ocgt.setCapacity(500);
        ocgt.setIntermittent(false);
        ocgt.setApplicableForLongTermContract(true);
        ocgt.setPeakSegmentDependentAvailability(1);
        ocgt.setBaseSegmentDependentAvailability(1);
        ocgt.setMaximumInstalledCapacityFractionPerAgent(1);
        ocgt.setMaximumInstalledCapacityFractionInCountry(1);
        ocgt.setMinimumFuelQuality(1);
        ocgt.setExpectedPermittime(1);
        ocgt.setExpectedLeadtime(1);
        ocgt.setExpectedLifetime(30);
        ocgt.setFixedOperatingCostModifierAfterLifetime(.05);
        ocgt.setMinimumRunningHours(0);
        ocgt.setDepreciationTime(15);
        ocgt.setEfficiencyTimeSeries(ocgtEfficiencyTimeSeries);
        ocgt.setFixedOperatingCostTimeSeries(ocgtFixedOperatingCostTimeSeries);
        ocgt.setInvestmentCostTimeSeries(ocgtInvestmentCostTimeSeries);
        Set<Substance> ocgtFuels = new HashSet<>();
        ocgtFuels.add(naturalGas);
        ocgt.setFuels(ocgtFuels);

        PowerPlantCSVFactory powerPlantCSVFactory = new PowerPlantCSVFactory(reps);
        powerPlantCSVFactory.setCsvFile("/data/toyModelPowerPlants.csv");
        for (PowerPlant plant : powerPlantCSVFactory.read()) {
            reps.createPowerPlantFromPlant(plant);
        }
    }
}
