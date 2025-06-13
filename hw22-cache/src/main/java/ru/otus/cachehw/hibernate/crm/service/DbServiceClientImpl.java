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

    public Client saveClient(Client client) {
        return transactionManager.doInTransaction(session -> {
            var clientCloned = client.clone();
            Client savedOrUpdatedClient;

            if (client.getId() == null) {
                savedOrUpdatedClient = clientDataTemplate.insert(session, clientCloned);
            } else {
                savedOrUpdatedClient = clientDataTemplate.update(session, clientCloned);
            }

            cache.put(getCacheKey(savedOrUpdatedClient.getId()), savedOrUpdatedClient);
            return savedOrUpdatedClient;
        });
    }

    @Override
    public Optional<Client> getClient(long id) {
        String cacheKey = getCacheKey(id);
        Client cachedClient = cache.get(cacheKey);
        if (cachedClient != null) {
            return Optional.of(cachedClient);
        }

        return transactionManager.doInReadOnlyTransaction(session -> {
            Optional<Client> clientOptional = clientDataTemplate.findById(session, id);
            log.info("client: {}", clientOptional);
            clientOptional.ifPresent(client -> cache.put(getCacheKey(client.getId()), client));
            return clientOptional;
        });
    }

    @Override
    public List<Client> findAll() {
        return transactionManager.doInReadOnlyTransaction(session -> {
            List<Client> clientList = clientDataTemplate.findAll(session);
            log.info("clientList: {}", clientList);
            clientList.forEach(client -> cache.put(getCacheKey(client.getId()), client));
            return clientList;
        });
    }

    private String getCacheKey(Long id) {
        return CACHE_KEY_PREFIX + id;
    }
}
