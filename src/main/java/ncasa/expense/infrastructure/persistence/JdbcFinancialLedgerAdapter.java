package ncasa.expense.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import ncasa.expense.application.port.out.*;
import ncasa.expense.domain.HouseholdRef;
import ncasa.expense.domain.MemberRef;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcFinancialLedgerAdapter implements FinancialLedgerReadPort {
    private final JdbcTemplate jdbc;
    public JdbcFinancialLedgerAdapter(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @Override public List<ExpenseLedgerRow> expenseTotals(HouseholdRef household,LocalDate from,LocalDate to){
        String expenseDates=(from==null?"":" and expense_date>=?")+(to==null?"":" and expense_date<=?");
        String allocationDates=(from==null?"":" and e.expense_date>=?")+(to==null?"":" and e.expense_date<=?");
        String sql=("""
            select currency, member_id, sum(paid) paid, sum(allocated) allocated from (
              select currency, payer_member_id member_id, amount paid, 0 allocated from expenses
               where household_id=? and status='CONFIRMED'%s
              union all
              select e.currency, a.member_id, 0 paid, a.amount allocated from expenses e join expense_allocations a on a.expense_id=e.id
               where e.household_id=? and e.status='CONFIRMED'%s
            ) ledger group by currency, member_id order by currency, member_id
            """).formatted(expenseDates,allocationDates);
        var arguments=new java.util.ArrayList<Object>();arguments.add(household.value());if(from!=null)arguments.add(from);if(to!=null)arguments.add(to);arguments.add(household.value());if(from!=null)arguments.add(from);if(to!=null)arguments.add(to);
        return jdbc.query(sql,(rs,n)->new ExpenseLedgerRow(rs.getString("currency"),new MemberRef(rs.getObject("member_id",UUID.class)),rs.getBigDecimal("paid"),rs.getBigDecimal("allocated")),
                arguments.toArray());
    }
    @Override public List<SettlementLedgerRow> settlementTotals(HouseholdRef household,LocalDate to){
        String sql="""
            select currency, member_id, sum(settled_out) settled_out, sum(settled_in) settled_in from (
              select currency, from_member_id member_id, amount settled_out, 0 settled_in from settlements where household_id=? and status='CONFIRMED' and settlement_date<=?
              union all
              select currency, to_member_id member_id, 0 settled_out, amount settled_in from settlements where household_id=? and status='CONFIRMED' and settlement_date<=?
            ) ledger group by currency, member_id order by currency, member_id
            """;
        return jdbc.query(sql,(rs,n)->new SettlementLedgerRow(rs.getString("currency"),new MemberRef(rs.getObject("member_id",UUID.class)),rs.getBigDecimal("settled_out"),rs.getBigDecimal("settled_in")),household.value(),to,household.value(),to);
    }
}
