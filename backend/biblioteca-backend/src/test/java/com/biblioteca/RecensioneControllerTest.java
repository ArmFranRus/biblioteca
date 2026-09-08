package com.biblioteca;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecensioneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token(String email, String password) throws Exception {
        var login = Map.of("email", email, "password", password);
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }

    private void registra(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email, "password", "password123",
                                "nome", "Rec", "cognome", "Ensore"))))
                .andExpect(status().isCreated());
    }

    private long creaLibro(String adminToken, String titolo) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/books/create")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("titolo", titolo))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void recensione_creaLeggiDuplicaElimina() throws Exception {
        String admin = token("admin@biblioteca.it", "admin1234");
        long libroId = creaLibro(admin, "Il fu Mattia Pascal");
        registra("recensore@example.com");
        String utente = token("recensore@example.com", "password123");

        // crea
        MvcResult creata = mockMvc.perform(post("/api/reviews/create")
                        .header("Authorization", "Bearer " + utente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("libroId", libroId, "voto", 5, "testo", "Un capolavoro."))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.voto").value(5))
                .andReturn();
        long recId = objectMapper.readTree(creata.getResponse().getContentAsString()).get("id").asLong();

        // lettura pubblica (senza token)
        mockMvc.perform(get("/api/reviews/all/" + libroId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].voto").value(5))
                .andExpect(jsonPath("$[0].autore").value("Rec Ensore"));

        // seconda recensione dello stesso utente -> 409
        mockMvc.perform(post("/api/reviews/create")
                        .header("Authorization", "Bearer " + utente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("libroId", libroId, "voto", 3, "testo", "Ci ripenso."))))
                .andExpect(status().isConflict());

        // l'autore elimina la propria recensione
        mockMvc.perform(delete("/api/reviews/delete/" + recId)
                        .header("Authorization", "Bearer " + utente))
                .andExpect(status().isNoContent());
    }
}
