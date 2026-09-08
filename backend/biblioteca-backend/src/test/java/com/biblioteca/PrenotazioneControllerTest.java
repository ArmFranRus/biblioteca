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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrenotazioneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtenteRepository utenteRepository;

    private String token(String email, String password) throws Exception {
        var login = Map.of("email", email, "password", password);
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }

    private long registra(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email, "password", "password123",
                                "nome", "N", "cognome", "C"))))
                .andExpect(status().isCreated());
        return utenteRepository.findByEmail(email).orElseThrow().getId();
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

    private long aggiungiCopia(String adminToken, long libroId, String codice) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/books/add/" + libroId + "/copies")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("codiceInventario", codice))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void prenotazione_conCopiaDisponibile_rifiutata() throws Exception {
        String admin = token("admin@biblioteca.it", "admin1234");
        long libroId = creaLibro(admin, "Se questo e' un uomo");
        aggiungiCopia(admin, libroId, "PREN-001");
        registra("prenlettore@example.com");
        String utente = token("prenlettore@example.com", "password123");

        mockMvc.perform(post("/api/reservations/create")
                        .header("Authorization", "Bearer " + utente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("libroId", libroId))))
                .andExpect(status().isConflict());
    }

    @Test
    void coda_promossaAPrestitoRestituito() throws Exception {
        String admin = token("admin@biblioteca.it", "admin1234");
        long libroId = creaLibro(admin, "La coscienza di Zeno");
        long copiaId = aggiungiCopia(admin, libroId, "PREN-100");

        long u1 = registra("coda_u1@example.com");
        registra("coda_u2@example.com");
        String u2 = token("coda_u2@example.com", "password123");

        // presto l'unica copia a u1 -> nessuna disponibile
        MvcResult prestito = mockMvc.perform(post("/api/loans/create")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("copiaId", copiaId, "utenteId", u1))))
                .andExpect(status().isCreated())
                .andReturn();
        long prestitoId = objectMapper.readTree(prestito.getResponse().getContentAsString()).get("id").asLong();

        // u2 prenota il titolo
        MvcResult pren = mockMvc.perform(post("/api/reservations/create")
                        .header("Authorization", "Bearer " + u2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("libroId", libroId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stato").value("IN_ATTESA"))
                .andReturn();
        long prenId = objectMapper.readTree(pren.getResponse().getContentAsString()).get("id").asLong();

        // restituzione -> la prenotazione di u2 diventa PRONTA
        mockMvc.perform(post("/api/loans/return/" + prestitoId)
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reservations/mine")
                        .header("Authorization", "Bearer " + u2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value((int) prenId))
                .andExpect(jsonPath("$[0].stato").value("PRONTA"));
    }
}
