package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.TestDto;
import ru.practicum.entity.Test;
import ru.practicum.mapper.TestMapper;
import ru.practicum.repostory.TestRepository;

import java.util.Collection;

@Service
public class TestServiceImpl {
    @Autowired
    TestRepository testRepository;

    public Collection<TestDto> getAll(){
        return testRepository.findAll().stream()
                .map(TestMapper.INSTANCE::getTestDto)
                .toList();
    }

    public TestDto create(TestDto testDto){
        Test test = TestMapper.INSTANCE.getTest(testDto);

        return TestMapper.INSTANCE.getTestDto(testRepository.save(test));
    }
}
