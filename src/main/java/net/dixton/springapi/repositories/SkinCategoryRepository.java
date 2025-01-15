package net.dixton.springapi.repositories;

import net.dixton.model.skin.SkinCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkinCategoryRepository extends JpaRepository<SkinCategory, Long> {
}