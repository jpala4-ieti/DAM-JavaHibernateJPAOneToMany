package com.project;

import org.junit.jupiter.api.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CartItemTest {
    
    private static Cart testCart;
    private static Item testItem1;
    private static Item testItem2;
    
    @BeforeAll
    public static void setup() {
        // Inicialitzar Hibernate
        Manager.createSessionFactory();
    }
    
    @AfterAll
    public static void cleanup() {
        // Tancar la sessió de Hibernate
        Manager.close();
    }
    
    @Test
    @Order(1)
    public void testCreateCart() {
        // Provar la creació d'un nou carret
        testCart = Manager.addCart("Carret de Prova");
        assertNotNull(testCart, "El carret no hauria de ser null després de crear-lo");
        assertTrue(testCart.getCartId() > 0, "El carret hauria de tenir un ID vàlid després de crear-lo");
        assertEquals("Carret de Prova", testCart.getType(), "El tipus de carret hauria de coincidir amb l'entrada");
        assertTrue(testCart.getItems().isEmpty(), "El nou carret hauria de tenir el conjunt d'items buit");
    }
    
    @Test
    @Order(2)
    public void testCreateItems() {
        // Provar la creació de nous items
        testItem1 = Manager.addItem("Item de Prova 1");
        testItem2 = Manager.addItem("Item de Prova 2");
        
        assertNotNull(testItem1, "L'item 1 no hauria de ser null després de crear-lo");
        assertNotNull(testItem2, "L'item 2 no hauria de ser null després de crear-lo");
        assertTrue(testItem1.getItemId() > 0, "L'item 1 hauria de tenir un ID vàlid");
        assertTrue(testItem2.getItemId() > 0, "L'item 2 hauria de tenir un ID vàlid");
    }
    
    @Test
    @Order(3)
    public void testAddItemsToCart() {
        // Crear un conjunt d'items
        Set<Item> items = new HashSet<>();
        items.add(testItem1);
        items.add(testItem2);
        
        // Actualitzar el carret amb els nous items
        Manager.updateCart(testCart.getCartId(), testCart.getType(), items);
        
        // Obtenir el carret actualitzat de la base de dades
        Cart updatedCart = Manager.getCartWithItems(testCart.getCartId());
        
        assertNotNull(updatedCart, "El carret actualitzat no hauria de ser null");
        assertEquals(2, updatedCart.getItems().size(), "El carret hauria de tenir 2 items");
        assertTrue(updatedCart.getItems().contains(testItem1), "El carret hauria de contenir l'item 1");
        assertTrue(updatedCart.getItems().contains(testItem2), "El carret hauria de contenir l'item 2");
    }
    
    @Test
    @Order(4)
    public void testUpdateItem() {
        // Actualitzar el nom de l'item
        String newName = "Item Actualitzat 1";
        Manager.updateItem(testItem1.getItemId(), newName);
        
        // Obtenir l'item actualitzat
        Item updatedItem = Manager.getById(Item.class, testItem1.getItemId());
        assertEquals(newName, updatedItem.getName(), "El nom de l'item hauria d'estar actualitzat");
    }
    
    @Test
    @Order(5)
    public void testListItems() {
        // Provar llistar tots els items
        Collection<?> items = Manager.listCollection(Item.class);
        assertNotNull(items, "La col·lecció d'items no hauria de ser null");
        assertTrue(items.size() >= 2, "Hauria d'haver-hi almenys 2 items");
    }
    
    @Test
    @Order(6)
    public void testRemoveItemFromCart() {
        // Obtenir carret amb items
        Cart cart = Manager.getCartWithItems(testCart.getCartId());
        Set<Item> items = new HashSet<>(cart.getItems());
        
        // Eliminar un item
        items.remove(testItem1);
        Manager.updateCart(cart.getCartId(), cart.getType(), items);
        
        // Verificar l'actualització
        Cart updatedCart = Manager.getCartWithItems(testCart.getCartId());
        assertEquals(1, updatedCart.getItems().size(), "El carret hauria de tenir 1 item després de l'eliminació");
        assertFalse(updatedCart.getItems().contains(testItem1), "El carret no hauria de contenir l'item eliminat");
        assertTrue(updatedCart.getItems().contains(testItem2), "El carret encara hauria de contenir l'item restant");
    }
    
    @Test
    @Order(7)
    public void testDeleteItems() {
        // Eliminar items
        Manager.delete(Item.class, testItem1.getItemId());
        Manager.delete(Item.class, testItem2.getItemId());
        
        // Verificar l'eliminació
        assertNull(Manager.getById(Item.class, testItem1.getItemId()), "L'item 1 hauria d'estar eliminat");
        assertNull(Manager.getById(Item.class, testItem2.getItemId()), "L'item 2 hauria d'estar eliminat");
    }
    
    @Test
    @Order(8)
    public void testDeleteCart() {
        // Eliminar carret
        Manager.delete(Cart.class, testCart.getCartId());
        
        // Verificar l'eliminació
        assertNull(Manager.getById(Cart.class, testCart.getCartId()), "El carret hauria d'estar eliminat");
    }
}