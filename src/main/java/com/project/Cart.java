package com.project;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

// @Entity: Marca aquesta classe com una entitat JPA que es mapeja a una taula de la base de dades.
// Serializable: Permet que l'objecte es pugui convertir en bytes (necessari per caché, sessions, etc.)
@Entity
@Table(name = "carts")
public class Cart implements Serializable {

    // @Id: Defineix la clau primària de l'entitat.
    // @GeneratedValue(IDENTITY): La BBDD genera automàticament el valor (auto-increment).
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="cartId", unique=true, nullable=false)
    private Long cartId;

    private String type;

    // RELACIÓ ONE-TO-MANY (Un Cart té molts Items):
    // - mappedBy="cart": El costat INVERS de la relació. L'atribut "cart" a Item és el propietari.
    // - FetchType.LAZY: No carrega els items fins que s'accedeixen (millora rendiment).
    // - CascadeType.ALL: Operacions (persist, merge, remove) es propaguen als items.
    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Item> items = new HashSet<>();

    // UUID: Identificador únic generat ABANS de guardar a la BBDD.
    // Útil per equals/hashCode ja que cartId és null fins que es persisteix.
    @Column(name = "uuid", nullable = false, updatable = false, unique = true)
    private String uuid = UUID.randomUUID().toString();

    public Cart() {}

    public Cart(String type) {
        this.type = type;
    }

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Set<Item> getItems() { return items; }

    public void setItems(Set<Item> items) {
        this.items.clear();
        if (items != null) {
            items.forEach(this::addItem);
        }
    }

    // MÈTODES HELPER: Mantenen la COHERÈNCIA BIDIRECCIONAL.
    // Quan afegeixes un Item, també s'actualitza la referència inversa (item.setCart).
    public void addItem(Item item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(Item item) {
        items.remove(item);
        item.setCart(null);
    }

    @Override
    public String toString() {
        String llistaItems = "Buit";
        if (items != null && !items.isEmpty()) {
            llistaItems = items.stream()
                .map(Item::getName)
                .collect(Collectors.joining(", "));
        }
        return String.format("Cart [ID=%d, Type=%s, Items: %s]", cartId, type, llistaItems);
    }
    
    // EQUALS i HASHCODE basats en UUID (no en cartId):
    // Garanteix consistència abans i després de persistir l'entitat.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cart)) return false;
        Cart cart = (Cart) o;
        return Objects.equals(uuid, cart.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }    
}