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

/**
 * Classe MANAGER: Patró DAO (Data Access Object)
 * Centralitza totes les operacions amb la base de dades.
 * Gestiona el cicle de vida de les sessions Hibernate.
 */
public class Manager {

    // SESSIONFACTORY: Objecte pesat que es crea UNA SOLA VEGADA.
    // Gestiona la connexió amb la BBDD i la creació de sessions.
    private static SessionFactory factory;

    // ═══════════════════════════════════════════════════════════════════
    // INICIALITZACIÓ DE HIBERNATE
    // ═══════════════════════════════════════════════════════════════════

    public static void createSessionFactory() {
        createSessionFactory("hibernate.properties");
    }

    public static void createSessionFactory(String propertiesFileName) {
        try {
            // CONFIGURATION: Configura Hibernate programàticament
            Configuration configuration = new Configuration();
            
            // Registrem les classes @Entity que Hibernate ha de gestionar
            configuration.addAnnotatedClass(Cart.class);
            configuration.addAnnotatedClass(Item.class);

            // Carreguem les propietats des del fitxer (URL BBDD, usuari, contrasenya...)
            Properties properties = new Properties();
            try (InputStream input = Manager.class.getClassLoader().getResourceAsStream(propertiesFileName)) {
                if (input == null) {
                    throw new IOException("No s'ha pogut trobar " + propertiesFileName);
                }
                properties.load(input);
            }
            configuration.addProperties(properties);
            
            // SERVICE REGISTRY: Gestiona els serveis interns d'Hibernate
            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();
                
            // Construïm el SessionFactory (operació costosa, només es fa un cop)
            factory = configuration.buildSessionFactory(serviceRegistry);
            
        } catch (Throwable ex) { 
            System.err.println("Error en crear sessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex); 
        }
    }

    public static void close() {
        if (factory != null) factory.close();
    }

    // ═══════════════════════════════════════════════════════════════════
    // CRUD - CREATE (Creació d'entitats)
    // ═══════════════════════════════════════════════════════════════════
  
    public static Cart addCart(String type) {
        Transaction tx = null;
        // TRY-WITH-RESOURCES: Tanca la Session automàticament al acabar
        // (Session implementa AutoCloseable)
        try (Session session = factory.openSession()) {
            // TRANSACTION: Agrupa operacions. Si falla alguna, es pot fer rollback.
            tx = session.beginTransaction();
            Cart cart = new Cart(type);
            // PERSIST: Guarda l'objecte a la BBDD i li assigna un ID
            session.persist(cart);
            // COMMIT: Confirma els canvis a la BBDD
            tx.commit();
            return cart;
        } catch (Exception e) {
            // ROLLBACK: Desfà tots els canvis si hi ha error
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("Error creant Cart: " + e.getMessage());
            e.printStackTrace(); 
            return null;
        }
    }

    public static Item addItem(String name) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            Item item = new Item(name);
            session.persist(item);
            tx.commit();
            return item;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("Error creant Item: " + e.getMessage());
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // CRUD - UPDATE (Actualització d'entitats)
    // ═══════════════════════════════════════════════════════════════════

    public static void updateItem(Long itemId, String name) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            // GET: Recupera l'entitat per ID. Retorna null si no existeix.
            Item item = session.get(Item.class, itemId); 
            if (item != null) {
                item.setName(name);
                // MERGE: Sincronitza l'estat de l'objecte amb la BBDD
                session.merge(item);
                tx.commit();
                System.out.println("Item " + itemId + " actualitzat.");
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
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
                // PAS 1: Eliminar items que ja no estan a la nova llista
                // Còpia per evitar ConcurrentModificationException mentre iterem i modifiquem
                Set<Item> currentItems = new HashSet<>(cart.getItems());
                for (Item dbItem : currentItems) {
                    if (!items.contains(dbItem)) {
                        cart.removeItem(dbItem);
                    }
                }

                // PAS 2: Afegir o actualitzar items de la nova llista
                for (Item itemInput : items) {
                    if (itemInput.getItemId() != null) {
                        // FIND: Recupera l'entitat "managed" (gestionada per la sessió)
                        // Evita errors de "detached entity" quan l'objecte ve de fora la sessió
                        Item managedItem = session.find(Item.class, itemInput.getItemId());
                        if (managedItem != null && !cart.getItems().contains(managedItem)) {
                            cart.addItem(managedItem);
                        }
                    } else {
                        // Item nou sense ID: s'afegeix i es persistirà per CASCADE
                        cart.addItem(itemInput);
                    }
                }
            } else {
                // Si items és null, eliminem tots els items del cart
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

    // ═══════════════════════════════════════════════════════════════════
    // CRUD - READ (Lectura d'entitats)
    // ═══════════════════════════════════════════════════════════════════
        
    public static Cart getCartWithItems(Long cartId) {
        try (Session session = factory.openSession()) {
            // JOIN FETCH: Soluciona el problema de LAZY LOADING.
            // Carrega Cart + Items en UNA SOLA consulta SQL.
            // Sense això, accedir a getItems() fora de la sessió llançaria
            // LazyInitializationException.
            String hql = "SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.cartId = :id";
            return session.createQuery(hql, Cart.class)
                          .setParameter("id", cartId)
                          .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // MÈTODE GENÈRIC: Funciona amb qualsevol classe Entity gràcies a <T>
    public static <T> List<T> findAll(Class<T> clazz) {
        try (Session session = factory.openSession()) {
            // HQL (Hibernate Query Language): Similar a SQL però usa noms de classes Java
            return session.createQuery("FROM " + clazz.getName(), clazz).list();
        }
    }

    public static List<Cart> findAllCartsWithItems() {
        try (Session session = factory.openSession()) {
            // DISTINCT: Evita duplicats del Cart pare quan té múltiples Items
            // (el JOIN multiplica files: 1 Cart amb 3 Items = 3 files)
            return session.createQuery(
                "SELECT DISTINCT c FROM Cart c LEFT JOIN FETCH c.items", 
                Cart.class
            ).list();
        }
    }    

    // ═══════════════════════════════════════════════════════════════════
    // CRUD - DELETE (Eliminació d'entitats)
    // ═══════════════════════════════════════════════════════════════════

    // MÈTODE GENÈRIC amb Serializable: Funciona amb Long, Integer, String com a ID
    public static <T> void delete(Class<T> clazz, Serializable id) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            T obj = session.get(clazz, id);
            if (obj != null) {
                // REMOVE: Elimina l'entitat de la BBDD
                session.remove(obj);
                tx.commit();
                System.out.println("Eliminat objecte " + clazz.getSimpleName() + " amb id " + id);
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // UTILITATS
    // ═══════════════════════════════════════════════════════════════════

    public static <T> String collectionToString(Collection<T> collection) {
        StringBuilder sb = new StringBuilder();
        for (T obj : collection) {
            sb.append(obj.toString()).append("\n");
        }
        return sb.toString();
    }
}