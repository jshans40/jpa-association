package sql.entity;

import jdbc.JdbcTemplate;
import jpa.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import persistence.sql.Dialect;
import persistence.sql.H2Dialect;
import persistence.sql.ddl.CreateQueryBuilder;
import persistence.sql.ddl.Person;
import persistence.sql.ddl.QueryBuilder;
import persistence.sql.entity.Order;
import persistence.sql.entity.OrderItem;
import persistence.sql.model.EntityColumnValue;
import sql.ddl.JdbcServerExtension;
import sql.ddl.JdbcServerTest;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcServerTest
class EntityManagerImplTest {

    private static final Dialect dialect = new H2Dialect();
    private static final JdbcTemplate jdbcTemplate = JdbcServerExtension.getJdbcTemplate();
    private static final EntityPersister entityPersister = new EntityPersisterImpl(jdbcTemplate);
    private static final EntityLoader entityLoader = new EntityLoader(jdbcTemplate);
    private static final EntityManager entityManager = new EntityManagerImpl(entityPersister, entityLoader);

    @BeforeAll
    static void init() {
        QueryBuilder createQueryBuilder = new CreateQueryBuilder(Person.class, dialect);
        jdbcTemplate.execute(createQueryBuilder.build());

        QueryBuilder orderCreateQueryBuilder = new CreateQueryBuilder(Order.class, dialect);
        jdbcTemplate.execute(orderCreateQueryBuilder.build());

        QueryBuilder orderItemCreateQueryBuilder = new CreateQueryBuilder(OrderItem.class, dialect);
        jdbcTemplate.execute(orderItemCreateQueryBuilder.build());
    }

    @Test
    void 데이터_삽입_및_조회() throws NoSuchFieldException {
        String name = "이름";
        int age = 10;
        String email = "jsss@test.com";
        int index = 1;
        Person person = new Person(name, age, email, index);
        entityManager.persist(person);

        Person savedPerson = entityManager.find(Person.class, 1L);
        Field nameField = savedPerson.getClass().getDeclaredField("name");
        Field ageField = savedPerson.getClass().getDeclaredField("age");
        Field emailField = savedPerson.getClass().getDeclaredField("email");

        EntityColumnValue entityColumnValue = new EntityColumnValue(nameField, savedPerson);

        assertThat(entityColumnValue.getValue()).isEqualTo(name);
    }

    @Test
    void 데이터_수정() throws NoSuchFieldException {
        String name = "이름";
        int age = 11;
        String email = "jsss@test.co1m";
        int index = 1;
        Person person = new Person(name, age, email, index);
        Person savedPerson = entityManager.persist(person);

        Person insertedPerson = entityManager.find(Person.class, savedPerson.getId());
        String updateEmail = "test@naver.com";
        insertedPerson.setEmail(updateEmail);

        Person updatedPerson = entityManager.find(Person.class, 1L);

        Field emailField = updatedPerson.getClass().getDeclaredField("email");
        EntityColumnValue emailColumnValue = new EntityColumnValue(emailField, insertedPerson);

        assertThat(emailColumnValue.getValue()).isEqualTo(updateEmail);
    }

    @Test
    void save후_더티체크() {
        String name = "이름";
        int age = 11;
        String email = "jsss@test.co1m";
        int index = 1;
        Person person = new Person(name, age, email, index);
        Person savedPerson = entityManager.persist(person);

        String updatedEmail = "updateemail@test.com";
        person.setEmail(updatedEmail);
        entityManager.merge(person);
        entityManager.flush();

        EntityManager newEntityManager = new EntityManagerImpl(entityPersister, entityLoader);
        Person dirtyCheckedPerson = newEntityManager.find(Person.class, savedPerson.getId());

        assertThat(dirtyCheckedPerson.getEmail()).isEqualTo(updatedEmail);
    }

    @Test
    void test() {
        Order order = new Order("orderNumber");

        OrderItem orderItem1 = new OrderItem("product", 1);
        OrderItem orderItem2 = new OrderItem("product2", 1);

        order.getOrderItems().add(orderItem1);
        order.getOrderItems().add(orderItem2);

        entityManager.persist(order);

        EntityPersister entityPersister = new EntityPersisterImpl(jdbcTemplate);
        EntityLoader entityLoader = new EntityLoader(jdbcTemplate);
        EntityManager entityManagers = new EntityManagerImpl(entityPersister, entityLoader);

        Order order1 = entityManagers.find(Order.class, 1L);
        System.out.println("das");
    }
}
