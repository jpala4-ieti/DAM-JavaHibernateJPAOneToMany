package com.project;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "items")
public class Item implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="itemID", unique=true, nullable=false)    
    private Long itemId; // Canviat a Objecte Long

    private String name;

    @ManyToOne(fetch = FetchType.LAZY) // OPTIMITZACIÓ: Lazy loading
    @JoinColumn(name="cartId")
    private Cart cart;

    // Afegeixo UUID per gestionar equals/hashCode correctament abans de tenir ID de BBDD
    @Column(name = "uuid", nullable = false, updatable = false, unique = true)
    private String uuid = UUID.randomUUID().toString();

    public Item() {}

    public Item(String name) {
        this.name = name;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    @Override
    public String toString() {
        return itemId + ": " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // IMPORTANT: Utilitzem instanceof per compatibilitat amb Hibernate Proxies
        if (!(o instanceof Item)) return false; 
        Item item = (Item) o;
        return Objects.equals(uuid, item.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}