package com.project;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        String basePath = System.getProperty("user.dir") + "/data/";
        File dir = new File(basePath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.out.println("Error creating 'data' folder");
            }
        }

        Manager.createSessionFactory();

        Cart refCart1 = Manager.addCart("Cart 1");
        Cart refCart2 = Manager.addCart("Cart 2");
        Cart refCart3 = Manager.addCart("Cart 3");

        Item refItem1 = Manager.addItem("Item 1");
        Item refItem2 = Manager.addItem("Item 2");
        Item refItem3 = Manager.addItem("Item 3");
        Item refItem4 = Manager.addItem("Item 4");
        Item refItem5 = Manager.addItem("Item 5");
        Item refItem6 = Manager.addItem("Item 6");

        System.out.println("Punt 1: Després de la creació inicial d'elements");
        System.out.println(Manager.collectionToString(Cart.class, Manager.listCollection(Cart.class, "")));
        System.out.println(Manager.collectionToString(Item.class, Manager.listCollection(Item.class, "")));

        Set<Item> itemsCard1 = new HashSet<Item>();
        itemsCard1.add(refItem1);
        itemsCard1.add(refItem2);
        itemsCard1.add(refItem3);

        Manager.updateCart(refCart1.getCartId(), refCart1.getType(), itemsCard1);

        Set<Item> itemsCard2 = new HashSet<Item>();
        itemsCard2.add(refItem4);
        itemsCard2.add(refItem5);
        
        Manager.updateCart(refCart2.getCartId(), refCart2.getType(), itemsCard2);

        System.out.println("Punt 2: Després d'actualitzar carrets");
        System.out.println(Manager.collectionToString(Cart.class, Manager.listCollection(Cart.class, "")));
        System.out.println(Manager.collectionToString(Item.class, Manager.listCollection(Item.class, "")));

        // Actualització del nom del carret
        Manager.updateCart(refCart1.getCartId(), "Cart 1 actualitzat", itemsCard1);
        Manager.updateCart(refCart2.getCartId(), "Cart 2 actualitzat", itemsCard2);

        // Actualització del nom dels articles
        Manager.updateItem(refItem1.getItemId(), "Item 1 actualitzat");
        Manager.updateItem(refItem4.getItemId(), "Item 4 actualitzat");

        System.out.println("Punt 3: Després d'actualització de noms");
        System.out.println(Manager.collectionToString(Cart.class, Manager.listCollection(Cart.class, "")));
        System.out.println(Manager.collectionToString(Item.class, Manager.listCollection(Item.class, "")));

        Manager.delete(Cart.class, refCart3.getCartId());
        Manager.delete(Item.class, refItem6.getItemId());

        System.out.println("Punt 4: després d'esborrat");
        System.out.println(Manager.collectionToString(Cart.class, Manager.listCollection(Cart.class, "")));
        System.out.println(Manager.collectionToString(Item.class, Manager.listCollection(Item.class, "")));

        // Recuperar el carret per la seva ID
        long cartId = refCart1.getCartId(); // Canvia aquest valor per l'ID del carret que vols recuperar
        Cart cart = Manager.getCartWithItems(cartId);
        Set<Item> items = cart.getItems();
        for (Item item : items) {
            System.out.println("- " + item.getName());
        }

        Manager.close();
    }
}