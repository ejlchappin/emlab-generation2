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
package emlab.gen.role.operating;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.contract.Loan;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.engine.AbstractRole;
import emlab.gen.engine.Role;
import emlab.gen.engine.Schedule;
import emlab.gen.repository.Reps;
import java.util.logging.Level;

/**
 * {@link EnergyProducer}s repay their loans
 *
 * @author alfredas
 * @author emile
 *
 */
public class PayForLoansRole extends AbstractRole<EnergyProducer> implements Role<EnergyProducer> {

    public PayForLoansRole(Schedule schedule) {
        super(schedule);
    }

    public void act(EnergyProducer producer) {

        logger.finer("Process accepted bids to cash flow now");

        // for (Loan loan : loanRepository.findLoansFromAgent(producer)) {
        for (PowerPlant plant : getReps().findPowerPlantsByOwner(producer)) {
            Loan loan = plant.getLoan();
            if (loan != null) {
                logger.finer("Found a loan: {}" + loan);
                if (loan.getNumberOfPaymentsDone() < loan.getTotalNumberOfPayments()) {

                    double payment = loan.getAmountPerPayment();
                    getReps().createCashFlow(producer, loan.getTo(), payment,
                            CashFlow.LOAN, getCurrentTick(), loan.getRegardingPowerPlant());

                    loan.setNumberOfPaymentsDone(loan.getNumberOfPaymentsDone() + 1);

                    logger.log(Level.FINER, "Paying {0} (euro) for loan {1}", new Object[]{payment, loan});
                    logger.log(Level.FINER, "Number of payments done {0}, total needed: {1}", new Object[]{loan.getNumberOfPaymentsDone(), loan.getTotalNumberOfPayments()});
                }
            }
            Loan downpayment = plant.getDownpayment();
            if (downpayment != null) {
                logger.finer("Found downpayment");
                if (downpayment.getNumberOfPaymentsDone() < downpayment.getTotalNumberOfPayments()) {
                    double payment = downpayment.getAmountPerPayment();
                    getReps().createCashFlow(producer, downpayment.getTo(), payment,
                            CashFlow.DOWNPAYMENT, getCurrentTick(),
                            downpayment.getRegardingPowerPlant());
                    downpayment.setNumberOfPaymentsDone(downpayment.getNumberOfPaymentsDone() + 1);
                    logger.log(Level.FINER, "Paying {0} (euro) for downpayment {1}", new Object[]{payment, downpayment});
                    logger.log(Level.FINER, "Number of payments done {0}, total needed: {1}", new Object[]{downpayment.getNumberOfPaymentsDone(), downpayment.getTotalNumberOfPayments()});
                }
            }
        }
    }
}
