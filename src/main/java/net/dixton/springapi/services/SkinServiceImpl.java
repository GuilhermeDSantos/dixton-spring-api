package net.dixton.springapi.services;

import lombok.RequiredArgsConstructor;
import net.dixton.exceptions.DixtonRuntimeException;
import net.dixton.exceptions.SkinNotFoundException;
import net.dixton.model.skin.Skin;
import net.dixton.model.skin.SkinCategory;
import net.dixton.services.SkinService;
import net.dixton.springapi.repositories.SkinCategoryRepository;
import net.dixton.springapi.repositories.SkinRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkinServiceImpl implements SkinService {

    private final SkinRepository skinRepository;
    private final SkinCategoryRepository skinCategoryRepository;

    @Override
    public Skin findById(Long id) {
        return skinRepository.findById(id).orElseThrow(() -> new DixtonRuntimeException(new SkinNotFoundException(id)));
    }

    @Override
    public List<SkinCategory> findAllCategories() {
        return skinCategoryRepository.findAll();
    }

    public Skin findRandomSkin() {
        return skinRepository.findRandomSkin();
    }
}