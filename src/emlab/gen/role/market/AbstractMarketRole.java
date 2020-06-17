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
package emlab.gen.role.market;

import emlab.gen.domain.market.Bid;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.DecarbonizationMarket;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.engine.AbstractRole;
import emlab.gen.engine.Schedule;

/**
 * Calculates {@link ClearingPoint} for any {@link Market}. If demand is smaller
 * than supply, a clearing price of the lowest supply is given, but the clearing
 * volume is set to 0.
 *
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a>
 * @author JCRichstein
 */
public abstract class AbstractMarketRole<T extends DecarbonizationMarket> extends AbstractRole<T> {

    public AbstractMarketRole(Schedule schedule) {
        super(schedule);
    }

    public ClearingPoint calculateClearingPoint(DecarbonizationMarket market, long time) {
        double clearedVolume = 0d;
        double clearedPrice = 0d;
        double totalSupplyPrice = calculateTotalSupplyPriceForMarketForTime(market, time);
        double totalSupply = calculateTotalSupplyForMarketForTime(market, time);
        logger.finer("total supply " + totalSupply + " total price " + totalSupplyPrice);
        double totalDemandForPrice = calculateTotalDemandForMarketForTimeForPrice(market, time, totalSupplyPrice);
        logger.finer("total demand " + totalDemandForPrice + " for price " + totalSupplyPrice);

        double minimumSupplyPrice = calculateMinimumSupplyPriceForMarketForTime(market, time);
        double demandAtMinimumSupplyPrice = calculateTotalDemandForMarketForTimeForPrice(market, time, totalSupplyPrice);

        if (demandAtMinimumSupplyPrice <= 0) {
            clearedPrice = minimumSupplyPrice;
            clearedVolume = 0;
        } else if (totalDemandForPrice > totalSupply) {
            // Not enough to meet demand
            clearedVolume = totalSupply;
            if (market.isAuction()) {
                clearedPrice = calculateTotalDemandForMarketForTimeForPrice(market, time, 0d);
            } else {
                clearedPrice = market instanceof ElectricitySpotMarket ? ((ElectricitySpotMarket) market).getValueOfLostLoad()
                        : totalSupplyPrice;
            }
        } else { // Supply exceeds demand
            double totalOfferAmount = 0d;
            double previousPrice = 0d;
            for (Bid offer : getReps().findOffersForMarketForTime(market, time)) {
                double price = offer.getPrice();
                double amount = offer.getAmount();
                double demand = calculateTotalDemandForMarketForTimeForPrice(market, time, price);
                if (demand < totalOfferAmount + amount) {
                    if (demand == 0) {
                        if (getCurrentTick() > 0) {
                            ClearingPoint cp = getReps().findClearingPointForMarketAndTime(market,
                                    getCurrentTick() - 1, false);
                            if (cp != null) {
                                previousPrice = cp.getPrice();
                            }
                        }
                        clearedPrice = previousPrice;
                        clearedVolume = totalOfferAmount;
                    } else if (totalOfferAmount >= demand) {
                        clearedPrice = previousPrice;
                        clearedVolume = totalOfferAmount;
                    } else {
                        clearedPrice = price;
                        clearedVolume = demand;
                    }
                    break;
                }
                totalOfferAmount += amount;
                previousPrice = price;
            }
        }
        ClearingPoint point = getReps().createOrUpdateClearingPoint(market, Math.max(0, clearedPrice), clearedVolume, time, false);
        logger.finer("clearing point for: " + market.getName() + " volume "+ clearedVolume + " and price " + clearedPrice);
        // set bids to accepted and check for partial acceptance
        // DEMAND
        double previousPrice = markAcceptedBids(point, false);
        // if auction - last accepted demand bid sets the price
        if (market.isAuction()) {
            point.setPrice(Math.max(0, previousPrice));
        }
        // SUPPLY
        markAcceptedBids(point, true);
        return point;
    }

    private double markAcceptedBids(ClearingPoint point, boolean isSupply) {
        long time = point.getTime();
        DecarbonizationMarket market = point.getAbstractMarket();
        double clearedPrice = point.getPrice();
        double clearedVolume = point.getVolume();
        double totalBidVolume = 0d;
        double previousPrice = Double.NEGATIVE_INFINITY;
        double accpetedSamePriceVolume = 0d;

        Iterable<Bid> bids = isSupply ? getReps().findOffersForMarketForTimeBelowPrice(market, time, clearedPrice) : market
                .isAuction() ? getReps().findDemandBidsForMarketForTime(market, time) : getReps().findDemandBidsForMarketForTimeAbovePrice(market, time, clearedPrice);

        for (Bid bid : bids) {
            double amount = bid.getAmount();
            totalBidVolume += amount;
            accpetedSamePriceVolume = bid.getPrice() == previousPrice ? accpetedSamePriceVolume + amount : amount;
            if (totalBidVolume < clearedVolume) {
                bid.setStatus(Bid.ACCEPTED);
                bid.setAcceptedAmount(bid.getAmount());
            } else {
                double lastAvailableBidSize = clearedVolume - (totalBidVolume - accpetedSamePriceVolume);
                double samePriceVolume = calculateBidsForMarketForTimeForPrice(market, time, bid.getPrice(), isSupply);
                double adjustRatio = lastAvailableBidSize / samePriceVolume;
                for (Bid partBid : isSupply ? getReps().findOffersForMarketForTimeForPrice(market, time, bid.getPrice())
                        : getReps().findDemandBidsForMarketForTimeForPrice(market, time, bid.getPrice())) {
                    partBid.setStatus(Bid.PARTLY_ACCEPTED);
                    partBid.setAcceptedAmount(partBid.getAmount() * adjustRatio);
                }
                break;
            }
            previousPrice = bid.getPrice();
        }
        return previousPrice;
    }

    private double calculateBidsForMarketForTimeForPrice(DecarbonizationMarket market, long time, double price, boolean isSupply) {
        try {
            return isSupply ? getReps().calculateOffersForMarketForTimeForPrice(market, time, price) : getReps().calculateDemandBidsForMarketForTimeForPrice(market, time, price);
        } catch (NullPointerException npe) {
            logger.warning("going wrong 1");
        }
        return 0d;
    }

    private double calculateTotalDemandForMarketForTimeForPrice(DecarbonizationMarket market, long time, double price) {
        try {
            return market.isAuction() ? getReps().calculateTotalDemandForMarketForTime(market, time) : getReps().calculateTotalDemandForMarketForTimeForPrice(market, time, price);
        } catch (NullPointerException npe) {
            logger.warning("going wrong 2");
        }
        return 0d;
    }

    private double calculateTotalSupplyPriceForMarketForTime(DecarbonizationMarket market, long time) {
        try {
            return getReps().calculateTotalSupplyPriceForMarketForTime(market, time);
        } catch (NullPointerException e) {
        }
        return 0d;
    }

    private double calculateTotalSupplyForMarketForTime(DecarbonizationMarket market, long time) {
        try {
            return getReps().calculateTotalSupplyForMarketForTime(market, time);
        } catch (NullPointerException e) {
        }
        return 0d;
    }

    private double calculateMinimumSupplyPriceForMarketForTime(DecarbonizationMarket market, long time) {
        try {
            return getReps().calculateMinimumSupplyPriceForMarketForTime(market, time);
        } catch (NullPointerException e) {
        }
        return 0d;
    }

}
