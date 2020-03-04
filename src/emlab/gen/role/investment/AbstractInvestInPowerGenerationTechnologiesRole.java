/** *****************************************************************************
 * Copyright 2013 the original author or authors.
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
package emlab.gen.role.investment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import emlab.gen.domain.agent.BigBank;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.PowerPlantManufacturer;
import emlab.gen.domain.agent.StrategicReserveOperator;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.contract.Loan;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.policy.PowerGeneratingTechnologyTarget;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGeneratingTechnologyNodeLimit;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.domain.technology.SubstanceShareInFuelMix;
import emlab.gen.engine.Schedule;
import emlab.gen.util.GeometricTrendRegression;
import emlab.gen.util.MapValueComparator;

/**
 * @author JCRichstein
 *
 */
public abstract class AbstractInvestInPowerGenerationTechnologiesRole<T extends EnergyProducer> extends GenericInvestmentRole<T> {
	
    private boolean useFundamentalCO2Forecast = false;
    
	protected long futureTimePoint;
    private Map<ElectricitySpotMarket, Double> expectedCO2Price;
    private Map<Substance, Double> expectedFuelPrices;
    private Map<ElectricitySpotMarket, Double> expectedDemand;
    
    private ElectricitySpotMarket market;
    private MarketInformation marketInformation;
    protected EnergyProducer agent;
    	
	public AbstractInvestInPowerGenerationTechnologiesRole(Schedule schedule) {
        super(schedule);
    }
	
	/**
	 * Sets time horizon and expectations (CO, demand, fuel) to evaluate an investment for an agent
	 * @param agent
	 */
	public void initEvaluationForEnergyProducer(EnergyProducer agent, ElectricitySpotMarket market) {
			
		setAgent(agent);
        setMarket(market);

    	setTimeHorizon();
    	setExpectations();
    		
	}

    
    private void setTimeHorizon() {

        
        logger.log(Level.INFO, 
        		agent + " is considering investment with horizon " + agent.getInvestmentFutureTimeHorizon());
        
        futureTimePoint = getCurrentTick() + agent.getInvestmentFutureTimeHorizon();
        
    }
    
    public void setExpectations() {
        
        // Expectations
        expectedFuelPrices = predictFuelPrices(agent, futureTimePoint);
        
        
        if(!useFundamentalCO2Forecast) {
        	expectedCO2Price = determineExpectedCO2PriceInclTaxAndFundamentalForecast(
                    futureTimePoint, agent.getNumberOfYearsBacklookingForForecasting(), 0, getCurrentTick());
        } else {
	        expectedCO2Price = determineExpectedCO2PriceInclTax(futureTimePoint,
	                agent.getNumberOfYearsBacklookingForForecasting(), getCurrentTick());
        }

        
        expectedDemand = new HashMap<ElectricitySpotMarket, Double>();
        for (ElectricitySpotMarket elm : getReps().electricitySpotMarkets) {
            GeometricTrendRegression gtr = new GeometricTrendRegression();
            for (long time = getCurrentTick(); time > getCurrentTick() - agent.getNumberOfYearsBacklookingForForecasting() && time >= 0; time = time - 1) {
                gtr.addData(time, elm.getDemandGrowthTrend().getValue(time));
            }
            expectedDemand.put(elm, gtr.predict(futureTimePoint));
        }

        marketInformation = new MarketInformation(market, expectedDemand, expectedFuelPrices, expectedCO2Price.get(market)
                .doubleValue(), futureTimePoint);
        
        /*
         * if (marketInfoMap.containsKey(market) && marketInfoMap.get(market).time == futureTimePoint) { marketInformation = marketInfoMap.get(market); } else { marketInformation = new
         * MarketInformation(market, expectedFuelPrices, expectedCO2Price, futureTimePoint); marketInfoMap.put(market, marketInformation); }
         */

        // logger.warn(agent + " is expecting a CO2 price of " +
        // expectedCO2Price.get(market) + " Euro/MWh at timepoint "
        // + futureTimePoint + " in Market " + market);
        // logger.warn("Agent {}  found the expected prices to be {}", agent,
        // marketInformation.expectedElectricityPricesPerSegment);
        // logger.warn("Agent {}  found that the installed capacity in the market {} in future to be "
        // + marketInformation.capacitySum +
        // "and expectde maximum demand to be "
        // + marketInformation.maxExpectedLoad, agent, market);

        
    }
   


