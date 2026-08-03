package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dal.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({DirectorDbStorage.class, DirectorRowMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class DirectorDbStorageTest {

    @Autowired
    private DirectorDbStorage directorStorage;

    private Director makeDirector() {
        Director director = new Director();
        director.setName("Режиссер");
        return director;
    }

    @Test
    @DisplayName("Создание и получение режиссёра по id")
    void createAndGetById() {
        Director created = directorStorage.create(makeDirector());

        Optional<Director> found = directorStorage.getById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(director -> {
                    assertThat(director.getId()).isEqualTo(created.getId());
                    assertThat(director.getName()).isEqualTo("Режиссер");
                });
    }

    @Test
    @DisplayName("Получение несуществующего режиссёра")
    void getById_notFound() {
        assertThat(directorStorage.getById(999L)).isEmpty();
    }

    @Test
    @DisplayName("Список всех режиссёров")
    void getAll() {
        directorStorage.create(makeDirector());
        directorStorage.create(makeDirector());

        List<Director> directors = directorStorage.getAll();

        assertThat(directors).hasSize(2);
    }

    @Test
    @DisplayName("Обновление режиссёра")
    void updateDirector() {
        Director created = directorStorage.create(makeDirector());
        created.setName("Новый режиссер");

        directorStorage.update(created);

        assertThat(directorStorage.getById(created.getId()))
                .isPresent()
                .hasValueSatisfying(d -> assertThat(d.getName()).isEqualTo("Новый режиссер"));
    }

    @Test
    @DisplayName("Удаление режиссёра")
    void deleteDirector() {
        Director created = directorStorage.create(makeDirector());

        directorStorage.delete(created.getId());

        assertThat(directorStorage.getById(created.getId())).isEmpty();
    }
}
