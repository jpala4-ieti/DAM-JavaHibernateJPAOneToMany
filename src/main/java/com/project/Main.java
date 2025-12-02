package com.project;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        // 0. PREPARACIÓ DE L'ENTORN
        String basePath = System.getProperty("user.dir") + "/data/";
        File dir = new File(basePath);
        if (!dir.exists()) dir.mkdirs();

        Manager.createSessionFactory();

        // ---------------------------------------------------------------
        // PUNT 1: CREACIÓ DE DADES (CREATE)
        // ---------------------------------------------------------------
        
        Cart refCart1 = Manager.addCart("Cart 1");
        Cart refCart2 = Manager.addCart("Cart 2");
        Cart refCart3 = Manager.addCart("Cart 3");

        Item refItem1 = Manager.addItem("Item 1");
        Item refItem2 = Manager.addItem("Item 2");
        Item refItem3 = Manager.addItem("Item 3");
        Item refItem4 = Manager.addItem("Item 4");
        Item refItem5 = Manager.addItem("Item 5");
        Item refItem6 = Manager.addItem("Item 6");

        printState("Punt 1: Després de la creació inicial d'elements");

        // ---------------------------------------------------------------
        // PUNT 2: ASSIGNACIÓ D'ITEMS (UPDATE)
        // ---------------------------------------------------------------

        // Cart 1: Assignem Item 1 i Item 2
        Set<Item> itemsCart1 = new HashSet<>();
        itemsCart1.add(refItem1);
        itemsCart1.add(refItem2);
        Manager.updateCart(refCart1.getCartId(), refCart1.getType(), itemsCart1);

        // Cart 2: Assignem Item 4 i Item 5
        Set<Item> itemsCart2 = new HashSet<>();
        itemsCart2.add(refItem4);
        itemsCart2.add(refItem5);
        Manager.updateCart(refCart2.getCartId(), refCart2.getType(), itemsCart2);

        printState("Punt 2: Després d'actualitzar carrets");

        // ---------------------------------------------------------------
        // PUNT 3: ACTUALITZACIONS DE NOMS
        // ---------------------------------------------------------------

        // Actualitzem noms dels Items
        Manager.updateItem(refItem1.getItemId(), "Item 1 actualitzat");
        Manager.updateItem(refItem4.getItemId(), "Item 4 actualitzat");

        // Actualitzem noms dels Carrets (hem de tornar a passar els items per no perdre la relació)
        // Nota: En un entorn real, Manager.updateCart hauria de permetre passar null als items per no tocar-los,
        // però segons el teu codi actual, cal passar el Set.
        
        // Cart 1 -> "Cart 1 actualitzat" (Manté Item 1 i 2)
        // Recarreguem els items locals amb els nous noms o simplement passem les referències
        // Hibernate ja sap que són els mateixos objectes per ID/UUID.
        Manager.updateCart(refCart1.getCartId(), "Cart 1 actualitzat", itemsCart1);
        
        // Cart 2 -> "Cart 2 actualitzat" (Manté Item 4 i 5)
        Manager.updateCart(refCart2.getCartId(), "Cart 2 actualitzat", itemsCart2);

        printState("Punt 3: Després d'actualització de noms");

        // ---------------------------------------------------------------
        // PUNT 4: ESBORRATS (DELETE)
        // ---------------------------------------------------------------

        // Esborrem Cart 3
        Manager.delete(Cart.class, refCart3.getCartId());

        // Esborrem Item 6
        Manager.delete(Item.class, refItem6.getItemId());

        // Imprimim l'estat. Nota: El punt 4 de la teva sortida no mostra el llistat complet d'items,
        // només els que estan assignats o existents. El mètode genèric mostrarà el que quedi a la BD.
        printState("Punt 4: després d'esborrat");

        // ---------------------------------------------------------------
        // PUNT 5: RECUPERACIÓ ESPECÍFICA
        // ---------------------------------------------------------------
        
        System.out.println("Punt 5: Recuperació d'items d'un carret específic");
        Cart cart = Manager.getCartWithItems(refCart1.getCartId());
        
        if (cart != null) {
            System.out.println("Items del carret '" + cart.getType() + "':");
            if (cart.getItems() != null) {
                // Ordenem per ID per garantir l'ordre de sortida (1, després 2)
                cart.getItems().stream()
                    .sorted(Comparator.comparing(Item::getItemId))
                    .forEach(item -> System.out.println("- " + item.getName()));
            }
        }

        Manager.close();
    }

    /**
     * Mètode auxiliar per imprimir l'estat exactament amb el format sol·licitat
     * Format: "ID: Nom, Items: [Item A | Item B]"
     */
    private static void printState(String header) {
        System.out.println(header);

        // 1. Imprimim Carrets amb els seus items formatats
        List<Cart> carts = Manager.findAllCartsWithItems();
        
        // Ordenem carrets per ID per garantir l'ordre 1, 2, 3...
        carts.sort(Comparator.comparing(Cart::getCartId));

        for (Cart c : carts) {
            String itemsStr = "[]";
            if (c.getItems() != null && !c.getItems().isEmpty()) {
                // Construïm l'string [Item X | Item Y]
                String joinedNames = c.getItems().stream()
                    .sorted(Comparator.comparing(Item::getItemId)) // Ordenem items per ID dins la llista
                    .map(Item::getName)
                    .collect(Collectors.joining(" | "));
                itemsStr = "[" + joinedNames + "]";
            }
            System.out.println(c.getCartId() + ": " + c.getType() + ", Items: " + itemsStr);
        }

        // 2. Imprimim Llistat d'Items a sota
        // Nota: Al Punt 4 de la teva sortida d'exemple sembla que no mostra items orfes esborrats.
        // Aquí mostrem tots els items que queden a la BBDD.
        List<Item> items = Manager.findAll(Item.class);
        items.sort(Comparator.comparing(Item::getItemId));
        
        for (Item item : items) {
            System.out.println(item.getItemId() + ": " + item.getName());
        }
    }
}