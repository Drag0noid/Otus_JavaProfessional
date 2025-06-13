package otus.cfg;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import ru.otus.cachehw.HwCache;
import ru.otus.cachehw.MyCache;
import ru.otus.cachehw.hibernate.core.repository.DataTemplateHibernate;
import ru.otus.cachehw.hibernate.core.repository.HibernateUtils;
import ru.otus.cachehw.hibernate.core.sessionmanager.TransactionManagerHibernate;
import ru.otus.cachehw.hibernate.crm.dbmigrations.MigrationsExecutorFlyway;
import ru.otus.cachehw.hibernate.crm.model.Client;
import ru.otus.cachehw.hibernate.crm.service.DBServiceClient;
import ru.otus.cachehw.hibernate.crm.service.DbServiceClientImpl;

import static ru.otus.cachehw.hibernate.core.repository.HibernateUtils.HIBERNATE_CFG_FILE;

public abstract class AbstractHibernate {
    protected SessionFactory sessionFactory;
    protected TransactionManagerHibernate transactionManager;
    protected DataTemplateHibernate<Client> clientTemplate;
    protected DBServiceClient dbServiceClient;
    protected HwCache<String, Client> cache;

    private static TestContainersConfig.CustomPostgreSQLContainer container;

    @BeforeAll
    public static void init() {
        container = TestContainersConfig.CustomPostgreSQLContainer.getInstance();
        container.start();
    }

    @AfterAll
    public static void shutdown() {
        container.stop();
    }

    @BeforeEach
    public void setUp() {
        String dbUrl = System.getProperty("app.datasource.demo-db.jdbcUrl");
        String dbUserName = System.getProperty("app.datasource.demo-db.username");
        String dbPassword = System.getProperty("app.datasource.demo-db.password");

        var migrationsExecutor = new MigrationsExecutorFlyway(dbUrl, dbUserName, dbPassword);
        migrationsExecutor.executeMigrations();

        Configuration configuration = new Configuration().configure(HIBERNATE_CFG_FILE);
        configuration.setProperty("hibernate.connection.url", dbUrl);
        configuration.setProperty("hibernate.connection.username", dbUserName);
        configuration.setProperty("hibernate.connection.password", dbPassword);

        sessionFactory = HibernateUtils.buildSessionFactory(configuration, Client.class);

        transactionManager = new TransactionManagerHibernate(sessionFactory);
        clientTemplate = new DataTemplateHibernate<>(Client.class);
        cache = new MyCache<>();
        dbServiceClient = new DbServiceClientImpl(transactionManager, clientTemplate, cache);
    }
}