	/**
     * Finds a possible installation Node. For dispatchable technologies just choose a random node. 
     * For intermittent technologies, returns all PowerGridNode.
     * 
     * @param technology
     * @return
     */
    public Iterable<PowerGridNode> findPossibleInstallationNodes(PowerGeneratingTechnology technology){
    	
        Iterable<PowerGridNode> possibleInstallationNodes;
        // double nodeLimitNonIntermittentTechnology = 0d;
        
        logger.log(Level.FINER, "technology considered is" + technology.getName());
        
        if (technology.isIntermittent()) {
            possibleInstallationNodes = getReps().findAllPowerGridNodesByZone(market.getZone());
        } else {
            possibleInstallationNodes = new LinkedList<PowerGridNode>();
            ((LinkedList<PowerGridNode>) possibleInstallationNodes).add(getReps()
                    .findAllPowerGridNodesByZone(market.getZone()).iterator().next());
        
			//LinkedList<Double> PGNodeLimitlist = new LinkedList<Double>();
			
            for (PowerGridNode nodeItem : getReps().findAllPowerGridNodesByZone(market.getZone())) {
			
			logger.log(Level.FINE, "For PG Node limits: node is " + nodeItem.getName() + "technology is "
			 + technology.getName());
			 
			// PGNodeLimitlist.add(getReps().findPowerGeneratingTechnologyNodeLimitByNodeAndTechnology(nodeItem,
			// technology));
			// nodeLimitNonIntermittentTechnology =
			// Collections.max(PGNodeLimitlist);
			 
			}

        }

        logger.log(Level.FINER, "Calculating for " + technology.getName() +
         ", for Nodes: "
         + possibleInstallationNodes.toString());   
        
        return possibleInstallationNodes;
    	
    }
    
    

    /**
     * Creates a new power plant with a fuel mix in Zone of the market. Does not add the plant to the repository.
     * 
     * @param technology
     * @return PowerPlant
     */
    public PowerPlant createPowerPlant(PowerGeneratingTechnology technology) {        
        return createPowerPlant(technology, getNodeForZone(market.getZone()));
    }
    
    /**
     * Creates a new power plant with a fuel mix in node. Does not add the plant to the repository.
     * 
     * @param technology
     * @param zone
     * @return PowerPlant
     */
    public PowerPlant createPowerPlant(PowerGeneratingTechnology technology, PowerGridNode node) {
    	
        PowerPlant plant = getReps().createAndSpecifyTemporaryPowerPlant(getCurrentTick(), agent, node, technology);
    	setFuelMixForPlant(technology, plant);
    	return plant;


    }
    
   public void setFuelMixForPlant(PowerGeneratingTechnology technology, PowerPlant plant) {
		
        Map<Substance, Double> myFuelPrices = new HashMap<Substance, Double>();
        for (Substance fuel : technology.getFuels()) {
            myFuelPrices.put(fuel, expectedFuelPrices.get(fuel));
        }
        // TODO: all investment: change to an empty fuel mix default.
        // TODO: check if this works with tender stuff
        Set<SubstanceShareInFuelMix> fuelMix = new HashSet<SubstanceShareInFuelMix>();
        if (myFuelPrices.size() > 0) {
            fuelMix = calculateFuelMix(plant, myFuelPrices, expectedCO2Price.get(market));
        }
        plant.setFuelMix(fuelMix);
    	
	}
	
	
	private void invest(PowerPlant plant) {
        
		logger.log(Level.FINE, "{0} invests in technology {1} at tick {2}", new Object[]{agent, plant.getTechnology(), getCurrentTick()});

        getReps().createPowerPlantFromPlant(plant);
        
        //TODO recalculate fuelmix in other investment roles!

        Map<Substance, Double> myFuelPrices = new HashMap<Substance, Double>();
        for (Substance fuel : plant.getTechnology().getFuels()) {
            myFuelPrices.put(fuel, expectedFuelPrices.get(fuel));
        }
        plant.setFuelMix(calculateFuelMix(plant, myFuelPrices, expectedCO2Price.get(market)));
        
        PowerPlantManufacturer manufacturer = getReps().powerPlantManufacturer;
        BigBank bigbank = getReps().bigBank;

        double investmentCostPayedByEquity = plant.getActualInvestedCapital() * (1 - agent.getDebtRatioOfInvestments());
        double investmentCostPayedByDebt = plant.getActualInvestedCapital() * agent.getDebtRatioOfInvestments();
        double downPayment = investmentCostPayedByEquity;
        createSpreadOutDownPayments(agent, manufacturer, downPayment, plant);

        double amount = determineLoanAnnuities(investmentCostPayedByDebt, plant.getTechnology().getDepreciationTime(),
                agent.getLoanInterestRate());
        logger.log(Level.FINE, "Loan amount is: " + amount);
        Loan loan = getReps().createLoan(agent, bigbank, amount, plant.getTechnology().getDepreciationTime(),
                getCurrentTick(), plant);
        plant.createOrUpdateLoan(loan);
	}
	
	
	private void dontInvest() {
		
		logger.log(Level.FINE, agent + " found no suitable technology anymore to invest in at tick " + getCurrentTick());
        
		// agent will not participate in the next round of investment if he does not invest now
        setNotWillingToInvest(agent);
		
	}
	
