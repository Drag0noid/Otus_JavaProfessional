package ru.otus.cachehw.hibernate.crm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "client")
public class Client implements Cloneable {

    @Id
    @SequenceGenerator(name = "client_gen", sequenceName = "client_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_gen")
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id")
    private Address address;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Phone> phones;

    public Client(String name) {
        this.id = null;
        this.name = name;
    }

    public Client(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public <E> Client(Long id, String name, Address address, List<Phone> phones) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phones = phones;

        if (phones != null) {
            for (Phone phone : phones) {
                phone.setClient(this);
            }
        }
    }

    @Override
    @SuppressWarnings({"java:S2975", "java:S1182"})
    public Client clone() {
        Client clonedClient = new Client(this.id, this.name);
        if (this.address != null) {
            Address clonedAddress = new Address(this.address.getId(), this.address.getStreet());
            clonedClient.setAddress(clonedAddress);
        }
        if (this.phones != null) {
            clonedClient.setPhones(this.phones.stream()
                    .map(phone -> {
                        Phone clonedPhone = new Phone(phone.getId(), phone.getNumber());
                        clonedPhone.setClient(clonedClient);
                        return clonedPhone;
                    })
                    .collect(Collectors.toList()));
        }
        return clonedClient;
    }

    @Override
    public String toString() {
        return "Client{" + "id="
                + id + ", name='"
                + name + '\'' + ", address="
                + (address != null ? address.getStreet() : "null") + ", phones="
                + (phones != null ? phones.stream().map(Phone::getNumber).collect(Collectors.joining(", ")) : "null")
                + '}';
    }
}
