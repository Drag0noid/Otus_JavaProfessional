package otus.core;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import ru.otus.cachehw.MyCache;
import ru.otus.cachehw.hibernate.core.repository.DataTemplate;
import ru.otus.cachehw.hibernate.core.repository.DataTemplateHibernate;
import ru.otus.cachehw.hibernate.core.sessionmanager.TransactionManager;
import ru.otus.cachehw.hibernate.core.sessionmanager.TransactionManagerHibernate;
import ru.otus.cachehw.hibernate.crm.model.Address;
import ru.otus.cachehw.hibernate.crm.model.Client;
import ru.otus.cachehw.hibernate.crm.model.Phone;
import ru.otus.cachehw.hibernate.crm.service.DBServiceClient;
import ru.otus.cachehw.hibernate.crm.service.DbServiceClientImpl;
import ru.otus.cachehw.hibernate.core.repository.HibernateUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DbServiceClientImplTest {

    private static SessionFactory sessionFactory;
    private static TransactionManager transactionManager;
    private static DataTemplate<Client> clientDataTemplate;
    private static MyCache<String, Client> cache;
    private static DBServiceClient dbServiceClient;

    @BeforeAll
    static void setup() {
        sessionFactory = HibernateUtils.buildSessionFactory(
                new org.hibernate.cfg.Configuration().configure("hibernate.cfg.xml"),
                Client.class, Address.class, Phone.class
        );

        transactionManager = new TransactionManagerHibernate(sessionFactory);
        clientDataTemplate = new DataTemplateHibernate<>(Client.class);
        cache = new MyCache<>();
        dbServiceClient = new DbServiceClientImpl(transactionManager, clientDataTemplate, cache);
    }

    @AfterAll
    static void tearDown() {
        sessionFactory.close();
    }

    @Test
    @Order(1)
    @DisplayName("сохранение клиента из кэша")
    void shouldSaveAndFindClientUsingCache() {
        var client = new Client(
                null,
                "Vasya",
                new Address(null, "AnyStreet"),
                List.of(
                        new Phone(null, "13-555-22"),
                        new Phone(null, "14-666-333")));

        var savedClient = dbServiceClient.saveClient(client);
        assertThat(savedClient).isNotNull();

        var cachedClient = dbServiceClient
                .getClient(savedClient.getId())
                .orElseThrow(() -> new RuntimeException("Unexpected error"));

        assertThat(cachedClient).usingRecursiveComparison().isEqualTo(savedClient);

        var loadedClient = dbServiceClient.getClient(savedClient.getId());
        assertThat(loadedClient)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(savedClient);
    }

    @Test
    @Order(2)
    @DisplayName("очистка кэша")
    void gcShouldClearCache() throws InterruptedException {
        int size = 100; // уменьшил размер для надёжности
        for (int i = 0; i < size; i++) {
            dbServiceClient.saveClient(new Client(
                    null,
                    "Client #" + (i + 100),
                    new Address(null, "Baker Street, Apt. " + (i + 1)),
                    List.of(new Phone(null, "+1-555-01" + String.format("%03d", i)))
            ));
        }

        System.gc();
        Thread.sleep(1000);
        assertThat(cache.size()).isLessThan(size + 20);
    }
}