	public void decideToInvestInPlant(PowerPlant plant) {
        
		if (plant != null) {
        	invest(plant);
        } else {
        	dontInvest();
        }
		
        logger.log(Level.INFO, "Investment done for " + agent);
		
	}
	
    
    
    public void setNotWillingToInvest(EnergyProducer agent) {
	      agent.setWillingToInvest(false);
	
	
	  }

	public double determineExpectedMarginalCost(PowerPlant plant) {
        double mc = determineExpectedMarginalFuelCost(plant, expectedFuelPrices);
        double co2Intensity = plant.calculateEmissionIntensity();
        mc += co2Intensity * expectedCO2Price.get(market);
        
        logger.log(Level.FINE, "expected marginal cost of plant" + plant + " is " + mc);
        return mc;

    }

    
    

    // Create a powerplant investment and operation cash-flow in the form of a
	  // map. If only investment, or operation costs should be considered set
	  // totalInvestment or operatingProfit to 0
	  public TreeMap<Integer, Double> calculateSimplePowerPlantInvestmentCashFlow(int depriacationTime, int buildingTime,
	          double totalInvestment, double operatingProfit) {
	      TreeMap<Integer, Double> investmentCashFlow = new TreeMap<Integer, Double>();
	      double equalTotalDownPaymentInstallement = totalInvestment / buildingTime;
	      for (int i = 0; i < buildingTime; i++) {
	          investmentCashFlow.put(new Integer(i), -equalTotalDownPaymentInstallement);
	      }
	      for (int i = buildingTime; i < depriacationTime + buildingTime; i++) {
	          investmentCashFlow.put(new Integer(i), operatingProfit);
	      }
	
	      return investmentCashFlow;
	  }




	public double npv(TreeMap<Integer, Double> netCashFlow, double wacc) {
	      double npv = 0;
	      for (Integer iterator : netCashFlow.keySet()) {
	          npv += netCashFlow.get(iterator).doubleValue() / Math.pow(1 + wacc, iterator.intValue());
	      }
	      return npv;
	  }




	public double determineExpectedMarginalFuelCost(PowerPlant powerPlant, Map<Substance, Double> expectedFuelPrices) {
	      double fc = 0d;
	      logger.info("Fuel mix of plant: " + powerPlant + " of owner " + powerPlant.getOwner() + " is " + powerPlant.getFuelMix());
	      for (SubstanceShareInFuelMix mix : powerPlant.getFuelMix()) {
	          double amount = mix.getShare();
	          logger.info("amount of fuel: " + amount);
	          logger.info("fuel prices: " + expectedFuelPrices.size());
	          double fuelPrice = expectedFuelPrices.get(mix.getSubstance());
	          fc += amount * fuelPrice;
	      }
	      return fc;
	  }




	public PowerGridNode getNodeForZone(Zone zone) {
	      for (PowerGridNode node : getReps().powerGridNodes) {
	          if (node.getZone().equals(zone)) {
	              return node;
	          }
	      }
	      return null;
	  }




	// Creates n downpayments of equal size in each of the n building years of a
	  // power plant
	  public void createSpreadOutDownPayments(EnergyProducer agent, PowerPlantManufacturer manufacturer, double totalDownPayment,
	          PowerPlant plant) {
	      int buildingTime = (int) plant.getActualLeadtime();
	      getReps().createCashFlow(agent, manufacturer, totalDownPayment / buildingTime,
	              CashFlow.DOWNPAYMENT, getCurrentTick(), plant);
	      Loan downpayment = getReps().createLoan(agent, manufacturer, totalDownPayment / buildingTime,
	              buildingTime - 1, getCurrentTick(), plant);
	      plant.createOrUpdateDownPayment(downpayment);
	  }


