package com.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.hibernate.Session; 
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Manager {

    private static SessionFactory factory;

    // Mantenim la lògica original de creació de factoria
    public static void createSessionFactory() {
        createSessionFactory("hibernate.properties");
    }

    public static void createSessionFactory(String propertiesFileName) {
        try {
            Configuration configuration = new Configuration();
            configuration.addAnnotatedClass(Cart.class);
            configuration.addAnnotatedClass(Item.class);

            Properties properties = new Properties();
            try (InputStream input = Manager.class.getClassLoader().getResourceAsStream(propertiesFileName)) {
                if (input == null) {
                    throw new IOException("No s'ha pogut trobar " + propertiesFileName);
                }
                properties.load(input);
            }
            configuration.addProperties(properties);
            
            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();
            factory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) { 
            System.err.println("Error en crear sessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex); 
        }
    }

    public static void close () {
        if (factory != null) factory.close();
    }
  
    public static Cart addCart(String type){
        Transaction tx = null;
        // Try-with-resources tanca la sessió automàticament
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            Cart cart = new Cart(type);
            session.persist(cart);
            tx.commit();
            return cart;
        } catch (Exception e) {
            if (tx!=null && tx.isActive()) tx.rollback();
            System.err.println("Error creant Cart: " + e.getMessage());
            e.printStackTrace(); 
            return null;
        }
    }

    public static Item addItem(String name){
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            Item item = new Item(name);
            session.persist(item);
            tx.commit();
            return item;
        } catch (Exception e) {
            if (tx!=null && tx.isActive()) tx.rollback();
            System.err.println("Error creant Item: " + e.getMessage());
            return null;
        }
    }

    public static void updateItem(Long itemId, String name){
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            Item item = session.get(Item.class, itemId); 
            if (item != null) {
                item.setName(name);
                session.merge(item);
                tx.commit();
                System.out.println("Item " + itemId + " actualitzat.");
            }
        } catch (Exception e) {
            if (tx!=null && tx.isActive()) tx.rollback();
            e.printStackTrace(); 
        }
    }

public static void updateCart(Long cartId, String type, Set<Item> items) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            Cart cart = session.get(Cart.class, cartId);
            
            if (cart == null) {
                System.err.println("Cart no trobat amb id: " + cartId);
                return;
            }
            
            cart.setType(type);
            
            if (items != null) {
                // 1. IDENTIFICAR I ESBORRAR: Items que estan a la BBDD però NO a la nova llista
                // Fem una còpia del set actual per evitar ConcurrentModificationException
                Set<Item> currentItems = new HashSet<>(cart.getItems());
                for (Item dbItem : currentItems) {
                    // Gràcies al equals() amb UUID, això detecta si l'item falta a la nova llista
                    if (!items.contains(dbItem)) {
                        cart.removeItem(dbItem);
                    }
                }

                // 2. AFEGIR O ACTUALITZAR: Items de la nova llista
                for (Item itemInput : items) {
                    if (itemInput.getItemId() != null) {
                        // Si l'item existeix, el recuperem de la sessió per evitar errors de "detached entity"
                        Item managedItem = session.find(Item.class, itemInput.getItemId());
                        if (managedItem != null) {
                            // Només l'afegim si no hi és ja
                            if (!cart.getItems().contains(managedItem)) {
                                cart.addItem(managedItem);
                            }
                        }
                    } else {
                        // Si és un item nou (sense ID), l'afegim directament
                        cart.addItem(itemInput);
                    }
                }
            } else {
                // Si la llista nova és null, ho esborrem tot
                new HashSet<>(cart.getItems()).forEach(cart::removeItem);
            }
            
            session.merge(cart);
            tx.commit();
            System.out.println("Cart " + cartId + " actualitzat.");
            
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
    }
        
    public static Cart getCartWithItems(Long cartId) {
        try (Session session = factory.openSession()) {
            // OPTIMITZACIÓ: Utilitzem JPQL amb JOIN FETCH per carregar els items
            // en una sola consulta, evitant el problema N+1 i respectant el FetchType.LAZY
            String hql = "SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.cartId = :id";
            return session.createQuery(hql, Cart.class)
                          .setParameter("id", cartId)
                          .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Hem eliminat el mètode listCollection insegur i l'hem substituït per findAll
    public static <T> List<T> findAll(Class<T> clazz) {
        try (Session session = factory.openSession()) {
            // Consulta segura sense concatenació
            return session.createQuery("FROM " + clazz.getName(), clazz).list();
        }
    }

    public static List<Cart> findAllCartsWithItems() {
        try (Session session = factory.openSession()) {
            // DISTINCT és important aquí perquè el JOIN FETCH pot duplicar el Cart pare per cada Item fill
            return session.createQuery("SELECT DISTINCT c FROM Cart c LEFT JOIN FETCH c.items", Cart.class).list();
        }
    }    

    public static <T> void delete(Class<T> clazz, Serializable id) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            T obj = session.get(clazz, id);
            if (obj != null) {
                session.remove(obj);
                tx.commit();
                System.out.println("Eliminat objecte " + clazz.getSimpleName() + " amb id " + id);
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
    }

    // Mètode helper per mostrar col·leccions
    public static <T> String collectionToString(Collection<T> collection){
        StringBuilder sb = new StringBuilder();
        for(T obj: collection) {
            sb.append(obj.toString()).append("\n");
        }
        return sb.toString();
    }
}