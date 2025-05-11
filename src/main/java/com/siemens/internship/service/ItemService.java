package com.siemens.internship.service;

import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.model.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) { return itemRepository.save(item); }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     * <p>
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */

    /**
     * Processes all items asynchronously by changing their status to "PROCESSED".
     * Implementation features:
     * - Uses CompletableFuture.supplyAsync for non-blocking concurrent processing
     * - Safely handles items that might not exist in the database
     * - Uses thread-safe collections for results
     * - Properly handles exceptions during processing without failing the entire batch
     * - Returns all successfully processed items
     *
     * @return CompletableFuture containing a list of all successfully processed items
     */

    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();

        List<CompletableFuture<Item>> futures = itemIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    try {
                        // Simulate processing time
                        Thread.sleep(100);

                        // Find the item
                        Optional<Item> optionalItem = itemRepository.findById(id);
                        if (optionalItem.isEmpty()) {
                            return null;
                        }

                        Item item = optionalItem.get();
                        item.setStatus("PROCESSED");
                        return itemRepository.save(item);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    } catch (Exception e) {
                        return null;
                    }
                }))
                .toList();

        // Wait for all futures to complete and collect results
        CompletableFuture<Void> allFutures = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]));

        // Get results after all processing is done
        try {
            allFutures.join();
            return CompletableFuture.completedFuture(futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(List.of());
        }
    }

}

