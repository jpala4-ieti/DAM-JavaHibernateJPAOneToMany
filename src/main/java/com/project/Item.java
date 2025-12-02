package com.project;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

// @Entity: Marca aquesta classe com una entitat JPA mapejada a la taula "items".
@Entity
@Table(name = "items")
public class Item implements Serializable {

    // CLAU PRIMÀRIA amb auto-increment gestionat per la BBDD.
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="itemID", unique=true, nullable=false)    
    private Long itemId;

    private String name;

    // RELACIÓ MANY-TO-ONE (Molts Items pertanyen a un Cart):
    // - Aquest és el costat PROPIETARI de la relació (té el @JoinColumn).
    // - @JoinColumn: Defineix la columna FK "cartId" a la taula items.
    // - FetchType.LAZY: No carrega el Cart fins que s'accedeix (optimització).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cartId")
    private Cart cart;

    // UUID: Identificador únic generat al crear l'objecte.
    // Necessari per equals/hashCode quan itemId encara és null (abans de persist).
    @Column(name = "uuid", nullable = false, updatable = false, unique = true)
    private String uuid = UUID.randomUUID().toString();

    public Item() {}

    public Item(String name) {
        this.name = name;
    }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    @Override
    public String toString() {
        return itemId + ": " + name;
    }

    // EQUALS amb instanceof (no getClass()):
    // Hibernate crea PROXIES (subclasses) per lazy loading.
    // Utilitzar getClass() fallaria perquè Proxy.class != Item.class.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false; 
        Item item = (Item) o;
        return Objects.equals(uuid, item.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}