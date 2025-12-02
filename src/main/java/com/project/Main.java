package com.project;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        
        // PREPARACIÓ: Crea el directori "data" si no existeix i inicialitza Hibernate.
        String basePath = System.getProperty("user.dir") + "/data/";
        File dir = new File(basePath);
        if (!dir.exists()) dir.mkdirs();

        Manager.createSessionFactory();

        // ───────────────────────────────────────────────────────────────
        // CRUD - CREATE: Creació d'entitats a la BBDD
        // ───────────────────────────────────────────────────────────────
        
        Cart refCart1 = Manager.addCart("Cart 1");
        Cart refCart2 = Manager.addCart("Cart 2");
        Cart refCart3 = Manager.addCart("Cart 3");

        Item refItem1 = Manager.addItem("Item 1");
        Item refItem2 = Manager.addItem("Item 2");
        Manager.addItem("Item 3");
        Item refItem4 = Manager.addItem("Item 4");
        Item refItem5 = Manager.addItem("Item 5");
        Item refItem6 = Manager.addItem("Item 6");

        printState("1. CREACIÓ", "Després de la creació inicial");

        // ───────────────────────────────────────────────────────────────
        // CRUD - UPDATE: Assignació d'Items a Carts (establir relacions)
        // ───────────────────────────────────────────────────────────────

        Set<Item> itemsCart1 = new HashSet<>();
        itemsCart1.add(refItem1);
        itemsCart1.add(refItem2);
        Manager.updateCart(refCart1.getCartId(), refCart1.getType(), itemsCart1);

        Set<Item> itemsCart2 = new HashSet<>();
        itemsCart2.add(refItem4);
        itemsCart2.add(refItem5);
        Manager.updateCart(refCart2.getCartId(), refCart2.getType(), itemsCart2);

        printState("2. ASSIGNACIÓ D'ITEMS", "Després d'actualitzar relacions");

        // ───────────────────────────────────────────────────────────────
        // CRUD - UPDATE: Modificació de noms d'entitats existents
        // ───────────────────────────────────────────────────────────────

        Manager.updateItem(refItem1.getItemId(), "Item 1 ACTUALITZAT");
        Manager.updateCart(refCart1.getCartId(), "Cart 1 ACTUALITZAT", itemsCart1);

        printState("3. ACTUALITZACIÓ DE CAMPS", "Després d'actualitzar noms");

        // ───────────────────────────────────────────────────────────────
        // CRUD - DELETE: Eliminació d'entitats
        // ───────────────────────────────────────────────────────────────

        Manager.delete(Cart.class, refCart3.getCartId());
        Manager.delete(Item.class, refItem6.getItemId());

        printState("4. ESBORRAT", "Després d'esborrar");

        // ───────────────────────────────────────────────────────────────
        // CRUD - READ: Recuperació amb LAZY LOADING
        // ───────────────────────────────────────────────────────────────
        
        System.out.println("--- 5. RECUPERACIÓ EAGER ---");
        
        // getCartWithItems: Carrega el Cart I els seus Items dins la mateixa sessió
        // (necessari perquè items és LAZY i fora de sessió donaria LazyInitializationException)
        Cart cart = Manager.getCartWithItems(refCart1.getCartId());
        
        if (cart != null) {
            System.out.println("Items del carret '" + cart.getType() + "':");
            if (cart.getItems() != null) {
                // STREAM API: Ordenem i iterem la col·lecció de forma funcional
                cart.getItems().stream()
                    .sorted(Comparator.comparing(Item::getItemId))
                    .forEach(item -> System.out.println("- " + item.getName()));
            }
        }

        // Tanca el SessionFactory i allibera recursos
        Manager.close();
    }

    /**
     * Mètode auxiliar per mostrar l'estat actual de la BBDD.
     * @param title Títol de la secció (ex: "1. CREACIÓ")
     * @param subtitle Descripció (ex: "Després de la creació inicial")
     */
    private static void printState(String title, String subtitle) {
        System.out.println("--- " + title + " ---");
        System.out.println("[" + subtitle + "]");

        // ─── CARTS ───
        System.out.println("CARTS:");
        List<Cart> carts = Manager.findAllCartsWithItems();
        carts.sort(Comparator.comparing(Cart::getCartId));

        for (Cart c : carts) {
            // Construïm la llista d'items: [Item A, Item B] o []
            String itemsStr = "[]";
            if (c.getItems() != null && !c.getItems().isEmpty()) {
                // STREAM PIPELINE: Ordena per ID i uneix noms amb ", "
                String joinedNames = c.getItems().stream()
                    .sorted(Comparator.comparing(Item::getItemId))
                    .map(Item::getName)
                    .collect(Collectors.joining(", "));
                itemsStr = "[" + joinedNames + "]";
            }
            // Format: Cart [ID=X, Type=Y, Items: [...]]
            System.out.println("Cart [ID=" + c.getCartId() + 
                             ", Type=" + c.getType() + 
                             ", Items: " + itemsStr + "]");
        }

        // ─── ITEMS ───
        System.out.println("ITEMS:");
        List<Item> items = Manager.findAll(Item.class);
        items.sort(Comparator.comparing(Item::getItemId));
        
        for (Item item : items) {
            // Format: Item [ID=X, Name=Y]
            System.out.println("Item [ID=" + item.getItemId() + 
                             ", Name=" + item.getName() + "]");
        }
        
        // Separador visual entre seccions
        System.out.println("------------------------------");
    }
}
