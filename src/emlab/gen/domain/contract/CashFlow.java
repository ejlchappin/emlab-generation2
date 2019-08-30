/*******************************************************************************
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
 ******************************************************************************/
package emlab.gen.domain.contract;

import emlab.gen.domain.agent.EMLabAgent;
import emlab.gen.domain.technology.PowerPlant;

public class CashFlow {

    public static final int UNCLASSIFIED = 0;
    public static final int ELECTRICITY_SPOT = 1;
    public static final int ELECTRICITY_LONGTERM = 2;
    public static final int FIXEDOMCOST = 3;
    public static final int COMMODITY = 4;
    public static final int CO2TAX = 5;
    public static final int CO2AUCTION = 6;
    public static final int LOAN = 7;
    public static final int DOWNPAYMENT = 8;
    public static final int NATIONALMINCO2 = 9;
    public static final int STRRESPAYMENT = 10;
    public static final int CAPMARKETPAYMENT = 11;
    public static final int CO2HEDGING = 12;

//    @RelatedTo(type = "FROM_AGENT", elementClass = EMLabAgent.class, direction = Direction.OUTGOING)
    private EMLabAgent from;

//    @RelatedTo(type = "TO_AGENT", elementClass = EMLabAgent.class, direction = Direction.OUTGOING)
    private EMLabAgent to;

//    @RelatedTo(type = "REGARDING_POWERPLANT", elementClass = PowerPlant.class, direction = Direction.OUTGOING)
    private PowerPlant regardingPowerPlant;

    private int type;
    private double money;
    private long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public EMLabAgent getFrom() {
        return from;
    }

    public void setFrom(EMLabAgent from) {
        this.from = from;
    }

    public EMLabAgent getTo() {
        return to;
    }

    public void setTo(EMLabAgent to) {
        this.to = to;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String toString() {
        return "from " + getFrom() + " to " + getTo() + " type " + getType() + " amount " + getMoney();
    }

    public PowerPlant getRegardingPowerPlant() {
        return regardingPowerPlant;
    }

    public void setRegardingPowerPlant(PowerPlant regardingPowerPlant) {
        this.regardingPowerPlant = regardingPowerPlant;
    }

}
