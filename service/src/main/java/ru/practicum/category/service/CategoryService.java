package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto create(CategoryDto categoryDto);

    CategoryDto update(Long categoryId, CategoryDto categoryDto);

    void delete(Long categoryId);

    List<CategoryDto> getAll(int from, int size);

    CategoryDto getById(Long categoryId);
}
