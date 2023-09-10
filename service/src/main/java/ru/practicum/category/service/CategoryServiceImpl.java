package ru.practicum.category.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public CategoryDto create(CategoryDto categoryDto) {
        Category category = CategoryMapper.toCategory(categoryDto);
        Category categoryToSave = categoryRepository.save(category);

        return CategoryMapper.toCategoryDto(categoryToSave);
    }

    @Override
    @Transactional
    public CategoryDto update(Long categoryId, CategoryDto categoryDto) {

        Category categoryToUpdate = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена."));

        Category category = CategoryMapper.toCategory(categoryDto);

        if (category.getName() != null) {
            categoryToUpdate.setName(category.getName());
        }

        return CategoryMapper.toCategoryDto(categoryRepository.save(categoryToUpdate));
    }

    @Override
    @Transactional
    public void delete(Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена."));

        categoryRepository.deleteById(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAll(int from, int size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id")))
                .stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена."));

        return CategoryMapper.toCategoryDto(category);
    }
}