	  // Setters and Getters
	
	 public EnergyProducer getAgent() {
		return agent;
	}

	public void setAgent(EnergyProducer agent) {
		this.agent = agent;
	}

	/**
	 * Returns the tick for which the investor currently evaluates the technology
	 * @return long time point
	 */
	public long getFutureTimePoint() {
		return futureTimePoint;
	}

	public void setFutureTimePoint(long futureTimePoint) {
		this.futureTimePoint = futureTimePoint;
	}

	public boolean isUseFundamentalCO2Forecast() {
		return useFundamentalCO2Forecast;
	}

	public void setUseFundamentalCO2Forecast(boolean useFundamentalCO2Forecast) {
		this.useFundamentalCO2Forecast = useFundamentalCO2Forecast;
	}

	public ElectricitySpotMarket getMarket() {
		return market;
	}

	public void setMarket(ElectricitySpotMarket market) {
		this.market = market;
	}

	public MarketInformation getMarketInformation() {
		return marketInformation;
	}

	public void setMarketInformation(MarketInformation marketInformation) {
		this.marketInformation = marketInformation;
	}




	/**
     * Checks if any "hard limits" inhibits the investor from investing.
     * @return
     */
    class FutureCapacityExpectation{
    	
    	double expectedInstalledCapacityOfTechnology;
    	double expectedInstalledCapacityOfTechnologyInNode;
        double expectedOwnedTotalCapacityInMarket;
        double expectedOwnedCapacityInMarketOfThisTechnology;
        double capacityOfTechnologyInPipeline;
        double operationalCapacityOfTechnology;
        double capacityInPipelineInMarket;
        
        boolean viableInvestment = false;
        
        PowerGeneratingTechnology technology;
        PowerPlant plant; 
        PowerGridNode node;
        
        protected double pgtNodeLimit = Double.MAX_VALUE;
        
        public FutureCapacityExpectation(PowerGeneratingTechnology technology, PowerPlant plant){
        
            this(technology, plant, plant.getLocation());

        }

        
        public FutureCapacityExpectation(PowerGeneratingTechnology technology, PowerPlant plant, PowerGridNode node){
        	
        	this.technology = technology;
        	this.plant = plant;
        	this.node = node;
        	
        	calculateNodeLimit();
        	
            expectedInstalledCapacityOfTechnology = getReps().calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(market, technology, futureTimePoint);
            
            PowerGeneratingTechnologyTarget technologyTarget = getReps().findPowerGeneratingTechnologyTargetByTechnologyAndMarket(technology, market);
            if (technologyTarget != null) {
                double technologyTargetCapacity = technologyTarget.getTrend().getValue(futureTimePoint);
                expectedInstalledCapacityOfTechnology = (technologyTargetCapacity > expectedInstalledCapacityOfTechnology) ? technologyTargetCapacity : expectedInstalledCapacityOfTechnology;
            }
                 
            
            expectedInstalledCapacityOfTechnologyInNode = getReps()
            		.calculateCapacityOfExpectedOperationalPowerPlantsByNodeAndTechnology(plant.getLocation(),technology, futureTimePoint);
            
            expectedOwnedTotalCapacityInMarket = getReps()
            		.calculateCapacityOfExpectedOperationalPowerPlantsInMarketByOwner(market, futureTimePoint, agent);
            
            expectedOwnedCapacityInMarketOfThisTechnology = getReps()
                    .calculateCapacityOfExpectedOperationalPowerPlantsInMarketByOwnerAndTechnology(market, technology, futureTimePoint, agent);
            
            capacityOfTechnologyInPipeline = getReps()
            		.calculateCapacityOfPowerPlantsByTechnologyInPipeline(technology, getCurrentTick());
            
            operationalCapacityOfTechnology = getReps()
            		.calculateCapacityOfOperationalPowerPlantsByTechnology(technology, getCurrentTick());
            
            capacityInPipelineInMarket = getReps()
                    .calculateCapacityOfPowerPlantsByMarketInPipeline(market, getCurrentTick());
            
            
            check();
      	
        }
        
        
        
