package net.dixton.springapi;

import lombok.RequiredArgsConstructor;
import net.dixton.model.skin.Skin;
import net.dixton.model.skin.SkinCategory;
import net.dixton.springapi.services.SkinServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/skins")
@RequiredArgsConstructor
public class SkinController {

    private final SkinServiceImpl skinService;

    @GetMapping
    public ResponseEntity<List<SkinCategory>> findAll() {
        return ResponseEntity.ok(skinService.findAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Skin> findById(@PathVariable Long id) {
        return ResponseEntity.ok(skinService.findById(id));
    }
}
