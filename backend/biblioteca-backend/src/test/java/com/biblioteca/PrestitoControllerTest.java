package com.biblioteca;

import com.biblioteca.repository.UtenteRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrestitoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtenteRepository utenteRepository;

    private String tokenAdmin() throws Exception {
        var login = Map.of("email", "admin@biblioteca.it", "password", "admin1234");
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }

    private long creaCopia(String token, String titolo, String codice) throws Exception {
        MvcResult libro = mockMvc.perform(post("/api/books/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("titolo", titolo))))
                .andExpect(status().isCreated())
                .andReturn();
        long libroId = objectMapper.readTree(libro.getResponse().getContentAsString()).get("id").asLong();

        MvcResult copia = mockMvc.perform(post("/api/books/add/" + libroId + "/copies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("codiceInventario", codice))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(copia.getResponse().getContentAsString()).get("id").asLong();
    }

    private long registraUtente(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "password123",
                                "nome", "Test",
                                "cognome", "Utente"))))
                .andExpect(status().isCreated());
        return utenteRepository.findByEmail(email).orElseThrow().getId();
    }

    @Test
    void prestitoERestituzione_flussoCompleto() throws Exception {
        String token = tokenAdmin();
        long copiaId = creaCopia(token, "Il deserto dei Tartari", "INV-001");
        long utenteId = registraUtente("lettore1@example.com");

        MvcResult prestito = mockMvc.perform(post("/api/loans/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("copiaId", copiaId, "utenteId", utenteId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stato").value("ATTIVO"))
                .andReturn();
        long prestitoId = objectMapper.readTree(prestito.getResponse().getContentAsString()).get("id").asLong();

        // la stessa copia e' ora PRESTATA: un secondo prestito deve dare 409
        mockMvc.perform(post("/api/loans/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("copiaId", copiaId, "utenteId", utenteId))))
                .andExpect(status().isConflict());

        // restituzione
        mockMvc.perform(post("/api/loans/return/" + prestitoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stato").value("RESTITUITO"));
    }

    @Test
    void prestito_senzaTokenStaff_negato() throws Exception {
        mockMvc.perform(post("/api/loans/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("copiaId", 1, "utenteId", 1))))
                .andExpect(status().isUnauthorized());
    }
}
