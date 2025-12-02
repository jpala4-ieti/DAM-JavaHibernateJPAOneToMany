package com.project;

import org.junit.jupiter.api.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUNIT 5 - Framework de testing per Java
 * 
 * Estructura d'un test:
 * 1. ARRANGE (Preparar) - Crear objectes necessaris
 * 2. ACT (Actuar) - Executar l'acció a testejar
 * 3. ASSERT (Afirmar) - Verificar el resultat esperat
 */

// @TestMethodOrder: Controla l'ordre d'execució dels tests.
// OrderAnnotation: Els tests s'executen segons el valor de @Order(n)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CartItemTest {
    
    // Variables ESTÀTIQUES: Compartides entre tots els tests.
    // Necessari perquè cada @Test s'executa en una instància nova de la classe.
    private static Cart testCart;
    private static Item testItem1;
    private static Item testItem2;
    
    // ═══════════════════════════════════════════════════════════════════
    // SETUP i CLEANUP - Cicle de vida dels tests
    // ═══════════════════════════════════════════════════════════════════
    
    // @BeforeAll: S'executa UNA SOLA VEGADA abans de tots els tests.
    // Ha de ser static perquè s'executa abans de crear instàncies de la classe.
    @BeforeAll
    public static void setup() {
        Manager.createSessionFactory();
    }
    
    // @AfterAll: S'executa UNA SOLA VEGADA després de tots els tests.
    // Allibera recursos (connexions BBDD, fitxers, etc.)
    @AfterAll
    public static void cleanup() {
        Manager.close();
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // TESTS CRUD - CREATE
    // ═══════════════════════════════════════════════════════════════════
    
    @Test  // Marca el mètode com un test executable
    @Order(1)  // Primer test a executar
    public void testCreateCart() {
        // ARRANGE & ACT: Creem un cart
        testCart = Manager.addCart("Carret de Prova");
        
        // ASSERT: Verifiquem el resultat
        // assertNotNull: Comprova que l'objecte NO és null
        assertNotNull(testCart, "El carret no hauria de ser null després de crear-lo");
        
        // assertTrue: Comprova que la condició és TRUE
        assertTrue(testCart.getCartId() > 0, "El carret hauria de tenir un ID vàlid després de crear-lo");
        
        // assertEquals: Comprova que dos valors són IGUALS
        assertEquals("Carret de Prova", testCart.getType(), "El tipus de carret hauria de coincidir");
        
        // isEmpty(): El cart nou no té items
        assertTrue(testCart.getItems().isEmpty(), "El nou carret hauria de tenir el conjunt d'items buit");
    }
    
    @Test
    @Order(2)
    public void testCreateItems() {
        // ARRANGE & ACT
        testItem1 = Manager.addItem("Item de Prova 1");
        testItem2 = Manager.addItem("Item de Prova 2");
        
        // ASSERT: Verifiquem que s'han creat correctament
        assertNotNull(testItem1, "L'item 1 no hauria de ser null després de crear-lo");
        assertNotNull(testItem2, "L'item 2 no hauria de ser null després de crear-lo");
        assertTrue(testItem1.getItemId() > 0, "L'item 1 hauria de tenir un ID vàlid");
        assertTrue(testItem2.getItemId() > 0, "L'item 2 hauria de tenir un ID vàlid");
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // TESTS CRUD - UPDATE (Relacions)
    // ═══════════════════════════════════════════════════════════════════
    
    @Test
    @Order(3)
    public void testAddItemsToCart() {
        // ARRANGE: Preparem el Set d'items
        Set<Item> items = new HashSet<>();
        items.add(testItem1);
        items.add(testItem2);
        
        // ACT: Actualitzem el cart amb els items
        Manager.updateCart(testCart.getCartId(), testCart.getType(), items);
        
        // IMPORTANT: Recuperem el cart FRESC de la BBDD amb JOIN FETCH
        // (les variables locals poden estar desactualitzades - "stale")
        Cart updatedCart = Manager.getCartWithItems(testCart.getCartId());
        
        // ASSERT
        assertNotNull(updatedCart, "El carret actualitzat no hauria de ser null");
        assertEquals(2, updatedCart.getItems().size(), "El carret hauria de tenir 2 items");
        
        // contains() funciona gràcies a equals() basat en UUID
        assertTrue(updatedCart.getItems().contains(testItem1), "El carret hauria de contenir l'item 1");
        assertTrue(updatedCart.getItems().contains(testItem2), "El carret hauria de contenir l'item 2");
    }
    
    @Test
    @Order(4)
    public void testUpdateItem() {
        // ARRANGE
        String newName = "Item Actualitzat 1";
        
        // ACT
        Manager.updateItem(testItem1.getItemId(), newName);
        
        // ASSERT: Recuperem l'item fresc de la BBDD
        Item updatedItem = findItemById(testItem1.getItemId());
        assertNotNull(updatedItem, "L'item no hauria de ser null");
        assertEquals(newName, updatedItem.getName(), "El nom de l'item hauria d'estar actualitzat");
    }
    
    @Test
    @Order(5)
    public void testUpdateCartType() {
        // ARRANGE
        String newType = "Carret Actualitzat";
        Cart cart = Manager.getCartWithItems(testCart.getCartId());
        Set<Item> currentItems = cart.getItems();
        
        // ACT: Actualitzem el tipus mantenint els items
        Manager.updateCart(testCart.getCartId(), newType, currentItems);
        
        // ASSERT
        Cart updatedCart = Manager.getCartWithItems(testCart.getCartId());
        assertEquals(newType, updatedCart.getType(), "El tipus del carret hauria d'estar actualitzat");
        assertEquals(2, updatedCart.getItems().size(), "Els items s'haurien de mantenir");
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // TESTS CRUD - READ
    // ═══════════════════════════════════════════════════════════════════
    
    @Test
    @Order(6)
    public void testListItems() {
        // ACT: Llistem tots els items
        Collection<?> items = Manager.findAll(Item.class);
        
        // ASSERT
        assertNotNull(items, "La col·lecció d'items no hauria de ser null");
        assertTrue(items.size() >= 2, "Hauria d'haver-hi almenys 2 items");
    }
    
    @Test
    @Order(7)
    public void testGetCartWithItems() {
        // ACT: Recuperem cart amb JOIN FETCH
        Cart cart = Manager.getCartWithItems(testCart.getCartId());
        
        // ASSERT: Podem accedir als items FORA de la sessió gràcies a JOIN FETCH
        assertNotNull(cart, "El carret no hauria de ser null");
        assertNotNull(cart.getItems(), "Els items no haurien de ser null");
        
        // assertFalse: Comprova que la condició és FALSE
        assertFalse(cart.getItems().isEmpty(), "El carret hauria de tenir items");
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // TESTS - GESTIÓ DE RELACIONS
    // ═══════════════════════════════════════════════════════════════════
    
    @Test
    @Order(8)
    public void testRemoveItemFromCart() {
        // ARRANGE: Obtenim el cart actual
        Cart cart = Manager.getCartWithItems(testCart.getCartId());
        Set<Item> items = new HashSet<>(cart.getItems());
        
        // ACT: Eliminem un item del Set
        items.remove(testItem1);
        Manager.updateCart(cart.getCartId(), cart.getType(), items);
        
        // ASSERT
        Cart updatedCart = Manager.getCartWithItems(testCart.getCartId());
        assertEquals(1, updatedCart.getItems().size(), "El carret hauria de tenir 1 item");
        
        // assertFalse: L'item eliminat NO hauria d'estar al cart
        assertFalse(updatedCart.getItems().contains(testItem1), "El carret no hauria de contenir l'item eliminat");
        assertTrue(updatedCart.getItems().contains(testItem2), "El carret hauria de contenir l'item restant");
    }
    
    @Test
    @Order(9)
    public void testOrphanItemStillExists() {
        // TEST IMPORTANT: Sense orphanRemoval, l'item tret del cart NO s'elimina de la BBDD
        
        // ACT: Busquem l'item que hem tret del cart al test anterior
        Item orphanItem = findItemById(testItem1.getItemId());
        
        // ASSERT: L'item encara existeix a la BBDD (només s'ha desvinculat del cart)
        assertNotNull(orphanItem, "L'item orfe hauria de continuar existint a la BBDD");
        
        // Opcional: Verificar que no té cart assignat
        // Nota: Això requereix que findItemById retorni l'item amb el cart carregat
    }
    
    @Test
    @Order(10)
    public void testMoveItemBetweenCarts() {
        // TEST IMPORTANT: Moure un item d'un cart a un altre
        
        // ARRANGE: Creem un segon cart
        Cart cart2 = Manager.addCart("Carret 2");
        
        // ACT: Afegim l'item orfe (testItem1) al nou cart
        Set<Item> itemsCart2 = new HashSet<>();
        itemsCart2.add(testItem1);
        Manager.updateCart(cart2.getCartId(), cart2.getType(), itemsCart2);
        
        // ASSERT
        Cart updatedCart2 = Manager.getCartWithItems(cart2.getCartId());
        assertTrue(updatedCart2.getItems().contains(testItem1), "L'item s'hauria d'haver mogut al cart 2");
        assertEquals(1, updatedCart2.getItems().size(), "El cart 2 hauria de tenir 1 item");
        
        // Cleanup: Eliminem el cart2 creat per aquest test
        Set<Item> emptySet = new HashSet<>();
        Manager.updateCart(cart2.getCartId(), cart2.getType(), emptySet);
        Manager.delete(Cart.class, cart2.getCartId());
    }
    
    @Test
    @Order(11)
    public void testEmptyCart() {
        // TEST: Buidar completament un cart
        
        // ARRANGE
        Cart cart = Manager.getCartWithItems(testCart.getCartId());
        
        // ACT: Passem un Set buit
        Set<Item> emptyItems = new HashSet<>();
        Manager.updateCart(cart.getCartId(), cart.getType(), emptyItems);
        
        // ASSERT
        Cart updatedCart = Manager.getCartWithItems(testCart.getCartId());
        assertTrue(updatedCart.getItems().isEmpty(), "El carret hauria d'estar buit");
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // TESTS CRUD - DELETE
    // ═══════════════════════════════════════════════════════════════════
    
    @Test
    @Order(12)
    public void testDeleteItems() {
        // ACT: Eliminem els items
        Manager.delete(Item.class, testItem1.getItemId());
        Manager.delete(Item.class, testItem2.getItemId());
        
        // ASSERT: assertNull comprova que l'objecte ÉS null
        assertNull(findItemById(testItem1.getItemId()), "L'item 1 hauria d'estar eliminat");
        assertNull(findItemById(testItem2.getItemId()), "L'item 2 hauria d'estar eliminat");
    }
    
    @Test
    @Order(13)
    public void testDeleteCart() {
        // ACT
        Manager.delete(Cart.class, testCart.getCartId());
        
        // ASSERT
        assertNull(findCartById(testCart.getCartId()), "El carret hauria d'estar eliminat");
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // TESTS ADDICIONALS - CASOS LÍMIT (Edge Cases)
    // ═══════════════════════════════════════════════════════════════════
    
    @Test
    @Order(14)
    public void testCreateCartWithEmptyType() {
        // TEST: Crear cart amb tipus buit
        Cart cart = Manager.addCart("");
        
        assertNotNull(cart, "Es pot crear un cart amb tipus buit");
        assertEquals("", cart.getType());
        
        // Cleanup
        Manager.delete(Cart.class, cart.getCartId());
    }
    
    @Test
    @Order(15)
    public void testUpdateNonExistentCart() {
        // TEST: Intentar actualitzar un cart que no existeix
        Long fakeId = 99999L;
        Set<Item> items = new HashSet<>();
        
        // ACT & ASSERT: No hauria de llançar excepció, simplement no fa res
        // (segons la implementació actual del Manager)
        assertDoesNotThrow(() -> {
            Manager.updateCart(fakeId, "Fake", items);
        }, "Actualitzar cart inexistent no hauria de llançar excepció");
    }
    
    @Test
    @Order(16)
    public void testDeleteNonExistentItem() {
        // TEST: Intentar eliminar un item que no existeix
        Long fakeId = 99999L;
        
        // assertDoesNotThrow: Verifica que NO es llança cap excepció
        assertDoesNotThrow(() -> {
            Manager.delete(Item.class, fakeId);
        }, "Eliminar item inexistent no hauria de llançar excepció");
    }

    // ═══════════════════════════════════════════════════════════════════
    // MÈTODES HELPER - Utilitats pels tests
    // ═══════════════════════════════════════════════════════════════════
    
    /**
     * Cerca un Item per ID usant Stream API.
     * Alternativa a un hipotètic Manager.getById()
     */
    private Item findItemById(Long id) {
        List<Item> items = Manager.findAll(Item.class);
        return items.stream()
                .filter(i -> i.getItemId().equals(id))  // Filtra per ID
                .findFirst()                              // Agafa el primer (o cap)
                .orElse(null);                            // Retorna null si no troba
    }

    /**
     * Cerca un Cart per ID usant Stream API.
     */
    private Cart findCartById(Long id) {
        List<Cart> carts = Manager.findAll(Cart.class);
        return carts.stream()
                .filter(c -> c.getCartId().equals(id))
                .findFirst()
                .orElse(null);
    }
}