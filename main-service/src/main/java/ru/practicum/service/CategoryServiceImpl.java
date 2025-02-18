package ru.practicum.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.dal.CategoryRepository;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mappers.CategoryMapper;
import ru.practicum.model.Category;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    @Override
    public CategoryDto save(NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.save(categoryMapper.toCategory(newCategoryDto));
        log.info("New category with id {} saved", category.getId());
        return categoryMapper.toCategoryDto(category);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        getCategoryIfExists(id);
        log.info("Category with id {} deleted", id);
        categoryRepository.deleteById(id);
    }

    @Transactional
    @Override
    public CategoryDto update(Long id, CategoryDto categoryDto) {
        Category category = getCategoryIfExists(id);
        log.info("Updating category - from: {} to: {}", category, categoryDto);
        category.setName(categoryDto.getName());
        // если есть @Transactional то не надо дергать save репозитория
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by(Sort.Direction.ASC, "id"));
        return categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    public CategoryDto getById(Long id) {
        Category category = getCategoryIfExists(id);
        log.info("Category with id {} found: {}", category.getId(), category);
        return categoryMapper.toCategoryDto(category);
    }

    private Category getCategoryIfExists(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id %d not found".formatted(id)));
    }
}