        /**
         * Returns the Node Limit by Technology or the max double numer if none was found
         * 
         * @param market
         * @param technology
         * @param plant
         * @param futureTimePoint
         * @return
         */
        public void calculateNodeLimit() {
        	        	
        	PowerGeneratingTechnologyNodeLimit pgtLimit = getReps().findOneByTechnologyAndNode(technology, node);
        	if (pgtLimit != null) {
        		pgtNodeLimit = pgtLimit.getUpperCapacityLimit(futureTimePoint);
        	}
        	

        }
        
        
        /**
         * Checks if future capacity expansion is viable.
         */
        public void check() {
        	
            if ((expectedInstalledCapacityOfTechnology + plant.getActualNominalCapacity())
                    / (marketInformation.maxExpectedLoad + plant.getActualNominalCapacity()) > technology
                    .getMaximumInstalledCapacityFractionInCountry()) {
            	
                logger.log(Level.FINE, 
                		agent + " will not invest in {} technology because there's too much of this type in the market", technology);
            
            } else if ((expectedInstalledCapacityOfTechnologyInNode + plant.getActualNominalCapacity()) > pgtNodeLimit) {
            	 
            	 

            } else if (expectedOwnedCapacityInMarketOfThisTechnology > expectedOwnedTotalCapacityInMarket
                    * technology.getMaximumInstalledCapacityFractionPerAgent()) {
                 
            	logger.log(Level.FINE, 
                		 agent + " will not invest in {} technology because there's too much capacity planned by him", technology);
            
            } else if (capacityInPipelineInMarket > 0.2 * marketInformation.maxExpectedLoad) {
            	logger.log(Level.FINE, "Not investing because more than 20% of demand in pipeline.");

            
            } else if ((capacityOfTechnologyInPipeline > 2.0 * operationalCapacityOfTechnology)
                    && capacityOfTechnologyInPipeline > 9000) { // TODO: reflects that you cannot expand a technology out of zero.
            	logger.log(Level.FINE, agent +" will not invest in {} technology because there's too much capacity in the pipeline", technology);
            
            } else if (plant.getActualInvestedCapital() * (1 - agent.getDebtRatioOfInvestments()) > agent
                    .getDownpaymentFractionOfCash() * agent.getCash()) {
            	logger.log(Level.FINE, agent +" will not invest in {} technology as he does not have enough money for downpayment", technology);
            
            } else {
            	
            	logger.log(Level.FINE,  technology + " passes capacity limit. " + agent + " will now calculate financial viability.");
            	setViableInvestment(true);
            	
	
            }      
            
        
       }

        /**
         * Return true if the checks in this class have all been passed.
         * This means that future capacity expansion is viable.
         * @return
         */
        public boolean isViableInvestment() {
			return viableInvestment;
		}


		public void setViableInvestment(boolean viableInvestment) {
			this.viableInvestment = viableInvestment;
		}

        
    }
    
    

    
    public class FutureFinancialExpectation{
    	
    	protected PowerPlant plant;
    	protected PowerGridNode node;
    	protected PowerGeneratingTechnology technology;
    	
    	protected double expectedMarginalCost;
    	protected double runningHours  = 0d;
    	protected double expectedGeneration  = 0d;
    	protected double expectedGrossProfit = 0d;
    	protected double expectedAnnualVariableCost = 0d;
    	protected double expectedAnnualVariableRevenue = 0d;

    	protected double fixedOMCost;
    	protected double wacc;
		
    	protected double discountedCapitalCosts;
    	protected double discountedOperatingCost;
    	protected double discountedOperatingProfit;
    	protected double projectValue;
    	protected double projectCost;

    	
    	public FutureFinancialExpectation(PowerPlant plant){
    		
    		this.plant = plant;
    		this.technology = plant.getTechnology();
    		this.node = plant.getLocation();   	
        	
    		expectedMarginalCost = determineExpectedMarginalCost(plant);
        	
            fixedOMCost = calculateFixedOperatingCost(plant, getCurrentTick());
            
            // TODO Alter discount rate on the basis of the amount
            // in long-term contracts?
            // TODO Alter discount rate on the basis of other stuff,
            // such as amount of money, market share, portfolio
            // size.
            // Calculation of weighted average cost of capital,
            // based on the companies debt-ratio
            wacc = (1 - agent.getDebtRatioOfInvestments()) * agent.getEquityInterestRate()
                    + agent.getDebtRatioOfInvestments() * agent.getLoanInterestRate();
                        
            calculateFinancialIndicatorsForAllSegments();

            
    	}
    	
