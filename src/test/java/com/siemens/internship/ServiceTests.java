package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ServiceTests {

    @InjectMocks
    ItemService itemService;

    @Mock
    ItemRepository itemRepository;

    @Test
    void testSaveItem(){
        Item item = new Item();
        item.setId(1L);
        item.setName("test");
        item.setDescription("test description");
        item.setStatus("PENDING");
        item.setEmail("test@test.com");

        Mockito.when(itemRepository.save(item)).thenReturn(item);

        Item savedItem = itemService.save(item);

        assertNotNull(savedItem);
        assertEquals(item.getId(), savedItem.getId());
        assertEquals(item.getName(), savedItem.getName());
        assertEquals(item.getDescription(), savedItem.getDescription());
        assertEquals(item.getStatus(), savedItem.getStatus());
        assertEquals(item.getEmail(), savedItem.getEmail());

        Mockito.verify(itemRepository).save(item);
    }

    @Test
    void testInvalidEmail() {
        Item item = new Item();
        item.setId(1L);
        item.setName("test");
        item.setDescription("test description");
        item.setStatus("PENDING");
        item.setEmail("invalid-email");

        Mockito.when(itemRepository.save(item)).thenThrow(new IllegalArgumentException("Invalid email format"));

        try {
            itemService.save(item);
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid email format", e.getMessage());
        }

        Mockito.verify(itemRepository).save(item);
    }

    @Test
    void testFindItemById() {
        Item item = new Item();
        item.setId(1L);
        item.setName("test");
        item.setDescription("test description");
        item.setStatus("PENDING");
        item.setEmail("test@test.com");

        Mockito.when(itemRepository.findById(1L)).thenReturn(java.util.Optional.of(item));

        Optional<Item> foundItem = itemService.findById(1L);

        assertNotNull(foundItem);
        assertEquals(item.getId(), foundItem.get().getId());
        assertEquals(item.getName(), foundItem.get().getName());
        assertEquals(item.getDescription(), foundItem.get().getDescription());
        assertEquals(item.getStatus(), foundItem.get().getStatus());
        assertEquals(item.getEmail(), foundItem.get().getEmail());

        Mockito.verify(itemRepository).findById(1L);
    }

    @Test
    void testFindAllItems() {
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("test1");
        item1.setDescription("test description 1");
        item1.setStatus("PENDING");
        item1.setEmail("test1@test.com");

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("test2");
        item2.setDescription("test description 2");
        item2.setStatus("PENDING");
        item2.setEmail("test2@test.com");

        List<Item> items = List.of(item1, item2);

        Mockito.when(itemRepository.findAll()).thenReturn(items);

        List<Item> foundItems = itemService.findAll();

        assertNotNull(foundItems);
        assertThat(foundItems).isSameAs(items);
        Mockito.verify(itemRepository).findAll();

    }

    @Test
    void testDeleteItemById() {
        Mockito.doNothing().when(itemRepository).deleteById(1L);
        itemService.deleteById(1L);
        Mockito.verify(itemRepository).deleteById(1L);
    }

    @Test
    void testProcessItemsAsync() throws ExecutionException, InterruptedException {
        List<Long> itemIds = List.of(1L, 2L);
        Mockito.when(itemRepository.findAllIds()).thenReturn(itemIds);

        Item item1 = new Item(1L, "Item 1", "Description 1", "PENDING", "test1@example.com");
        Item item2 = new Item(2L, "Item 2", "Description 2", "PENDING", "test2@example.com");

        Item processedItem1 = new Item(1L, "Item 1", "Description 1", "PROCESSED", "test1@example.com");
        Item processedItem2 = new Item(2L, "Item 2", "Description 2", "PROCESSED", "test2@example.com");

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        Mockito.when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));

        Mockito.when(itemRepository.save(ArgumentMatchers.any(Item.class)))
                .thenAnswer(invocation -> {
                    Item item = invocation.getArgument(0);
                    if (item.getId().equals(1L)) {
                        return processedItem1;
                    } else {
                        return processedItem2;
                    }
                });

        CompletableFuture<List<Item>> futureResult = itemService.processItemsAsync();
        List<Item> processedItems = futureResult.get();

        assertNotNull(processedItems);
        assertEquals(2, processedItems.size());
        assertEquals("PROCESSED", processedItems.get(0).getStatus());
        assertEquals("PROCESSED", processedItems.get(1).getStatus());
    }

}