package ru.otus.cachehw.hibernate.crm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.cachehw.HwCache;
import ru.otus.cachehw.HwListener;
import ru.otus.cachehw.hibernate.core.repository.DataTemplate;
import ru.otus.cachehw.hibernate.core.sessionmanager.TransactionManager;
import ru.otus.cachehw.hibernate.crm.model.Client;

import java.util.List;
import java.util.Optional;

public class DbServiceClientImpl implements DBServiceClient {
    private static final Logger log = LoggerFactory.getLogger(DbServiceClientImpl.class);

    private final DataTemplate<Client> clientDataTemplate;
    private final TransactionManager transactionManager;
    public static final String CACHE_KEY_PREFIX = "client:";
    private final HwCache<String, Client> cache;

    public DbServiceClientImpl(
            TransactionManager transactionManager,
            DataTemplate<Client> clientDataTemplate,
            HwCache<String, Client> cache) {
        this.transactionManager = transactionManager;
        this.clientDataTemplate = clientDataTemplate;
        this.cache = cache;

        HwListener<String, Client> listener =
                (key, value, action) -> log.info("notify: key={}, value={}, action={}", key, value, action);
        cache.addListener(listener);
    }

    @Override
    public Client saveClient(Client client) {
        return transactionManager.doInTransaction(session -> {
            var clientCloned = client.clone();
            if (client.getId() == null) {
                var savedClient = clientDataTemplate.insert(session, clientCloned);
                cache.put(CACHE_KEY_PREFIX + savedClient.getId(), savedClient);
                return savedClient;
            } else {
                var updatedClient = clientDataTemplate.update(session, clientCloned);
                cache.put(CACHE_KEY_PREFIX + updatedClient.getId(), updatedClient);
                return updatedClient;
            }
        });
    }

    @Override
    public Optional<Client> getClient(long id) {
        var cacheKey = CACHE_KEY_PREFIX + id;
        var client = cache.get(cacheKey);
        if (client != null) {
            return Optional.of(client);
        }
        return transactionManager.doInReadOnlyTransaction(session -> {
            var clientOptional = clientDataTemplate.findById(session, id);
            log.info("client: {}", clientOptional);
            clientOptional.ifPresent(value -> cache.put(cacheKey, value));
            return clientOptional;
        });
    }

    @Override
    public List<Client> findAll() {
        return transactionManager.doInReadOnlyTransaction(session -> {
            var clientList = clientDataTemplate.findAll(session);
            log.info("clientList:{}", clientList);
            clientList.forEach(value -> cache.put(CACHE_KEY_PREFIX + value.getId(), value));
            return clientList;
        });
    }
}