    	/**
    	 * Calculates based on expected electricity prices and the plants running hours and capacity
    	 * the expected gross profit, variable costs, variable revenue, running hours and expected generation
    	 * @return void  
    	 */
    	private void calculateFinancialIndicatorsForAllSegments() {
    		
            long numberOfSegments = getReps().segments.size();

            for (SegmentLoad segmentLoad : getMarket().getLoadDurationCurve()) {
                double expectedElectricityPrice = getMarketInformation().expectedElectricityPricesPerSegment.get(segmentLoad
                        .getSegment());
                
            	double hours = segmentLoad.getSegment().getLengthInHours();
            	double capacity = plant.getAvailableCapacity(getFutureTimePoint(), segmentLoad.getSegment(), numberOfSegments); // this considers intermittency 
                double generationInSegment =  hours * capacity;
                
                calculateFinancialIndicatorsForOneSegment(expectedElectricityPrice, hours, capacity, generationInSegment);
                
            }
            
            // TOD calculateProjectValue() should all be here... otherwise it is confusing to tell difference between  calculateProjectValue and getProjectValue
    		
    	}
    	
    	protected void calculateFinancialIndicatorsForOneSegment(double expectedElectricityPrice, double hours, double capacity, double generationInSegment) {
            
    		if (expectedMarginalCost <= expectedElectricityPrice) {

	        	runningHours += hours;
	            expectedGeneration += generationInSegment;
	            
	            expectedAnnualVariableCost += expectedMarginalCost * generationInSegment;
	            expectedAnnualVariableRevenue += expectedElectricityPrice * generationInSegment;
	            expectedGrossProfit += expectedAnnualVariableRevenue - expectedAnnualVariableCost;

    		
    		}	
    	
    	}

    	

    	protected double calculateDiscountedCashFlowForPlant(int depriacationTime,
                double totalInvestment, double operatingProfit, double wacc) {
    		
            TreeMap<Integer, Double> cashflow = calculateSimplePowerPlantInvestmentCashFlow(
            		depriacationTime, (int) plant.getActualLeadtime(),
            		totalInvestment, operatingProfit);
            
            return npv(cashflow, wacc);
    	}
    	
    	

    	
    	protected double calculateDiscountedCashFlowForPlant(int depriacationTime,
                double totalInvestment, double operatingProfit) {
    		
    		return calculateDiscountedCashFlowForPlant(depriacationTime,
                    totalInvestment, operatingProfit, wacc);
    	  	
    	}
    	
    	
    	/**
    	 * Calculates the the cash outflow during the power plant building
         * <b>Note</b>: that the cash-flow is defined negative!
    	 * 
    	 * @return double discounted capital costs
    	 */
    	private double calculateDiscountedCapitalCosts() {
            
            double discountedCapitalCosts = calculateDiscountedCashFlowForPlant(
                    technology.getDepreciationTime(), plant.getActualInvestedCapital(), 0);
            
            logger.log(Level.FINE, 
            		"Agent " + agent +  " found the discounted capital costs for " + technology + " to be " + discountedCapitalCosts);
            
            return discountedCapitalCosts;
    	}
    	
    	/**
    	 * Calculates the the cash inflow during the power plant operation
    	 * 
    	 * @return discountedCapitalCosts
    	 */
    	private double calculateDiscountedOperatingProfit() {
            
    		double operatingProfit = expectedGrossProfit - fixedOMCost;
            double discountedOperatingProfit = calculateDiscountedCashFlowForPlant(
                    technology.getDepreciationTime(), 0, operatingProfit);
    	
            logger.log(Level.FINE, 
            		"Agent " + agent +  " found the discounted operating profit for " + technology + " to be " + discountedOperatingProfit);

            return discountedOperatingProfit;  
    	}
    	

		
		    
		/**
		 * Calculates the discounted operating costs without scheme
		 * 
		 * @return discountedCapitalCosts
		 */
		private double calculateDiscountedOperatingCost() {
			
			
		    double operatingCost = expectedAnnualVariableCost + fixedOMCost;		    
            double discountedProjectOperatingCost = calculateDiscountedCashFlowForPlant(
                    technology.getDepreciationTime(), 0, -operatingCost);
		   
		    logger.log(Level.FINE, 
		    		"Agent " + agent +  " found the discounted operating costs for " + technology + " to be " + discountedProjectOperatingCost);
		
		    return discountedProjectOperatingCost;  
		}
		




