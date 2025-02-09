package ru.practicum.repostory;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.entity.Test;

public interface TestRepository extends JpaRepository<Test, Long> {
}
