package sql.dml;

import org.junit.jupiter.api.Test;
import persistence.sql.ddl.Person;
import persistence.sql.dml.SelectQuery;
import persistence.sql.entity.Order;
import persistence.sql.exception.ExceptionMessage;
import persistence.sql.exception.RequiredClassException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SelectQueryTest {

    @Test
    void SELECT_쿼리_조회() {
        SelectQuery selectQuery = SelectQuery.getInstance();
        assertThat(selectQuery.findAll(Person.class)).isEqualTo("SELECT users.id, users.nick_name, users.old, users.email FROM users users");
    }

    @Test
    void 매개변수_NULL로_예외_발생() {
        assertThatThrownBy(() -> SelectQuery.getInstance().findAll(null))
                .isInstanceOf(RequiredClassException.class)
                .hasMessage(ExceptionMessage.REQUIRED_CLASS.getMessage());
    }

    @Test
    void 아이디로_조회_쿼리() {
        SelectQuery selectQuery = SelectQuery.getInstance();
        assertThat(selectQuery.findById(Person.class, 1L)).isEqualTo("SELECT users.id, users.nick_name, users.old, users.email FROM users users WHERE users.id = 1");
    }

    @Test
    void Join_조회_쿼리() {
        SelectQuery selectQuery = SelectQuery.getInstance();
        System.out.println("selectQuery.bu = " + selectQuery.findById(Order.class, 1L));

//        assertThat(selectQuery.findById(Order.class, 1L)).isEqualTo("SELECT orders.id, orders.orderNumber, order_items.id, order_items.product, order_items.quantity, order_items.order_id FROM orders orders LEFT JOIN order_items ON id = order_id  WHERE orders.id = 1");

    }

}
