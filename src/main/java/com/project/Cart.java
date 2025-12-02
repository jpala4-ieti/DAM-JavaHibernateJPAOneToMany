package com.project;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "carts")
public class Cart implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="cartId", unique=true, nullable=false)
    private Long cartId;

    private String type;

    // RENDIMENT: Canviat a LAZY per evitar carregar tota la BBDD en memòria.
    // Cascade ALL permet que si guardes el Cart, es guardin els Items.
    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Item> items = new HashSet<>();

    // UUID per a identitat única abans de persistir
    @Column(name = "uuid", nullable = false, updatable = false, unique = true)
    private String uuid = UUID.randomUUID().toString();

    public Cart() {}

    public Cart(String type) {
        this.type = type;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<Item> getItems() {
        return items;
    }

    public void setItems(Set<Item> items) {
        // Neteja i afegeix per mantenir la referència de la col·lecció
        this.items.clear();
        if (items != null) {
            items.forEach(this::addItem);
        }
    }

    // Mètodes helper per mantenir la coherència bidireccional
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
        // No imprimim 'items' aquí per evitar LazyInitializationException
        return cartId + ": " + type; 
    }
    
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