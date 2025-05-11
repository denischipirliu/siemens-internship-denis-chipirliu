package com.siemens.internship;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAllItems() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/items"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testGetItem() throws Exception {
        String itemJson = """
        {
            "name": "Test Item",
            "description": "Test Description",
            "status": "PENDING", 
            "email": "test@example.com"
        }
        """;

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/items")
                        .contentType("application/json")
                        .content(itemJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        Long id = JsonPath.parse(responseJson).read("$.id", Long.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + id))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testCreateItem() throws Exception {
        String itemJson = """
            {
                "name": "Test Item",
                "description": "Test Description",
                "status": "PENDING",
                "email": "test@example.com"
            }
            """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/items")
                .contentType("application/json")
                .content(itemJson))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void testUpdateItem() throws Exception {
        String itemJson = """
        {
            "name": "Original Item",
            "description": "Original Description",
            "status": "PENDING",
            "email": "test@example.com"
        }
        """;

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/items")
                        .contentType("application/json")
                        .content(itemJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        Long id = JsonPath.parse(responseJson).read("$.id", Long.class);

        String updateJson = """
        {
            "name": "Updated Item",
            "description": "Updated Description",
            "status": "PROCESSED",
            "email": "updated@example.com"
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/items/" + id)
                        .contentType("application/json")
                        .content(updateJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Updated Item"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PROCESSED"));
    }

    @Test
    void testDeleteItem() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/items/1"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }



    @Test
    void testProcessItems() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/items/process"))
                .andExpect(MockMvcResultMatchers.status().isAccepted());
    }
}
