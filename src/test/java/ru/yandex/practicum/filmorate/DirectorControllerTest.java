package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class DirectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DirectorService directorService;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    private Director validDirector() {
        Director director = new Director();
        director.setId(1L);
        director.setName("Режиссер");
        return director;
    }

    // ─── GET /directors ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Список всех режиссёров")
    void getDirectors_success() throws Exception {
        when(directorService.getAll()).thenReturn(List.of(validDirector()));

        mockMvc.perform(get("/directors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ─── GET /directors/{id} ───────────────────────────────────────────────────

    @Test
    @DisplayName("Получение режиссёра по id")
    void getDirectorById_success() throws Exception {
        when(directorService.getById(1L)).thenReturn(validDirector());

        mockMvc.perform(get("/directors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Режиссер"));
    }

    @Test
    @DisplayName("Режиссёр не найден")
    void getDirectorById_notFound() throws Exception {
        when(directorService.getById(999L)).thenThrow(new NotFoundException("Режиссер с id 999 не найден"));

        mockMvc.perform(get("/directors/999"))
                .andExpect(status().isNotFound());
    }

    // ─── POST /directors ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Создание режиссёра")
    void createDirector_valid() throws Exception {
        Director director = validDirector();
        when(directorService.addDirector(any(Director.class))).thenReturn(director);

        mockMvc.perform(post("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(director)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Режиссер"));
    }

    @Test
    @DisplayName("Пустое имя режиссёра")
    void createDirector_blankName() throws Exception {
        Director director = validDirector();
        director.setName("");

        mockMvc.perform(post("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(director)))
                .andExpect(status().isBadRequest());
    }

    // ─── PUT /directors ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Обновление режиссёра")
    void updateDirector_success() throws Exception {
        Director director = validDirector();
        director.setName("Новый режиссер");
        when(directorService.updateDirector(any(Director.class))).thenReturn(director);

        mockMvc.perform(put("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(director)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новый режиссер"));
    }

    @Test
    @DisplayName("Обновление несуществующего режиссёра")
    void updateDirector_notFound() throws Exception {
        Director director = validDirector();
        director.setId(999L);
        when(directorService.updateDirector(any(Director.class)))
                .thenThrow(new NotFoundException("Режиссер с id 999 не найден"));

        mockMvc.perform(put("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(director)))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /directors/{id} ────────────────────────────────────────────────

    @Test
    @DisplayName("Удаление режиссёра")
    void deleteDirector_success() throws Exception {
        mockMvc.perform(delete("/directors/1"))
                .andExpect(status().isOk());
        verify(directorService).deleteDirector(1L);
    }

    @Test
    @DisplayName("Удаление несуществующего режиссёра")
    void deleteDirector_notFound() throws Exception {
        doThrow(new NotFoundException("Режиссер с id 999 не найден"))
                .when(directorService).deleteDirector(999L);

        mockMvc.perform(delete("/directors/999"))
                .andExpect(status().isNotFound());
    }
}