		private double calculateProjectCost(){
    		return discountedOperatingCost + discountedCapitalCosts;       
    	}
    	
    	// TODO it can be confusing that the tender on is different with the waccAjustend.
    	protected double calculateProjectValue(){

            double projectValue = discountedOperatingProfit + discountedCapitalCosts;
    		
            logger.info(agent + " found the project value for technology " + technology + " to be " + Math.round(projectValue / (plant.getActualNominalCapacity() * 1e3)) / 1e3 + " million EUR/kW (running hours: " + runningHours + ")");
            // double projectTotalValue = projectValuePerMW *
            // plant.getActualNominalCapacity();
            // double projectReturnOnInvestment = discountedOpProfit
            // / (-discountedCapitalCosts);         
            return projectValue;       
            
    	}
    	
    	/**
    	 * Calculates capital costs, operating costs & revenue, profit and project values
    	 */
    	public void calculateDiscountedValues() {
    		
    		discountedCapitalCosts = calculateDiscountedCapitalCosts();
    		discountedOperatingCost = calculateDiscountedOperatingCost();
    		discountedOperatingProfit = calculateDiscountedOperatingProfit();
    		
    		projectValue = calculateProjectValue();
    		projectCost = calculateProjectCost();
    			
    	}

    	
    	
    	public boolean plantHasRequiredRunningHours() {
            
    		if (getRunningHours() < plant.getTechnology().getMinimumRunningHours()) {
                logger.log(Level.FINE, 
                		agent + " will not invest in " + plant.getTechnology() + " technology as he expect to have " + getRunningHours() + " running hours, which is lower then required");
                return false; 
             } else {
            	 return true;
             }
            
    	}

		public double getRunningHours() {
			return runningHours;
		}

		public PowerGeneratingTechnology getTechnology() {
			return technology;
		}

		public void setTechnology(PowerGeneratingTechnology technology) {
			this.technology = technology;
		}

		public double getExpectedMarginalCost() {
			return expectedMarginalCost;
		}

		public double getExpectedGeneration() {
			return expectedGeneration;
		}

		public double getExpectedGrossProfit() {
			return expectedGrossProfit;
		}

		public double getExpectedOperatingCost() {
			return expectedAnnualVariableCost;
		}

		public double getExpectedOperatingRevenue() {
			return expectedAnnualVariableRevenue;
		}

		public double getWacc() {
			return wacc;
		}


		public double getDiscountedCapitalCosts() {
			return discountedCapitalCosts;
		}

		public double getDiscountedOperatingCost() {
			return discountedOperatingCost;
		}


		public double getDiscountedOperatingProfit() {
			return discountedOperatingProfit;
		}

		public double getProjectValue() {
			return projectValue;
		}

		public double getProjectCost() {
			return projectCost;
		}

    }
    
 
    
    public class MarketInformation {

    	public Map<Segment, Double> expectedElectricityPricesPerSegment;
        public double maxExpectedLoad = 0d;
        public Map<PowerPlant, Double> meritOrder;
        public double capacitySum;

