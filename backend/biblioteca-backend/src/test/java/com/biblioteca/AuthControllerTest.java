package com.biblioteca;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @Test
    void registrazione_creaUtentePublicERestituisceToken() throws Exception {
        var body = Map.of(
                "email", "mario.rossi@example.com",
                "password", "password123",
                "nome", "Mario",
                "cognome", "Rossi");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.ruolo").value("PUBLIC"));
    }

    @Test
    void login_conCredenzialiCorrette_restituisceToken() throws Exception {
        var reg = Map.of(
                "email", "luigi.verdi@example.com",
                "password", "password123",
                "nome", "Luigi",
                "cognome", "Verdi");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(reg)))
                .andExpect(status().isCreated());

        var login = Map.of(
                "email", "luigi.verdi@example.com",
                "password", "password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_conPasswordErrata_restituisce401() throws Exception {
        var reg = Map.of(
                "email", "anna.bianchi@example.com",
                "password", "password123",
                "nome", "Anna",
                "cognome", "Bianchi");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(reg)))
                .andExpect(status().isCreated());

        var login = Map.of(
                "email", "anna.bianchi@example.com",
                "password", "sbagliata999");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(login)))
                .andExpect(status().isUnauthorized());
    }
}
