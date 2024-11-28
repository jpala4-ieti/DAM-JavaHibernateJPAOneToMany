package com.project;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Main {
   public static void main(String[] args) {
       // Creem el directori data si no existeix
       String basePath = System.getProperty("user.dir") + "/data/";
       File dir = new File(basePath);
       if (!dir.exists()) {
           if (!dir.mkdirs()) {
               System.out.println("Error creating 'data' folder");
           }
       }

       // Inicialitzem la connexió amb Hibernate
       Manager.createSessionFactory();

       // CREATE - Creem els carrets
       Cart refCart1 = Manager.addCart("Cart 1");
       Cart refCart2 = Manager.addCart("Cart 2"); 
       Cart refCart3 = Manager.addCart("Cart 3");

       // CREATE - Creem els items
       Item refItem1 = Manager.addItem("Item 1");
       Item refItem2 = Manager.addItem("Item 2");
       Item refItem3 = Manager.addItem("Item 3");
       Item refItem4 = Manager.addItem("Item 4");
       Item refItem5 = Manager.addItem("Item 5");
       Item refItem6 = Manager.addItem("Item 6");

       // READ - Mostrem tots els elements creats
       System.out.println("Punt 1: Després de la creació inicial d'elements");
       System.out.println(Manager.collectionToString(Cart.class, Manager.listCollection(Cart.class, "")));
       System.out.println(Manager.collectionToString(Item.class, Manager.listCollection(Item.class, "")));

       // Creem un set d'items pel primer carret
       Set<Item> itemsCard1 = new HashSet<Item>();
       itemsCard1.add(refItem1);
       itemsCard1.add(refItem2);
       itemsCard1.add(refItem3);

       // UPDATE - Actualitzem el primer carret amb els seus items
       Manager.updateCart(refCart1.getCartId(), refCart1.getType(), itemsCard1);

       // Creem un set d'items pel segon carret
       Set<Item> itemsCard2 = new HashSet<Item>();
       itemsCard2.add(refItem4);
       itemsCard2.add(refItem5);
       
       // UPDATE - Actualitzem el segon carret amb els seus items
       Manager.updateCart(refCart2.getCartId(), refCart2.getType(), itemsCard2);

       // Creem un set d'items nous pel primer carret
       Set<Item> itemsCard3 = new HashSet<Item>();
       itemsCard3.add(refItem1);
       itemsCard3.add(refItem2);

       Manager.updateCart(refCart1.getCartId(), refCart1.getType(), itemsCard3);

       // READ - Mostrem l'estat després d'assignar items als carrets
       System.out.println("Punt 2: Després d'actualitzar carrets");
       System.out.println(Manager.collectionToString(Cart.class, Manager.listCollection(Cart.class, "")));
       System.out.println(Manager.collectionToString(Item.class, Manager.listCollection(Item.class, "")));

       // UPDATE - Actualitzem els noms dels carrets
       Manager.updateCart(refCart1.getCartId(), "Cart 1 actualitzat", itemsCard3);
       Manager.updateCart(refCart2.getCartId(), "Cart 2 actualitzat", itemsCard2);

       // UPDATE - Actualitzem els noms dels items
       Manager.updateItem(refItem1.getItemId(), "Item 1 actualitzat");
       Manager.updateItem(refItem4.getItemId(), "Item 4 actualitzat");

       // READ - Mostrem l'estat després d'actualitzar els noms
       System.out.println("Punt 3: Després d'actualització de noms");
       System.out.println(Manager.collectionToString(Cart.class, Manager.listCollection(Cart.class, "")));
       System.out.println(Manager.collectionToString(Item.class, Manager.listCollection(Item.class, "")));

       // DELETE - Esborrem el tercer carret i el sisè item
       Manager.delete(Cart.class, refCart3.getCartId());
       Manager.delete(Item.class, refItem6.getItemId());

       // READ - Mostrem l'estat després d'esborrar elements
       System.out.println("Punt 4: després d'esborrat");
       System.out.println(Manager.collectionToString(Cart.class, Manager.listCollection(Cart.class, "")));
       System.out.println(Manager.collectionToString(Item.class, Manager.listCollection(Item.class, "")));

       // READ - Exemple de com recuperar i mostrar els items d'un carret específic
       System.out.println("Punt 5: Recuperació d'items d'un carret específic");
       Cart cart = Manager.getCartWithItems(refCart1.getCartId());
       if (cart != null) {
           System.out.println("Items del carret '" + cart.getType() + "':");
           Set<Item> items = cart.getItems();
           if (items != null && !items.isEmpty()) {
               for (Item item : items) {
                   System.out.println("- " + item.getName());
               }
           } else {
               System.out.println("El carret no té items");
           }
       } else {
           System.out.println("No s'ha trobat el carret");
       }
       
       // Tanquem la connexió amb Hibernate
       Manager.close();
   }
}