package ru.otus.cachehw.hibernate.crm.service;

import ru.otus.cachehw.hibernate.crm.model.Client;

import java.util.List;
import java.util.Optional;

public interface DBServiceClient {

    Client saveClient(Client client);

    Optional<Client> getClient(long id);

    List<Client> findAll();
}
