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

public class Contract {

//    @RelatedTo(type = "CONTRACT_FROM", elementClass = EMLabAgent.class, direction = Direction.OUTGOING)
    private EMLabAgent from;

//    @RelatedTo(type = "CONTRACT_TO", elementClass = EMLabAgent.class, direction = Direction.OUTGOING)
    private EMLabAgent to;

    private double pricePerUnit;
    private boolean signed;
    private long start;
    private long finish;

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getFinish() {
        return finish;
    }

    public void setFinish(long finish) {
        this.finish = finish;
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
}
