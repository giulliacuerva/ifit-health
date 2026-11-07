package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.RoomCatalogResponse;
import br.ifsp.demo.controller.dto.SportCatalogResponse;
import br.ifsp.demo.controller.dto.TrainerCatalogResponse;
import br.ifsp.demo.service.CatalogQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {

    private final CatalogQueryService catalogQueryService;

    public CatalogController(CatalogQueryService catalogQueryService) {
        this.catalogQueryService = catalogQueryService;
    }

    @GetMapping("/sports")
    public List<SportCatalogResponse> findSports() {
        return catalogQueryService.findSports();
    }

    @GetMapping("/rooms")
    public List<RoomCatalogResponse> findRooms() {
        return catalogQueryService.findRooms();
    }

    @GetMapping("/trainers")
    public List<TrainerCatalogResponse> findTrainers() {
        return catalogQueryService.findTrainers();
    }
}
