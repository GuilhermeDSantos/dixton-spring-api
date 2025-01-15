package net.dixton.springapi.repositories;

import net.dixton.model.skin.Skin;
import net.dixton.model.skin.SkinCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SkinRepository extends JpaRepository<Skin, Long> {

    List<Skin> findByCategory(SkinCategory category);

    @Query(value = "SELECT * FROM skin ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Skin findRandomSkin();
}