        public MarketInformation(ElectricitySpotMarket market, Map<ElectricitySpotMarket, Double> expectedDemand, Map<Substance, Double> fuelPrices, double co2price, long time) {
            // determine expected power prices
            expectedElectricityPricesPerSegment = new HashMap<Segment, Double>();
            Map<PowerPlant, Double> marginalCostMap = new HashMap<PowerPlant, Double>();
            capacitySum = 0d;

            // get merit order for this market
            for (PowerPlant plant : getReps().findExpectedOperationalPowerPlantsInMarket(market, time)) {

                //double plantMarginalCost = determineExpectedMarginalCost(plant, fuelPrices, co2price);
                double plantMarginalCost = determineExpectedMarginalCost(plant);
                marginalCostMap.put(plant, plantMarginalCost);
                capacitySum += plant.getActualNominalCapacity();
            }

            //get difference between technology target and expected operational capacity
            for (PowerGeneratingTechnologyTarget pggt : getReps().findAllPowerGeneratingTechnologyTargetsByMarket(market)) {
                double expectedTechnologyCapacity = getReps().calculateCapacityOfExpectedOperationalPowerPlantsInMarketAndTechnology(market, pggt.getPowerGeneratingTechnology(), time);
                double targetDifference = pggt.getTrend().getValue(time) - expectedTechnologyCapacity;
                if (targetDifference > 0) {
                    PowerPlant plant = getReps().createAndSpecifyTemporaryPowerPlant(getCurrentTick(), new EnergyProducer(), getReps().findFirstPowerGridNodeByElectricitySpotMarket(market), pggt.getPowerGeneratingTechnology());
                    plant.setActualNominalCapacity(targetDifference);
                    Map<Substance, Double> myFuelPrices = new HashMap<Substance, Double>();
                    for (Substance fuel : plant.getTechnology().getFuels()) {
                        myFuelPrices.put(fuel, fuelPrices.get(fuel));
                    }
                    plant.setFuelMix(calculateFuelMix(plant, myFuelPrices, co2price));
                    double plantMarginalCost = determineExpectedMarginalCost(plant);
                    //double plantMarginalCost = determineExpectedMarginalCost(plant, fuelPrices, co2price);
                    marginalCostMap.put(plant, plantMarginalCost);
                    capacitySum += targetDifference;
                }
            }

            MapValueComparator comp = new MapValueComparator(marginalCostMap);
            meritOrder = new TreeMap<PowerPlant, Double>(comp);
            meritOrder.putAll(marginalCostMap);

            long numberOfSegments = getReps().segments.size();

            double demandFactor = expectedDemand.get(market).doubleValue();

            // find expected prices per segment given merit order
            for (SegmentLoad segmentLoad : market.getLoadDurationCurve()) {

                double expectedSegmentLoad = segmentLoad.getBaseLoad() * demandFactor;

                if (expectedSegmentLoad > maxExpectedLoad) {
                    maxExpectedLoad = expectedSegmentLoad;
                }

                double segmentSupply = 0d;
                double segmentPrice = 0d;
                double totalCapacityAvailable = 0d;

                for (Entry<PowerPlant, Double> plantCost : meritOrder.entrySet()) {
                    PowerPlant plant = plantCost.getKey();
                    double plantCapacity = 0d;
                    // Determine available capacity in the future in this
                    // segment
                    plantCapacity = plant.getExpectedAvailableCapacity(time, segmentLoad.getSegment(), numberOfSegments);
                    totalCapacityAvailable += plantCapacity;
                    // logger.warn("Capacity of plant " + plant.toString() +
                    // " is " +
                    // plantCapacity/plant.getActualNominalCapacity());
                    if (segmentSupply < expectedSegmentLoad) {
                        segmentSupply += plantCapacity;
                        segmentPrice = plantCost.getValue();
                    }

                }

                // logger.warn("Segment " +
                // segmentLoad.getSegment().getSegmentID() + " supply equals " +
                // segmentSupply + " and segment demand equals " +
                // expectedSegmentLoad);
                // Find strategic reserve operator for the market.
                double reservePrice = 0;
                double reserveVolume = 0;
                for (StrategicReserveOperator operator : getReps().strategicReserveOperators) {
                    ElectricitySpotMarket market1 = getReps().findElectricitySpotMarketForZone(operator
                            .getZone());
                    if (market.equals(market1)) {
                        reservePrice = operator.getReservePriceSR();
                        reserveVolume = operator.getReserveVolume();
                    }
                }
                
                
               MarketInformationReport report = getReps().findMarketInformationReport(segmentLoad.getSegment(), agent, time);
               report.schedule = schedule; 
               report.setExpectedSegmentLoad(expectedSegmentLoad); 
               report.setSegmentSupply(segmentSupply); 
               report.setTotalCapacityAvailable(totalCapacityAvailable);
               
               double expectedElectricityPrice;


                if (segmentSupply >= expectedSegmentLoad
                        && ((totalCapacityAvailable - expectedSegmentLoad) <= (reserveVolume))) {
                
                	expectedElectricityPrice = reservePrice;
                    report.setResult(1); 

                } else if (segmentSupply >= expectedSegmentLoad
                        && ((totalCapacityAvailable - expectedSegmentLoad) > (reserveVolume))) {
                	
                	expectedElectricityPrice = segmentPrice;
                    report.setResult(2); 

                } else {
                	expectedElectricityPrice = market.getValueOfLostLoad();
                    report.setResult(3); 
                }
                
                expectedElectricityPricesPerSegment.put(segmentLoad.getSegment(), expectedElectricityPrice);

                report.setExpectedElectricityPrice(expectedElectricityPrice);


            }
        }
    }

     
  	
  
}
        	
        	
       
        
        
        
        
    	

    
        
        
        


    


